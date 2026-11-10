package br.com.senac.rentacar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Regras achadas na bateria de testes de bugs: CPF unico, ano, km, revisao pendente, status e cliente com locacao. */
class RegrasDeNegocioTest extends ApiTestBase {

    @Test
    void cpfNaoPodeRepetirEntreUsuarios() throws Exception {
        String admin = tokenAdmin();
        String cpf = cpfUnico();                       // 11 digitos, sem pontuacao
        String formatado = cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
        assertEquals(200, chamar("POST", "/usuarios", admin, """
                {"nome":"A","email":"%s","senha":"senha123","cpf":"%s"}""".formatted(email(), formatado)).status());
        assertEquals(400, chamar("POST", "/usuarios", admin, """
                {"nome":"B","email":"%s","senha":"senha123","cpf":"%s"}""".formatted(email(), cpf)).status());
        // mesmo CPF so com digitos tambem conta como repetido
        assertEquals(400, chamar("POST", "/usuarios", admin, """
                {"nome":"C","email":"%s","senha":"senha123","cpf":"%s"}""".formatted(email(), cpf)).status());
    }

    @Test
    void cpfNaoPodeRepetirEntreClientes() throws Exception {
        String t = tokenAdmin();
        String cpf = cpfUnico();
        var criado = chamar("POST", "/clientes", t, """
                {"nome":"X","cpf":"%s","telefone":"1","email":"x@x.com"}""".formatted(cpf));
        assertEquals(200, criado.status());
        long id = ((Number) criado.json().get("id")).longValue();
        assertEquals(400, chamar("POST", "/clientes", t, """
                {"nome":"Y","cpf":"%s","telefone":"1","email":"y@x.com"}""".formatted(cpf)).status());
        // editar mantendo o proprio CPF continua valendo
        assertEquals(200, chamar("PUT", "/clientes/" + id, t, """
                {"nome":"X2","cpf":"%s","telefone":"1","email":"x@x.com"}""".formatted(cpf)).status());
    }

    @Test
    void autoCadastroDeClienteGeraRoleClienteERecusaCpfOuEmailRepetido() throws Exception {
        String e = email(), cpf = cpfUnico();
        String corpo = """
                {"nome":"Cli","cpf":"%s","telefone":"1","email":"%s","senha":"senha123","role":"ROLE_ADMIN"}""".formatted(cpf, e);
        var r = chamar("POST", "/auth/cadastro", null, corpo);
        assertEquals(200, r.status(), r.body());
        assertEquals("ROLE_CLIENTE", r.json().get("role"));   // a role enviada no corpo e ignorada
        assertEquals(400, chamar("POST", "/auth/cadastro", null, corpo.replace(e, e.toUpperCase())).status()); // e-mail em maiuscula
        assertEquals(400, chamar("POST", "/auth/cadastro", null, corpo.replace(e, email())).status());          // CPF repetido
    }

    @Test
    void anoDoVeiculoPrecisaSerValido() throws Exception {
        String t = tokenAdmin();
        long cat = criarCategoria(t, "Ano " + SEQ.incrementAndGet());
        for (String ano : new String[]{"abc", "1800", "2999", ""}) {
            assertEquals(400, chamar("POST", "/veiculos", t, """
                    {"marca":"F","modelo":"M","placa":"%s","ano":"%s","valorDiaria":99,"categoriaId":%d}"""
                    .formatted(placa(), ano, cat)).status(), "ano " + ano);
        }
    }

    @Test
    void quilometragemNaoPodeDiminuirPelaEdicao() throws Exception {
        String t = tokenAdmin();
        long cat = criarCategoria(t, "Km " + SEQ.incrementAndGet());
        String placa = placa();
        long id = criarVeiculo(t, cat, placa, 5000);
        String corpo = """
                {"marca":"Fiat","modelo":"Uno","placa":"%s","ano":"2020","valorDiaria":100,"quilometragem":%d,"categoriaId":%d}""";
        assertEquals(400, chamar("PUT", "/veiculos/" + id, t, corpo.formatted(placa, 4000, cat)).status());
        assertEquals(200, chamar("PUT", "/veiculos/" + id, t, corpo.formatted(placa, 6000, cat)).status());
    }

    @Test
    void veiculoComRevisaoPendenteNaoPodeSerAlugadoAteRegistrarARevisao() throws Exception {
        String t = tokenAdmin();
        long cat = criarCategoria(t, "Rev " + SEQ.incrementAndGet());
        long cliente = criarCliente(t);
        // 20.000 km rodados e ultima revisao aos 5.000 km -> 15.000 km desde a revisao (limite 10.000)
        var v = chamar("POST", "/veiculos", t, """
                {"marca":"F","modelo":"M","placa":"%s","ano":"2022","valorDiaria":100,"quilometragem":20000,"kmUltimaRevisao":5000,"categoriaId":%d}"""
                .formatted(placa(), cat));
        assertEquals(200, v.status(), v.body());
        long id = ((Number) v.json().get("id")).longValue();

        var recusada = criarLocacao(t, cliente, id);
        assertEquals(400, recusada.status());
        assertTrue(recusada.body().contains("revisao pendente"), recusada.body());
        assertFalse(chamar("GET", "/publico/veiculos", null, null).body().contains("\"id\":" + id + ","),
                "a vitrine nao deve mostrar carro com revisao pendente");

        assertEquals(200, chamar("PATCH", "/veiculos/" + id + "/revisao", t, null).status());
        assertEquals(200, criarLocacao(t, cliente, id).status());
    }

    @Test
    void statusDeUsuarioInvalidoOuAusenteDa400ENaoGravaNull() throws Exception {
        String admin = tokenAdmin();
        String e = email();
        long id = criarUsuarioComum(admin, e, "senha123");
        assertEquals(400, chamar("PATCH", "/usuarios/" + id + "/status", admin, "{}").status());
        assertEquals(400, chamar("PATCH", "/usuarios/" + id + "/status", admin, "{\"status\":\"INVALIDO\"}").status());
        assertNotNull(login(e, "senha123"));   // continua ativo
    }

    @Test
    void naoExcluiClienteComLocacaoEmAndamento() throws Exception {
        String t = tokenAdmin();
        long cat = criarCategoria(t, "Cli " + SEQ.incrementAndGet());
        long cliente = criarCliente(t);
        long veiculo = criarVeiculo(t, cat, placa(), 100);
        var loc = criarLocacao(t, cliente, veiculo);
        assertEquals(200, loc.status(), loc.body());
        assertEquals(400, chamar("DELETE", "/clientes/" + cliente, t, null).status());

        long idLoc = ((Number) loc.json().get("id")).longValue();
        assertEquals(200, chamar("PATCH", "/locacoes/" + idLoc + "/devolucao", t, "{\"quilometragemFinal\":150}").status());
        assertEquals(200, chamar("DELETE", "/clientes/" + cliente, t, null).status());   // depois da devolucao pode
    }

    @Test
    void jsonMalformadoDa400() throws Exception {
        String admin = tokenAdmin();
        assertEquals(400, chamar("POST", "/clientes", admin, "{nao e json").status());
    }

    @Test
    void clienteLogadoAlugaSoPelaPropriaAreaEOServidorCalculaOValor() throws Exception {
        String admin = tokenAdmin();
        long cat = criarCategoria(admin, "Alu " + SEQ.incrementAndGet());
        long veiculo = criarVeiculo(admin, cat, placa(), 100);   // diaria 100

        String e = email();
        assertEquals(200, chamar("POST", "/auth/cadastro", null, """
                {"nome":"Cli","cpf":"%s","telefone":"1","email":"%s","senha":"senha123"}""".formatted(cpfUnico(), e)).status());
        String cli = login(e, "senha123");

        assertEquals(403, chamar("GET", "/clientes", cli, null).status());      // sem acesso ao painel
        assertEquals(403, chamar("GET", "/minhas-locacoes", admin, null).status());

        String inicio = java.time.LocalDate.now().plusDays(1).toString();
        String fim = java.time.LocalDate.now().plusDays(4).toString();
        var r = chamar("POST", "/minhas-locacoes", cli, """
                {"veiculoId":%d,"dataInicio":"%s","dataFim":"%s","valorTotal":1}""".formatted(veiculo, inicio, fim));
        assertEquals(200, r.status(), r.body());
        assertEquals(300.0, ((Number) r.json().get("valorTotal")).doubleValue());    // 3 diarias, valor enviado ignorado
        assertEquals(400, chamar("POST", "/minhas-locacoes", cli, """
                {"veiculoId":%d,"dataInicio":"%s","dataFim":"%s"}""".formatted(veiculo, inicio, fim)).status()); // ja alugado
    }
}
