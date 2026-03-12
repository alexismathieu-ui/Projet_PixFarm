package FarmController;

import FarmEngine.SaveSystem;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.util.function.Consumer;

public class SaveChoiceController {
    @FXML private Button slot1Btn, slot2Btn, slot3Btn;

    private Consumer<Integer> onSlotSelected;

    public void init(Consumer<Integer> callback) {
        this.onSlotSelected = callback;

        slot1Btn.setText("SLOT 1\n" + SaveSystem.getSaveSummary(1));
        slot2Btn.setText("SLOT 2\n" + SaveSystem.getSaveSummary(2));
        slot3Btn.setText("SLOT 3\n" + SaveSystem.getSaveSummary(3));
    }

    @FXML private void selectSlot1() { handleSelection(1); }
    @FXML private void selectSlot2() { handleSelection(2); }
    @FXML private void selectSlot3() { handleSelection(3); }

    private void handleSelection(int slot) {
        if (onSlotSelected != null) {
            onSlotSelected.accept(slot);
        }
        close();
    }

    @FXML
    private void close() {
        Stage stage = (Stage) slot1Btn.getScene().getWindow();
        stage.close();
    }
}