package FarmController;

import Farm.Farms;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class InventoryController {
    @FXML
    private Label wheatSeedLabel;
    @FXML
    private Label wheatCropLabel;

    @FXML
    private ListView<String> inventoryList;

    public void update(Farms farms) {
        inventoryList.getItems().clear();

        String[] items = {"Wheat", "Carrot", "Potato", "Tomato", "Lemon", "Strawberry", "Corn", "Pineapple", "Egg", "Milk", "Truff","Wool"};

        for (String name : items) {
            int seeds = farms.getInventory().getQuantity(name + "_Seed");
            int crops = farms.getInventory().getQuantity(name + "_Crop");

            if (seeds > 0) inventoryList.getItems().add("Graines de " + name + " : " + seeds);
            if (crops > 0) inventoryList.getItems().add(name + " récolté(s) : " + crops);
        }
    }

    @FXML
    private void closeInventory(){
        Stage stage = (Stage) wheatSeedLabel.getScene().getWindow();
        stage.close();
    }
}
