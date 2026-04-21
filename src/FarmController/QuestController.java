package FarmController;

import Farm.*;
import FarmEngine.AudioPaths;
import FarmEngine.I18n;
import FarmEngine.SoundManager;
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
    @FXML private Label headerTitleLabel;
    @FXML private Label headerSubtitleLabel;
    @FXML private Button closeBtn;

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
        String base = itemName.replace("_Crop", "").replace("_Seed", "");
        String key = "quest.item." + base;
        String tr = I18n.tr(key);
        return tr.equals(key) ? base : tr;
    }

    private void refreshUI() {
        questContainer.getChildren().clear();
        long now = System.currentTimeMillis();

        if (now < farms.getNextQuestTime()) {
            Label w = new Label(I18n.tr("quest.waiting"));
            w.getStyleClass().add("quest-empty-label");
            questContainer.getChildren().add(w);
            return;
        }

        if (farms.getActiveQuests().isEmpty()) farms.generalQuests();

        if (farms.getActiveQuests().isEmpty()) {
            Label e = new Label(I18n.tr("quest.none"));
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
        Label questTag = new Label("📜  " + I18n.tr("quest.order"));
        questTag.getStyleClass().add("quest-card-title");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Label stockBadge = new Label(I18n.tr("quest.stock", inStock));
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
        Label amtLabel  = new Label("× " + q.getAmountNeeded() + " " + I18n.tr("quest.requested")); amtLabel.getStyleClass().add("quest-amount");
        nameBox.getChildren().addAll(nameLabel, amtLabel);
        itemRow.getChildren().add(nameBox);
        body.getChildren().add(itemRow);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:#c8a96e;");
        body.getChildren().add(sep);

        HBox rewardRow = new HBox(16);
        rewardRow.getStyleClass().add("quest-rewards-box");
        rewardRow.setAlignment(Pos.CENTER_LEFT);
        Label rewardTitle = new Label(I18n.tr("quest.rewards"));
        rewardTitle.setStyle("-fx-font-family:'Courier New'; -fx-font-size:11px; -fx-text-fill:#8b6914; -fx-font-weight:bold;");
        Label money = new Label("💰 " + (int) q.getRewardMoney() + " $"); money.getStyleClass().add("reward-money");
        Label xp    = new Label("⭐ " + q.getRewardXP() + " XP");          xp.getStyleClass().add("reward-xp");
        rewardRow.getChildren().addAll(rewardTitle, money, xp);
        body.getChildren().add(rewardRow);

        // Boutons
        HBox btnRow = new HBox(8);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        Button btnRefuse  = new Button("✖  " + I18n.tr("quest.refuse"));  btnRefuse.getStyleClass().add("btn-refuse");
        Button btnDeliver = new Button("✔  " + I18n.tr("quest.deliver"));   btnDeliver.getStyleClass().add("btn-deliver");
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
            SoundManager.playSfx(AudioPaths.SFX_SELL);
            farms.getInventory().add(q.getTargetItem(), -q.getAmountNeeded());
            if (q.getTargetItem().endsWith("_Crop")) {
                String base = q.getTargetItem().replace("_Crop", "");
                farms.consumeQualityBonus(base, q.getAmountNeeded());
            }
            farms.winMoney(q.getRewardMoney());
            farms.addXP(q.getRewardXP());
            removeQuest(q);
        }
    }

    private void removeQuest(Quest q) {
        SoundManager.playSfx(AudioPaths.SFX_CLICK);
        farms.getActiveQuests().remove(q);
        if (farms.getActiveQuests().isEmpty())
            farms.setNextQuestTime(System.currentTimeMillis() + (5 * 60 * 1000));
        refreshUI();
        onUpdate.run();
    }

    public void init(Farms farms, Runnable onUpdate) {
        this.farms    = farms;
        this.onUpdate = onUpdate;
        applyStaticTexts();

        liveTimer = new Timeline(new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> {
            long now  = System.currentTimeMillis();
            long next = farms.getNextQuestTime();
            if (now < next) {
                long diff = (next - now) / 1000;
                timerLabel.setText(I18n.tr("quest.available.in", diff / 60, diff % 60));
                timerLabel.getStyleClass().setAll("timer-label");
            } else {
                timerLabel.setText("✔  " + I18n.tr("quest.available.now"));
                timerLabel.getStyleClass().setAll("timer-label-ok");
                if (farms.getActiveQuests().isEmpty()) refreshUI();
            }
        }));
        liveTimer.setCycleCount(Timeline.INDEFINITE);
        liveTimer.play();
        refreshUI();
    }

    private void applyStaticTexts() {
        if (headerTitleLabel != null) headerTitleLabel.setText(I18n.tr("quest.header.title"));
        if (headerSubtitleLabel != null) headerSubtitleLabel.setText(I18n.tr("quest.header.subtitle"));
        if (closeBtn != null) closeBtn.setText("✖  " + I18n.tr("quest.close"));
        if (timerLabel != null) timerLabel.setText(I18n.tr("quest.loading"));
    }

    @FXML
    protected void close() {
        if (liveTimer != null) liveTimer.stop();
        ((Stage) questContainer.getScene().getWindow()).close();
    }
}
