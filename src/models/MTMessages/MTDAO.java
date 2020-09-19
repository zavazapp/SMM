package models.MTMessages;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import utils.DateUtils;
import utils.FileUtils;

/**
 * Data Access Object class. Collects files from the source.
 *
 * @author Miodrag Spasic.
 */
public class MTDAO {

    FileUtils fileUtils = new FileUtils();
    private ArrayList<MTEntity> list;
    private Calendar c;

    public MTDAO() {
    }

    public ArrayList<MTEntity> getAll(Path root, Date date, String currentSelection, boolean isLive) {
        File observedDir;
        if (DateUtils.isDoday(date) && isLive) { //&& !AbstractBaseModule.ARCHIVE_MODE
            observedDir = Paths.get(root.toString(), currentSelection).toFile();
        } else {
            if (c == null) {
                c = Calendar.getInstance();
            }
            c.setTime(date);

            String archivePath = root + File.separator
                    + "MT_arhiva" + File.separator
                    + c.get(Calendar.YEAR) + File.separator
                    + currentSelection + "_archive" + File.separator //AbstractBaseModule.LAST_SELECTED_FOLDER
                    + DateUtils.getFormatedDate(date.getTime());

            observedDir = new File(archivePath);
        }

        if (observedDir.list() == null) {
            list = new ArrayList<>();
            return list;
        }

        if (observedDir.isDirectory()) {

            new Runnable() {
                @Override
                public void run() {
                    list = new ArrayList<>();
                    for (File file : observedDir.listFiles()) {
                        MTEntity mt;

                        if (file.getName().endsWith(".pdf")) {

                            try {
                                mt = fileUtils.readMt(file);
                                list.add(mt);
                            } catch (IOException ex) {
                                Logger.getLogger(MTDAO.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }
                    }
                }
            }.run();
        }
        return list;
    }

    /**
     * Filters collection - exclude directories and files that do not end with
     * ".pdf".
     *
     * @param path - file path of reading directory
     * @param date
     * @param isLive
     * @return - all files in path that ends with ".pdf"
     */
    public int getCount(Path path, Date date, boolean isLive) {
        if (DateUtils.isDoday(date) && isLive) { //&& !AbstractBaseModule.ARCHIVE_MODE
            return getCountFor(path);
        } else {
            if (c == null) {
                c = Calendar.getInstance();
            }
            c.setTime(date);

            String stringPath = path.toString();
            
            String specFolder = stringPath.substring(stringPath.length()-6, stringPath.length());
            
            String archivePath = path.toFile().getParentFile().getPath() + File.separator
                    + "MT_arhiva" + File.separator
                    + c.get(Calendar.YEAR) + File.separator
                    + specFolder + "_archive" + File.separator
                    + DateUtils.getFormatedDate(date.getTime());
            return getCountFor(Paths.get(archivePath));
        }
    }

    private int getCountFor(Path path) {
        if (path.toFile().isDirectory()) {
            return path.toFile().list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".pdf");
                }
            }).length;
        }
        return 0;
    }

    public int getMasterCount(Path root, String[] folders, Date date, boolean isLive) {
        int masterCount = 0;
        for (String folder : folders) {
            masterCount += getCount(Paths.get(root.toString(), folder), date, isLive);
        }
        return masterCount;
    }

    public ObservableList<MTEntity> getObservableList(Path ppiRoot, Date date, String currentSelection, boolean isLive) {
        return FXCollections.observableArrayList(getAll(ppiRoot, date, currentSelection, isLive));
        
    }

}
