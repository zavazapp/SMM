package models.DisplayDataModels;

import java.time.LocalDate;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import models.MTMessages.MTEntity;

/**
 *
 * @author Korisnik
 */
public class TBODisplayDataModel extends DisplayDataModel {

    public TBODisplayDataModel(boolean live, int totalCount, LocalDate date, ObservableList<MTEntity> data, Button renameButton, Button archiveButton, Button sendButton) {
        super(live, totalCount, date, data, renameButton, archiveButton, sendButton);
    }

}
