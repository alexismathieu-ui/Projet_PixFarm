package FarmController;

import Farm.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.animation.Timeline;

public class QuestController {

    @FXML private VBox  questContainer;
    @FXML private Label timerLabel;

    private Farms    farms;
    private Runnable onUpdate;
    private Timeline liveTimer;

    private ImageView cropIcon(String itemName, int size) {
        String key = itemName.replace("_Crop", "").replace("_Seed", "").toLowerCase();
        String filename = switch (key) {
            case "wheat"      -> "BléIC.png";
            case "carrot"     -> "CarotteIC.png";
            case "potato"     -> "PatateIC.png";
            case "tomato"     -> "TomateIC.png";
            case "lemon"      -> "CitronIC.png";
            case "strawberry" -> "FraiseIC.png";
            case "corn"       -> "MaisIC.png";
            case "pineapple"  -> "ananasIC.png";
            case "egg"        -> "EggIC.png";
            case "wool"       -> "WoolIC.png";
            case "milk"       -> "MilkIC.png";
            case "truff"      -> "TruffIC.png";
            default           -> null;
        };
        if (filename == null) return null;
        java.io.InputStream is = getClass().getResourceAsStream("/Sprite/images/Icons/" + filename);
        if (is == null) return null;
        ImageView iv = new ImageView(new Image(is));
        iv.setFitWidth(size); iv.setFitHeight(size); iv.setPreserveRatio(true);
        return iv;
    }

    private String cropDisplayName(String itemName) {
        return switch (itemName.replace("_Crop", "").replace("_Seed", "")) {
            case "Wheat"      -> "Blé";
            case "Carrot"     -> "Carotte";
            case "Potato"     -> "Patate";
            case "Tomato"     -> "Tomate";
            case "Lemon"      -> "Citron";
            case "Strawberry" -> "Fraise";
            case "Corn"       -> "Maïs";
            case "Pineapple"  -> "Ananas";
            case "Egg"        -> "Oeuf";
            case "Wool"       -> "Laine";
            case "Milk"       -> "Lait";
            case "Truff"      -> "Truffe";
            default           -> itemName.replace("_Crop", "");
        };
    }

    private void refreshUI() {
        questContainer.getChildren().clear();
        long now = System.currentTimeMillis();

        if (now < farms.getNextQuestTime()) {
            Label w = new Label("⏳  Nouvelles quêtes bientôt disponibles...");
            w.getStyleClass().add("quest-empty-label");
            questContainer.getChildren().add(w);
            return;
        }

        if (farms.getActiveQuests().isEmpty()) farms.generalQuests();

        if (farms.getActiveQuests().isEmpty()) {
            Label e = new Label("Aucune quête active.");
            e.getStyleClass().add("quest-empty-label");
            questContainer.getChildren().add(e);
            return;
        }

        for (Quest q : farms.getActiveQuests()) {
            questContainer.getChildren().add(buildQuestCard(q));
        }
    }

    private VBox buildQuestCard(Quest q) {
        String displayName = cropDisplayName(q.getTargetItem());
        int inStock        = farms.getInventory().getQuantity(q.getTargetItem());
        boolean canDeliver = inStock >= q.getAmountNeeded();

        VBox card = new VBox();
        card.getStyleClass().add("quest-card");

        HBox cardHeader = new HBox(8);
        cardHeader.getStyleClass().add("quest-card-header");
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        Label questTag = new Label("📜  COMMANDE");
        questTag.getStyleClass().add("quest-card-title");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Label stockBadge = new Label("En stock : " + inStock);
        stockBadge.setStyle(
                "-fx-font-family:'Courier New'; -fx-font-size:11px; -fx-font-weight:bold; " +
                        (canDeliver ? "-fx-text-fill:#88ff66;" : "-fx-text-fill:#ff8888;")
        );
        cardHeader.getChildren().addAll(questTag, spacer, stockBadge);
        card.getChildren().add(cardHeader);

        VBox body = new VBox(10);
        body.getStyleClass().add("quest-card-body");

        HBox itemRow = new HBox(10);
        itemRow.setAlignment(Pos.CENTER_LEFT);
        ImageView icon = cropIcon(q.getTargetItem(), 36);
        if (icon != null) itemRow.getChildren().add(icon);
        else { Label fb = new Label("🌾"); fb.setStyle("-fx-font-size:28px;"); itemRow.getChildren().add(fb); }

        VBox nameBox = new VBox(2);
        Label nameLabel = new Label(displayName); nameLabel.getStyleClass().add("quest-item-name");
        Label amtLabel  = new Label("× " + q.getAmountNeeded() + " demandés"); amtLabel.getStyleClass().add("quest-amount");
        nameBox.getChildren().addAll(nameLabel, amtLabel);
        itemRow.getChildren().add(nameBox);
        body.getChildren().add(itemRow);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:#c8a96e;");
        body.getChildren().add(sep);

        HBox rewardRow = new HBox(16);
        rewardRow.getStyleClass().add("quest-rewards-box");
        rewardRow.setAlignment(Pos.CENTER_LEFT);
        Label rewardTitle = new Label("Récompenses : ");
        rewardTitle.setStyle("-fx-font-family:'Courier New'; -fx-font-size:11px; -fx-text-fill:#8b6914; -fx-font-weight:bold;");
        Label money = new Label("💰 " + (int) q.getRewardMoney() + " $"); money.getStyleClass().add("reward-money");
        Label xp    = new Label("⭐ " + q.getRewardXP() + " XP");          xp.getStyleClass().add("reward-xp");
        rewardRow.getChildren().addAll(rewardTitle, money, xp);
        body.getChildren().add(rewardRow);

        // Boutons
        HBox btnRow = new HBox(8);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        Button btnRefuse  = new Button("✖  Refuser");  btnRefuse.getStyleClass().add("btn-refuse");
        Button btnDeliver = new Button("✔  Livrer");   btnDeliver.getStyleClass().add("btn-deliver");
        if (!canDeliver) { btnDeliver.setDisable(true); btnDeliver.setOpacity(0.45); }
        btnRefuse.setOnAction(e  -> removeQuest(q));
        btnDeliver.setOnAction(e -> deliverQuest(q));
        btnRow.getChildren().addAll(btnRefuse, btnDeliver);
        body.getChildren().add(btnRow);

        card.getChildren().add(body);
        return card;
    }

    private void deliverQuest(Quest q) {
        if (farms.getInventory().getQuantity(q.getTargetItem()) >= q.getAmountNeeded()) {
            farms.getInventory().add(q.getTargetItem(), -q.getAmountNeeded());
            farms.winMoney(q.getRewardMoney());
            farms.addXP(q.getRewardXP());
            removeQuest(q);
        }
    }

    private void removeQuest(Quest q) {
        farms.getActiveQuests().remove(q);
        if (farms.getActiveQuests().isEmpty())
            farms.setNextQuestTime(System.currentTimeMillis() + (5 * 60 * 1000));
        refreshUI();
        onUpdate.run();
    }

    public void init(Farms farms, Runnable onUpdate) {
        this.farms    = farms;
        this.onUpdate = onUpdate;

        liveTimer = new Timeline(new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> {
            long now  = System.currentTimeMillis();
            long next = farms.getNextQuestTime();
            if (now < next) {
                long diff = (next - now) / 1000;
                timerLabel.setText(String.format("Nouvelles quêtes dans : %d:%02d", diff / 60, diff % 60));
                timerLabel.getStyleClass().setAll("timer-label");
            } else {
                timerLabel.setText("✔  Quêtes disponibles !");
                timerLabel.getStyleClass().setAll("timer-label-ok");
                if (farms.getActiveQuests().isEmpty()) refreshUI();
            }
        }));
        liveTimer.setCycleCount(Timeline.INDEFINITE);
        liveTimer.play();
        refreshUI();
    }

    @FXML
    protected void close() {
        if (liveTimer != null) liveTimer.stop();
        ((Stage) questContainer.getScene().getWindow()).close();
    }
}
