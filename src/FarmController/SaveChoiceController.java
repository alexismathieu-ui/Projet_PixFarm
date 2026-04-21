package FarmController;

import FarmEngine.SaveSystem;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import java.util.function.Consumer;

public class SaveChoiceController {
    @FXML private Button slot1Btn, slot2Btn, slot3Btn;

    private Consumer<Integer> onSlotSelected;

    public void init(Consumer<Integer> callback) {
        this.onSlotSelected = callback;

        slot1Btn.setText("🌾  SLOT 1  —  " + SaveSystem.getSaveSummary(1));
        slot2Btn.setText("🌾  SLOT 2  —  " + SaveSystem.getSaveSummary(2));
        slot3Btn.setText("🌾  SLOT 3  —  " + SaveSystem.getSaveSummary(3));
    }

    @FXML private void selectSlot1() { handleSelection(1); }
    @FXML private void selectSlot2() { handleSelection(2); }
    @FXML private void selectSlot3() { handleSelection(3); }

    @FXML private void deleteSlot1() { handleDelete(1); }
    @FXML private void deleteSlot2() { handleDelete(2); }
    @FXML private void deleteSlot3() { handleDelete(3); }

    private void handleDelete(int slot) {
        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Supprimer définitivement la sauvegarde du slot " + slot + " ?",
                ButtonType.YES,
                ButtonType.NO
        );
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Suppression de sauvegarde");

        if (confirmation.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }

        java.io.File file = new java.io.File("saves/save" + slot + ".txt");
        java.io.File backup = new java.io.File("saves/save" + slot + ".txt.bak");
        java.io.File temp = new java.io.File("saves/save" + slot + ".txt.tmp");
        if (file.exists()) {
            file.delete();
        }
        if (backup.exists()) {
            backup.delete();
        }
        if (temp.exists()) {
            temp.delete();
        }

        slot1Btn.setText("🌾  SLOT 1  —  " + FarmEngine.SaveSystem.getSaveSummary(1));
        slot2Btn.setText("🌾  SLOT 2  —  " + FarmEngine.SaveSystem.getSaveSummary(2));
        slot3Btn.setText("🌾  SLOT 3  —  " + FarmEngine.SaveSystem.getSaveSummary(3));
    }

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