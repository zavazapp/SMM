package controlers;

import Tools.IOnFileReceived;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import models.MTMessages.MTEntity;
import utils.FileUtils;

/**
 * FXML Controller class
 *
 * @author Miodrag Spasic
 */
public class NotificationController implements Initializable, IOnFileReceived {

    private static ObservableList<MTEntity> data;
    private final FileUtils fileUtils = new FileUtils();

    public static Stage stage;
    public static NotificationModel notification;
    public static MTEntity clickedRow;

    @FXML
    private TableView<MTEntity> table;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        data = table.getItems();
        setRawClickListener();

    }

    @Override
    public void onFileReceived(Path root, Path filePath) {
        if (notification == null) {
            try {
                notification = new NotificationModel(
                        new FXMLLoader(NotificationController.class.getResource("/FXMLFiles/Notification.fxml")),
                        new Stage(StageStyle.TRANSPARENT)
                );
            } catch (IOException ex) {
                Logger.getLogger(NotificationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (!notification.isShowing()) {
            notification.show();
        }

        try {
            setData(root, filePath);
        } catch (IOException ex) {
            Logger.getLogger(NotificationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void onCloseClick() {
        notification.hide();
        notification = null;
    }

    public void setData(Path root, Path filePath) throws IOException {
        MTEntity mt = null;
        File file = new File(root + File.separator + filePath); //file that will be displayed in table

        mt = fileUtils.readMt(file);
        data.add(mt);
    }

    private void setRawClickListener() {
        table.setRowFactory(new Callback() {
            @Override
            public Object call(Object tv) {
                TableRow<MTEntity> row = new TableRow<>();
                row.setOnMouseClicked((MouseEvent event) -> {
                    if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY
                            && event.getClickCount() == 2) {

                        clickedRow = row.getItem();
                        try {
                            loadScene("mt_dispay");
                        } catch (IOException ex) {
                            Logger.getLogger(MasterControler.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (Exception ex) {
                            Logger.getLogger(MasterControler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
                return row;
            }
        });
    }

    public void loadScene(String id) throws IOException, Exception {

        FXMLLoader loader = null;
        switch (id) {
            case "mt_dispay":
                loader = new FXMLLoader(MasterControler.class.getResource("/FXMLFiles/MtTextDisplay.fxml"));
                stage = new Stage();
                break;
        }

        if (loader != null) {
            Scene newScene = new Scene(loader.load());
            newScene.setFill(null);

            switch (id) {
                case "mt_dispay":
                    MtTextDisplayController c = loader.getController();
                    c.setText(clickedRow);
                    break;
            }

            if (stage != null) {
                stage.setScene(newScene);
                stage.show();
            }
        }
    }
}
