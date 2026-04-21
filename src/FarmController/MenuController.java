package FarmController;

import Farm.Farms;
import FarmEngine.SaveSystem;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MenuController {
    @FXML private VBox menuPane;
    @FXML private Button slot1Btn;

    @FXML
    private void onPlayClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/SaveChoiceView.fxml"));
            Parent root = loader.load();

            SaveChoiceController controller = loader.getController();

            controller.init(slotId -> {
                System.out.println("Chargement du slot : " + slotId);
                launchGame(slotId);
            });

            Stage stage = new Stage();
            stage.setTitle("Sélection de la ferme");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onContinueClicked() {
        int lastSlot = SaveSystem.getMostRecentSaveSlot();
        if (lastSlot == -1) {
            onPlayClicked();
            return;
        }
        launchGame(lastSlot);
    }

    private void launchGame(int slotId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/MainView.fxml"));
            Parent root = loader.load();

            MainController gameController = loader.getController();

            Farms myFarm = new Farms(20.0);
            myFarm.setCurrentSaveSlot(slotId);

            File saveFile = new File("saves/save" + slotId + ".txt");
            if (saveFile.exists()) {
                SaveSystem.load(myFarm, slotId);
            } else {
                myFarm.generalQuests();
            }

            gameController.init(myFarm);

            Stage stage = (Stage) menuPane.getScene().getWindow();

            Scene scene = new Scene(root);


            stage.setScene(scene);
            stage.setTitle("Farm My Farm - Slot " + slotId);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleQuit(ActionEvent event) {
        Platform.exit();
    }
}