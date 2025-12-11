package UI;

import domain.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import service.NetworkService;

/**
 * Simple login dialog that authenticates a user before showing the main UI.
 */
public class LoginController {
    private final NetworkService service;
    private final Stage stage;
    private final Controller appController;
    private final Stage primaryStage;
    private final Parent root;

    public LoginController(NetworkService service, Controller appController, Stage primaryStage, Parent root) {
        this.service = service;
        this.appController = appController;
        this.primaryStage = primaryStage;
        this.root = root;
        this.stage = new Stage();
    }

    public void show() {
        GridPane pane = new GridPane();
        pane.setPadding(new Insets(20));
        pane.setVgap(10);
        pane.setHgap(10);
        pane.setAlignment(Pos.CENTER);

        TextField emailField = new TextField();
        emailField.setPromptText("email");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("parola");
        Button loginButton = new Button("Login");

        pane.add(new Label("Email"), 0, 0);
        pane.add(emailField, 1, 0);
        pane.add(new Label("Parola"), 0, 1);
        pane.add(passwordField, 1, 1);
        pane.add(loginButton, 1, 2);

        loginButton.setOnAction(e -> handleLogin(emailField.getText(), passwordField.getText()));

        stage.setScene(new Scene(pane, 350, 200));
        stage.setTitle("Autentificare");
        stage.show();
    }

    private void handleLogin(String email, String password) {
        try {
            User user = service.login(email, password);
            appController.setLoggedInUser(user);
            primaryStage.setScene(new Scene(root, 900, 600));
            primaryStage.setTitle("Duck Social Network");
            primaryStage.show();
            stage.close();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage());
            alert.setHeaderText("Login esuat");
            alert.showAndWait();
        }
    }
}
