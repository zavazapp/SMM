package models.DisplayDataModels;

import java.time.LocalDate;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import models.MTMessages.MTEntity;

/**
 *
 * @author Miodrag Spasic
 */
public class PPIDisplayDataModel extends DisplayDataModel {

    private final Button sendToPensButton;

    public PPIDisplayDataModel(boolean live, int totalCount, int ticketCount, LocalDate date, ObservableList<MTEntity> data, Button renameButton, Button archiveButton, Button sendButton, Button sendToPensButton) {
        super(live, totalCount, ticketCount, date, data, renameButton, archiveButton, sendButton);
        this.sendToPensButton = sendToPensButton;
    }
}
