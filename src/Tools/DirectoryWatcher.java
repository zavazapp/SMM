package Tools;

import controlers.MasterControler;
import controlers.NotificationController;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import static controlers.MasterControler.CHANGING_FILE_NAME_ONLY;
import controlers.SettingsController;

/**
 * This class sets watchers on directories that are checked in settings to
 * observe
 *
 * @author Miodrag Spasic
 */
public class DirectoryWatcher implements Runnable {

    public static WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private ArrayList<Path> dirs;
    static Path filename;
    private final IOnFileReceived notificationControler = new NotificationController();

    public DirectoryWatcher(ArrayList<Path> dirs) throws IOException {

        this.watcher = FileSystems.getDefault().newWatchService();
        this.dirs = dirs;
        this.keys = new HashMap<>();
        register(dirs);
        startNewThread(); //run watcher on saeparate thread
    }

    private void register(ArrayList<Path> dir) {
        for (Path path : dir) {
            WatchKey key = null;
            try {
                key = path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY, OVERFLOW);
            } catch (IOException ex) {
                Logger.getLogger(DirectoryWatcher.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }
            if (key != null && !path.toString().isEmpty() && path.toFile().exists() && path.toFile() != null) {
                keys.put(key, path);
            }

        }
    }

    public void startMTListener() throws IOException {
        System.err.println("Observer started");

        for (;;) {
            // wait for key to be signaled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                System.out.println(x.getMessage());
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            //loop stops here and waith for event
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                filename = ev.context();

                if (kind == StandardWatchEventKinds.ENTRY_MODIFY
                        || kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    continue;
                }

                //when i change the name of the file it trigers on create ?!?
                //use CHANGING_FILE_NAME_ONLY variable to avoid notification on file rename
                if (kind == StandardWatchEventKinds.ENTRY_CREATE
                        && filename.getFileName().toString().endsWith(".pdf")) {
                    if (!CHANGING_FILE_NAME_ONLY) {
                        System.out.println(filename);
                        Platform.runLater(() -> {
                            notificationControler.onFileReceived(keys.get(key), filename);
                        });
                        
                    }
                }
            }
            CHANGING_FILE_NAME_ONLY = false;

            // Reset the key -- this step is critical if you want to
            // receive further watch events.  If the key is no longer valid,
            // the directory is inaccessible so exit the loop.
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);
                break;
            }
        }
    }

    @Override
    public void run() {
        if (MasterControler.OBSERVE_ON) {
            try {
                startMTListener();
            } catch (IOException ex) {
                Logger.getLogger(DirectoryWatcher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void startNewThread() {
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.start();
        setStatus(true);

    }

    public static void closeWatcher() {
        if (watcher != null) {
            try {
                watcher.close();
                System.err.println("Observer STOPED");
            } catch (IOException ex) {
                Logger.getLogger(DirectoryWatcher.class.getName()).log(Level.SEVERE, null, ex);
            }
            setStatus(false);
        }
    }

    //Makes watcher globaly available mainly for stoping watcher when needed
    //TODO may not need this
    public static WatchService getWatcher() {
        return watcher;
    }

    //sets the label at the botom of the main screen to show if any observer is active
    private static void setStatus(boolean status) {
//        if (TBO.getWindows().length > 0) {
//            TBO.setStatus(status);
//        }
//
//        if (PPI.getWindows().length > 0) {
//            PPI.setStatus(status);
//
//        }
    }

}
