package FarmController;

import Farm.Crops.*;
import Farm.Culture;
import Farm.Farms;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class StoreController {
    private Farms farms;
    private Runnable onPurchaseCallback;

    @FXML private Label moneyLabel;

    @FXML private Button Wheat_Seed;
    @FXML private Button Carrot_Seed;
    @FXML private Button Potato_Seed;
    @FXML private Button Tomato_Seed;
    @FXML private Button Lemon_Seed;
    @FXML private Button Strawberry_Seed;
    @FXML private Button Corn_Seed;
    @FXML private Button Pineapple_Seed;

    @FXML private Label wheatSellLabel;
    @FXML private Label carrotSellLabel;
    @FXML private Label potatoSellLabel;
    @FXML private Label tomatoSellLabel;
    @FXML private Label lemonSellLabel;
    @FXML private Label strawberrySellLabel;
    @FXML private Label cornSellLabel;
    @FXML private Label pineappleSellLabel;

    public void setFarms(Farms farms) {
        this.farms = farms;
        updateUI();
    }

    private double getSellPrice(String name) {
        return switch (name) {
            case "Egg_Crop" -> 500.0;
            case "Wool_Crop" -> 3000.0;
            case "Milk_Crop" -> 20000.0;
            case "Truff_Crop" -> 150000.0;
            case "Wheat_Crop" -> 15.0;
            case "Carrot_Crop" -> 400.0;
            case "Potato_Crop" -> 2100.0;
            case "Tomato_Crop" -> 6500.0;
            case "Lemon_Crop" -> 24000.0;
            case "Strawberry_Crop" -> 150000.0;
            case "Corn_Crop" -> 575000.0;
            case "Pineapple_Crop" -> 3000000.0;
            default -> 0.0;
        };
    }

    @FXML
    public void updateUI() {
        if (farms == null) return;

        moneyLabel.setText("Argent : " + (int)farms.getMoney() + " $");

        updateButtonState(Wheat_Seed, 1);
        updateButtonState(Carrot_Seed, 2);
        updateButtonState(Potato_Seed, 3);
        updateButtonState(Tomato_Seed, 5);
        updateButtonState(Lemon_Seed, 6);
        updateButtonState(Strawberry_Seed, 7);
        updateButtonState(Corn_Seed, 10);
        updateButtonState(Pineapple_Seed, 15);

        updatePriceLabel(wheatSellLabel, new Wheat());
        updatePriceLabel(carrotSellLabel, new Carrot());
        updatePriceLabel(potatoSellLabel, new Potato());
        updatePriceLabel(tomatoSellLabel, new Tomato());
        updatePriceLabel(lemonSellLabel, new Lemon());
        updatePriceLabel(strawberrySellLabel, new Strawberry());
        updatePriceLabel(cornSellLabel, new Corn());
        updatePriceLabel(pineappleSellLabel, new Pineapple());
    }

    private void updateButtonState(Button btn, int requiredLevel) {
        if (btn != null) {
            boolean isLocked = farms.getLevel() < requiredLevel;
            btn.setDisable(isLocked);
            if (isLocked) {
                btn.setText("🔒 Niv. " + requiredLevel);
            } else {
                btn.setText("Acheter (" + (int)getPriceforSeeds(btn.getId()) + " $)");
            }
        }
    }

    private void updatePriceLabel(Label label, Culture c) {
        if (label == null) return;

        double dynamicPrice = farms.getDemandPrice(c.getName(), c.getSellPrice());
        label.setText("Prix de vente actuel : " + (int)dynamicPrice + " $");


        if (dynamicPrice < c.getSellPrice() * 0.7) {
            label.setStyle("-fx-text-fill: #e74c3c;");
        } else {
            label.setStyle("-fx-text-fill: #81c784;");
        }
    }

    @FXML
    private void buySeeds(ActionEvent event) {
        Button buybtn = (Button) event.getSource();
        String seedType = buybtn.getId();
        double price = getPriceforSeeds(seedType);

        if (farms.spending(price)) {
            farms.getInventory().add(seedType, 1);
            updateUI();
            if (onPurchaseCallback != null) onPurchaseCallback.run();
        }
    }

    @FXML
    private void sellingCrops() {
        java.util.Map<String, Integer> items = farms.getInventory().getItems();
        java.util.List<String> keys = new java.util.ArrayList<>(items.keySet());

        for (String itemName : keys) {
            int qty = items.get(itemName);
            if (qty <= 0) continue;

            double basePrice = 0;
            String displayName = itemName;

            if (itemName.endsWith("_Crop")) {
                basePrice = getSellPrice(itemName);
                displayName = itemName.replace("_Crop", "");
            } else {
                basePrice = getSellPrice(itemName);
            }

            if (basePrice > 0) {
                double pricePerUnit = farms.getDemandPrice(displayName, basePrice);
                farms.winMoney(pricePerUnit * qty);
                farms.recordSale(displayName, qty);
                farms.getInventory().add(itemName, -qty);

                System.out.println("Vendu: " + qty + " " + displayName + " pour " + (pricePerUnit * qty) + "$");
            }
        }

        updateUI();
        if (onPurchaseCallback != null) onPurchaseCallback.run();
    }

    private double getPriceforSeeds(String type) {
        return switch (type) {
            case "Wheat_Seed" -> 5.0;
            case "Carrot_Seed" -> 180.0;
            case "Potato_Seed" -> 850.0;
            case "Tomato_Seed" -> 3500.0;
            case "Lemon_Seed" -> 15000.0;
            case "Strawberry_Seed" -> 60000.0;
            case "Corn_Seed" -> 250000.0;
            case "Pineapple_Seed" -> 950000.0;
            default -> 0.0;
        };
    }

    private Culture createCulture(String seedType) {
        return switch (seedType) {
            case "Wheat_Seed" -> new Wheat();
            case "Potato_Seed" -> new Potato();
            case "Carrot_Seed" -> new Carrot();
            case "Tomato_Seed" -> new Tomato();
            case "Strawberry_Seed" -> new Strawberry();
            case "Lemon_Seed" -> new Lemon();
            case "Corn_Seed" -> new Corn();
            case "Pineapple_Seed" -> new Pineapple();
            default -> null;
        };
    }

    public void setOnPurchaseCallback(Runnable callback) {
        this.onPurchaseCallback = callback;
    }

    @FXML
    private void closeStore(ActionEvent event) {
        Stage stage = (Stage) moneyLabel.getScene().getWindow();
        stage.close();
    }
}