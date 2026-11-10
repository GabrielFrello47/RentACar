package br.com.senac.rentacar;

import br.com.senac.rentacar.domain.entities.EnumStatusUsuario;
import br.com.senac.rentacar.domain.entities.Usuario;
import br.com.senac.rentacar.domain.valueobject.CPF;
import com.auth0.jwt.JWT;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sobe a aplicacao inteira (porta aleatoria, H2 em memoria) e testa a API de verdade, por HTTP,
 * passando pelo JwtFilter, pelo Spring Security e pelo CORS.
 */
class ApiIntegrationTest extends ApiTestBase {

    // ---------------------------------------------------------------- autenticacao
    @Test
    void semTokenTudoRetorna401() throws Exception {
        assertEquals(401, chamar("GET", "/clientes", null, null).status());
        assertEquals(401, chamar("GET", "/veiculos", null, null).status());
        assertEquals(401, chamar("GET", "/locacoes", null, null).status());
        assertEquals(401, chamar("GET", "/usuarios", null, null).status());
        assertEquals(401, chamar("GET", "/clientes", "token.invalido.qualquer", null).status());
    }

    @Test
    void chaveDeAdminErradaRetorna403ENaoCriaUsuario() throws Exception {
        String e = email();
        assertEquals(403, criarAdmin(e, "senha123", "chave-errada").status());
        assertTrue(usuarioRepository.findFirstByEmail(e).isEmpty());
        assertEquals(403, chamar("POST", "/usuarios/admin", null, """
                {"nome":"A","email":"%s","senha":"senha123","cpf":"%s"}""".formatted(email(), CPF_OK)).status()); // sem secretKey
    }

    @Test
    void criarAdminValidaDadosEEmailDuplicado() throws Exception {
        String e = email();
        assertEquals(200, criarAdmin(e, "senha123", CHAVE).status());
        assertEquals(400, criarAdmin(e, "senha123", CHAVE).status());                       // email duplicado
        assertEquals(400, criarAdmin(email(), "123", CHAVE).status());                       // senha curta
        assertEquals(400, chamar("POST", "/usuarios/admin", null, """
                {"nome":"A","email":"%s","senha":"senha123","cpf":"123.456.789-00","secretKey":"%s"}"""
                .formatted(email(), CHAVE)).status());                                       // CPF invalido
    }

    @Test
    void senhaFicaGravadaComoDigitadaIgualAoProjetoDoProfessor() throws Exception {
        String e = email();
        criarAdmin(e, "senha123", CHAVE);
        var u = usuarioRepository.findFirstByEmail(e).orElseThrow();
        assertEquals("senha123", u.getSenha());
    }

    @Test
    void loginComSenhaErradaOuUsuarioInexistenteRetorna400() throws Exception {
        String e = email();
        criarAdmin(e, "senha123", CHAVE);
        assertEquals(400, chamar("POST", "/auth/login", null, """
                {"email":"%s","senha":"errada"}""".formatted(e)).status());
        assertEquals(400, chamar("POST", "/auth/login", null, """
                {"email":"naoexiste@teste.com","senha":"senha123"}""").status());
        assertEquals(400, chamar("POST", "/auth/login", null, "{}").status());
    }

    @Test
    void usuarioComSenhaEmTextoPuroLoga() throws Exception {
        String e = email();
        var legado = new Usuario();
        legado.setNome("Legado");
        legado.setCpf(new CPF(cpfUnico()));
        legado.setEmail(e);
        legado.setSenha("antiga123");           // como o projeto antigo gravava
        usuarioRepository.save(legado);

        assertNotNull(login(e, "antiga123"));
        assertEquals("antiga123", usuarioRepository.findFirstByEmail(e).orElseThrow().getSenha());   // nada e convertido
        assertNotNull(login(e, "antiga123"));
    }

    @Test
    void doisLoginsSeguidosDoMesmoUsuarioGeramTokensDistintosEAmbosFuncionam() throws Exception {
        String e = email();
        criarAdmin(e, "senha123", CHAVE);

        // antes: no mesmo segundo o JWT saia identico, a tabela ganhava 2 linhas iguais e o token virava 401
        String t1 = login(e, "senha123");
        String t2 = login(e, "senha123");
        String t3 = login(e, "senha123");

        assertNotEquals(t1, t2);
        assertNotEquals(t2, t3);
        for (String t : new String[]{t1, t2, t3}) {
            assertEquals(200, chamar("GET", "/clientes", t, null).status());
        }
    }

    @Test
    void tokenExpiraNoTempoConfiguradoIndependenteDoFusoHorario() throws Exception {
        String token = tokenAdmin();
        Instant exp = JWT.decode(token).getExpiresAt().toInstant();
        long minutos = java.time.Duration.between(Instant.now(), exp).toMinutes();
        // spring.tempo_expiracao=30 nos testes (antes o fuso fixo -03:00 deslocava isso em horas)
        assertTrue(minutos >= 28 && minutos <= 30, "minutos ate expirar: " + minutos);
    }

    // ---------------------------------------------------------------- autorizacao
    @Test
    void usuarioComumNaoAcessaUsuariosMasAcessaOResto() throws Exception {
        String admin = tokenAdmin();
        String e = email();
        criarUsuarioComum(admin, e, "senha123");
        String comum = login(e, "senha123");

        assertEquals(403, chamar("GET", "/usuarios", comum, null).status());
        assertEquals(403, chamar("PATCH", "/usuarios/1/status", comum, """
                {"status":"BLOQUEADO"}""").status());
        assertEquals(200, chamar("GET", "/clientes", comum, null).status());

        var eu = chamar("GET", "/usuarios/buscarusuariologado", comum, null);
        assertEquals(200, eu.status());
        assertEquals(e, eu.json().get("email"));
        assertEquals("ROLE_USER", eu.json().get("role"));
        assertFalse(eu.body().contains("senha"));
    }

    @Test
    void adminListaUsuariosSemExcluidos() throws Exception {
        String admin = tokenAdmin();
        String e = email();
        Long id = criarUsuarioComum(admin, e, "senha123");

        assertTrue(chamar("GET", "/usuarios", admin, null).body().contains(e));
        assertEquals(200, chamar("DELETE", "/usuarios/" + id + "/excluir", admin, null).status());
        assertFalse(chamar("GET", "/usuarios", admin, null).body().contains(e));
    }

    @Test
    void usuarioBloqueadoOuExcluidoNaoLogaEPerdeOAcessoImediatamente() throws Exception {
        String admin = tokenAdmin();
        String e = email();
        Long id = criarUsuarioComum(admin, e, "senha123");
        String tokenDele = login(e, "senha123");
        assertEquals(200, chamar("GET", "/clientes", tokenDele, null).status());

        assertEquals(200, chamar("PATCH", "/usuarios/" + id + "/status", admin, """
                {"status":"BLOQUEADO"}""").status());

        assertEquals(401, chamar("GET", "/clientes", tokenDele, null).status());          // token antigo morreu
        assertEquals(400, chamar("POST", "/auth/login", null, """
                {"email":"%s","senha":"senha123"}""".formatted(e)).status());              // nao loga mais

        chamar("PATCH", "/usuarios/" + id + "/status", admin, """
                {"status":"ATIVO"}""");
        assertNotNull(login(e, "senha123"));                                               // desbloqueado volta
        assertEquals(EnumStatusUsuario.ATIVO, usuarioRepository.findById(id).orElseThrow().getStatus());
    }

    @Test
    void atualizarUsuarioSemInformarSenhaMantemASenhaAtual() throws Exception {
        String admin = tokenAdmin();
        String e = email();
        Long id = criarUsuarioComum(admin, e, "senha123");

        var r = chamar("PUT", "/usuarios/" + id, admin, """
                {"nome":"Novo Nome","email":"%s","senha":"","cpf":"%s"}""".formatted(e, CPF_OK));
        assertEquals(200, r.status(), r.body());
        assertNotNull(login(e, "senha123"));
        assertEquals(404, chamar("PUT", "/usuarios/999999", admin, """
                {"nome":"X","email":"%s","senha":"","cpf":"%s"}""".formatted(email(), CPF_OK)).status());
    }

    // ---------------------------------------------------------------- CRUDs
    @Test
    void crudDeClienteComValidacao() throws Exception {
        String t = tokenAdmin();
        assertEquals(400, chamar("POST", "/clientes", t, """
                {"nome":"","cpf":"%s"}""".formatted(CPF_OK)).status());
        assertEquals(400, chamar("POST", "/clientes", t, """
                {"nome":"Bia","cpf":"111.111.111-11"}""").status());

        var criado = chamar("POST", "/clientes", t, """
                {"nome":"Bia","cpf":"%s","telefone":"4899999","email":"bia@x.com"}""".formatted(CPF_OK));
        assertEquals(200, criado.status(), criado.body());
        long id = ((Number) criado.json().get("id")).longValue();

        assertEquals(200, chamar("PUT", "/clientes/" + id, t, """
                {"nome":"Bia Souza","cpf":"%s","telefone":"1","email":"bia@x.com"}""".formatted(CPF_OK)).status());
        assertTrue(chamar("GET", "/clientes/" + id, t, null).body().contains("Bia Souza"));
        assertEquals(400, chamar("PUT", "/clientes/" + id, t, """
                {"nome":"Bia","cpf":"000"}""").status());

        assertEquals(200, chamar("DELETE", "/clientes/" + id, t, null).status());
        assertEquals(404, chamar("GET", "/clientes/" + id, t, null).status());
        assertEquals(404, chamar("DELETE", "/clientes/" + id, t, null).status());
        assertEquals(404, chamar("PUT", "/clientes/" + id, t, """
                {"nome":"Bia","cpf":"%s"}""".formatted(CPF_OK)).status());
    }

    @Test
    void crudDeVeiculoComValidacao() throws Exception {
        String t = tokenAdmin();
        long cat = criarCategoria(t, "Economico " + SEQ.incrementAndGet());

        assertEquals(400, chamar("POST", "/veiculos", t, """
                {"marca":"Fiat","modelo":"Uno","placa":"","ano":"2020","valorDiaria":100,"categoriaId":%d}""".formatted(cat)).status());
        assertEquals(400, chamar("POST", "/veiculos", t, """
                {"marca":"Fiat","modelo":"Uno","placa":"ABC1D23","ano":"2020","valorDiaria":0,"categoriaId":%d}""".formatted(cat)).status());
        assertEquals(400, chamar("POST", "/veiculos", t, """
                {"marca":"Fiat","modelo":"Uno","placa":"ABC1D23","ano":"2020","valorDiaria":99}""").status());          // sem categoria
        assertEquals(400, chamar("POST", "/veiculos", t, """
                {"marca":"Fiat","modelo":"Uno","placa":"ABC1D23","ano":"2020","valorDiaria":99,"categoriaId":999999}""").status());

        String placa = placa();
        long id = criarVeiculo(t, cat, placa, 500);
        assertEquals(400, chamar("POST", "/veiculos", t, """
                {"marca":"X","modelo":"Y","placa":"%s","ano":"2020","valorDiaria":99,"categoriaId":%d}""".formatted(placa.toLowerCase(), cat)).status()); // placa repetida

        var get = chamar("GET", "/veiculos/" + id, t, null).json();
        assertEquals("DISPONIVEL", get.get("status"));
        assertEquals(500, ((Number) get.get("quilometragem")).intValue());
        assertEquals(500, ((Number) get.get("kmUltimaRevisao")).intValue());   // sem informar, nao nasce com alerta
        assertEquals(false, get.get("revisaoPendente"));

        assertEquals(200, chamar("PUT", "/veiculos/" + id, t, """
                {"marca":"Fiat","modelo":"Mobi","placa":"%s","ano":"2021","valorDiaria":120,"categoriaId":%d}""".formatted(placa, cat)).status());
        assertTrue(chamar("GET", "/veiculos/" + id, t, null).body().contains("Mobi"));
        assertEquals(200, chamar("DELETE", "/veiculos/" + id, t, null).status());
        assertEquals(404, chamar("DELETE", "/veiculos/" + id, t, null).status());
    }

    @Test
    void crudDeLocacaoComValidacaoDeDatas() throws Exception {
        String t = tokenAdmin();
        long cliente = criarCliente(t);
        long veiculo = criarVeiculo(t, criarCategoria(t, "Cat " + SEQ.incrementAndGet()), placa(), 0);

        assertEquals(400, chamar("POST", "/locacoes", t, """
                {"dataInicio":"2026-11-15","dataFim":"2026-11-10","valorTotal":500,"cliente":"%d","veiculo":"%d"}""".formatted(cliente, veiculo)).status()); // fim antes do inicio
        assertEquals(400, chamar("POST", "/locacoes", t, """
                {"dataInicio":"2026-11-10","dataFim":"2026-11-15","valorTotal":-1,"cliente":"%d","veiculo":"%d"}""".formatted(cliente, veiculo)).status());
        assertEquals(400, chamar("POST", "/locacoes", t, """
                {"dataInicio":"2026-11-10","dataFim":"2026-11-15","valorTotal":500}""").status());                              // sem cliente/veiculo
        assertEquals(400, criarLocacao(t, 999999, veiculo).status());                                                             // cliente inexistente
        assertEquals(400, criarLocacao(t, cliente, 999999).status());                                                             // veiculo inexistente

        var criada = criarLocacao(t, cliente, veiculo);
        assertEquals(200, criada.status(), criada.body());
        long id = ((Number) criada.json().get("id")).longValue();
        assertEquals("EM_ANDAMENTO", criada.json().get("status"));

        assertEquals(200, chamar("PUT", "/locacoes/" + id, t, """
                {"dataInicio":"2026-11-10","dataFim":"2026-11-20","valorTotal":900,"cliente":"%d","veiculo":"%d"}""".formatted(cliente, veiculo)).status());
        assertTrue(chamar("GET", "/locacoes/" + id, t, null).body().contains("2026-11-20"));
        assertEquals(200, chamar("DELETE", "/locacoes/" + id, t, null).status());
        assertEquals(404, chamar("GET", "/locacoes/" + id, t, null).status());
    }

    // ---------------------------------------------------------------- CORS
    @Test
    void preflightDoCorsParaOFrontFunciona() throws Exception {
        var req = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + "/clientes"))
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "authorization")
                .build();
        var r = http.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, r.statusCode());
        assertEquals("http://localhost:3000", r.headers().firstValue("Access-Control-Allow-Origin").orElse(null));
    }
}
