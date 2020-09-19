package controlers;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import models.MTMessages.MTEntity;

/**
 *
 * @author Miodrag Spasic
 */
public class CustomCellFormat extends TableCell<MTEntity, String> {


    public CustomCellFormat() {
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
        setText(empty ? "" : item);
        setGraphic(null);

        TableRow<MTEntity> currentRow = getTableRow();

        if (!empty && currentRow != null && currentRow.getItem() != null) {

            if (!currentRow.getItem().isRenamed()) {
                setTextFill(javafx.scene.paint.Color.web("8F00FF"));
            } else {
                setTextFill(javafx.scene.paint.Color.BLACK);
            }

            if (LoginController.MODE.equals("TBO") && (currentRow.getItem().getField20().contains("015")
                    || currentRow.getItem().getField20().contains("016")
                    || currentRow.getItem().getField21().contains("015")
                    || currentRow.getItem().getField21().contains("016"))) {
                setTextFill(javafx.scene.paint.Color.web("002AFD"));
            }

        }

    }

}
