package models;

import Tools.TicketParser;
import controlers.MasterControler;
import controlers.MtTextDisplayController;
import controlers.SettingsController;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;
import preferences.TBOPreferences;
import utils.AlertUtils;
import utils.DateUtils;
import utils.FileUtils;

/**
 *
 * @author Miodrag Spasic
 */
public class TicketTable {

    private final FileUtils fileUtils = new FileUtils();
    private final preferences.MasterPreferences preferences;
    TableView<TicketEntity2> tableView;
    static ObservableList<TicketEntity2> data;
    File[] files;
    String text;
    HashMap<String, String> ticketFields;
    private TicketEntity2 clickedRow;

    public TicketTable() {
        this.preferences = TBOPreferences.getInstance(SettingsController.class);
    }

    public TableView<TicketEntity2> getTableView() throws IOException {
        tableView = FXMLLoader.load(MasterControler.class.getResource("/FXMLFiles/TicketsTable.fxml"));
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setRawClickListener();

        data = tableView.getItems();
        return tableView;
    }

    public void setData(LocalDate date) throws IOException {
        TicketEntity2 ticket;
        if (date.equals(LocalDate.now())) {
            files = preferences.getTicketsFolder().toFile().listFiles();
        } else {
            String ta = preferences.getRoot() + File.separator + "MT_arhiva" + File.separator + date.getYear() + File.separator + "Tiketi_archive"
                    + File.separator + DateUtils.getFormatedDate(date);
            files = new File(ta).listFiles();
        }

        if (data != null) {
            data.clear();
            for (File file : files) {
                ticket = getTicketEntity(file);
                if (ticket != null) {
                    data.add(ticket);
                }
            }
        }

    }

    private TicketEntity2 getTicketEntity(File file) throws IOException {
        text = fileUtils.readPdf(file);
        ticketFields = TicketParser.scan(text);

        return new TicketEntity2(file, text, ticketFields.get("ticketNumber"),
                ticketFields.get("dealType"),
                ticketFields.get("counterParty"),
                ticketFields.get("dealDetails"),
                ticketFields.get("amount1"),
                ticketFields.get("amount2"),
                ticketFields.get("date1"),
                ticketFields.get("date2"));
    }

    private void setRawClickListener() {
        tableView.setRowFactory(new Callback<TableView<TicketEntity2>, TableRow<TicketEntity2>>() {
            @Override
            public TableRow<TicketEntity2> call(TableView<TicketEntity2> param) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        tableView.setRowFactory(new Callback<TableView<TicketEntity2>, TableRow<TicketEntity2>>() {
            @Override
            public TableRow<TicketEntity2> call(TableView<TicketEntity2> tv) {
                TableRow<TicketEntity2> row = new TableRow<>();
                row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY
                                && event.getClickCount() == 2) {

                            clickedRow = row.getItem();
                            FXMLLoader loader = new FXMLLoader(TicketTable.class.getResource("/FXMLFiles/MtTextDisplay.fxml"));
                            Stage stage = new Stage();
                            try {
                                Scene newScene = new Scene(loader.load());
                                MtTextDisplayController c = loader.getController();
                                c.setText(clickedRow.getText());

                                stage.setScene(newScene);
                                stage.show();
                                stage.centerOnScreen();

                            } catch (IOException ex) {
                                Logger.getLogger(TicketTable.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                });
                return row;
            }
        });
    }

    public void onArchiveClick(Stage stage) {
        ObservableList<Integer> selectedIndices = tableView.getSelectionModel().getSelectedIndices();
        if (!checkSelection(stage, selectedIndices)) {
            return;
        }
        startArchiving(stage, selectedIndices);
    }

    private boolean checkSelection(Stage stage, ObservableList<Integer> selectedIndices) {
        //check selection
        if (selectedIndices.isEmpty()) {
            AlertUtils.getSimpleAlert(stage, Alert.AlertType.INFORMATION, "No sellected message", "Action aborted!", "You must sellect at least one message").show();
            return false;
        }
        return true;
    }

    private String getArchiveFolder(TicketEntity2 ticket) {

        File archiveFolder; //destination folder for archiving messages.

        //create archive folders
        //TODO - check earlier if folders are set and mark as set to avoid unnecesary code execution
        archiveFolder = new File(preferences.getRoot() + File.separator + "MT_arhiva/");

        archiveFolder = new File(archiveFolder + File.separator + DateUtils.getYearFromTicket(DateUtils.getFormatedDate(ticket.getDate2().concat("ticket")))
                + File.separator + "Tiketi_archive/");

        if (!archiveFolder.exists()) {
            archiveFolder.mkdirs();
        }

        return archiveFolder.getPath();
    }

    private void startArchiving(Stage stage, ObservableList<Integer> selectedIndices) {
        tableView.getScene().setCursor(Cursor.WAIT);

        int counter = 0; //count messages that are succesfuly renamed
        int control = 0;
        ArrayList indecisToRemove = new ArrayList();

        for (Integer i : selectedIndices) {
            control = fileUtils.archiveMt(getArchiveFolder(data.get(i)), data.get(i).getFile(), DateUtils.getFormatedDate(data.get(i).getDate2().concat("ticket")));
            counter += control;
            if (control == 1) {
                indecisToRemove.add(i);
            }
        }

        //refresh data
        for (int i = indecisToRemove.size() - 1; i > -1; i--) {
            data.remove((int) indecisToRemove.get(i));
        }

        AlertUtils.getSimpleAlert(stage, Alert.AlertType.INFORMATION, "", "Action completed!", counter + " files archived.").show();
        tableView.getScene().setCursor(Cursor.DEFAULT);
    }
}
