package Tools;

import controlers.NotificationController;
import java.nio.file.Path;

/**
 *
 * @author Miodrag Spasic
 */
public interface IOnFileReceived {
    public void onFileReceived(Path root, Path filePath);
}
