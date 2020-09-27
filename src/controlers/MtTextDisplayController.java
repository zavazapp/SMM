package controlers;

import java.awt.print.PrinterJob;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import models.MTMessages.MTEntity;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import preferences.PPIPreferences;

/**
 * FXML Controller class
 *
 * @author Miodrag Spasic
 */
public class MtTextDisplayController implements Initializable {

    private final ObservableList<Double> comboItems = FXCollections.observableArrayList(8d,10d,12d,14d,16d,18d,20d);
    private final PPIPreferences preferences = PPIPreferences.getInstance(getClass());
    
    private MTEntity mt;
    @FXML
    private TextArea textArea;
    @FXML
    private ComboBox<Double> fontComboBox;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        textArea.setFont(Font.font(preferences.getFont()));
        fontComboBox.setValue(textArea.getFont().getSize());
        fontComboBox.setItems(comboItems);
        
        
    }

    public void setText(MTEntity mt) {
        this.mt = mt;
        textArea.setText(mt.getText());
    }

    @FXML
    private void onMouseClicked(MouseEvent evt) {
        String buttonName = ((Button)evt.getSource()).getId();
        System.out.println(buttonName);
        switch (buttonName) {
            case "close":
                Stage s = (Stage) textArea.getScene().getWindow();
                s.close();
                break;
            case "print":
                try (PDDocument doc = PDDocument.load(mt.getFile())) {
                    PrinterJob printJob = PrinterJob.getPrinterJob();
                    printJob.setPageable(new PDFPageable(doc));
                    printJob.setJobName("MT message from SMM");
                    if (printJob.printDialog()) {
                        printJob.print();
                    }
                    doc.close();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            break;
        }
    }
    
    @FXML
    private void onFontChanges(ActionEvent evt){
        textArea.setFont(Font.font(fontComboBox.getValue()));
        preferences.setFont(fontComboBox.getValue());
    }
}
