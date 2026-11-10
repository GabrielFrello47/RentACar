package com.example.back;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CategoriaController {

    @FXML
    private TextField txtNome;

    @FXML
    private TextField txtDescricao;

    // POST /categorias exige um ADMIN logado: informe o e-mail e a senha de um administrador
    @FXML
    private TextField txtEmailAdmin;

    @FXML
    private PasswordField txtSenhaAdmin;

    @FXML
    public void salvarCategoria(ActionEvent event) throws Exception {

        // Validar Dados
        if (txtNome.getText().isBlank()) {
            showMessage(Alert.AlertType.ERROR, "Informe o nome da categoria!");
            return;
        }
        if (txtEmailAdmin.getText().isBlank() || txtSenhaAdmin.getText().isBlank()) {
            showMessage(Alert.AlertType.ERROR, "Informe o e-mail e a senha de um administrador!");
            return;
        }

        try {
            // 1) login do administrador -> token
            String jsonLogin = String.format("{\n" +
                            "  \"email\": \"%s\",\n" +
                            "  \"senha\": \"%s\"\n" +
                            "}", txtEmailAdmin.getText(),
                    txtSenhaAdmin.getText());

            String[] login = executaMetodoAPI("http://localhost:8080/auth/login", jsonLogin, "POST", null);

            if (!login[0].equals("200")) {
                showMessage(Alert.AlertType.ERROR, "E-mail ou senha do administrador invalidos!");
                return;
            }

            Matcher m = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(login[1]);
            if (!m.find()) {
                showMessage(Alert.AlertType.ERROR, "A API nao devolveu o token de acesso!");
                return;
            }

            // 2) cadastro da categoria usando o token
            String jsonRequest = String.format("{\n" +
                            "  \"nome\": \"%s\",\n" +
                            "  \"descricao\": \"%s\"\n" +
                            "}", txtNome.getText(),
                    txtDescricao.getText());

            String[] resposta = executaMetodoAPI("http://localhost:8080/categorias", jsonRequest, "POST", m.group(1));

            if (resposta[0].equals("200") || resposta[0].equals("201")) {
                showMessage(Alert.AlertType.INFORMATION, "Categoria salva com sucesso!");
                voltar(event);
            } else if (resposta[0].equals("403")) {
                showMessage(Alert.AlertType.ERROR, "Somente administradores podem cadastrar categorias!");
            } else {
                showMessage(Alert.AlertType.ERROR, "Erro ao salvar categoria! " + resposta[1]);
            }

        } catch (IOException e) {
            showMessage(Alert.AlertType.ERROR, "Nao foi possivel conectar a API (localhost:8080). Ela esta rodando?");
        }

    }


    @FXML
    public void voltar(ActionEvent event) throws IOException {
        FXMLLoader loader =
                new FXMLLoader(getClass().getResource("/com/example/back/menu-view.fxml"));

        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);

    }

    // Devolve {codigo http, corpo da resposta}
    private String[] executaMetodoAPI(String url, String json, String protocoloHttp, String token)
            throws Exception {

        URL urlAPI = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlAPI.openConnection();
        connection.setRequestMethod(protocoloHttp);
        connection.setDoOutput(true);

        if (!json.isEmpty()) {
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            if (token != null) {
                connection.setRequestProperty("Authorization", "Bearer " + token);
            }

            try (OutputStream os = connection.getOutputStream()) {
                os.write(json.getBytes("UTF-8"));
            }

        }

        int codigo = connection.getResponseCode();
        var stream = codigo >= 400 ? connection.getErrorStream() : connection.getInputStream();

        var sb = new StringBuilder();
        if (stream != null) {
            var br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
        }

        return new String[]{String.valueOf(codigo), sb.toString()};

    }

    private void showMessage(Alert.AlertType type, String msg) {
        Alert alerta = new Alert(type);
        alerta.setTitle("Mensagem do Sistema!");
        alerta.setHeaderText(null);
        alerta.setContentText(msg);
        alerta.showAndWait();
    }

}
