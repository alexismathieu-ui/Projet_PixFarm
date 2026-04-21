package FarmController;

import Farm.Crops.*;
import Farm.Culture;
import Farm.Farms;
import FarmEngine.GameBalance;
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
        return GameBalance.getSellBasePrice(name);
    }

    private String normalizeMarketItemName(String itemName) {
        return itemName.replace("_Crop", "").replace("_Seed", "");
    }

    @FXML
    public void updateUI() {
        if (farms == null) return;

        moneyLabel.setText("Argent : " + (int)farms.getMoney() + " $");

        updateButtonState(Wheat_Seed, GameBalance.getSeedUnlockLevel("Wheat_Seed"));
        updateButtonState(Carrot_Seed, GameBalance.getSeedUnlockLevel("Carrot_Seed"));
        updateButtonState(Potato_Seed, GameBalance.getSeedUnlockLevel("Potato_Seed"));
        updateButtonState(Tomato_Seed, GameBalance.getSeedUnlockLevel("Tomato_Seed"));
        updateButtonState(Lemon_Seed, GameBalance.getSeedUnlockLevel("Lemon_Seed"));
        updateButtonState(Strawberry_Seed, GameBalance.getSeedUnlockLevel("Strawberry_Seed"));
        updateButtonState(Corn_Seed, GameBalance.getSeedUnlockLevel("Corn_Seed"));
        updateButtonState(Pineapple_Seed, GameBalance.getSeedUnlockLevel("Pineapple_Seed"));

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

        double dynamicPrice = farms.getDemandPrice(normalizeMarketItemName(c.getName()), c.getSellPrice());
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
            String displayName = normalizeMarketItemName(itemName);
            String sellKey = itemName.endsWith("_Crop") ? itemName : displayName + "_Crop";
            basePrice = getSellPrice(sellKey);

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
        return GameBalance.getSeedPrice(type);
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