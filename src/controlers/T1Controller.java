package controlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import preferences.TBOPreferences;
import utils.AlertUtils;

/**
 * FXML Controller class
 *
 * @author Miodrag Spasic
 */
public class T1Controller implements Initializable {

    private ObservableList<T1Item> tempTickets;
    private final preferences.MasterPreferences preferences = TBOPreferences.getInstance(getClass());
    private ObservableList<T1Item> tempDataReceived;
    private ObservableList<T1Item> tempDataSent;
    private ObservableList<T1Item> finalData;
    private String[] availableFolders;
    private HashMap<String, TableView> tables;
    private Scene scene;

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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        finalData = resultTable.getItems();
        setHashMapOfTables();
        availableFolders = preferences.getFOLDERS();
        
//        tempTickets = ticketsTable.getItems();
//        changeTempData("tickets");
//        fillTable(availableFolders.length - 1, tempTickets);
    }

    private void fillTable(int spec, ObservableList<T1Item> list) {
        if (list != null) {
            list.clear();
            File[] folderItems = preferences.getSpecificForder(spec).toFile().listFiles();

            for (File folderItem : folderItems) {
                T1Item item = new T1Item(folderItem.getName(), folderItem);
                if (!itemIsInResult(item)) {
                    list.add(item);
                }
            }
        }
    }

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
            changeTempData(clickedTab.getId());
        }

    }

    @FXML
    void onCreateClick() {
        scene = tabPane.getScene();
        scene.setCursor(Cursor.WAIT);
        File mergedFile = null;

        String mergedName = tryToCreateName(finalData.get(0).fileName.get());

        PDFMergerUtility mergeUtil = new PDFMergerUtility();

        Optional<ButtonType> result = AlertUtils.getSimpleAlert(Alert.AlertType.CONFIRMATION, "PDF Merger", "PDF merger", "Merge listed files?").showAndWait();

        if (result.get() == ButtonType.OK) {
            mergeUtil.setDestinationFileName(System.getProperty("user.home") + "/Desktop/" + mergedName);
            mergedFile = new File(System.getProperty("user.home") + "/Desktop/" + mergedName);
            
            for (T1Item tempTicket : tempTickets) {
                try {
                    mergeUtil.addSource(tempTicket.getFile());
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(PDFMergerController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            try {
                mergeUtil.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
                Alert a = AlertUtils.getSimpleAlert(Alert.AlertType.INFORMATION, "Merge completed", "Merge completed", "Merge file saved on Desktop:\n" + mergedName);

                Optional<ButtonType> o = a.showAndWait();

            } catch (IOException ex) {
                AlertUtils.getSimpleAlert(Alert.AlertType.WARNING, "Merge not completed", "Action aborted", ex.getMessage()).show();
                Logger.getLogger(PDFMergerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        scene.setCursor(Cursor.DEFAULT);
    }
    
     //file name creation for merged pdf document
    private String tryToCreateName(String name) {
        String mergedName;
        String[] parts = name.split("_");
        if (parts != null && parts.length > 2) {
            mergedName = "Merged_" + parts[0] + "_" + parts[2] + ".pdf";
        } else {
            mergedName = "Merged_no_name_" + System.currentTimeMillis() + ".pdf";
        }

        return mergedName;
    }

    private void changeTempData(String id) {
        System.out.println(id);
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
//                for (T1Item t1Item : tempTickets) {
//                    if (finalData != null && finalData.contains(t1Item)) {
//                        tempTickets.remove(t1Item);
//                    }
//                }
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

    @FXML
    private void onTempTableRawClicked(MouseEvent evt) {
        TableView t = (TableView) evt.getSource();
        if (evt.getClickCount() == 2) {
            int index = t.getSelectionModel().getSelectedIndex();
            T1Item clickedItem = (T1Item) t.getSelectionModel().getSelectedItem();

            finalData.add(clickedItem);

            if (t.getId().contains("O")) {
                tempDataReceived.remove(clickedItem);
            }
            if (tempDataSent != null && t.getId().contains("I")) {
                tempDataSent.remove(clickedItem);
            }
            if (t.getId().contains("tickets")) {
                tempTickets.remove(clickedItem);
            }
        }
    }

    @FXML
    private void onResultTableRawClicked(MouseEvent evt) {
        if (evt.getClickCount() == 2) {
            T1Item clickedItem = (T1Item) resultTable.getSelectionModel().getSelectedItem();
            finalData.remove(clickedItem);
            onTabSelectionChange(null);
        }
    }

    private boolean itemIsInResult(T1Item item) {
        if (finalData == null) {
            return false;
        }
        finalData = resultTable.getItems();
        for (T1Item t : finalData) {
            if (t.getFile().equals(item.getFile()) && t.getFileName().equals(item.getFileName())) {
                return true;
            }
        }
        return false;
    }

    public class T1Item {

        SimpleStringProperty fileName;
        File file;

        public T1Item(String fileName, File file) {
            this.fileName = new SimpleStringProperty(fileName);
            this.file = file;
        }

        public T1Item() {
            this("", null);
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

    }
}
