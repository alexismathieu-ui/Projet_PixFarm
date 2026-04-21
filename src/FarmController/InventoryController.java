package FarmController;

import Farm.Farms;
import FarmEngine.I18n;
import FarmEngine.GameSettings;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class InventoryController {
    private Farms currentFarms;

    @FXML private Label wheatSeedLabel;
    @FXML private Label wheatCropLabel;

    @FXML private GridPane seedGrid;
    @FXML private GridPane cropGrid;
    @FXML private GridPane animalGrid;
    @FXML private Label    totalItemsLabel;
    @FXML private Label    invStatusLabel;
    @FXML private Label invTitleLabel;
    @FXML private Label seedSectionLabel;
    @FXML private Label cropSectionLabel;
    @FXML private Label animalSectionLabel;
    @FXML private javafx.scene.control.Button closeBtn;
    @FXML private ComboBox<String> filterTypeCombo;
    @FXML private CheckBox filterNonEmptyCheck;
    @FXML private Label toolsDurabilityLabel;

    private static final String[] CROPS = {"Wheat","Carrot","Potato","Tomato","Lemon","Strawberry","Corn","Pineapple"};
    private static final String[] ANIMALS_PROD = {"Egg","Milk","Wool","Truff","Compost"};

    private static final java.util.Map<String,String> EMOJI = new java.util.HashMap<>();
    static {
        EMOJI.put("Wheat","🌾"); EMOJI.put("Carrot","🥕"); EMOJI.put("Potato","🥔");
        EMOJI.put("Tomato","🍅"); EMOJI.put("Lemon","🍋"); EMOJI.put("Strawberry","🍓");
        EMOJI.put("Corn","🌽"); EMOJI.put("Pineapple","🍍");
        EMOJI.put("Egg","🥚"); EMOJI.put("Milk","🥛"); EMOJI.put("Wool","🧶"); EMOJI.put("Truff","🍄"); EMOJI.put("Compost","♻️");
    }
    private static final java.util.Map<String,String> FR = new java.util.HashMap<>();
    private static final java.util.Map<String,String> EN = new java.util.HashMap<>();
    static {
        FR.put("Wheat","Blé"); FR.put("Carrot","Carotte"); FR.put("Potato","Patate");
        FR.put("Tomato","Tomate"); FR.put("Lemon","Citron"); FR.put("Strawberry","Fraise");
        FR.put("Corn","Maïs"); FR.put("Pineapple","Ananas");
        FR.put("Egg","Œuf"); FR.put("Milk","Lait"); FR.put("Wool","Laine"); FR.put("Truff","Truffe"); FR.put("Compost","Compost");

        EN.put("Wheat","Wheat"); EN.put("Carrot","Carrot"); EN.put("Potato","Potato");
        EN.put("Tomato","Tomato"); EN.put("Lemon","Lemon"); EN.put("Strawberry","Strawberry");
        EN.put("Corn","Corn"); EN.put("Pineapple","Pineapple");
        EN.put("Egg","Egg"); EN.put("Milk","Milk"); EN.put("Wool","Wool"); EN.put("Truff","Truffle"); EN.put("Compost","Compost");
    }

    public void update(Farms farms) {
        this.currentFarms = farms;
        if (seedGrid == null) return;
        applyStaticTexts();

        seedGrid.getChildren().clear();
        cropGrid.getChildren().clear();
        animalGrid.getChildren().clear();

        int total = 0;
        int col;

        java.util.Map<String, String> names = "EN".equalsIgnoreCase(GameSettings.getLanguage()) ? EN : FR;
        String typeFilter = filterTypeCombo != null && filterTypeCombo.getValue() != null ? filterTypeCombo.getValue() : "ALL";
        boolean nonEmptyOnly = filterNonEmptyCheck != null && filterNonEmptyCheck.isSelected();

        col = 0;
        for (String name : CROPS) {
            int qty = farms.getInventory().getQuantity(name + "_Seed");
            total += qty;
            if (nonEmptyOnly && qty <= 0) continue;
            if ("CROPS".equals(typeFilter) || "ANIMALS".equals(typeFilter)) continue;
            String suffix = I18n.tr("inventory.type.seed");
            VBox card = makeCard(EMOJI.getOrDefault(name,"🌱"), names.getOrDefault(name,name), qty, "seed", names.getOrDefault(name,name) + suffix);
            seedGrid.add(card, col % 4, col / 4);
            col++;
        }

        col = 0;
        for (String name : CROPS) {
            int qty = farms.getInventory().getQuantity(name + "_Crop");
            total += qty;
            if (nonEmptyOnly && qty <= 0) continue;
            if ("SEEDS".equals(typeFilter) || "ANIMALS".equals(typeFilter)) continue;
            String suffix = I18n.tr("inventory.type.crop");
            VBox card = makeCard(EMOJI.getOrDefault(name,"🌾"), names.getOrDefault(name,name), qty, "crop", names.getOrDefault(name,name) + suffix);
            cropGrid.add(card, col % 4, col / 4);
            col++;
        }

        col = 0;
        for (String name : ANIMALS_PROD) {
            int qty = farms.getInventory().getQuantity(name + "_Crop");
            total += qty;
            if (nonEmptyOnly && qty <= 0) continue;
            if ("SEEDS".equals(typeFilter) || "CROPS".equals(typeFilter)) continue;
            VBox card = makeCard(EMOJI.getOrDefault(name,"📦"), names.getOrDefault(name,name), qty, "animal", names.getOrDefault(name,name));
            animalGrid.add(card, col % 4, col / 4);
            col++;
        }

        if (totalItemsLabel != null) {
            boolean english = "EN".equalsIgnoreCase(GameSettings.getLanguage());
            totalItemsLabel.setText(english ? I18n.tr("inventory.items.count.en", total) : I18n.tr("inventory.items.count", total));
        }
        if (toolsDurabilityLabel != null) {
            toolsDurabilityLabel.setText(I18n.tr("inventory.tools.durability",
                    farms.getToolDurability("Hoe"), farms.getToolMaxDurability("Hoe"),
                    farms.getToolDurability("Sickle"), farms.getToolMaxDurability("Sickle"),
                    farms.getToolDurability("CompostShovel"), farms.getToolMaxDurability("CompostShovel")));
        }
    }

    private void applyStaticTexts() {
        invTitleLabel.setText("🎒 " + I18n.tr("inventory.header"));
        seedSectionLabel.setText("🌱 " + I18n.tr("inventory.section.seeds"));
        cropSectionLabel.setText("🌾 " + I18n.tr("inventory.section.crops"));
        animalSectionLabel.setText("🐾 " + I18n.tr("inventory.section.animals"));
        closeBtn.setText("✖ " + I18n.tr("inventory.close"));
        closeBtn.setTooltip(new Tooltip(I18n.tr("tooltip.inventory.close")));
        if (filterTypeCombo != null && filterTypeCombo.getItems().isEmpty()) {
            filterTypeCombo.getItems().addAll("ALL", "SEEDS", "CROPS", "ANIMALS");
            filterTypeCombo.setValue("ALL");
            filterTypeCombo.setOnAction(e -> { if (currentFarms != null) update(currentFarms); });
        }
        if (filterNonEmptyCheck != null) {
            filterNonEmptyCheck.setOnAction(e -> { if (currentFarms != null) update(currentFarms); });
        }
        if (invStatusLabel != null && (invStatusLabel.getText() == null || invStatusLabel.getText().isBlank())) {
            invStatusLabel.setText(I18n.tr("inventory.hint"));
        }
    }

    private VBox makeCard(String emoji, String name, int qty, String type, String fullName) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("inv-card");
        if (qty == 0) card.getStyleClass().add("inv-card-empty");
        else if (type.equals("seed")) card.getStyleClass().add("inv-card-seed");
        else if (type.equals("crop")) card.getStyleClass().add("inv-card-crop");
        else card.getStyleClass().add("inv-card-animal");

        Label emojiLbl = new Label(emoji);
        emojiLbl.getStyleClass().add("inv-emoji");

        Label nameLbl = new Label(name);
        nameLbl.getStyleClass().add("inv-item-name");

        Label qtyLbl = new Label("×" + qty);
        qtyLbl.getStyleClass().add(qty > 0 ? "inv-qty" : "inv-qty-zero");

        card.getChildren().addAll(emojiLbl, nameLbl, qtyLbl);

        card.setOnMouseClicked(e -> {
            if (invStatusLabel != null)
                invStatusLabel.setText(qty > 0
                        ? "📦 " + fullName + " : " + qty + " " + I18n.tr("inventory.stock")
                        : "🚫 " + fullName + " : " + I18n.tr("inventory.none"));
        });

        return card;
    }

    @FXML
    private void closeInventory(){
        // wheatSeedLabel always present (compat), use it or adminRoot
        try {
            Stage stage;
            if (wheatSeedLabel != null && wheatSeedLabel.getScene() != null)
                stage = (Stage) wheatSeedLabel.getScene().getWindow();
            else if (seedGrid != null)
                stage = (Stage) seedGrid.getScene().getWindow();
            else return;
            stage.close();
        } catch (Exception ignored) {}
    }
}
