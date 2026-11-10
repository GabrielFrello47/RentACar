package com.example.back;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {


    // Chave digitada na tela de login do desktop.
    private String key = "gabrielgay";

    // Chave da API (spring.secret no application.properties), enviada como "secretKey"
    // ao cadastrar o administrador. A API exige no minimo 16 caracteres, por isso e outra.
    public static final String KEY = "dfgnufby80feubgvhbnf";

    @FXML
    private TextField txtKey;

    @FXML
    protected void onLoginButtonClick(ActionEvent event) throws IOException {

        if (txtKey.getText().equals(key)) {
            showMessage(Alert.AlertType.INFORMATION, "Login Efetuado com sucesso!");

            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/com/example/back/menu-view.fxml"));

            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);

        } else {
            showMessage(Alert.AlertType.ERROR, "Erro ao Efetuar login!");
        }
    }

    private void showMessage(Alert.AlertType type, String msg) {
        Alert alerta = new Alert(type);
        alerta.setTitle("Mensagem do Sistema!");
        alerta.setHeaderText(null);
        alerta.setContentText(msg);
        alerta.showAndWait();
    }

}
