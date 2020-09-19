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
public class MasterPreferences {

    public final Preferences preferences;

    public MasterPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    //getters and setters
    public Path getRoot() {
        return null;
    }

    public void setRoot(Path root) {
    }

    public String[] getFOLDERS() {
        return null;
    }

    public Path getSpecificForder(int spec) {
        return null;
    }

    public void setSpecificFolder(int spec, Path newPath) {
    }

    public void setSpecificFolder(String spec, Path newPath) {

    }

    public Path getSendDir() {
        return null;
    }

    public void setSendDir(Path ppiDir) {
    }

    public void resetPreferencesToDefault() {
    }

    public File getLastOppenedDir() {
        return null;
    }
    
    public void setLastOppenedDir(String lastOppened) {
    }
}
