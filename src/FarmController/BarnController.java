package FarmController;

import Farm.Animals;
import Farm.Farms;
import FarmController.MainController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Stack;

public class BarnController {

    @FXML
    private BorderPane barnRoot;
    @FXML
    private GridPane animalGrid;
    @FXML private Label levelLabel;
    @FXML private ProgressBar xpBar;
    @FXML private Label xpLabel;
    @FXML
    private Label animalCountLabel;
    @FXML
    private Label labelStatus;

    private Farms farms;
    private InventoryController currentInventoryCtrl;

    public void setFarms(Farms farms) {
        this.farms = farms;
        Timeline autoRefresh = new Timeline(new KeyFrame(Duration.millis(700), event -> {
            updateLevelUI();
            refreshAnimalGrid();
        }));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();
    }
    private void updateLevelUI(){
        if (levelLabel != null && xpBar != null){
            levelLabel.setText("Niveau " + farms.getLevel());

            double progress = farms.getCurrentXP() / farms.getNextLevelXP();
            xpBar.setProgress(progress);

            if (xpLabel != null) {
                xpLabel.setText((int)farms.getCurrentXP() + " / " + (int)farms.getNextLevelXP() + " XP");
            }
        }
    }

    private void refreshAnimalGrid() {
        animalGrid.getChildren().clear();

        int col = 0;
        int row = 0;

        for (Animals animals : farms.getMyAnimals()){
            StackPane card = new StackPane();
            Rectangle background = new Rectangle(120,120);
            background.setFill(Color.web("#8D6E63"));
            background.setArcWidth(15);
            background.setArcHeight(15);

            VBox info = new VBox(5);
            info.setAlignment(Pos.CENTER);
            Label name = new Label(animals.getSpecies());
            name.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

            Label statusLabel = new Label();
            if (animals.hasProduced()) {
                statusLabel.setText("PRÊT ! (Récolter)");
                statusLabel.setStyle("-fx-text-fill: #00FF00; -fx-font-weight: bold;");
            } else if (animals.isHungry()) {
                statusLabel.setText("A FAIM (Donner : " + animals.getFoodNeeded() + ")");
                statusLabel.setStyle("-fx-text-fill: #FF5555;");
            } else {
                statusLabel.setText("Travaille...");
                statusLabel.setStyle("-fx-text-fill: #FFFF00;");
            }

            info.getChildren().addAll(name, statusLabel);
            card.getChildren().addAll(background, info);

            col++;
            if (col > 4){col = 0; row++;}

            card.setOnMouseClicked(event -> {
                if (animals.hasProduced()) {
                    farms.getInventory().add(animals.getProductType() + "_Crop", 1);
                    farms.addXP(50);
                    animals.setProduced(false);
                    animals.setHungry(true);
                    System.out.println("Récolté : " + animals.getProductType());
                }
                else if (animals.isHungry()) {
                    String food = animals.getFoodNeeded();
                    if (farms.getInventory().getQuantity(food) > 0) {
                        farms.getInventory().add(food, -1);
                        farms.addXP(25);
                        animals.setHungry(false);
                        System.out.println(animals.getSpecies() + " mange et commence à produire...");
                    } else {
                        System.out.println("Tu n'as pas de " + food + " !");
                    }
                }
                else {
                    System.out.println("Laisse-le tranquille, il travaille !");
                }
                refreshAnimalGrid();
                refreshInventoryUI();
            });
            animalGrid.add(card, col, row);
        }
        animalCountLabel.setText("Animals : " + farms.getMyAnimals().size());
    }

    @FXML
    private void goToFarm() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/MainView.fxml"));
        Parent root = loader.load();
        MainController mainCtrl = loader.getController();
        mainCtrl.setFarms(this.farms);

        Stage stage = (Stage) barnRoot.getScene().getWindow();
        stage.getScene().setRoot(root);
    }

    @FXML
    private void onOpenAnimalShop() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/AnimalShopView.fxml"));
            Parent root = loader.load();

            AnimalShopController storeCtrl = loader.getController();
            storeCtrl.setFarms(this.farms);

            storeCtrl.setOnUpdateCallback(() -> {
                refreshAnimalGrid();
            });

            Stage stage = new Stage();
            stage.setTitle("Marchand d'animaux");

            stage.initOwner(animalGrid.getScene().getWindow());
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);


            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshInventoryUI(){
        if(currentInventoryCtrl != null){
            currentInventoryCtrl.update(this.farms);
        }
    }

    @FXML
    private void onOpenInventory() {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/InventoryView.fxml"));
            Parent root = loader.load();



            currentInventoryCtrl = loader.getController();
            currentInventoryCtrl.update(this.farms);

            Stage inventoryStage = new Stage();
            inventoryStage.setTitle("Inventory");
            inventoryStage.setScene(new Scene(root));

            inventoryStage.setOnCloseRequest(e -> currentInventoryCtrl = null);

            inventoryStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}