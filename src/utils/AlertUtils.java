package utils;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

/**
 *
 * @author Korisnik
 */
public class AlertUtils {

    public static Alert getSimpleAlert(Alert.AlertType type, String title, String headerText, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(content);
        return alert;
    }

    public static Alert getSimpleAlert(Stage stage, Alert.AlertType type, String title, String headerText, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        if (stage != null) {
            alert.initOwner(stage);
        }
        alert.setHeaderText(headerText);
        alert.setContentText(content);
        return alert;
    }

    public static TextInputDialog showInputDialog(String title, String headerText, String content) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(content);
        return dialog;
    }

    public static TextInputDialog showInputDialog(Stage stage, String title, String headerText, String content) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(content);
        if (stage != null) {
            dialog.initOwner(stage);
        }
        return dialog;
    }

}
