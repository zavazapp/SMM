package controlers;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import preferences.PPIPreferences;
import preferences.TBOPreferences;

/**
 * FXML Controller class
 *
 * @author Miodrag Spasic
 */
public class SettingsController implements Initializable {

    private preferences.MasterPreferences preferences;
    ObservableList<SettingsItem> data;
    private ArrayList<SettingsItem> items;
    public static BooleanProperty SETTINGS_CHANGED = new SimpleBooleanProperty(false);
    public static BooleanProperty OBSERVER_STATUS = new SimpleBooleanProperty(false);
    private boolean watchStatus;

    @FXML
    private TableView table;
    @FXML
    private Label title;
    @FXML
    private RadioButton notificationRadioButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        title.setText(LoginController.MODE + " Settings");
        setTableData();
        notificationRadioButton.setSelected(preferences.getObserverStatus());
        watchStatus = notificationRadioButton.isSelected();

    }

    private void setTableData() {
        items = new ArrayList<>();
        table.getItems().clear();

        if (LoginController.MODE.equals("PPI")) {
            preferences = PPIPreferences.getInstance(getClass());
            items.add(new SettingsItem("PPI_ROOT", preferences.getRoot().toString()));
            items.add(new SettingsItem("TBO_DIR", preferences.getSendDir().toString()));
            for (String s : preferences.getFOLDERS()) {
                items.add(new SettingsItem(s, preferences.getSpecificForder(Arrays.asList(preferences.getFOLDERS()).indexOf(s)).toString()));
            }

        }

        if (LoginController.MODE.equals("TBO")) {
            preferences = TBOPreferences.getInstance(getClass());
            items.add(new SettingsItem("TBO_ROOT", preferences.getRoot().toString()));
            items.add(new SettingsItem("TBO_DOSIJEI", preferences.getDosijeiFolder().toString()));
            items.add(new SettingsItem("PPI_DIR", preferences.getSendDir().toString()));
            for (String s : preferences.getFOLDERS()) {
                items.add(new SettingsItem(s, preferences.getSpecificForder(Arrays.asList(preferences.getFOLDERS()).indexOf(s)).toString()));
            }
        }

        data = table.getItems();
        data.addAll(items);
    }

    @FXML
    private void onRawClicked(MouseEvent evt) {
        if (evt.getClickCount() == 2) {
            int index = table.getSelectionModel().getSelectedIndex();
            SettingsItem clickedItem = (SettingsItem) table.getSelectionModel().getSelectedItem();

            File fromChooser = openFileChooser(clickedItem.getPath());
            if (fromChooser != null) {
                clickedItem.setPath(fromChooser.getPath());
                switch (index) {
                    case 0:
                        preferences.setRoot(fromChooser.toPath());
                        setTableData();
                        break;
                    case 1:
                        preferences.setDosijeiFolder(fromChooser.toPath());
                        break;
                    case 2:
                        preferences.setSendDir(fromChooser.toPath());
                        break;
                    default:
                        preferences.setSpecificFolder(clickedItem.getName(), fromChooser.toPath());
                        break;
                }

                data.set(index, clickedItem);
                SETTINGS_CHANGED.set(!SETTINGS_CHANGED.get());
            }

        }
    }

    private File openFileChooser(String path) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Choose directory");
        if (new File(path).exists()) {
            dirChooser.setInitialDirectory(new File(path));
        } else {
            dirChooser.setInitialDirectory(null);
        }

        return dirChooser.showDialog(table.getScene().getWindow());
    }

    @FXML
    private void onResetToDefaultClick() {
        preferences.resetPreferencesToDefault();
        setTableData();
    }

    @FXML
    private void onCloseClick() {
        ((Stage) title.getScene().getWindow()).close();
    }

    //Settings item model
    public class SettingsItem {

        private SimpleStringProperty name;
        private SimpleStringProperty path;

        public SettingsItem(String name, String path) {
            this.name = new SimpleStringProperty(name);
            this.path = new SimpleStringProperty(path);
        }

        public SettingsItem() {
            this("", "");
        }

        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public String getPath() {
            return path.get();
        }

        public void setPath(String path) {
            this.path.set(path);
        }

    }

    @FXML
    private void onNotificationRBClick(ActionEvent evt) {
        MasterControler.OBSERVE_ON = notificationRadioButton.isSelected();
        preferences.setObserverStatus(MasterControler.OBSERVE_ON);
        OBSERVER_STATUS.set(MasterControler.OBSERVE_ON);
    }
}
