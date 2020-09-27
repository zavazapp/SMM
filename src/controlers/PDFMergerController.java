package controlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import preferences.MasterPreferences;
import preferences.PPIPreferences;
import utils.AlertUtils;

/**
 * FXML Controller class
 *
 * @author Miodrag Spasic
 */
public class PDFMergerController extends javafx.application.Application implements Initializable {

    @FXML
    private ListView<File> list;
    @FXML
    private AnchorPane scene;

    private ObservableList<File> data = FXCollections.observableArrayList();
    MasterPreferences preferences = new PPIPreferences(getClass());

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        list.setItems(data);
    }

    @FXML
    private void addFileClicked() {
        FileChooser filechooser = new FileChooser();
        filechooser.setTitle("Choose files to merge");
        filechooser.setInitialDirectory(preferences.getLastOppenedDir());

        List<File> filesToAdd = filechooser.showOpenMultipleDialog(list.getScene().getWindow());
        if (filesToAdd != null && !filesToAdd.isEmpty()) {
            data.addAll(filesToAdd);
        }
    }

    @FXML
    private void removeFileClicked() {
        if (!data.isEmpty()) {
            data.removeAll(list.getSelectionModel().getSelectedItems());
        }
    }

    @FXML
    private void onMegreClicked() {
        scene.setCursor(Cursor.WAIT);
        File mergedFile = null;

        String mergedName = tryToCreateName(data.get(0).getName());

        PDFMergerUtility mergeUtil = new PDFMergerUtility();

        Optional<ButtonType> result = AlertUtils.getSimpleAlert(Alert.AlertType.CONFIRMATION, "PDF Merger", "PDF merger", "Merge listed files?").showAndWait();

        if (result.get() == ButtonType.OK) {
            mergeUtil.setDestinationFileName(System.getProperty("user.home") + "/Desktop/" + mergedName);
            mergedFile = new File(System.getProperty("user.home") + "/Desktop/" + mergedName);

            for (File selectedFile : data) {
                try {
                    mergeUtil.addSource(selectedFile);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(PDFMergerController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            try {
                mergeUtil.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
                Alert a = AlertUtils.getSimpleAlert(Alert.AlertType.CONFIRMATION, "Merge completed", "Merge completed", "Merge file saved on Desktop:\n" + mergedName);
                a.getButtonTypes().setAll( new ButtonType("Open file"));

                Optional<ButtonType> o = a.showAndWait();
                if (o.get().equals(a.getButtonTypes().get(0))) {
                    getHostServices().showDocument(mergedFile.toString());
                }

            } catch (IOException ex) {
                AlertUtils.getSimpleAlert(Alert.AlertType.WARNING, "Merge not completed", "Action aborted", ex.getMessage()).show();
                Logger.getLogger(PDFMergerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        onCancelClicked();
        scene.setCursor(Cursor.DEFAULT);
    }

    @FXML
    private void onUpClicked() {
        int index = list.getSelectionModel().getSelectedIndex();
        if (index != 0) {
            Collections.swap(data, index, index - 1);
            list.getSelectionModel().clearSelection();
            list.getSelectionModel().select(index - 1);
        }
    }

    @FXML
    private void onDownClicked() {
        int index = list.getSelectionModel().getSelectedIndex();
        if (index != data.size()) {
            Collections.swap(data, index, index + 1);
            list.getSelectionModel().clearSelection();
            list.getSelectionModel().select(index + 1);
        }

    }

    @FXML
    private void onCancelClicked() {
        ((Stage) scene.getScene().getWindow()).close();
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


    @Override
    public void start(Stage primaryStage) throws Exception {
        
    }
    
    
}
