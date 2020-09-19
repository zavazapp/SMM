package utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;

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

    public static TextInputDialog showInputDialog(String title, String headerText, String content) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(content);
        return dialog;
    }

}
