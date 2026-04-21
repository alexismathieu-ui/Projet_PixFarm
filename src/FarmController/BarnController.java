package FarmController;

import Farm.Animals;
import Farm.Enclosure.Enclosure;
import Farm.Enclosure.EnclosureManager;
import Farm.Farms;
import FarmEngine.AudioPaths;
import FarmEngine.I18n;
import FarmEngine.SoundManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class BarnController {

    @FXML private BorderPane barnRoot;
    @FXML private GridPane enclosureGrid;
    @FXML private Label levelLabel;
    @FXML private ProgressBar xpBar;
    @FXML private Label xpLabel;
    @FXML private Label animalCountLabel;
    @FXML private Label readyCountLabel;
    @FXML private Label hungryCountLabel;
    @FXML private Label enclosureCountLabel;
    @FXML private Label labelStatus;
    @FXML private Label bottomStatus;

    private Farms farms;
    private EnclosureManager enclosureManager;
    private InventoryController currentInventoryCtrl;

    public void setFarms(Farms farms) {
        this.farms = farms;
        SoundManager.playMusic(AudioPaths.MUSIC_RANCH);

        // Init enclosure manager from Farms (or create new)
        if (farms.getEnclosureManager() == null) {
            this.enclosureManager = new EnclosureManager();
            farms.setEnclosureManager(this.enclosureManager);
        } else {
            this.enclosureManager = farms.getEnclosureManager();
        }

        // Auto-refresh every 700ms
        Timeline autoRefresh = new Timeline(new KeyFrame(Duration.millis(700), e -> {
            updateLevelUI();
            refreshEnclosureGrid();
            updateStats();
        }));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();

        refreshEnclosureGrid();
        updateStats();
        updateLevelUI();
    }

    private void updateLevelUI() {
        if (levelLabel != null) {
            levelLabel.setText(I18n.tr("barn.level", farms.getLevel()));
            double progress = farms.getCurrentXP() / farms.getNextLevelXP();
            xpBar.setProgress(progress);
            if (xpLabel != null)
                xpLabel.setText(I18n.tr("barn.xp", (int)farms.getCurrentXP(), (int)farms.getNextLevelXP()));
        }
    }

    private void updateStats() {
        int total = enclosureManager.getTotalAnimals();
        int ready = enclosureManager.getTotalReady();
        int hungry = enclosureManager.getTotalHungry();
        int enclCount = enclosureManager.getEnclosures().size();
        int maxEncl = enclosureManager.getMaxEnclosures();

        animalCountLabel.setText(I18n.tr("barn.stats.animals", total));
        readyCountLabel.setText(I18n.tr("barn.stats.ready", ready));
        hungryCountLabel.setText(I18n.tr("barn.stats.hungry", hungry));
        enclosureCountLabel.setText(I18n.tr("barn.stats.enclosures", enclCount, maxEncl));
        if (bottomStatus != null && total > 0) {
            double avgHealth = farms.getMyAnimals().stream().mapToDouble(Animals::getHealth).average().orElse(100.0);
            double avgHappiness = farms.getMyAnimals().stream().mapToDouble(Animals::getHappiness).average().orElse(100.0);
            bottomStatus.setText(I18n.tr("enclosure.animal.stats", (int) avgHealth, (int) avgHappiness));
        }
    }

    private void refreshEnclosureGrid() {
        enclosureGrid.getChildren().clear();
        int col = 0, row = 0;
        int maxCols = 3;

        for (Enclosure encl : enclosureManager.getEnclosures()) {
            VBox card = buildEnclosureCard(encl);
            enclosureGrid.add(card, col, row);
            col++;
            if (col >= maxCols) { col = 0; row++; }
        }
    }

    private VBox buildEnclosureCard(Enclosure encl) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("enclosure-card");

        // Choose card style based on status
        if (encl.isEmpty()) {
            card.getStyleClass().add("enclosure-card-empty");
        } else if (encl.getReadyCount() > 0) {
            card.getStyleClass().add("enclosure-card-ready");
        } else if (encl.getHungryCount() > 0) {
            card.getStyleClass().add("enclosure-card-hungry");
        }

        // Name
        Label nameLabel = new Label(encl.getName());
        nameLabel.getStyleClass().add("enclosure-name");

        // Big emoji
        Label emojiLabel = new Label(getEnclosureEmoji(encl));
        emojiLabel.getStyleClass().add("enclosure-emoji");

        // Capacity bar
        ProgressBar capBar = new ProgressBar(encl.isEmpty() ? 0 : (double) encl.getAnimalCount() / encl.getMaxCapacity());
        capBar.getStyleClass().add("capacity-bar");

        // Count
        Label countLabel = new Label(I18n.tr("barn.enclosure.count", encl.getAnimalCount(), encl.getMaxCapacity()));
        countLabel.getStyleClass().add("enclosure-count");

        // Status
        Label statusLabel = new Label(getEnclosureStatus(encl));
        if (encl.isEmpty()) statusLabel.getStyleClass().add("enclosure-status-empty");
        else if (encl.getReadyCount() > 0) statusLabel.getStyleClass().add("enclosure-status-ready");
        else if (encl.getHungryCount() > 0) statusLabel.getStyleClass().add("enclosure-status-hungry");
        else statusLabel.getStyleClass().add("enclosure-status-working");

        card.getChildren().addAll(nameLabel, emojiLabel, capBar, countLabel, statusLabel);

        // Click → open enclosure detail
        card.setOnMouseClicked(e -> openEnclosureDetail(encl));

        return card;
    }

    private String getEnclosureEmoji(Enclosure encl) {
        if (encl.isEmpty()) return "🌿";
        String species = encl.getDominantSpecies();
        return switch (species) {
            case "Chicken" -> "🐔";
            case "Cow"     -> "🐄";
            case "Sheep"   -> "🐑";
            case "Pig"     -> "🐷";
            default        -> "🐾";
        };
    }

    private String getEnclosureStatus(Enclosure encl) {
        if (encl.isEmpty()) return I18n.tr("barn.enclosure.status.empty");
        if (encl.getReadyCount() > 0) return I18n.tr("barn.enclosure.status.ready", encl.getReadyCount());
        if (encl.getHungryCount() > 0) return I18n.tr("barn.enclosure.status.hungry", encl.getHungryCount());
        return I18n.tr("barn.enclosure.status.working");
    }

    private void openEnclosureDetail(Enclosure encl) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/EnclosureView.fxml"));
            Parent root = loader.load();
            EnclosureController ctrl = loader.getController();
            ctrl.setData(farms, enclosureManager, encl);
            ctrl.setOnClose(() -> {
                refreshEnclosureGrid();
                updateStats();
            });

            Stage stage = new Stage();
            stage.setTitle(I18n.tr("barn.enclosure.title", encl.getName()));
            stage.initOwner(barnRoot.getScene().getWindow());
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onAddEnclosure() {
        if (!enclosureManager.canAddMoreEnclosures()) {
            setStatus(I18n.tr("barn.status.maxEnclosures"));
            return;
        }
        int n = enclosureManager.getEnclosures().size() + 1;
        enclosureManager.addEnclosure(I18n.tr("barn.enclosure.name", n), 4);
        refreshEnclosureGrid();
        updateStats();
        setStatus(I18n.tr("barn.status.enclosureCreated", n));
    }

    @FXML
    private void goToFarm() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/MainView.fxml"));
        Parent root = loader.load();
        MainController mainCtrl = loader.getController();
        mainCtrl.setFarms(this.farms);
        SoundManager.playMusic(AudioPaths.MUSIC_GAME);
        Stage stage = (Stage) barnRoot.getScene().getWindow();
        stage.getScene().setRoot(root);
    }

    @FXML
    private void onOpenAnimalShop() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/AnimalShopView.fxml"));
            Parent root = loader.load();
            AnimalShopController ctrl = loader.getController();
            ctrl.setFarms(this.farms);
            ctrl.setOnUpdateCallback(() -> {
                refreshEnclosureGrid();
                updateStats();
            });
            Stage stage = new Stage();
            stage.setTitle(I18n.tr("animalShop.title"));
            stage.initOwner(barnRoot.getScene().getWindow());
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onOpenInventory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/InventoryView.fxml"));
            Parent root = loader.load();
            currentInventoryCtrl = loader.getController();
            currentInventoryCtrl.update(this.farms);
            Stage stage = new Stage();
            stage.setTitle(I18n.tr("inventory.title"));
            stage.setScene(new Scene(root));
            stage.setOnCloseRequest(e -> currentInventoryCtrl = null);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setStatus(String msg) {
        if (labelStatus != null) labelStatus.setText(msg);
        if (bottomStatus != null) bottomStatus.setText(msg);
    }
}
