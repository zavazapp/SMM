package models.DisplayDataModels;

import java.time.LocalDate;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import models.MTMessages.MTEntity;

/**
 *
 * @author Miodrag Spasic
 */
public class DisplayDataModel {

    public boolean live; //is data from live folder or from archive
    public int totalCount; //number of messages in choosen folder
    public int ticketCount; //number of messages in choosen folder
    public LocalDate date; //choosen date
    public ObservableList<MTEntity> data;
    public Label[] navLabels;

    public Button renameButton;
    public Button archiveButton;
    public Button sendButton;

    public DisplayDataModel(boolean live, int totalCount, int ticketCount, LocalDate date, ObservableList<MTEntity> data, Button renameButton, Button archiveButton, Button sendButton) {
        this.live = live;
        this.totalCount = totalCount;
        this.ticketCount = ticketCount;
        this.date = date;
        this.data = data;
        this.renameButton = renameButton;
        this.archiveButton = archiveButton;
        this.sendButton = sendButton;
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public ObservableList<MTEntity> getData() {
        return data;
    }

    public void setData(ObservableList<MTEntity> data) {
        this.data = data;
    }

    public Button getRenameButton() {
        return renameButton;
    }

    public void setRenameButton(Button renameButton) {
        this.renameButton = renameButton;
    }

    public Button getArchiveButton() {
        return archiveButton;
    }

    public void setArchiveButton(Button archiveButton) {
        this.archiveButton = archiveButton;
    }

    public Button getSendButton() {
        return sendButton;
    }

    public void setSendButton(Button sendButton) {
        this.sendButton = sendButton;
    }

    public Label[] getNavLabels() {
        return navLabels;
    }

    public void setNavLabels(Label[] navLabels) {
        this.navLabels = navLabels;
    }

    public int getTicketCount() {
        return ticketCount;
    }

    public void setTicketCount(int ticketCount) {
        this.ticketCount = ticketCount;
    }

}
