package com.example.back;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * Teste de fumaca das telas: carrega cada FXML com o controller e confere se os campos (fx:id) foram ligados.
 * So roda quando existe ambiente grafico (no Linux sem DISPLAY e ignorado).
 */
class TelasFxmlTest {

    @BeforeAll
    static void iniciarJavaFx() throws Exception {
        boolean linuxSemTela = System.getProperty("os.name").toLowerCase().contains("linux")
                && (System.getenv("DISPLAY") == null || System.getenv("DISPLAY").isBlank());
        assumeTrue(!linuxSemTela, "sem ambiente grafico: teste de telas ignorado");

        CompletableFuture<Void> pronto = new CompletableFuture<>();
        try {
            Platform.startup(() -> pronto.complete(null));
        } catch (IllegalStateException jaIniciado) {
            pronto.complete(null);
        }
        pronto.get(20, TimeUnit.SECONDS);
    }

    private <T> T naThreadFx(Supplier<T> acao) throws Exception {
        CompletableFuture<T> resultado = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                resultado.complete(acao.get());
            } catch (Throwable t) {
                resultado.completeExceptionally(t);
            }
        });
        return resultado.get(20, TimeUnit.SECONDS);
    }

    private Object carregar(String fxml) throws Exception {
        return naThreadFx(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(AdminApplication.class.getResource(fxml));
                loader.load();
                return loader.getController();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void exigeCampos(Object controller, String... campos) throws Exception {
        for (String nome : campos) {
            Field f = controller.getClass().getDeclaredField(nome);
            f.setAccessible(true);
            assertNotNull(f.get(controller), "fx:id '" + nome + "' nao foi ligado no FXML");
        }
    }

    @Test
    void telaDeLoginCarrega() throws Exception {
        Object c = carregar("login-view.fxml");
        assertInstanceOf(LoginController.class, c);
        exigeCampos(c, "txtKey");
    }

    @Test
    void telaDeMenuCarrega() throws Exception {
        assertInstanceOf(MenuController.class, carregar("menu-view.fxml"));
    }

    @Test
    void telaDeCadastroCarrega() throws Exception {
        Object c = carregar("cadastro-usuario-view.fxml");
        assertInstanceOf(UsuarioController.class, c);
        exigeCampos(c, "txtNome", "txtCpf", "txtEmail", "txtSenha");
    }

    @Test
    void telaDeCategoriaCarrega() throws Exception {
        Object c = carregar("cadastro-categoria-view.fxml");
        assertInstanceOf(CategoriaController.class, c);
        exigeCampos(c, "txtNome", "txtDescricao", "txtEmailAdmin", "txtSenhaAdmin");
    }
}
