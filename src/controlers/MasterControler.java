package controlers;

import Tools.DirectoryWatcher;
import Tools.IOnFileReceived;
import Tools.IOnFileReceived2;
import Tools.IOnResetWatcher;
import static controlers.LoginController.MODE;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.DisplayDataModels.DisplayDataModel;
import models.DisplayDataModels.PPIDisplayDataModel;
import models.DisplayDataModels.TBODisplayDataModel;
import javafx.scene.control.DatePicker;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.MTMessages.MTDAO;
import models.MTMessages.MTEntity;
import models.TicketEntity;
import models.TicketEntity2;
import models.TicketTable;
import preferences.PPIPreferences;
import preferences.TBOPreferences;
import utils.AlertUtils;
import utils.DateUtils;
import utils.FileUtils;

public class MasterControler implements Initializable, ListChangeListener<MTEntity>, IOnFileReceived2, IOnResetWatcher {

    // <editor-fold defaultstate="collapsed" desc="user-description">
    public static boolean CHANGING_FILE_NAME_ONLY;
    public static boolean OBSERVE_ON;
    private static DisplayDataModel d;
    private static preferences.MasterPreferences preferences;
    private final MTDAO mtdao = new MTDAO();
    private Label previousSelectionLabel;
    private Label currentSelectionLabel;
    private ObservableList<MTEntity> tableData;
    private FileUtils fileUtils;
    private Stage stage;
    private final IOnFileReceived notificationControler = new NotificationTransitionController();
    private static DirectoryWatcher watcher;
    private static final SimpleIntegerProperty ticketsCount2 = new SimpleIntegerProperty();
    private RotateTransition rotateTransition;
    private static TicketTable ticketsTable;
    private static TableView<TicketEntity2> ticketsTableView;
    public static boolean IS_TICKET_MODE;

    @FXML
    private DatePicker datePicker;
    @FXML
    private HBox headerHBox;
    @FXML
    private VBox navHBox;
    @FXML
    MenuItem menuItemLive;
    @FXML
    MenuItem menuItemArchive;
    @FXML
    Label liveLabel;
    @FXML
    private Label masterCount;
    @FXML
    private Label ticketsCount;
    @FXML
    private Label completeLabel;
    @FXML
    private Label lastSelectionDisplay;
    @FXML
    private VBox tableContainer;
    @FXML
    private TableView table;
    @FXML
    private VBox scene;
    @FXML
    private Button renameButton;
    @FXML
    private Button archiveButton;
    @FXML
    private Button pensionButon;
    @FXML
    private Button sendButton;
    @FXML
    private Label refreshLabel;

    // </editor-fold>
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        IS_TICKET_MODE = false;
        createDisplayObject();
        d.setNavLabels(getLabels());
        invalidateLiveMenu();
        invalidateCount(LocalDate.now());
        datePicker.setValue(d.getDate());
        addDatePickerValueChangeListener();
        tableData = table.getItems();
        invalidateOnSeparateThread(LocalDate.now());
        setRawClickListener();
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        fileUtils = new FileUtils();
        setRawPaintPolicy();
        setSettingsListener();
        OBSERVE_ON = preferences.getObserverStatus();
        rotateTransition = new RotateTransition(Duration.millis(2000), refreshLabel);
        rotateTransition.setByAngle(360 * 7);
        if (OBSERVE_ON) {
            setDirectoryWatchers();
        }
        if (MODE.equals("TBO")) {
            ticketsCount2.set(mtdao.getTicketCount(preferences.getTicketsFolder()));
            ticketsCount.textProperty().bind(ticketsCount2.asString());
        }
    }

    private void createDisplayObject() {
        /*PPI*/
        if (LoginController.MODE.equals("PPI")) {
            preferences = PPIPreferences.getInstance(getClass());
            if (d != null) {
                d.getData().removeListener(this);
                d = null;
            }

            d = new PPIDisplayDataModel(
                    true,
                    mtdao.getMasterCount(preferences.getRoot(), preferences.getFOLDERS(), new Date(System.currentTimeMillis()), true),
                    0,
                    LocalDate.now(),
                    mtdao.getObservableList(preferences.getRoot(), new Date(System.currentTimeMillis()), "", true),
                    null, null, null, null
            );
        }

        /*TBO*/
        if (LoginController.MODE.equals("TBO")) {
            preferences = TBOPreferences.getInstance(getClass());
            d = new TBODisplayDataModel(
                    true,
                    mtdao.getMasterCount(preferences.getRoot(), preferences.getFOLDERS(), new Date(System.currentTimeMillis()), true), // mt messages
                    mtdao.getTicketCount(preferences.getTicketsFolder()), // tickets
                    LocalDate.now(),
                    mtdao.getObservableList(preferences.getRoot(), new Date(System.currentTimeMillis()), "", true),
                    null, null, null
            );
        }
        d.getData().addListener(this);

    }

    @FXML
    private void onMenuClick(ActionEvent evt) throws IOException, Exception {
        MenuItem e = (MenuItem) evt.getSource();
        switch (e.getId()) {
            case "close":
                System.exit(0);
                break;
            case "merge_pdf":
                loadScene("merge_pdf");
                break;
            case "settings":
                loadScene("settings");
                break;
            case "tbo":
                loadScene(e.getId());
                break;
            case "ppi":
                loadScene(e.getId());
                break;
            case "t1":
                loadScene(e.getId());
                break;
            case "live":
                d.setLive(true);
                datePicker.setValue(LocalDate.now());
                if (d.getDate().equals(datePicker.getValue())) {
                    invalidateLiveMenu();
                    invalidateScene();
                    invalidateCount(LocalDate.now());
                    invalidateOnSeparateThread(datePicker.getValue());
                }
                break;
            case "archive":
                d.setLive(false);
                invalidateLiveMenu();
                invalidateScene();
                invalidateCount(datePicker.getValue());
                invalidateOnSeparateThread(datePicker.getValue());
                break;
        }
    }

    private void invalidateLiveMenu() {
        menuItemLive.setDisable(d.isLive());
        menuItemArchive.setDisable(!d.isLive());
        liveLabel.setText(d.isLive() ? "Live" : "Archive");

    }

    private void invalidateScene() {
        renameButton.setDisable(!d.isLive());
        archiveButton.setDisable(!d.isLive());
        if (pensionButon != null) {
            pensionButon.setDisable(!d.isLive());
        }

        if (d.isLive()) {
            headerHBox.setOpacity(1d);
            navHBox.setOpacity(1d);
        } else {
            headerHBox.setOpacity(0.8d);
            navHBox.setOpacity(0.8d);
        }
    }

    private void invalidateCount(LocalDate newValue) {
        d.setTotalCount(mtdao.getMasterCount(preferences.getRoot(), preferences.getFOLDERS(), java.sql.Date.valueOf(newValue), d.isLive()));
        if (MODE.equals("TBO")) {
            d.setTicketCount(mtdao.getTicketCount(preferences.getTicketsFolder()));
            ticketsCount2.set(mtdao.getTicketCount(preferences.getTicketsFolder()));
        }

        masterCount.setText(String.valueOf(d.getTotalCount()));
        completeLabel.setVisible(d.getTotalCount() == 0);
    }

    private void invalidateData(LocalDate newValue) {
        if (currentSelectionLabel != null) {
            d.setData(mtdao.getObservableList(preferences.getRoot(), java.sql.Date.valueOf(newValue), currentSelectionLabel.getId(), d.isLive()));
        }
    }

    private void invalidateTable() {
        new Runnable() {
            @Override
            public void run() {
                tableData.clear();
                for (MTEntity mTEntity : d.getData()) {
                    tableData.add(mTEntity);
                }
            }
        }.run();

    }

    //can invalidate data, table and nav labels
    private Task task;
    private Thread thread;

    private void invalidateOnSeparateThread(LocalDate newValue) {
        setRawPaintPolicy();
        scene.setCursor(Cursor.WAIT);
        if (task != null) {
            task.cancel();
        }

        task = new Task() {
            @Override
            protected Object call() throws Exception {

                invalidateData(newValue);
                invalidateTable();
                invalidateNavLabels();
//                invalidateRowColors();
                scene.setCursor(Cursor.DEFAULT);
                return null;
            }

            int count;
            String labelText;

            private void invalidateNavLabels() {

                Platform.runLater(() -> {
                    for (int i = 0; i < d.getNavLabels().length; i++) {
                        count = mtdao.getCount(Paths.get(preferences.getRoot().toString(), preferences.getFOLDERS()[i]), java.sql.Date.valueOf(datePicker.getValue()), d.isLive());
                        if (d.getNavLabels()[i].getText().contains("Sent")) {
                            labelText = "Sent";
                        } else {
                            labelText = "Rec";
                        }

                        if (count > 0) {
                            d.getNavLabels()[i].setStyle("-fx-font-weight: bold;");
                        }
                        d.getNavLabels()[i].setText(labelText.concat("(").concat(String.valueOf(count).concat(")")));
                    }
                    invalidateCount(d.getDate());

                    invalidateButtons();
                });
            }

        };

        thread = new Thread(task);

        thread.start();
    }

    private void invalidateButtons() {
        if (currentSelectionLabel != null) {
            sendButton.setDisable(!(currentSelectionLabel.getId().contains("MT202")
                    || currentSelectionLabel.getId().contains("MT19")
                    || currentSelectionLabel.getId().contains("MT299")
                    || currentSelectionLabel.getId().contains("MT999")));
        }
        sendButton.setDisable(IS_TICKET_MODE);
        renameButton.setDisable(IS_TICKET_MODE);
    }

    private void addDatePickerValueChangeListener() {
        datePicker.valueProperty().addListener((ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) -> {
            d.setDate(newValue);
            d.setLive(newValue.equals(LocalDate.now()));
            invalidateLiveMenu();
            invalidateScene();
            invalidateOnSeparateThread(newValue);
            invalidateCount(newValue);
            if (IS_TICKET_MODE) {
                try {
                    ticketsTable.setData(newValue);
                } catch (IOException ex) {
                    Logger.getLogger(MasterControler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    @Override
    public void onChanged(Change<? extends MTEntity> c) {
        //update table and labels
        if (c.next()) {
            d.setData(mtdao.getObservableList(preferences.getRoot(), java.sql.Date.valueOf(datePicker.getValue()), currentSelectionLabel.getId(), d.isLive()));
        }
        invalidateCount(datePicker.getValue());
    }

    @FXML
    private void onNavMenuClicked(MouseEvent evt) {
        IS_TICKET_MODE = false;
        tableContainer.getChildren().set(0, table);
        if (previousSelectionLabel != null) {
            previousSelectionLabel.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.web(MODE.equals("PPI") ? "#5050C8" : "#512B58", 1d), new CornerRadii(0.8d), new Insets(0.8d))));
        }

        currentSelectionLabel = (Label) evt.getSource();
        currentSelectionLabel.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.web("#8383CA", 0.8d), new CornerRadii(5.0d), new Insets(0.0d))));
        previousSelectionLabel = currentSelectionLabel;
        lastSelectionDisplay.setText(currentSelectionLabel.getId() + " " + currentSelectionLabel.getText());

        invalidateOnSeparateThread(datePicker.getValue());
    }

    public void loadScene(String id) throws IOException, Exception {

        FXMLLoader loader = null;
        switch (id) {

            case "ppi":
            case "tbo":
                if (!id.equals(MODE.toLowerCase())) {
                    MODE = id.toUpperCase();
                    PPIPreferences.getInstance(getClass()).setLastMode();
                    loader = new FXMLLoader(MasterControler.class.getResource("/FXMLFiles/Login.fxml"));
                    stage = (Stage) scene.getScene().getWindow();

                }
                break;
            case "mt_dispay":
                loader = new FXMLLoader(MasterControler.class.getResource("/FXMLFiles/MtTextDisplay.fxml"));
                stage = new Stage();
                break;
            case "merge_pdf":
                loader = new FXMLLoader(MasterControler.class.getResource("/FXMLFiles/PDFMerger.fxml"));
                stage = new Stage();
                break;
            case "settings":
                loader = new FXMLLoader(MasterControler.class.getResource("/FXMLFiles/Settings.fxml"));
                stage = new Stage();
                break;
            case "t1":
                loader = new FXMLLoader(MasterControler.class.getResource("/FXMLFiles/T1.fxml"));
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
                case "t1":
                    break;
            }

            if (stage != null) {
                stage.setScene(newScene);
                stage.show();
                stage.centerOnScreen();

            }
        }
    }

    private Label[] getLabels() {
        String[] strings = preferences.getFOLDERS();
        Label[] labels = new Label[preferences.getFOLDERS().length];

        for (int i = 0; i < labels.length; i++) {
            labels[i] = (Label) scene.lookup("#" + strings[i]);
        }
        return labels;
    }

    private MTEntity clickedRow;

    private void setRawClickListener() {
        table.setRowFactory((Object tv) -> {
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
        });
    }

    @FXML
    private void onRenameClicked() {
        CHANGING_FILE_NAME_ONLY = true; //custom handling of avoidance to notification display on rename
        ObservableList<Integer> selectedIndices = table.getSelectionModel().getSelectedIndices();
        d.setData(table.getItems());

        int counter = 0; //number of renamed files - adds if fileRename methods return true or !=null

        //check how meny msgs. are selected
        if (selectedIndices.isEmpty()) {
            AlertUtils.getSimpleAlert(getStage(), Alert.AlertType.INFORMATION, "No sellected message", "Action aborted!", "You must sellect at least one message").show();
            return;
        }
        Optional o = AlertUtils.getSimpleAlert(getStage(), Alert.AlertType.CONFIRMATION, "You are about to rename files ... ", "Action rename!", "Rename selected files? " + "(" + selectedIndices.size() + ")\nFolder: " + lastSelectionDisplay.getText()).showAndWait();
        if (o.get().equals(ButtonType.OK)) {

        } else {
            scene.setCursor(Cursor.DEFAULT);
            return;
        }

        //start renaming
        scene.setCursor(Cursor.WAIT);
        for (int m : selectedIndices) {

            try {
                if (d.getData().get(m).getType().equals("FIN 950") || d.getData().get(m).getType().equals("FIN 940")) {
                    File newFile = fileUtils.renameAccountStatement(d.getData().get(m).getFile().getParent(), d.getData().get(m).getFile());
                    if (newFile != null) {
                        counter++;
                        d.getData().get(m).setRenamed(true);
                        d.getData().get(m).setFile(newFile);
                    }
                    //for other msgs. use default rename method
                } else {
                    if (fileUtils.renameMt(d.getData().get(m)) != null) {
                        d.getData().get(m).setRenamed(true);
                        counter++;
                    }

                }

            } catch (IOException ex) {
                Logger.getLogger(MasterControler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        CHANGING_FILE_NAME_ONLY = false;
        scene.setCursor(Cursor.DEFAULT);

        //inform user about action completion
        AlertUtils.getSimpleAlert(getStage(), Alert.AlertType.INFORMATION, "Files renamed.", "Action completed!", counter + " files renamed").show();
        table.refresh();
    }

    @FXML
    private void onArchiveClick(MouseEvent evt) {
        if (IS_TICKET_MODE) {
            ticketsTable.onArchiveClick(getStage());
            invalidateCount(datePicker.getValue());
            return;
        }

        ObservableList<Integer> selectedIndices = table.getSelectionModel().getSelectedIndices();
        d.setData(table.getItems());

        //check selection
        if (selectedIndices.isEmpty()) {
            AlertUtils.getSimpleAlert(getStage(), Alert.AlertType.INFORMATION, "No sellected message", "Action aborted!", "You must sellect at least one message").show();
            return;
        }

        //check if files are renamed
        for (Integer i : selectedIndices) {
            if (!d.getData().get(i).isRenamed()) {
                AlertUtils.getSimpleAlert(getStage(), Alert.AlertType.INFORMATION, "Archive action stoped", "Action aborted!", "You can not archive messages that are not renamed.").show();
                return;
            }
        }

        int counter = 0; //count messages that are succesfuly renamed
        File archiveFolder; //destination folder for archiving messages.
        boolean isPension = false; //differentiate pennsion related messages to store in specific folder defined in settings

        //check if it is pension (defined in clicked button name property)
        String name = ((Button) (evt.getSource())).getId();
        if (name != null && name.equals("pension")) {
            archiveFolder = preferences.getPensionsFolder().toFile();
            isPension = true;
        } else {
            //create archive folders
            //TODO - check earlier if folders are set and mark as set to avoid unnecesary code execution
            File archiveRoot = new File(d.getData().get(0).getFile().getParentFile().getParent() + File.separator + "MT_arhiva/");

            if (!archiveRoot.exists()) {
                archiveRoot.mkdir();
            }

            File archiveRootYear = new File(archiveRoot + File.separator + Calendar.getInstance().get(Calendar.YEAR));

            if (!archiveRootYear.exists()) {
                archiveRootYear.mkdir();
            }

            archiveFolder = new File(archiveRootYear + File.separator + currentSelectionLabel.getId() + "_archive");

            if (!archiveFolder.exists()) {
                archiveFolder.mkdir();
            }
        }

        //confirm dialog action
        Optional o = AlertUtils.getSimpleAlert(getStage(), Alert.AlertType.CONFIRMATION, "You are about to archive files ... ", "Action archive!", "Archive selected files? " + "(" + selectedIndices.size() + ")\nFolder: " + lastSelectionDisplay.getText()).showAndWait();

        if (!o.get().equals(ButtonType.OK)) {
            scene.setCursor(Cursor.DEFAULT);
            return;
        }

        //start archiving
        scene.setCursor(Cursor.WAIT);

        int control = 0;
        ArrayList indecisToRemove = new ArrayList();

        for (Integer i : selectedIndices) {

            if (isPension) {
                String clientName = "";
                Optional<String> result = AlertUtils.showInputDialog(getStage(), "Action pension archive!", "Client name:", "").showAndWait();
                if (result.isPresent()) {
                    clientName = result.get();
                }

                while (!result.isPresent()) {
                    result = AlertUtils.showInputDialog(getStage(), "Action pension archive!", "Client name:", "").showAndWait();
                    if (result.get() == null) {
                        scene.setCursor(Cursor.DEFAULT);
                        return;
                    } else {
                        clientName = result.get();
                    }
                }

                if (result.get() != null) {
                    d.getData().get(i).setFile(fileUtils.renamePension(d.getData().get(i), clientName.toUpperCase().trim()).getFile());
                    control = fileUtils.archiveMt(archiveFolder.getPath(), d.getData().get(i).getFile(), "");
                }

            } else if (d.getData().get(i).getType().contains("99") || d.getData().get(i).getType().contains("19")) {
                control = fileUtils.archiveMt(archiveFolder.getPath(), d.getData().get(i).getFile(), d.getData().get(i).getDateReceived().substring(0, 8).replaceAll("/", "."));
            } else {
                control = fileUtils.archiveMt(archiveFolder.getPath(), d.getData().get(i).getFile(), d.getData().get(i).getValueDate());
            }

            counter += control;

            if (control == 1) {
                indecisToRemove.add(i);
            }
            control = 0;
        }

        //refresh data
        for (int i = indecisToRemove.size() - 1; i > -1; i--) {
            d.getData().remove((int) indecisToRemove.get(i));
        }
        AlertUtils.getSimpleAlert(getStage(), Alert.AlertType.INFORMATION, "", "Action completed!", counter + " files archived.").show();
        scene.setCursor(Cursor.DEFAULT);
        invalidateOnSeparateThread(LocalDate.now());
    }

    @FXML
    private void onSendClick(MouseEvent evt) {
        String id = ((Button) (evt.getSource())).getId();
        int counter = 0; //counts sent messages.
        ObservableList<Integer> selectedIndices = table.getSelectionModel().getSelectedIndices();

        //check selection
        if (selectedIndices.isEmpty()) {
            AlertUtils.getSimpleAlert(getStage(), Alert.AlertType.INFORMATION, "No sellected message", "Action aborted!", "You must sellect at least one message").show();
            return;
        }

        //check if destination folder is set
        if (preferences.getSendDir().toString().isEmpty()) {
            AlertUtils.getSimpleAlert(getStage(), Alert.AlertType.INFORMATION, id + " folder not defined!", "Action aborted!", id + " folder not set.\nGo to settings to define " + id + " folder.").show();
            scene.setCursor(Cursor.DEFAULT);
            return;
        }

        //check if dest. folder is accesible
        //TODO - not tested
        if (!preferences.getSendDir().toFile().canRead() && !preferences.getSendDir().toFile().canWrite()) {
            AlertUtils.getSimpleAlert(getStage(), Alert.AlertType.INFORMATION, id + " \" access denied!", "Action aborted!", "You do not have permition to write to " + id + " folder.\n" + preferences.getSendDir().toString()).show();
            scene.setCursor(Cursor.DEFAULT);
            return;
        }

        //confirm sending action
        Optional o = AlertUtils.getSimpleAlert(getStage(), Alert.AlertType.CONFIRMATION, "Sending files to  " + id, "Action Send!", "Send selected files to" + id + "? " + "(" + selectedIndices.size() + ")\nFolder: " + lastSelectionDisplay.getText()).showAndWait();
        if (!o.get().equals(ButtonType.OK)) {
            scene.setCursor(Cursor.DEFAULT);
            return;
        }
        scene.setCursor(Cursor.WAIT);

        //start seding (changing file location with renameTo(File dest) method in FileUtils class)
        int control;
        ArrayList indecisToRemove = new ArrayList(); //colect sent messages indices

        for (Integer i : selectedIndices) {

            if (!d.isLive()) {
                control = fileUtils.archiveMt(preferences.getSendDir().toString(), d.getData().get(i).getFile(), d.getData().get(i).getFile().getParentFile().getParentFile().getName().replace("_archive", ""));
            } else {
                control = fileUtils.archiveMt(preferences.getSendDir().toString(), d.getData().get(i).getFile(), d.getData().get(i).getFile().getParentFile().getName());
            }
            counter += control;

            if (control == 1) {
                indecisToRemove.add(i);
            }
            control = 0;
        }

        //remove sent msgs. from data and table
        for (int i = indecisToRemove.size() - 1; i > -1; i--) {
            d.getData().remove((int) indecisToRemove.get(i));
        }

        //refresh table and labels
        AlertUtils.getSimpleAlert(getStage(), Alert.AlertType.INFORMATION, "Send to " + id + " completed", id + " completed", counter + " files sent to " + id).show();

        scene.setCursor(Cursor.DEFAULT);
        invalidateOnSeparateThread(LocalDate.now());
    }

    private void setRawPaintPolicy() {
        ObservableList<TableColumn<MTEntity, String>> cols = table.getColumns();
        setRawPaintPolicy(cols.get(0));
    }

    private void setRawPaintPolicy(TableColumn<MTEntity, String> calltypel) {
        calltypel.setCellFactory((TableColumn<MTEntity, String> column) -> {
            return new CustomCellFormat();
        });

    }

    private void setSettingsListener() {
        SettingsController.SETTINGS_CHANGED.addListener((Observable observable) -> {
            invalidateOnSeparateThread(LocalDate.now());

        });
        SettingsController.OBSERVER_STATUS.addListener((Observable o) -> {
            if (SettingsController.OBSERVER_STATUS.get()) {
                setDirectoryWatchers();
            } else {

            }
        });
    }

    private void setDirectoryWatchers() {

        if (!OBSERVE_ON || watcher != null) {
            watcher.closeWatcher();
        }
        Task t = new Task() {
            @Override
            protected Object call() {
                ArrayList<Path> dirs = new ArrayList<>();
                for (int i = 0; i < preferences.getFOLDERS().length; i++) {
                    if (preferences.getSpecificForder(i).toFile().exists() && !preferences.getSpecificForder(i).equals(preferences.getDosijeiFolder())) {
                        dirs.add(preferences.getSpecificForder(i));
                    }

                }
                if (MODE.equals("TBO") && preferences.getTicketsFolder().toFile().exists()) {
                    dirs.add(preferences.getTicketsFolder());
                }

                try {
                    watcher = new DirectoryWatcher(dirs);
                } catch (IOException ex) {
                    Logger.getLogger(MasterControler.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }
        };
        new Thread(t).start();
    }

    @Override
    public void onFileReceived(Path root, Path path) {
        Platform.runLater(() -> {
            if (countChanged(root)) {
                notificationControler.onFileReceived(root, path);
                if (MODE.equals("TBO")) {
                    ticketsCount2.set(mtdao.getTicketCount(preferences.getTicketsFolder()));
                }
            }
        });

    }

    private boolean countChanged(Path root) {
        int oldCount = 0;
        int newCount = mtdao.getCount(root, new java.sql.Date(System.currentTimeMillis()), true);
        String labelText;
        for (int i = 0; i < d.getNavLabels().length; i++) {
            labelText = d.getNavLabels()[i].getText();
            if (root.toString().contains(d.getNavLabels()[i].getId())) {
                oldCount = Integer.parseInt(labelText.substring(labelText.indexOf("(") + 1, labelText.indexOf(")")));
            }
        }
        return oldCount < newCount;
    }

    @Override
    public void resetWatcher() {
        setDirectoryWatchers();
    }

    @FXML
    private void onRefreshClick() {
        invalidateOnSeparateThread(LocalDate.now());
        invalidateCount(LocalDate.now());
        if (IS_TICKET_MODE) {
            try {
                ticketsTable.setData(datePicker.getValue());
            } catch (IOException ex) {
                Logger.getLogger(MasterControler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        rotateTransition.play();

    }

    private Stage getStage() {
        if (scene == null) {
            return null;
        }
        return (Stage) scene.getScene().getWindow();
    }

    @FXML
    private void onTicketLableClick() throws IOException {
        IS_TICKET_MODE = true;
        if (ticketsTable == null) {
            ticketsTable = new TicketTable();
            ticketsTableView = ticketsTable.getTableView();
        }

        tableContainer.getChildren().set(0, ticketsTableView);
        ticketsTable.setData(datePicker.getValue());
        invalidateButtons();
    }

}
