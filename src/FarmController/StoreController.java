package FarmController;

import Farm.Crops.*;
import Farm.Culture;
import Farm.Farms;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class StoreController {
    private Farms farms;
    private Runnable onPurchaseCallback;

    @FXML private Label moneyLabel;

    @FXML private Button Wheat_Seed;
    @FXML private Button Carrot_Seed;
    @FXML private Button Potato_Seed;
    @FXML private Button Tomato_Seed;
    @FXML private Button Kiwi_Seed;
    @FXML private Button Strawberry_Seed;
    @FXML private Button Corn_Seed;
    @FXML private Button Pumpkin_Seed;

    @FXML private Label wheatSellLabel;
    @FXML private Label carrotSellLabel;
    @FXML private Label potatoSellLabel;
    @FXML private Label tomatoSellLabel;
    @FXML private Label kiwiSellLabel;
    @FXML private Label strawberrySellLabel;
    @FXML private Label cornSellLabel;
    @FXML private Label pumpkinSellLabel;

    public void setFarms(Farms farms) {
        this.farms = farms;
        updateUI();
    }

    @FXML
    public void updateUI() {
        if (farms == null) return;

        moneyLabel.setText("Argent : " + (int)farms.getMoney() + " $");

        // 1. Mise à jour du verrouillage des boutons
        updateButtonState(Wheat_Seed, 1);
        updateButtonState(Carrot_Seed, 2);
        updateButtonState(Potato_Seed, 3);
        updateButtonState(Tomato_Seed, 5);
        updateButtonState(Kiwi_Seed, 6);
        updateButtonState(Strawberry_Seed, 7);
        updateButtonState(Corn_Seed, 10);
        updateButtonState(Pumpkin_Seed, 20);

        // 2. Mise à jour des labels de prix dynamiques
        updatePriceLabel(wheatSellLabel, new Wheat());
        updatePriceLabel(carrotSellLabel, new Carrot());
        updatePriceLabel(potatoSellLabel, new Potato());
        updatePriceLabel(tomatoSellLabel, new Tomato());
        updatePriceLabel(kiwiSellLabel, new Kiwi());
        updatePriceLabel(strawberrySellLabel, new Strawberry());
        updatePriceLabel(cornSellLabel, new Corn());
        updatePriceLabel(pumpkinSellLabel, new Pumpkin());
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
        for (String itemName : farms.getInventory().getItems().keySet()) {
            if (itemName.endsWith("_Crop")) {
                int qty = farms.getInventory().getQuantity(itemName);
                if (qty > 0) {
                    String cultureName = itemName.replace("_Crop", "");
                    Culture c = createCulture(cultureName + "_Seed");

                    if (c != null) {
                        double pricePerUnit = farms.getDemandPrice(c.getName(), c.getSellPrice());
                        farms.winMoney(pricePerUnit * qty);
                        farms.recordSale(c.getName(), qty);
                        farms.getInventory().add(itemName, -qty);
                    }
                }
            }
        }
        updateUI();
        if (onPurchaseCallback != null) onPurchaseCallback.run();
    }

    // Tes méthodes utilitaires restent identiques
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

    private Culture createCulture(String seedType) {
        return switch (seedType) {
            case "Wheat_Seed" -> new Wheat();
            case "Potato_Seed" -> new Potato();
            case "Carrot_Seed" -> new Carrot();
            case "Tomato_Seed" -> new Tomato();
            case "Strawberry_Seed" -> new Strawberry();
            case "Kiwi_Seed" -> new Kiwi();
            case "Corn_Seed" -> new Corn();
            case "Pumpkin_Seed" -> new Pumpkin();
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