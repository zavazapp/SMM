package controlers;

import Tools.HostServ;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import models.MTMessages.MTEntity;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import preferences.TBOPreferences;
import utils.AlertUtils;
import utils.DateUtils;
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
    private ObservableList<T1Item> dosijeData;
    private String[] availableFolders;
    private HashMap<String, TableView> tables;
    private Scene scene;
    private PDFMergerUtility mergeUtil;
    private String mergedName;
    private String notArchived;
    private String ticketDate;
    private SimpleStringProperty sugestedMergeName = new SimpleStringProperty();
    private SimpleStringProperty sugestedDosijeLocation = new SimpleStringProperty();

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
    private Button createButton;

    @FXML
    private TabPane tabPane;

    @FXML
    private SplitPane rootPane;

    @FXML
    private TextField sugMergeNameTF;

    @FXML
    private TextField sugMergeLocTF;
    //</editor-fold>

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dosijeData = resultTable.getItems();
        setHashMapOfTables();
        availableFolders = preferences.getFOLDERS();
        bindInfo();
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

        } else {
            tempDataReceived = tables.get(id + "O").getItems();
            tempDataSent = tables.get(id + "I").getItems();
        }

        int spec = 0;
        switch (id) {
            case "tickets":
                spec = 16;
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

        if (spec < 8) {
            fillTable(spec, tempDataReceived);
            fillTable(spec + 1, tempDataSent);
        } else {
            fillTable(spec, tempTickets);
        }
    }

    //adds items to observable lists and table data updates
    private void fillTable(int spec, ObservableList<T1Item> list) {
        if (list != null) {
            list.clear();
            File[] folderItems = preferences.getSpecificForder(spec).toFile().listFiles();

            for (File folderItem : folderItems) {
                T1Item item = new T1Item(folderItem.getName(), folderItem, preferences.getSpecificForder(spec).toFile().getName(), spec == 16 ? "ticket" : "mt");
                if (!itemIsInResult(item)) {
                    list.add(item);
                }
            }
        }
    }

    //on row double click, add item to dosije list
    @FXML
    private void onTempTableRawClicked(MouseEvent evt) {
        TableView t = (TableView) evt.getSource();
        if (evt.getClickCount() == 2) {
            T1Item clickedItem = (T1Item) t.getSelectionModel().getSelectedItem();

            //set default unique file names for dosije file
            if (sugestedMergeName.get() == null) {
                sugestedMergeName.set("Dosije_" + System.currentTimeMillis() + ".pdf");
            }

            if (sugestedDosijeLocation.get() == null) {
                try {
                    sugestedDosijeLocation.set(getDosijeArcFolder(clickedItem).getPath());
                } catch (IOException ex) {
                    Logger.getLogger(T1Controller.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            //update lower table which keeps files to be merged and archived
            dosijeData.add(clickedItem);

            if (t.getId().contains("O")) {
                tempDataReceived.remove(clickedItem);
            }
            if (tempDataSent != null && t.getId().contains("I")) {
                tempDataSent.remove(clickedItem);
            }

            if (t.getId().contains("tickets")) {
                tempTickets.remove(clickedItem);
                ticketDate = getTicketDate(clickedItem);
            }

            if (clickedItem.getResFolderName().contains("MT202I")) {
                try {
                    sugestedMergeName.set(clickedItem.getFileName().split("_")[2] + ".pdf");
                } catch (ArrayIndexOutOfBoundsException exc) {
                    AlertUtils.getSimpleAlert(Alert.AlertType.INFORMATION, "Action aborted", "Can not determin name.", "Selected file is not renamed").show();
                }
            }
        }
    }

    private File getDosijeArcFolder(T1Item clickedItem) throws IOException {
        String year = null;
        String month = null;
        String date = null;

        File archiveFolder = new File(preferences.getDosijeiFolder().toString()); //root folder for archiving Dosije
        if (!archiveFolder.exists()) { //must be set in settings
            AlertUtils.getSimpleAlert(Alert.AlertType.INFORMATION, "Destination folder not found", "Action aborted", "Destination folder does not exist: " + archiveFolder.getPath()).show();
            return null;
        }

        year = DateUtils.getYearFromTicket(getTicketDate(clickedItem));
        if (year == null || year.equals("") || year.equals("NA")) {
            year = DateUtils.getYearFromMt(fileUtils.readMt(clickedItem.getFile()).getValueDate());
        }
        if (year == null || year.equals("") || year.equals("NA")) {
            year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
            AlertUtils.getSimpleAlert(Alert.AlertType.ERROR, "Can not create year", "Action aborted", "File name is not correct: "
                    + clickedItem.getFileName()
                    + "\n" + "Year set to: " + year).show();
        }
        archiveFolder = new File(archiveFolder.getPath() + File.separator + year);
        if (!archiveFolder.exists()) {
            archiveFolder.mkdir();
        }

        month = DateUtils.getMonthFromTicket(getTicketDate(clickedItem));
        if (month == null || month.equals("") || month.equals("NA")) {
            month = DateUtils.getMonthFromMt(fileUtils.readMt(clickedItem.getFile()).getValueDate());
        }
        if (month == null || month.equals("") || month.equals("NA")) {
            month = String.valueOf(Calendar.getInstance().get(Calendar.MONTH + 1));
            AlertUtils.getSimpleAlert(Alert.AlertType.ERROR, "Can not create month", "Action aborted", "File name is not correct: "
                    + clickedItem.getFileName()
                    + "\n" + "Month set to: " + month).show();
        }

        archiveFolder = new File(archiveFolder.getPath() + File.separator + month);
        if (!archiveFolder.exists()) {
            archiveFolder.mkdir();
        }

        date = getTicketDate(clickedItem);
        if (date == null || date.equals("") || date.equals("NA")) {
            date = fileUtils.readMt(clickedItem.getFile()).getValueDate();
        }
        if (date == null || date.equals("") || date.equals("NA")) {
            date = DateUtils.getFormatedDate(System.currentTimeMillis());
            AlertUtils.getSimpleAlert(Alert.AlertType.ERROR, "Can not create date", "Action aborted", "File name is not correct: "
                    + clickedItem.getFileName()
                    + "\n" + "Date set to: " + date).show();
        }

        archiveFolder = new File(archiveFolder.getPath() + File.separator + date);
        if (!archiveFolder.exists()) {
            archiveFolder.mkdir();
        }
        return archiveFolder;
    }


    @FXML
    private void onResultTableRawClicked(MouseEvent evt) {
        if (evt.getClickCount() == 2) {
            T1Item clickedItem = (T1Item) resultTable.getSelectionModel().getSelectedItem();
            dosijeData.remove(clickedItem);
            onTabSelectionChange(null);
        }
    }

    @FXML
    void onCreateClick() {
        notArchived = "";
        mergeUtil = new PDFMergerUtility();
        scene = tabPane.getScene();
        scene.setCursor(Cursor.WAIT);
        File destinFile = null;

        mergedName = sugMergeLocTF.getText() + File.separator + sugMergeNameTF.getText();

        Optional<ButtonType> result = AlertUtils.getSimpleAlert(Alert.AlertType.CONFIRMATION, "PDF Merger", "PDF merger", "Merge listed files?\n"
                + getList(dosijeData)).showAndWait();

        if (result.get() == ButtonType.OK) {
            destinFile = new File(mergedName);
            mergeUtil.setDestinationFileName(mergedName);

            for (T1Item item : dosijeData) {
                try {
                    mergeUtil.addSource(item.getFile());
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(PDFMergerController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            try {
                mergeUtil.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
                if (destinFile.exists()) {
                    archiveFiles(dosijeData);
                }else{
                    AlertUtils.getSimpleAlert(Alert.AlertType.ERROR, "Merge not completed", "Action aborted", "Selected files not archived. Try again.").show();
                    resetDosijeLocAndName();
                    return;
                }
                
                Alert a = AlertUtils.getSimpleAlert(Alert.AlertType.INFORMATION, "Merge completed", "Merge completed", "Merge file saved on " + destinFile.getPath()
                        +  "\n\n"
                        + "Archived files: \n" + getList(dosijeData) + "\n"
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

        dosijeData.clear();
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

//    private File getResultFile() {
//        File rf = null;
//
//        rf = new File(preferences.getDosijeiFolder().toString());
//        if (!rf.exists()) {
//            AlertUtils.getSimpleAlert(Alert.AlertType.INFORMATION, "Destination folder not found", "Action aborted", "Destination folder does not exist: " + rf.getPath());
//            return null;
//        }
//
//        rf = new File(rf.getPath() + File.separator + Calendar.getInstance().get(Calendar.YEAR));
//        if (!rf.exists()) {
//            rf.mkdir();
//        }
//
//        return rf;
//    }

    private void archiveFiles(ObservableList<T1Item> finalData) {
        MTEntity mt = null;
        File archiveFolder = null;
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
                ticketDate = getTicketDate(t1Item);
                fileUtils.archiveMt(archiveFolder.getPath(), t1Item.getFile(), ticketDate);
            }
        }
    }


    private static String getTicketDate(T1Item t1Item) {
        String ticketDate = "NA";
        if (t1Item.getType().equals("ticket") && t1Item.getFileName().split("_").length > 2 && t1Item.getFileName().split("_")[3].length() > 10) {
            System.out.println(t1Item.getFileName());
            System.out.println(t1Item.getFileName().split("_")[3].substring(0, 10));
            ticketDate = DateUtils.getFormatedDate(t1Item.getFileName().split("_")[3].substring(0, 10));
        }
        System.out.println(ticketDate);

        return ticketDate;
    }

    private File getArchiveFolder(T1Item t1Item) {
        File archiveFolder;
        File archiveRoot = new File(t1Item.getFile().getParentFile().getParent() + File.separator + "MT_arhiva/");
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
    }

    private void bindInfo() {
        sugMergeNameTF.textProperty().bind(sugestedMergeName);
        sugMergeLocTF.textProperty().bind(sugestedDosijeLocation);
    }

    private boolean itemIsInResult(T1Item item) {
        if (dosijeData == null) {
            return false;
        }
        dosijeData = resultTable.getItems();
        for (T1Item t : dosijeData) {
            if (t.getFile().equals(item.getFile()) && t.getFileName().equals(item.getFileName())) {
                return true;
            }
        }
        return false;
    }

    private void resetDosijeLocAndName() {
        sugestedDosijeLocation.set(null);
        sugestedMergeName.set(null);
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

    }

}
