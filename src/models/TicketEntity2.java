package models;

import java.io.File;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author Miodrag Spasic
 */
public class TicketEntity2 {
    private File file;
    private final SimpleStringProperty ticketNumber;
    private final SimpleStringProperty dealType;
    private final SimpleStringProperty counterParty;
    private final SimpleStringProperty dealDetails;
    private final SimpleStringProperty amount1;
    private final SimpleStringProperty amount2;
    private final SimpleStringProperty date1;
    private final SimpleStringProperty date2;
    private String text;

    public TicketEntity2(File file, String text, String ticketNumber, String dealType, String counterParty, String dealDetails, String amount1, String amount2, String date1, String date2) {
        this.file = file;
        this.ticketNumber = new SimpleStringProperty(ticketNumber);
        this.dealType = new SimpleStringProperty(dealType);
        this.counterParty = new SimpleStringProperty(counterParty);
        this.dealDetails = new SimpleStringProperty(dealDetails);
        this.amount1 = new SimpleStringProperty(amount1);
        this.amount2 = new SimpleStringProperty(amount2);
        this.date1 = new SimpleStringProperty(date1);
        this.date2 = new SimpleStringProperty(date2);
        this.text = text;
        
    }

    // <editor-fold defaultstate="collapsed" desc="geters/setters">
    public String getTicketNumber() {
        return ticketNumber.get();
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber.set(ticketNumber);
    }

    public String getDealType() {
        return dealType.get();
    }

    public void setDealType(String dealType) {
        this.dealType.set(dealType);
    }

    public String getCounterParty() {
        return counterParty.get();
    }

    public void setCounterParty(String counterParty) {
        this.counterParty.set(counterParty);
    }

    public String getDealDetails() {
        return dealDetails.get();
    }

    public void setDealDetails(String dealDetails) {
        this.dealDetails.set(dealDetails);
    }

    public String getAmount1() {
        return amount1.get();
    }

    public void setAmount1(String amount1) {
        this.amount1.set(amount1);
    }

    public String getAmount2() {
        return amount2.get();
    }

    public void setAmount2(String amount2) {
        this.amount2.set(amount2);
    }

    public String getDate1() {
        return date1.get();
    }

    public void setDate1(String date1) {
        this.date1.set(date1);
    }

    public String getDate2() {
        return date2.get();
    }

    public void setDate2(String date2) {
        this.date2.set(date2);
    }
    
        public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    
    
    // </editor-fold>

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
