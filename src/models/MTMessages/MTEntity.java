package models.MTMessages;

import java.io.File;
import java.util.Vector;
import javafx.beans.property.SimpleStringProperty;

public class MTEntity {

    private final SimpleStringProperty type;
    private final SimpleStringProperty sender;
    private final SimpleStringProperty bene;
    private final SimpleStringProperty field20;
    private final SimpleStringProperty field21;
    private final SimpleStringProperty dateReceived;
    private final SimpleStringProperty valueDate;
    private final SimpleStringProperty currency;
    private final SimpleStringProperty amount;
    private final SimpleStringProperty text;
    private File file;
    private boolean renamed;
    private boolean selected;

    /**
     *
     * @param type - type of received message (FIN 202, FIN 299, etc.)
     * @param sender - bank that sends the messages
     * @param bene - beneficiary of funds (some msgs does not have bene, so if
     * it does not exists, value is set to "NA")
     * @param field20 - senders reference
     * @param field21 - related reference, usually recognizable by receiver
     * @param dateReceived - date and time string when msg was received
     * @param valueDate - used in financial messages to identify when funds will
     * be available
     * @param currency
     * @param amount
     * @param text - complete text of swift message
     * @param file - pdf document file corresponding to MTEntity object
     * @param isRenamed - identifies if file is renamed by the user
     * @param selected - used by rename and send in tables to form array of
     * messages
     */
    public MTEntity(
            String type,
            String sender,
            String bene,
            String field20,
            String field21,
            String dateReceived,
            String valueDate,
            String currency,
            String amount,
            String text,
            File file,
            boolean isRenamed,
            boolean selected) {

        this.type = new SimpleStringProperty(type);
        this.sender = new SimpleStringProperty(sender);
        this.bene = new SimpleStringProperty(bene);
        this.field20 = new SimpleStringProperty(field20);
        this.field21 = new SimpleStringProperty(field21);
        this.dateReceived = new SimpleStringProperty(dateReceived);
        this.valueDate = new SimpleStringProperty(valueDate);
        this.currency = new SimpleStringProperty(currency);
        this.amount = new SimpleStringProperty(amount);
        this.text = new SimpleStringProperty(text);
        this.renamed = isRenamed;
        this.file = file;
        this.selected = selected;
    }

    public MTEntity() {
        this("", "", "", "", "", "", "", "", "", "", null, false, false);
    }
    
    

    public String getType() {
        return type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public String getSender() {
        return sender.get();
    }

    public void setSender(String sender) {
        this.sender.set(sender);
    }

    public String getBene() {
        return bene.get();
    }

    public void setBene(String bene) {
        this.bene.set(bene);
    }

    public String getField20() {
        return field20.get();
    }

    public void setField20(String field20) {
        this.field20.set(field20);
    }

    public String getField21() {
        return field21.get();
    }

    public void setField21(String field21) {
        this.field21.set(field21);
    }

    public String getDateReceived() {
        return dateReceived.get();
    }

    public void setDateReceived(String dateReceived) {
        this.dateReceived.set(dateReceived);
    }

    public String getValueDate() {
        return valueDate.get();
    }

    public void setValueDate(String valueDate) {
        this.valueDate.set(valueDate);
    }

    public String getCurrency() {
        return currency.get();
    }

    public void setCurrency(String currency) {
        this.currency.set(currency);
    }

    public String getAmount() {
        return amount.get();
    }

    public void setAmount(String amount) {
        this.amount.set(amount);
    }

    public String getText() {
        return text.get();
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isRenamed() {
        return renamed;
    }

    public void setRenamed(boolean renamed) {
        this.renamed = renamed;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}
