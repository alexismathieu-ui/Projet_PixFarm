package FarmController;

import Farm.Crops.*;
import Farm.Culture;
import Farm.Farms;
import FarmEngine.GameBalance;
import FarmEngine.AudioPaths;
import FarmEngine.I18n;
import FarmEngine.SoundManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class StoreController {
    private Farms farms;
    private Runnable onPurchaseCallback;
    private static final java.util.Map<String, Integer> seedStocks = new java.util.HashMap<>();
    private static long nextRestockMs = 0;

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
    @FXML private Label storeTitleLabel;
    @FXML private Button closeBtn;
    @FXML private Tab buyTab;
    @FXML private Tab sellTab;
    @FXML private Label sellHintLabel;
    @FXML private Label marketTitleLabel;
    @FXML private Label marketSubLabel;
    @FXML private Button sellAllBtn;
    @FXML private Label restockTimerLabel;
    @FXML private Label sellFeedbackLabel;
    private Timeline restockUiTimer;

    @FXML
    public void initialize() {
        restockUiTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateRestockLabel()));
        restockUiTimer.setCycleCount(Timeline.INDEFINITE);
        restockUiTimer.play();
    }

    public void setFarms(Farms farms) {
        this.farms = farms;
        applyStaticTexts();
        updateUI();
    }

    private void applyStaticTexts() {
        if (storeTitleLabel != null) storeTitleLabel.setText("🛒 " + I18n.tr("store.header"));
        if (closeBtn != null) closeBtn.setText("✖ " + I18n.tr("store.close"));
        if (closeBtn != null) closeBtn.setTooltip(new Tooltip(I18n.tr("tooltip.store.close")));
        if (buyTab != null) buyTab.setText("🌱  " + I18n.tr("store.tab.buy"));
        if (sellTab != null) sellTab.setText("💰  " + I18n.tr("store.tab.sell"));
        if (sellHintLabel != null) sellHintLabel.setText(I18n.tr("store.sell.hint"));
        if (marketTitleLabel != null) marketTitleLabel.setText("📈 " + I18n.tr("store.market.title"));
        if (marketSubLabel != null) {
            farms.refreshSpecialOrderIfNeeded();
            String specialName = I18n.tr("quest.item." + farms.getSpecialOrderItem());
            int bonusPercent = (int) Math.round((farms.getSpecialOrderMultiplier() - 1.0) * 100.0);
            marketSubLabel.setText(I18n.tr("store.market.special", specialName, bonusPercent));
        }
        updateRestockLabel();
        if (sellAllBtn != null) sellAllBtn.setText("💰 " + I18n.tr("store.sellAll"));
        if (sellAllBtn != null) sellAllBtn.setTooltip(new Tooltip(I18n.tr("tooltip.store.sellAll")));
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
        ensureStockInitialized();
        maybeRestock();
        farms.refreshSpecialOrderIfNeeded();

        moneyLabel.setText(I18n.tr("store.money", (int) farms.getMoney()));

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
        updateRestockLabel();
    }

    private void updateButtonState(Button btn, int requiredLevel) {
        if (btn != null) {
            boolean isLocked = farms.getLevel() < requiredLevel;
            int stock = seedStocks.getOrDefault(btn.getId(), 0);
            btn.setDisable(isLocked || stock <= 0);
            if (isLocked) {
                btn.setText(I18n.tr("store.locked", requiredLevel));
            } else if (stock <= 0) {
                btn.setText(I18n.tr("store.outOfStock"));
            } else {
                btn.setText(I18n.tr("store.buy.withStock", (int)getPriceforSeeds(btn.getId()), stock));
            }
            btn.setTooltip(new Tooltip(I18n.tr("store.buy.withStock", (int)getPriceforSeeds(btn.getId()), stock)));
        }
    }

    private void updatePriceLabel(Label label, Culture c) {
        if (label == null) return;

        double dynamicPrice = farms.getDemandPrice(normalizeMarketItemName(c.getName()), c.getSellPrice());
        label.setText(I18n.tr("store.sell.dynamic", (int)dynamicPrice));


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
        int stock = seedStocks.getOrDefault(seedType, 0);
        if (stock <= 0) {
            updateUI();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, I18n.tr("store.confirm.buy", seedType, (int) price), ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(I18n.tr("store.confirm.title"));
        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;

        if (farms.spending(price)) {
            SoundManager.playSfx(AudioPaths.SFX_BUY);
            farms.getInventory().add(seedType, 1);
            seedStocks.put(seedType, stock - 1);
            updateUI();
            if (onPurchaseCallback != null) onPurchaseCallback.run();
        }
    }

    @FXML
    private void sellingCrops() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, I18n.tr("store.confirm.sellAll"), ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(I18n.tr("store.confirm.title"));
        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
        SoundManager.playSfx(AudioPaths.SFX_SELL);
        java.util.Map<String, Integer> items = farms.getInventory().getItems();
        java.util.List<String> keys = new java.util.ArrayList<>(items.keySet());

        double totalGain = 0;
        for (String itemName : keys) {
            int qty = items.get(itemName);
            if (qty <= 0) continue;

            double basePrice = 0;
            String displayName = normalizeMarketItemName(itemName);
            String sellKey = itemName.endsWith("_Crop") ? itemName : displayName + "_Crop";
            basePrice = getSellPrice(sellKey);

            if (basePrice > 0) {
                double pricePerUnit = farms.getDemandPrice(displayName, basePrice);
                double qualityBonusUnits = farms.consumeQualityBonus(displayName, qty);
                double specialMultiplier = 1.0;
                if (displayName.equals(farms.getSpecialOrderItem()) && System.currentTimeMillis() < farms.getSpecialOrderExpiryMs()) {
                    specialMultiplier = farms.getSpecialOrderMultiplier();
                }
                double gain = pricePerUnit * (qty + qualityBonusUnits) * specialMultiplier * farms.getPermanentSellMultiplier();
                farms.winMoney(gain);
                totalGain += gain;
                farms.recordSale(displayName, qty);
                farms.addSoldCount(qty);
                farms.getInventory().add(itemName, -qty);

                System.out.println("Vendu: " + qty + " " + displayName + " pour " + (pricePerUnit * qty) + "$");
            }
        }

        updateUI();
        if (sellFeedbackLabel != null) sellFeedbackLabel.setText(I18n.tr("store.sell.result", (int) totalGain));
        if (onPurchaseCallback != null) onPurchaseCallback.run();
    }

    private double getPriceforSeeds(String type) {
        return GameBalance.getSeedPrice(type);
    }

    private void ensureStockInitialized() {
        if (!seedStocks.isEmpty()) return;
        seedStocks.put("Wheat_Seed", 48);
        seedStocks.put("Carrot_Seed", 32);
        seedStocks.put("Potato_Seed", 28);
        seedStocks.put("Tomato_Seed", 22);
        seedStocks.put("Lemon_Seed", 18);
        seedStocks.put("Strawberry_Seed", 14);
        seedStocks.put("Corn_Seed", 10);
        seedStocks.put("Pineapple_Seed", 8);
        nextRestockMs = System.currentTimeMillis() + (5 * 60 * 1000L);
    }

    private void maybeRestock() {
        long now = System.currentTimeMillis();
        if (now < nextRestockMs) return;
        for (java.util.Map.Entry<String, Integer> e : seedStocks.entrySet()) {
            int max = switch (e.getKey()) {
                case "Wheat_Seed" -> 48;
                case "Carrot_Seed" -> 32;
                case "Potato_Seed" -> 28;
                case "Tomato_Seed" -> 22;
                case "Lemon_Seed" -> 18;
                case "Strawberry_Seed" -> 14;
                case "Corn_Seed" -> 10;
                case "Pineapple_Seed" -> 8;
                default -> 10;
            };
            e.setValue(Math.min(max, e.getValue() + Math.max(1, max / 4)));
        }
        nextRestockMs = now + (5 * 60 * 1000L);
    }

    private void updateRestockLabel() {
        if (restockTimerLabel == null) return;
        long secondsLeft = Math.max(0, (nextRestockMs - System.currentTimeMillis()) / 1000);
        long min = secondsLeft / 60;
        long sec = secondsLeft % 60;
        restockTimerLabel.setText(I18n.tr("store.restock.in", min, sec));
    }

    public static java.util.Map<String, Integer> snapshotSeedStocks() {
        return new java.util.HashMap<>(seedStocks);
    }

    public static long getNextRestockMs() {
        return nextRestockMs;
    }

    public static void restoreSeedStocks(java.util.Map<String, Integer> restored, long restoredNextRestockMs) {
        seedStocks.clear();
        seedStocks.putAll(restored);
        nextRestockMs = restoredNextRestockMs;
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
        if (restockUiTimer != null) restockUiTimer.stop();
        Stage stage = (Stage) moneyLabel.getScene().getWindow();
        stage.close();
    }
}