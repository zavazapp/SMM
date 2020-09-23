package controlers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import utils.AlertUtils;

/**
 * FXML Controller class
 *
 * @author Miodrag Spasic
 */
public class LoginController implements Initializable {

    public static String MODE;
    public static String NAME;

    private final String TBO_COLOR = "512B58";
    private final String PPI_COLOR = "5050C8";
    private final String VERSION = "2020.2.0";

    private Alert alert;
    private Button button;

    @FXML
    private Pane rightHalfPane;
    @FXML
    private Label versionLabel;
    @FXML
    private Label userEmailLabel;
    @FXML
    private Label deptLabel;
    @FXML
    private PasswordField passField;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        MODE = "PPI";
        deptLabel.setText(MODE);
        versionLabel.setText("Ver. " + VERSION);

//        String email = System.getProperty("user.name") != null ? System.getProperty("user.name") : "";
//        userEmailLabel.setText(email != null ? email.concat("@aikbanka.rs") : "Unknown user");
//
//        NAME = utils.StringUtils.getNameFromUser(email);
        setMode(null);
    }

    /**
     * Sets mode in static variable MODE and color right side of login scene
     *
     * @param evt
     */
    public void setMode(MouseEvent evt) {

        if (evt != null) {
            button = (Button) evt.getSource();
            MODE = button.getText();
            preferences.PPIPreferences.getInstance(getClass()).setLastMode();
        } else {
            MODE = preferences.PPIPreferences.getInstance(getClass()).getLastMode();
        }
        if (MODE == null) {
            MODE = "PPI";
        }

        switch (MODE) {
            case "PPI":
                rightHalfPane.setStyle("-fx-background-color: #" + PPI_COLOR);
                break;
            case "TBO":
                rightHalfPane.setStyle("-fx-background-color: #" + TBO_COLOR);
                break;
        }
        deptLabel.setText(MODE);
        passField.requestFocus();
    }

    public void exit() {
        System.exit(0);
    }

    public void login() throws IOException {
        if (passField.getText().equals(MODE.toLowerCase())) {
            loadScene();
        } else {

            alert.showAndWait();
            passField.setText("");
            passField.requestFocus();
        }
    }

    public void onKeyPressed(KeyEvent evt) throws IOException {
        if (evt.getCode().equals(KeyCode.ENTER)) {
            if (alert == null) {
                alert = AlertUtils.getSimpleAlert(Alert.AlertType.ERROR, "Login error!", "Wrong password!", "Try again.");
            }

            if (!alert.isShowing()) {
                login();
            }
        }
    }

    public void loadScene() throws IOException {

        FXMLLoader loader = null;
        switch (MODE) {
            case "PPI":
                loader = new FXMLLoader(getClass().getResource("/FXMLFiles/PPI.fxml"));
                break;
            case "TBO":
                loader = new FXMLLoader(getClass().getResource("/FXMLFiles/TBO.fxml"));
                break;
        }

        if (loader != null) {
            Stage stage = (Stage) rightHalfPane.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            Platform.runLater(() -> {
                stage.show();
            });
        }
    }
}
