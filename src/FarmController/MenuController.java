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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class MenuController {
    @FXML private VBox menuPane;
    @FXML private Button slot1Btn;

    @FXML
    public void handleNewGame(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/MainView.fxml"));
            Parent mainRoot = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(mainRoot);

            scene.getStylesheets().add(getClass().getResource("/FarmView/style.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Farm My Farm - Ma Ferme");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la ferme.");
        }
    }

    @FXML
    public void handleLoadGame(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/MainView.fxml"));
            Parent root = loader.load();


            MainController mainCtrl = loader.getController();
            Farms currentFarm = mainCtrl.getFarms();

            FarmEngine.SaveSystem.load(currentFarm, 1);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

            System.out.println("Partie chargée avec succès !");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    private void launchGame(int slotId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/MainView.fxml"));
            Parent root = loader.load();

            MainController gameController = loader.getController();

            Farms myFarm = new Farms(10000.0);
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