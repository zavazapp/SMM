
import Tools.HostServ;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Miodrag Spasic
 */
public class SMM_Plus extends Application {
    public static HostServices hostServices;
    
    @Override
    public void start(Stage stage) throws Exception {
        hostServices = getHostServices();
        HostServ.setHostServices(hostServices);
        Parent root = FXMLLoader.load(getClass().getResource("FXMLFiles/Login.fxml"));
        
        Scene scene = new Scene(root);
//        scene.setFill(null);
        stage.setScene(scene);
       
        stage.show();
        stage.setOnCloseRequest((WindowEvent e) -> {
            Platform.exit();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
