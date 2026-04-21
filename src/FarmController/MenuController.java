package FarmController;

import Farm.Farms;
import FarmEngine.GameSettings;
import FarmEngine.I18n;
import FarmEngine.SaveSystem;
import FarmEngine.SoundManager;
import FarmEngine.AudioPaths;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MenuController {
    @FXML private VBox menuPane;
    @FXML private Button slot1Btn;
    @FXML private Label lastSaveLabel;
    @FXML private Button playBtn;
    @FXML private Button continueBtn;
    @FXML private Button quitBtn;

    @FXML
    public void initialize() {
        GameSettings.load();
        playBtn.setText("🌱  " + I18n.tr("menu.play"));
        continueBtn.setText("▶  " + I18n.tr("menu.continue"));
        quitBtn.setText("✖  " + I18n.tr("menu.quit"));
        int slot = SaveSystem.getMostRecentSaveSlot();
        if (slot == -1) {
            lastSaveLabel.setText(I18n.tr("menu.continue.none"));
        } else {
            lastSaveLabel.setText(I18n.tr("menu.continue.last", slot, SaveSystem.getSaveSummary(slot)));
        }
    }

    @FXML
    private void onPlayClicked() {
        SoundManager.playSfx(AudioPaths.SFX_CLICK);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/SaveChoiceView.fxml"));
            Parent root = loader.load();

            SaveChoiceController controller = loader.getController();

            controller.init(slotId -> {
                System.out.println("Chargement du slot : " + slotId);
                launchGame(slotId);
            });

            Stage stage = new Stage();
            stage.setTitle(I18n.tr("menu.saveSelection.title"));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onContinueClicked() {
        SoundManager.playSfx(AudioPaths.SFX_CLICK);
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
            SoundManager.playMusic(AudioPaths.MUSIC_GAME);

            Stage stage = (Stage) menuPane.getScene().getWindow();

            Scene scene = new Scene(root);


            stage.setScene(scene);
            stage.setTitle(I18n.tr("menu.game.title", slotId));
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