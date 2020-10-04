package controlers;

import Tools.HostServ;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.control.DatePicker;
import models.MTMessages.MTEntity;
import models.TicketEntity;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import preferences.TBOPreferences;
import utils.AlertUtils;
import utils.DateUtils;
import utils.Dosije;
import utils.FileUtils;

/**
 * FXML Controller class
 *
 * @author Miodrag Spasic
 */
public class T1Controller implements Initializable {

    //   <editor-fold defaultstate="collapsed" desc="variables">
    private ObservableList<T1Item> tempTickets;
    private final preferences.MasterPreferences preferences = TBOPreferences.getInstance(getClass());
    private final FileUtils fileUtils = new FileUtils();
    private ObservableList<T1Item> tempDataReceived;
    private ObservableList<T1Item> tempDataSent;
    private ObservableList<T1Item> tempDosijeData;
    private ObservableList<T1Item> resultData;
    private String[] availableFolders;
    private HashMap<String, TableView> tables;
    private Scene scene;
    private PDFMergerUtility mergeUtil;
    private String mergedName;
    private String notArchived;
    private String ticketDate;
    private SimpleStringProperty sugestedDosijeLocation = new SimpleStringProperty();
    private File dosijeFolder = new File(preferences.getDosijeiFolder().toString()); //root folder for archiving Dosije
    private File archiveFolder;
    private TicketEntity ticket;
    private static Dosije dosije;
    private ObservableList<String> comboList;

    @FXML
    private TableView<T1Item> ticketsTable;

    @FXML
    private TableView<T1Item> MT200OTable;

    @FXML
    private TableView<T1Item> MT200ITable;

    @FXML
    private TableView<T1Item> MT202OTable;

    @FXML
    private TableView<T1Item> MT202ITable;

    @FXML
    private TableView<T1Item> MT300OTable;

    @FXML
    private TableView<T1Item> MT300ITable;

    @FXML
    private TableView<T1Item> MT320OTable;

    @FXML
    private TableView<T1Item> MT320ITable;

    @FXML
    private TableView<T1Item> resultTable;

    @FXML
    private TableView<T1Item> dosijeiTable;

    @FXML
    private Button createButton;

    @FXML
    private TabPane tabPane;

    @FXML
    private SplitPane rootPane;

    @FXML
    private ComboBox sugestedFileNameCombo;

    @FXML
    private TextField sugMergeLocTF;

    @FXML
    private DatePicker datePicker;
    //</editor-fold>

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        datePicker.setValue(LocalDate.now());
        dosije = new Dosije();
        resultData = resultTable.getItems();
        comboList = sugestedFileNameCombo.getItems();
        setHashMapOfTables();
        availableFolders = preferences.getFOLDERS();
        bindInfo();
        addDatePickerValueChangeListener();
    }

    //when user select tab
    @FXML
    private void onTabSelectionChange(Event evt) {
        Tab clickedTab = null;
        if (tables == null) {
            setHashMapOfTables();
        }
        if (evt != null) {
            clickedTab = (Tab) evt.getSource();
        } else {
            clickedTab = tabPane.getSelectionModel().getSelectedItem();
        }

        if (clickedTab.isSelected()) {
            changeTempData(clickedTab.getId()); //update shown tables
        }
    }

    //spec 16 = tickets
    //bind table data to observable lists
    private void changeTempData(String id) {
        if (id.equals("tickets")) {
            tempTickets = tables.get("tickets").getItems();

        } else if (id.equals("dosijei")) {
            tempDosijeData = tables.get("dosijei").getItems();
        } else {
            tempDataReceived = tables.get(id + "O").getItems();
            tempDataSent = tables.get(id + "I").getItems();
        }

        int spec = 0;
        switch (id) {
            case "tickets":
                spec = -1;
                break;
            case "dosijei":
                spec = -2;
                break;
            case "MT200":
                spec = 0;
                break;
            case "MT202":
                spec = 2;
                break;
            case "MT300":
                spec = 4;
                break;
            case "MT320":
                spec = 6;
                break;

        }

        if (spec == -1) {
            fillTable(spec, tempTickets);
        } else if (spec == -2) {
            fillTable(spec, tempDosijeData);
        } else {
            fillTable(spec, tempDataSent);
            fillTable(spec + 1, tempDataReceived);
        }

    }

    //adds items to observable lists and table data updates
    private void fillTable(int spec, ObservableList<T1Item> list) {
        String resFolderName;
        String type;

        if (list != null) {
            list.clear();
            File[] folderItems;

            if (spec == -1) {
                folderItems = preferences.getTicketsFolder().toFile().listFiles();
                resFolderName = preferences.getTicketsFolder().toFile().getName();
                type = "ticket";
            } else if (spec == -2) {
                String path = preferences.getDosijeiFolder().toString() + File.separator
                        + datePicker.getValue().getYear() + File.separator
                        + datePicker.getValue().getMonthValue() + File.separator
                        + DateUtils.getFormatedDate(datePicker.getValue());

                folderItems = Paths.get(path).toFile().listFiles();
                if (folderItems == null) {
                    folderItems = new File[0];
                }
                resFolderName = preferences.getDosijeiFolder().toFile().getName();
                type = "dosijei";
            } else {
                folderItems = preferences.getSpecificForder(spec).toFile().listFiles();
                resFolderName = preferences.getSpecificForder(spec).toFile().getName();
                type = "mt";
            }

            for (File folderItem : folderItems) {
                T1Item item = new T1Item(folderItem.getName(), folderItem,
                        resFolderName,
                        type);
                if (!itemIsInResult(item)) {
                    list.add(item);
                }
            }
        }
    }

    @FXML
    private void onTempTableRawClicked(MouseEvent evt) {
        TableView t = (TableView) evt.getSource();
        if (evt.getClickCount() == 2) {
            onAnyTableDoubleClick(t);
        }

        if (evt.getButton().equals(MouseButton.SECONDARY)) {
            onTempTableSecondaryButtonClicked(t, evt);
        }
    }

    @FXML
    private void onResultTableRawClicked(MouseEvent evt) {
        if (evt.getClickCount() == 2) {
            TableView t = (TableView) evt.getSource();
            onAnyTableDoubleClick(t);

        }
        if (evt.getButton().equals(MouseButton.SECONDARY)) {
            ContextMenu menu = new ContextMenu();
            MenuItem item = new MenuItem("Remove from DOSIJE");
            item.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    T1Item clickedItem = (T1Item) resultTable.getSelectionModel().getSelectedItem();
                    resultData.remove(clickedItem);
                    if (resultData.isEmpty()) {
                        sugestedDosijeLocation.setValue(null);
                        comboList.clear();
                    }
                    try {
                        dosije.removeItem(clickedItem);
                    } catch (IOException ex) {
                        Logger.getLogger(T1Controller.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    comboList.remove(dosije.getFileNameFromItem(clickedItem));
                    onTabSelectionChange(null);
                }

            });

            menu.getItems().add(item);
            menu.show(tabPane.getScene().getWindow(), evt.getScreenX(), evt.getScreenY());
        }
    }

    private void onTempTableSecondaryButtonClicked(TableView t, MouseEvent evt) {

        ContextMenu menu = new ContextMenu();
        MenuItem item = new MenuItem("Add to DOSIJE");
        item.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                T1Item clickedItem = (T1Item) t.getSelectionModel().getSelectedItem();
//                MTEntity mt = null;
//                TicketEntity tic = null;
                String fileName;

                try {
                    dosije.addItem(clickedItem);
                    fileName = dosije.getFileNameFromItem(clickedItem);

                    if (fileName != null) {
                        comboList.add(fileName);
                    }

                } catch (IOException ex) {
                    Logger.getLogger(T1Controller.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (dosije.getSize() == 1) {
                    dosije.setArchiveLocation(clickedItem);
                    sugestedDosijeLocation.set(dosije.getArchiveLocation());
                }
                sugestedFileNameCombo.setValue(comboList.size() > 0 ? comboList.get(0) : "Dosije_" + System.currentTimeMillis() + ".pdf");

                //update lower table which keeps files to be merged and archived
                resultData.add(clickedItem);

                if (t.getId().contains("O")) {
                    tempDataReceived.remove(clickedItem);
                }
                if (tempDataSent != null && t.getId().contains("I")) {
                    tempDataSent.remove(clickedItem);
                }

                if (t.getId().contains("tickets")) {
                    tempTickets.remove(clickedItem);
                    try {
                        ticket = new TicketEntity(clickedItem.getFile());
                    } catch (IOException ex) {
                        Logger.getLogger(T1Controller.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                if (t.getId().contains("dosijei")) {
                    tempDosijeData.remove(clickedItem);
                }
            }
        });

        menu.getItems().add(item);
        menu.show(tabPane.getScene().getWindow(), evt.getScreenX(), evt.getScreenY());
    }

    private void onAnyTableDoubleClick(TableView t) {
        String text = getMessageText(t);

        if (text.equals("")) {
            return;
        }
        Stage stage = new Stage();

        FXMLLoader loader = new FXMLLoader(T1Controller.class.getResource("/FXMLFiles/MtTextDisplay.fxml"));
        Scene newScene = null;
        try {
            newScene = new Scene(loader.load());
        } catch (IOException ex) {
            Logger.getLogger(T1Controller.class.getName()).log(Level.SEVERE, null, ex);
        }

        MtTextDisplayController c = loader.getController();
        c.setText(text);
        stage.setScene(newScene);
        stage.show();
    }

    private String getMessageText(TableView t) {
        String text = "";
        T1Item clickedItem = (T1Item) t.getSelectionModel().getSelectedItem();
        if (clickedItem == null) {
            return text;
        }
        try {
            text = new FileUtils().readPdf(clickedItem.getFile());
        } catch (IOException ex) {
            Logger.getLogger(T1Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
        return text;
    }

    @FXML
    void onCreateClick() {
        if (dosije.getSize() <= 0) {
            AlertUtils.getSimpleAlert(Alert.AlertType.ERROR, "Action aborted!", "Dosije is empty.", "You did not add any file to dosije.").show();
            return;
        }
        
        notArchived = "";
        mergedName = "";
        mergeUtil = new PDFMergerUtility();
        scene = tabPane.getScene();
        scene.setCursor(Cursor.WAIT);
        File destinFile = null;

        mergedName = sugMergeLocTF.getText().trim() + File.separator + sugestedFileNameCombo.getSelectionModel().getSelectedItem().toString().trim();
        dosijeFolder = new File(sugMergeLocTF.getText().trim());

        Optional<ButtonType> result = AlertUtils.getSimpleAlert(Alert.AlertType.CONFIRMATION, "PDF Merger", "PDF merger", "Merge listed files?\n"
                + getList(resultData)).showAndWait();

        if (result.get() == ButtonType.OK) {
            dosijeFolder.mkdirs();
            if (!dosijeFolder.exists()) {
                AlertUtils.getSimpleAlert(Alert.AlertType.ERROR, "Error!", "Folder creation failed", "Can not create DOSIJE folder: " + dosijeFolder
                        + "\nYou may not have access to folder: " + dosijeFolder);
                return;
            }

            destinFile = new File(mergedName);
            mergeUtil.setDestinationFileName(mergedName);

            for (T1Item item : resultData) {
                try {
                    mergeUtil.addSource(item.getFile());
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(PDFMergerController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            try {
                mergeUtil.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
                if (destinFile.exists()) {
                    archiveFiles(resultData);
                } else {
                    AlertUtils.getSimpleAlert(Alert.AlertType.ERROR, "Merge not completed", "Action aborted", "Selected files not archived. Try again.").show();
                    resetDosijeLocAndName();
                    return;
                }

                Alert a = AlertUtils.getSimpleAlert(Alert.AlertType.INFORMATION, "Merge completed", "Merge completed", "Merge file saved on " + destinFile.getPath()
                        + "\n\n"
                        + "Archived files: \n" + getList(resultData) + "\n"
                        + (notArchived.isEmpty() ? "" : ("Please review. There are not archived files:\n" + notArchived)));
                a.getButtonTypes().add(0, new ButtonType("Open file"));
                Optional<ButtonType> o = a.showAndWait();

                if (o.get().equals(a.getButtonTypes().get(0))) {
                    HostServ.getHostServices().showDocument(destinFile.getPath());
                }

            } catch (IOException ex) {
                AlertUtils.getSimpleAlert(Alert.AlertType.WARNING, "Merge not completed", "Action aborted", ex.getMessage()).show();
                Logger.getLogger(PDFMergerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            onTabSelectionChange(null);
        }

        resultData.clear();
        scene.setCursor(Cursor.DEFAULT);
        resetDosijeLocAndName();
    }

    //file name creation for merged pdf document
    private String tryToCreateName(String name) {
        String[] parts = name.split("_");
        if (parts != null && parts.length > 2) {
            mergedName = "Merged_" + parts[0] + "_" + parts[1] + ".pdf";
        } else {
            mergedName = "Merged_no_name_" + System.currentTimeMillis() + ".pdf";
        }
        return mergedName;
    }

    private void archiveFiles(ObservableList<T1Item> finalData) {
        MTEntity mt = null;
        archiveFolder = null;
        int result;
        for (T1Item t1Item : finalData) {
            archiveFolder = getArchiveFolder(t1Item);

            if (t1Item.getType().equals("mt")) {
                try {
                    mt = fileUtils.readMt(t1Item.getFile());

                    result = fileUtils.archiveMt(archiveFolder.getPath(), t1Item.getFile(), mt.getDateReceived().substring(0, 8));
                    if (result != 1) {
                        notArchived += t1Item.getFile() + "\n";
                    }
                } catch (IOException ex) {
                    Logger.getLogger(T1Controller.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (t1Item.getType().equals("ticket")) {
                ticketDate = ticket.getFormatedDate();
                if (ticketDate == null) {
                 AlertUtils.getSimpleAlert(Alert.AlertType.ERROR, "Error!", "Ticket date = null", "Ticket date can not be null!").show();
                 return;
                }
                
                fileUtils.archiveMt(archiveFolder.getPath(), t1Item.getFile(), ticketDate);
            }
        }
    }

    private File getArchiveFolder(T1Item t1Item) {
        archiveFolder = null;
        File archiveRoot = new File(preferences.getRoot() + File.separator + "MT_arhiva/");
        if (!archiveRoot.exists()) {
            archiveRoot.mkdir();
        }
        File archiveRootYear = new File(archiveRoot + File.separator + Calendar.getInstance().get(Calendar.YEAR));
        if (!archiveRootYear.exists()) {
            archiveRootYear.mkdir();
        }
        archiveFolder = new File(archiveRootYear + File.separator + t1Item.getResFolderName() + "_archive");
        if (!archiveFolder.exists()) {
            archiveFolder.mkdir();
        }
        return archiveFolder;
    }

    private String getList(ObservableList<T1Item> finalData) {
        String items = new String();
        for (T1Item t1Item : finalData) {
            items += t1Item.getFile().getPath() + "\n";
        }
        return items;
    }

    private void setHashMapOfTables() {
        tables = new HashMap<>();

        tables.put("MT200O", MT200OTable);
        tables.put("MT200I", MT200ITable);

        tables.put("MT202O", MT202OTable);
        tables.put("MT202I", MT202ITable);

        tables.put("MT300O", MT300OTable);
        tables.put("MT300I", MT300ITable);

        tables.put("MT320O", MT320OTable);
        tables.put("MT320I", MT320ITable);

        tables.put("tickets", ticketsTable);
        tables.put("dosijei", dosijeiTable);
    }

    private void bindInfo() {
//        sugMergeNameTF.textProperty().bindBidirectional(sugestedMergeName);
        sugMergeLocTF.textProperty().bindBidirectional(sugestedDosijeLocation);
//        sugestedFileNameCombo.
    }

    private boolean itemIsInResult(T1Item item) {
        if (resultData == null) {
            return false;
        }
        resultData = resultTable.getItems();
        for (T1Item t : resultData) {
            if (t.getFile().equals(item.getFile()) && t.getFileName().equals(item.getFileName())) {
                return true;
            }
        }
        return false;
    }

    private void resetDosijeLocAndName() {
        comboList.clear();
        sugMergeLocTF.clear();
        sugestedDosijeLocation.set(null);
        dosije.clear();

    }

    /**
     * Creates T1 model
     */
    public class T1Item {

        SimpleStringProperty fileName;
        File file;
        String resFolderName;
        String type; // mt or ticket

        /**
         *
         * @param fileName
         * @param file
         * @param resFolderName - like MT202I, MT202O...
         * @param type - mt or ticket
         */
        public T1Item(String fileName, File file, String resFolderName, String type) {
            this.fileName = new SimpleStringProperty(fileName);
            this.file = file;
            this.resFolderName = resFolderName;
            this.type = type;
        }

        public T1Item() {
            this("", null, "", "");
        }

        public String getFileName() {
            return fileName.get();
        }

        public void setFileName(String fileName) {
            this.fileName.set(fileName);
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public String getResFolderName() {
            return resFolderName;
        }

        public void setResFolderName(String resFolderName) {
            this.resFolderName = resFolderName;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {

            System.out.println("File name: " + getFileName());
            System.out.println("File: " + getFile());
            System.out.println("Res folder name: " + getResFolderName());
            System.out.println("Type: " + getType());

            return super.toString();
        }

    }

    private void addDatePickerValueChangeListener() {
        datePicker.valueProperty().addListener((ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) -> {
            fillTable(-2, tempDosijeData);
        });
    }

}
