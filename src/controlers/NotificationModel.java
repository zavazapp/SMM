package controlers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author Miodrag Spasic
 */
public class NotificationModel {

    private final Stage stage;
    private final Scene scene;

    private FadeTransition fadeInTransition, fadeOutTransition;
    private TranslateTransition slideOutTransition, slideInTransition, moveMoreDownTransition;

    public NotificationModel(FXMLLoader loader, Stage stage) throws IOException {
        this.scene = new Scene(loader.load());
        this.scene.setFill(null);

        stage.setScene(scene);
        stage.setX(4);
        stage.setY(4);
        this.stage = stage;

        loadTransitions();
    }

    private void loadTransitions() {
        if (fadeInTransition == null) {
            fadeInTransition = new FadeTransition(Duration.seconds(2), (AnchorPane) scene.getRoot());
            fadeInTransition.setFromValue(0);
            fadeInTransition.setToValue(1);
        }

        if (slideInTransition == null) {
            slideInTransition = new TranslateTransition(Duration.seconds(1), (AnchorPane) scene.getRoot());
            slideInTransition.setFromY(-300);
            slideInTransition.setToY(4);
        }

        if (fadeOutTransition == null) {
            fadeOutTransition = new FadeTransition(Duration.seconds(2), (AnchorPane) scene.getRoot());
            fadeOutTransition.setFromValue(1);
            fadeOutTransition.setToValue(0);
        }

        if (slideOutTransition == null) {
            slideOutTransition = new TranslateTransition(Duration.seconds(1), (AnchorPane) scene.getRoot());
            slideOutTransition.setFromY(4);
            slideOutTransition.setToY(-300);
        }

        if (moveMoreDownTransition == null) {
            moveMoreDownTransition = new TranslateTransition(Duration.seconds(1), (AnchorPane) scene.getRoot());
            moveMoreDownTransition.setFromY(4);
            moveMoreDownTransition.setToY(300);
        }
    }

    void show() {
        stage.show();
        slideInTransition.play();
    }

    void hide() {
        slideOutTransition.play();
        slideOutTransition.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stage.hide();
            }
        });
    }

    boolean isShowing() {
        return stage.isShowing();
    }

    void moveDown() {
        stage.setY(200d);
    }

    boolean isMovedDown() {
        return stage.getY() == 4.0;
    }

}
