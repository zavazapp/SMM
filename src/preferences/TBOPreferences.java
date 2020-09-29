package preferences;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

/**
 *
 * @author Miodrag Spasic
 */
/**
 * this class provide persistence of user seeings stored locally by
 * java.util.prefs.Preferences class
 */
public class TBOPreferences extends MasterPreferences {

    private static TBOPreferences tboPreferences;
    //default folders
    private final String TBO_ROOT = "\\\\sftp\\tbo\\MT_SWIFT";
    // private final String TBO_ROOT = "C:\\Users\\Korisnik\\Desktop\\MT_TBO";
    private final String PPI_DIR = "\\\\sftp\\PPI\\MT_SWIFT";
    // private final String PPI_DIR = "C:\\Users\\Korisnik\\Desktop\\MT_PPI\\MT_SWIFT";
    //folders available in navigation menu
    public final List<String> AVAILABLE_PPI_FOLDERS = Arrays.asList("MT202O", "MT202I", "MT199I", "MT199O", "MT299I", "MT299O", "MT999I", "MT999O");
    private final String[] TBO_FOLDERS = new String[]{"MT200I", "MT200O", "MT202I", "MT202O", "MT300I", "MT300O", "MT320I", "MT320O", "MT950I", "MT950O", "MT199I", "MT199O", "MT299I", "MT299O", "MT999I", "MT999O"};
    private final String TBO_DOSIJEI = "\\\\sftp\\tbo\\DOSIJEI\\";
    private final String TBO_TICKETS = "\\\\sftp\\tbo\\Tiketi\\";
//default value of observers
    private final boolean[] TBO_STATUSES = new boolean[]{false, false, false, false, false, false, false, false, false, false};
    //TODO - not implemented yet. Idea is to rename file automaticly
    private final boolean TBO_AUTO_RENAME = false;

    public TBOPreferences(Class myClass) {
        super(Preferences.userNodeForPackage(myClass));

    }
    //single instance

    public static TBOPreferences getInstance(Class myClass) {
        if (tboPreferences == null) {
            return new TBOPreferences(myClass);
        }
        return tboPreferences;
    }

    //getters and setters
    @Override
    public Path getRoot() {
        return Paths.get(preferences.get("TBO_ROOT", TBO_ROOT));
    }

    @Override
    public void setRoot(Path tboRoot) {
        preferences.put("TBO_ROOT", tboRoot.toString());
        onTBORootChanged(tboRoot);
    }

    @Override
    public Path getSendDir() {
        return Paths.get(preferences.get("PPI_DIR", PPI_DIR));
    }

    @Override
    public void setSendDir(Path ppiDir) {
        preferences.put("PPI_DIR", ppiDir.toString());
    }

    public boolean isTBOAutoRename() {
        return preferences.getBoolean("TBO_AUTO_RENAME", TBO_AUTO_RENAME);
    }

    public void setTBOAutoRename(boolean status) {
        preferences.putBoolean("TBO_AUTO_RENAME", status);
    }

    @Override
    public Path getSpecificForder(int spec) {
        return Paths.get(preferences.get("TBO_".concat(TBO_FOLDERS[spec]), getRoot().toString().concat("\\").concat(TBO_FOLDERS[spec])));
    }

    @Override
    public void setSpecificFolder(int spec, Path newPath) {
        preferences.put("TBO_".concat(TBO_FOLDERS[spec]), newPath.toString());
    }

    @Override
    public void setSpecificFolder(String spec, Path newPath) {
        preferences.put("TBO_".concat(spec), newPath.toString());
    }

    public boolean getTBOSpecificStatus(int spec) {
        return preferences.getBoolean("TBO_STATUS_".concat(TBO_FOLDERS[spec]), TBO_STATUSES[spec]);
    }

    public void setTBOSpecificStatus(int spec, boolean status) {
        preferences.putBoolean("TBO_STATUS_".concat(TBO_FOLDERS[spec]), status);
    }

    public boolean[] getAllStatuses() {
        boolean[] statuses = new boolean[TBO_STATUSES.length];

        for (int i = 0; i < statuses.length; i++) {
            statuses[i] = preferences.getBoolean("TBO_STATUS_".concat(TBO_FOLDERS[i]), TBO_STATUSES[i]);
        }
        return statuses;
    }

    @Override
    public File getLastOppenedDir() {
        return new File(preferences.get("last_oppened_dir", getRoot().toString()));
    }

    @Override
    public void setLastOppenedDir(String lastOppened) {
        preferences.put("last_oppened_dir", lastOppened);
    }

    @Override
    public String[] getFOLDERS() {
        return TBO_FOLDERS;
    }

    @Override
    public boolean getObserverStatus() {
        return preferences.getBoolean("observer_status", true);
    }

    @Override
    public void setObserverStatus(boolean status) {
        preferences.putBoolean("observer_status", status);
    }

    @Override
    public Path getDosijeiFolder() {
        return Paths.get(preferences.get("TBO_DOSIJEI", TBO_DOSIJEI));
    }

    @Override
    public void setDosijeiFolder(Path path) {
        preferences.put("TBO_DOSIJEI", path.toString());
    }

    @Override
    public Path getTicketsFolder() {
        return Paths.get(preferences.get("TBO_TICKETS", TBO_TICKETS));
    }

    @Override
    public void setTicketsFolder(Path path) {
        preferences.put("TBO_TICKETS", path.toString());
    }

    //on ResetToDefault click impl.
    @Override
    public void resetPreferencesToDefault() {
        setRoot(Paths.get(TBO_ROOT));
        setSendDir(Paths.get(PPI_DIR));
        setTBOAutoRename(false);
        setObserverStatus(true);
        resetTBORoot(TBO_ROOT);
        setDosijeiFolder(Paths.get(TBO_DOSIJEI));
        setTicketsFolder(Paths.get(TBO_TICKETS));
    }

    //if root folder is changed all other folders are changet to point to root folder
    public void onTBORootChanged(Path root) {
        resetTBORoot(root.toString());
    }

    private void resetTBORoot(String root) {
        int count = TBO_FOLDERS.length;
        for (int i = 0; i < count; i++) {
            setSpecificFolder(i, Paths.get(root.concat("\\").concat(TBO_FOLDERS[i])));
            setTBOSpecificStatus(i, false);
        }
    }

}
