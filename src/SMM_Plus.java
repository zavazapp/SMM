
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 *
 * @author Miodrag Spasic
 */
public class SMM_Plus extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("FXMLFiles/Login.fxml"));
        
        Scene scene = new Scene(root);
        
        stage.initStyle(StageStyle.UNIFIED);
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
