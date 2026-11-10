package br.com.senac.rentacar;

import br.com.senac.rentacar.domain.respository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Sobe a aplicacao inteira (porta aleatoria, H2 em memoria) e oferece helpers para chamar a API por HTTP. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class ApiTestBase {

    protected static final String CHAVE = "chave-somente-para-testes-1234567890";
    protected static final String CPF_OK = "529.982.247-25";

    private static final java.util.concurrent.atomic.AtomicInteger SEQ_CPF = new java.util.concurrent.atomic.AtomicInteger(1000);

    /** CPF valido e diferente a cada chamada (a API agora recusa CPF repetido). */
    protected static String cpfUnico() {
        int[] d = new int[11];
        String base = String.format("%09d", SEQ_CPF.incrementAndGet());
        for (int i = 0; i < 9; i++) d[i] = base.charAt(i) - 48;
        for (int j = 9; j < 11; j++) {
            int soma = 0;
            for (int i = 0; i < j; i++) soma += d[i] * (j + 1 - i);
            int r = (soma * 10) % 11;
            d[j] = r == 10 ? 0 : r;
        }
        StringBuilder sb = new StringBuilder();
        for (int x : d) sb.append(x);
        return sb.toString();
    }
    protected static final AtomicInteger SEQ = new AtomicInteger();

    @Value("${local.server.port}")
    protected int porta;

    @Autowired
    protected UsuarioRepository usuarioRepository;

    protected final HttpClient http = HttpClient.newHttpClient();

    protected record Resp(int status, String body) {
        Map<String, Object> json() { return JsonParserFactory.getJsonParser().parseMap(body); }
    }

    protected Resp chamar(String metodo, String caminho, String token, String json) throws Exception {
        var b = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho));
        if (token != null) b.header("Authorization", "Bearer " + token);
        if (json != null) b.header("Content-Type", "application/json");
        b.method(metodo, json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json));
        var r = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
        return new Resp(r.statusCode(), r.body());
    }

    protected String email() { return "user" + SEQ.incrementAndGet() + "@teste.com"; }

    protected Resp criarAdmin(String email, String senha, String chave) throws Exception {
        return chamar("POST", "/usuarios/admin", null, """
                {"nome":"Admin","email":"%s","senha":"%s","cpf":"%s","secretKey":"%s"}""".formatted(email, senha, cpfUnico(), chave));
    }

    protected String login(String email, String senha) throws Exception {
        var r = chamar("POST", "/auth/login", null, """
                {"email":"%s","senha":"%s"}""".formatted(email, senha));
        assertEquals(200, r.status(), r.body());
        return (String) r.json().get("token");
    }

    protected String tokenAdmin() throws Exception {
        String e = email();
        assertEquals(200, criarAdmin(e, "senha123", CHAVE).status());
        return login(e, "senha123");
    }

    protected Long criarUsuarioComum(String tokenAdmin, String email, String senha) throws Exception {
        var r = chamar("POST", "/usuarios", tokenAdmin, """
                {"nome":"Comum","email":"%s","senha":"%s","cpf":"%s"}""".formatted(email, senha, cpfUnico()));
        assertEquals(200, r.status(), r.body());
        return ((Number) r.json().get("id")).longValue();
    }


    // ---- helpers do dominio
    protected long criarCategoria(String token, String nome) throws Exception {
        var r = chamar("POST", "/categorias", token, """
                {"nome":"%s","descricao":"desc"}""".formatted(nome));
        assertEquals(200, r.status(), r.body());
        return ((Number) r.json().get("id")).longValue();
    }

    protected long criarVeiculo(String token, long categoriaId, String placa, int km) throws Exception {
        var r = chamar("POST", "/veiculos", token, """
                {"marca":"Fiat","modelo":"Uno","placa":"%s","ano":"2020","valorDiaria":100,"quilometragem":%d,"categoriaId":%d}"""
                .formatted(placa, km, categoriaId));
        assertEquals(200, r.status(), r.body());
        return ((Number) r.json().get("id")).longValue();
    }

    protected long criarCliente(String token) throws Exception {
        var r = chamar("POST", "/clientes", token, """
                {"nome":"Cliente","cpf":"%s","telefone":"1","email":"c@x.com"}""".formatted(cpfUnico()));
        assertEquals(200, r.status(), r.body());
        return ((Number) r.json().get("id")).longValue();
    }

    protected String placa() { return "TST" + (1000 + SEQ.incrementAndGet()); }

    protected Resp criarLocacao(String token, long clienteId, long veiculoId) throws Exception {
        return chamar("POST", "/locacoes", token, """
                {"dataInicio":"2026-11-10","dataFim":"2026-11-15","valorTotal":500,"cliente":"%d","veiculo":"%d"}"""
                .formatted(clienteId, veiculoId));
    }

    protected String statusDoVeiculo(String token, long veiculoId) throws Exception {
        return (String) chamar("GET", "/veiculos/" + veiculoId, token, null).json().get("status");
    }
}
