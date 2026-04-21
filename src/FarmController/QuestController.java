package FarmController;

import Farm.Farms;
import Farm.Quest;
import FarmEngine.AudioPaths;
import FarmEngine.I18n;
import FarmEngine.SoundManager;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class QuestController {
    @FXML private VBox narrativeContainer;
    @FXML private VBox guildContainer;
    @FXML private VBox talentContainer;
    @FXML private Label timerLabel;
    @FXML private Label talentPointsLabel;
    @FXML private Label headerTitleLabel;
    @FXML private Label headerSubtitleLabel;
    @FXML private Button closeBtn;

    private Farms farms;
    private Runnable onUpdate;
    private Timeline liveTimer;

    private ImageView cropIcon(String itemName, int size) {
        String key = itemName.replace("_Crop", "").replace("_Seed", "").toLowerCase();
        String filename = switch (key) {
            case "wheat" -> "BléIC.png";
            case "carrot" -> "CarotteIC.png";
            case "potato" -> "PatateIC.png";
            case "tomato" -> "TomateIC.png";
            case "lemon" -> "CitronIC.png";
            case "strawberry" -> "FraiseIC.png";
            case "corn" -> "MaisIC.png";
            case "pineapple" -> "ananasIC.png";
            case "egg" -> "EggIC.png";
            case "wool" -> "WoolIC.png";
            case "milk" -> "MilkIC.png";
            case "truff" -> "TruffIC.png";
            default -> null;
        };
        if (filename == null) return null;
        java.io.InputStream is = getClass().getResourceAsStream("/Sprite/images/Icons/" + filename);
        if (is == null) return null;
        ImageView iv = new ImageView(new Image(is));
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        iv.setPreserveRatio(true);
        return iv;
    }

    private String cropDisplayName(String itemName) {
        String base = itemName.replace("_Crop", "").replace("_Seed", "");
        String key = "quest.item." + base;
        String tr = I18n.tr(key);
        return tr.equals(key) ? base : tr;
    }

    private void refreshUI() {
        narrativeContainer.getChildren().clear();
        guildContainer.getChildren().clear();
        talentContainer.getChildren().clear();

        if (System.currentTimeMillis() >= farms.getNextQuestTime()) {
            if (farms.getActiveQuests().isEmpty()) farms.generalQuests();
            for (Quest q : farms.getActiveQuests()) {
                narrativeContainer.getChildren().add(buildQuestCard(q.getTargetItem(), q.getAmountNeeded(), q.getRewardMoney(), q.getRewardXP(), false, () -> deliverClassic(q)));
            }
        } else {
            narrativeContainer.getChildren().add(new Label(I18n.tr("quest.waiting")));
        }

        for (Farms.NarrativeQuest nq : farms.getNarrativeQuests()) {
            if (nq.chapterIndex == farms.getNarrativeChapter() && !farms.getCompletedNarrativeQuests().contains(nq.id)) {
                guildContainer.getChildren().add(buildQuestCard(nq.targetItem, nq.amountNeeded, nq.rewardMoney, nq.rewardXP, false, () -> {
                    if (deliver(nq.targetItem, nq.amountNeeded, nq.rewardMoney, nq.rewardXP)) {
                        farms.getCompletedNarrativeQuests().add(nq.id);
                        farms.setNarrativeChapter(farms.getNarrativeChapter() + 1);
                        refreshUI();
                    }
                }));
            }
        }

        farms.refreshGuildWeekIfNeeded();
        for (Farms.GuildQuest gq : farms.getWeeklyGuildQuests()) {
            guildContainer.getChildren().add(buildQuestCard(gq.targetItem, gq.amountNeeded, gq.rewardMoney, gq.rewardXP, gq.claimed, () -> {
                if (!gq.claimed && deliver(gq.targetItem, gq.amountNeeded, gq.rewardMoney, gq.rewardXP)) {
                    gq.claimed = true;
                    refreshUI();
                }
            }));
        }
        refreshTalents();
    }

    private void refreshTalents() {
        talentPointsLabel.setText(I18n.tr("talent.points", farms.getTalentPoints()));
        addTalent("talent.farm", "farm_yield");
        addTalent("talent.market", "market_margin");
        addTalent("talent.livestock", "livestock_care");
    }

    private void addTalent(String labelKey, String key) {
        int rank = farms.getTalentRanks().getOrDefault(key, 0);
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label(I18n.tr(labelKey) + " [" + rank + "/3]");
        Button btn = new Button(I18n.tr("talent.unlock"));
        btn.setDisable(rank >= 3 || farms.getTalentPoints() <= 0);
        btn.setOnAction(e -> {
            if (farms.unlockTalent(key)) {
                onUpdate.run();
                refreshUI();
            }
        });
        row.getChildren().addAll(label, btn);
        talentContainer.getChildren().add(row);
    }

    private VBox buildQuestCard(String item, int amount, double rewardMoney, int rewardXP, boolean claimed, Runnable onDeliver) {
        int stock = farms.getInventory().getQuantity(item);
        boolean canDeliver = stock >= amount && !claimed;
        VBox card = new VBox(8);
        card.getStyleClass().add("quest-card");
        HBox head = new HBox(8);
        head.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("📜 " + cropDisplayName(item) + " x" + amount);
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        head.getChildren().addAll(title, spacer, new Label(I18n.tr("quest.stock", stock)));
        HBox rewards = new HBox(8, new Label("💰 " + (int) rewardMoney + " $"), new Label("⭐ " + rewardXP + " XP"));
        Button deliverBtn = new Button(claimed ? I18n.tr("quest.claimed") : I18n.tr("quest.deliver"));
        deliverBtn.setDisable(!canDeliver);
        deliverBtn.setOnAction(e -> onDeliver.run());
        card.getChildren().addAll(head, rewards, deliverBtn);
        return card;
    }

    private void deliverClassic(Quest q) {
        if (deliver(q.getTargetItem(), q.getAmountNeeded(), q.getRewardMoney(), q.getRewardXP())) {
            farms.getActiveQuests().remove(q);
            if (farms.getActiveQuests().isEmpty()) farms.setNextQuestTime(System.currentTimeMillis() + (5 * 60 * 1000));
            refreshUI();
        }
    }

    private boolean deliver(String item, int amount, double rewardMoney, int rewardXP) {
        if (farms.getInventory().getQuantity(item) < amount) return false;
        SoundManager.playSfx(AudioPaths.SFX_SELL);
        farms.getInventory().add(item, -amount);
        if (item.endsWith("_Crop")) farms.consumeQualityBonus(item.replace("_Crop", ""), amount);
        farms.winMoney(rewardMoney);
        farms.addXP(rewardXP);
        onUpdate.run();
        return true;
    }

    public void init(Farms farms, Runnable onUpdate) {
        this.farms = farms;
        this.onUpdate = onUpdate;
        applyStaticTexts();
        liveTimer = new Timeline(new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> timerLabel.setText(I18n.tr("quest.available.now"))));
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
        ((Stage) narrativeContainer.getScene().getWindow()).close();
    }
}
