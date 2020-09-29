package preferences;

import controlers.LoginController;
import static controlers.SettingsController.SETTINGS_CHANGED;
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
 * this class provide persistence of user settings stored locally by
 * java.util.prefs.MasterPreferences class
 */
public class PPIPreferences extends MasterPreferences {

    private static PPIPreferences ppiPreferences;
    //default folders
    private final String PPI_ROOT = "\\\\sftp\\PPI\\MT_SWIFT";
    // private final String PPI_ROOT = "C:\\Users\\Korisnik\\Desktop\\MT_PPI\\MT_SWIFT";
    private final String TBO_DIR = "\\\\sftp\\tbo\\MT_SWIFT";
    private final String PENSIONS_DIR = "\\\\sftp\\PPI\\Spiskovi za penzije\\ZAHTEVI ZA POVRACAJ PENZIJA";
    //  private final String TBO_DIR = "C:\\Users\\Korisnik\\Desktop\\MT_TBO";
    //folders available in navigation menu
    private final List<String> AVAILABLE_TBO_FOLDERS = Arrays.asList("MT202I", "MT202O", "MT199I", "MT199O", "MT299I", "MT299O", "MT999I", "MT999O");
    private final String[] PPI_FOLDERS = new String[]{"MT192I", "MT192O", "MT195I", "MT195O", "MT196I", "MT196O", "MT199I", "MT199O", "MT202I", "MT202O", "MT299I", "MT299O", "MT999I", "MT999O"};
    //default value of observers
    private final boolean[] PPI_STATUSES = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
    //TODO - not implemented yet. Idea is to rename file automaticly
    private final boolean PPI_AUTO_RENAME = false;

    /**
     *
     * @param myClass
     */
    public PPIPreferences(Class myClass) {
        super(Preferences.userNodeForPackage(myClass));

    }

    //single instance
    public static PPIPreferences getInstance(Class myClass) {
        if (ppiPreferences == null) {
            return new PPIPreferences(myClass);
        }
        return ppiPreferences;
    }

    //getters and setters
    @Override
    public Path getRoot() {
        return Paths.get(preferences.get("PPI_ROOT", PPI_ROOT));
    }

    @Override
    public void setRoot(Path ppiRoot) {
        preferences.put("PPI_ROOT", ppiRoot.toString());
        onPPIRootChanged(ppiRoot);
    }

    @Override
    public Path getSendDir() {
        return Paths.get(preferences.get("TBO_DIR", TBO_DIR));
    }

    @Override
    public void setSendDir(Path tboDir) {
        preferences.put("TBO_DIR", tboDir.toString());
    }

    public boolean isPPIAutoRename() {
        return preferences.getBoolean("PPI_AUTO_RENAME", PPI_AUTO_RENAME);
    }

    public void setPPIAutoRename(boolean status) {
        preferences.putBoolean("PPI_AUTO_RENAME", status);
    }

    @Override
    public Path getSpecificForder(int spec) {
        return Paths.get(preferences.get("PPI_".concat(PPI_FOLDERS[spec]), getRoot().toString().concat("\\").concat(PPI_FOLDERS[spec])));
    }

    @Override
    public void setSpecificFolder(int spec, Path newPath) {
        preferences.put("PPI_".concat(PPI_FOLDERS[spec]), newPath.toString());
    }

    @Override
    public void setSpecificFolder(String spec, Path newPath) {
        preferences.put("PPI_".concat(spec), newPath.toString());
    }

    public boolean getPPISpecificStatus(int spec) {
        return preferences.getBoolean("PPI_STATUS_".concat(PPI_FOLDERS[spec]), PPI_STATUSES[spec]);
    }

    public void setPPISpecificStatus(int spec, boolean status) {
        preferences.putBoolean("PPI_STATUS_".concat(PPI_FOLDERS[spec]), status);
    }

    public boolean[] getAllStatuses() {
        boolean[] statuses = new boolean[PPI_STATUSES.length];

        for (int i = 0; i < statuses.length; i++) {
            statuses[i] = preferences.getBoolean("PPI_STATUS_".concat(PPI_FOLDERS[i]), PPI_STATUSES[i]);
        }
        return statuses;
    }

    @Override
    public String[] getFOLDERS() {
        return PPI_FOLDERS;
    }

    @Override
    public File getLastOppenedDir() {
        return new File(preferences.get("last_oppened_dir", getRoot().toString()));
    }

    @Override
    public void setLastOppenedDir(String lastOppened) {
        preferences.put("last_oppened_dir", lastOppened);
    }

    public void setLastMode() {
        preferences.put("last_mode", LoginController.MODE);
    }

    public String getLastMode() {
        return preferences.get("last_mode", "PPI");
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
    public void setPensionsFolder(Path path) {
        preferences.put("PENSIONS_DIR", path.toString());
    }

    @Override
    public Path getPensionsFolder() {
        return Paths.get(preferences.get("PENSIONS_DIR", PENSIONS_DIR));
    }

    public double getFont() {
        return preferences.getDouble("font", 12);
    }

    public void setFont(double font) {
        preferences.putDouble("font", font);
    }

    //on ResetToDefault click impl.
    @Override
    public void resetPreferencesToDefault() {
        setRoot(Paths.get(PPI_ROOT));
        setSendDir(Paths.get(TBO_DIR));
        setPensionsFolder(Paths.get(PENSIONS_DIR));
        setPPIAutoRename(false);
        setObserverStatus(true);
        resetPPIRoot(PPI_ROOT);
    }

    //if root folder is changed all other folders are changet to point to root folder
    public void onPPIRootChanged(Path root) {
        resetPPIRoot(root.toString());
    }

    private void resetPPIRoot(String root) {
        int count = PPI_FOLDERS.length;
        for (int i = 0; i < count; i++) {
            setSpecificFolder(i, Paths.get(root.concat("\\").concat(PPI_FOLDERS[i])));
            setPPISpecificStatus(i, false);
        }
        SETTINGS_CHANGED.set(!SETTINGS_CHANGED.get());
    }

}
