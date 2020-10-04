package utils;

import controlers.T1Controller;
import controlers.T1Controller.T1Item;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.MTMessages.MTEntity;
import models.TicketEntity;
import preferences.TBOPreferences;

/**
 *
 * @author Miodrag Spasic
 */
public class Dosije {

    private final preferences.MasterPreferences preferences = TBOPreferences.getInstance(T1Controller.class);
    private final FileUtils fileUtils = new FileUtils();
    private final Path root = preferences.getDosijeiFolder();
    private final ArrayList<T1Item> items;
    private String fileName;
    private String archiveLocation;
    private MTEntity mt;
    private TicketEntity tic;

    public Dosije() {
        items = new ArrayList<>();
    }

    public void addItem(T1Item item) throws IOException {
        items.add(item);
    }

    public void removeItem(T1Item item) throws IOException {
        items.remove(item);
    }

    public int getSize() {
        return items.size();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileNameFromItem(T1Item item) {
        if (item.getType().equals("mt")) {
            MTEntity mTEntity = (MTEntity) getModel(item);
            return mTEntity.getField20().concat(".pdf");
        }
        if (item.getType().equals("dosijei")) {
            return item.getFileName();
        }
        return null;
    }

    public void setArchiveLocation(T1Item clickedItem) {
        Object model = getModel(clickedItem);
        if (model instanceof MTEntity) {
            mt = (MTEntity) model;
            archiveLocation = root + File.separator
                    + DateUtils.getYearFromMt(mt.getValueDate()) + File.separator
                    + DateUtils.getMonthFromMt(mt.getValueDate()) + File.separator
                    + mt.getValueDate();
        }

        if (model instanceof TicketEntity) {
            tic = (TicketEntity) model;
            archiveLocation = root + File.separator
                    + DateUtils.getYearFromTicket(tic.getFormatedDate()) + File.separator
                    + DateUtils.getMonthFromTicket(tic.getFormatedDate()) + File.separator
                    + tic.getFormatedDate();
        }
        if (model == null) {
            archiveLocation = clickedItem.getFile().getParent();
            setFileName(clickedItem.getFileName());
        }
    }

    public String getArchiveLocation() {
        return archiveLocation;
    }

    private Object getModel(T1Item item) {
        Object o = null;
        switch (item.getType()) {
            case "mt":
                        try {
                o = fileUtils.readMt(item.getFile());
            } catch (IOException ex) {
                Logger.getLogger(T1Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
            break;

            case "ticket":
                        try {
                o = new TicketEntity(item.getFile());

            } catch (IOException ex) {
                Logger.getLogger(T1Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
            break;

            case "dosijei":

                break;
        }
        return o;
    }

    public void clear() {
        items.clear();
    }

}
