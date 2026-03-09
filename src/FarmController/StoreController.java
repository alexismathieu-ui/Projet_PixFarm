package FarmController;

import Farm.Crops.Carrot;
import Farm.Crops.Kiwi;
import Farm.Crops.Wheat;
import Farm.Farms;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class StoreController {
    private Farms farms;
    private Runnable onPurchaseCallback;

    @FXML
    private Button Tomato_Seed;
    @FXML
    private Button Pumpkin_Seed;
    @FXML
    private Button Potato_Seed;
    @FXML
    private Button Kiwi_Seed;
    @FXML
    private Button Corn_Seed;
    @FXML
    private Button Strawberry_Seed;
    @FXML
    private Button Carrot_Seed;

    public void setFarms(Farms farms) {
        this.farms = farms;
        updateUI();
    }

    private void updateUI(){
        updateButtonState(Tomato_Seed, 5);
        updateButtonState(Carrot_Seed, 2);
        updateButtonState(Potato_Seed, 3);
        updateButtonState(Kiwi_Seed, 6);
        updateButtonState(Strawberry_Seed, 7);
        updateButtonState(Corn_Seed, 10);
        updateButtonState(Pumpkin_Seed, 20);
    }

    private void updateButtonState(Button btn, int requiredLevel) {
        if (btn != null) {
            boolean isLocked = farms.getLevel() < requiredLevel;
            btn.setDisable(isLocked);
            if (isLocked) {
                btn.setText("🔒 Niv. " + requiredLevel);
            }
        }
    }

    public void setOnPurchaseCallback(Runnable callback) {
        this.onPurchaseCallback = callback;
    }

    @FXML
    private void buySeeds(ActionEvent event) {
        Button buybtn = (Button) event.getSource();
        String seedType = buybtn.getId();
        int reqLevel = getRequiredLevel(seedType);

        if (farms.getLevel() < reqLevel) {
            System.out.println("Bloqué ! Niveau " + reqLevel + " requis.");
            return;
        }

        double price = getPriceforSeeds(seedType);

        if (farms.spending(price)) {
            farms.getInventory().add(seedType, 1);

            System.out.println("Buy successful : " + seedType);
        } else {
            System.out.println("Not enough money to buy : " + seedType);
        }

        if (onPurchaseCallback != null) {
            onPurchaseCallback.run();
        }
    }

    private double getPriceforSeeds(String type) {
        return switch (type) {
            case "Wheat_Seed" -> 5.0;
            case "Carrot_Seed" -> 180.0;
            case "Potato_Seed" -> 850.0;
            case "Tomato_Seed" -> 3500.0;
            case "Kiwi_Seed" -> 15000.0;
            case "Strawberry_Seed" -> 60000.0;
            case "Corn_Seed" -> 250000.0;
            case "Pumpkin_Seed" -> 950000.0;
            default -> 0.0;
        };
    }

    @FXML
    private void closeStore(ActionEvent event) {
        Button btn = (Button) event.getSource();
        Stage stage = (Stage) btn.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void sellingCrops() {
        double totalGains = 0;
        String[] allCrops = {"Wheat", "Carrot", "Potato", "Tomato", "Kiwi", "Strawberry", "Corn", "Pumpkin", "Egg", "Milk", "Wool", "Truffle"};

        for (String name : allCrops) {
            String key = name + "_Crop";
            int qty = farms.getInventory().getQuantity(key);
            if (qty > 0) {
                double price = getSellPrice(name);
                totalGains += (qty * price);
                farms.addXP(qty * 5);
                farms.getInventory().add(key, -qty);
                if (onPurchaseCallback != null) {
                    onPurchaseCallback.run();
                }
            }
        }

        if (totalGains > 0) {
            farms.winMoney(totalGains);
            System.out.println("Total Gains : " + totalGains + "$");
        } else {
            System.out.println("Nothing to sell in the bag !");
        }
    }

    private double getSellPrice(String name) {
        return switch (name) {
            case "Wheat" -> 15.0;
            case "Carrot" -> 400.0;
            case "Potato" -> 2100.0;
            case "Tomato" -> 6400.0;
            case "Kiwi" -> 24000.0;
            case "Strawberry" -> 150000.0;
            case "Corn" -> 575000.0;
            case "Pumpkin" -> 3000000.0;
            case "Egg" -> 500;
            case "Wool" -> 3000;
            case "Milk" -> 20000;
            case "Truff" -> 150000;
            default -> 0.0;
        };
    }

    private int getRequiredLevel(String seedName) {
        return switch (seedName) {
            case "Wheat_Seed" -> 1;
            case "Tomato_Seed" -> 5;
            case "Carrot_Seed" -> 2;
            case "Potato_Seed" -> 3;
            case "Kiwi_Seed" -> 6;
            case "Strawberry_Seed" -> 7;
            case "Corn_Seed" -> 10;
            case "Pumpkin_Seed" -> 20;
            default -> 1;
        };
    }
}
