package FarmController;

import Farm.Animal.*;
import Farm.Animals;
import Farm.Enclosure.Enclosure;
import Farm.Enclosure.EnclosureManager;
import Farm.Farms;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class EnclosureController {

    @FXML private BorderPane enclosureRoot;
    @FXML private Label enclosureTitleLabel;
    @FXML private Label capacityLabel;
    @FXML private GridPane animalGrid;
    @FXML private Label emptyLabel;
    @FXML private Label statusLabel;
    @FXML private ComboBox<String> animalComboBox;

    private Farms farms;
    private EnclosureManager enclosureManager;
    private Enclosure enclosure;
    private Runnable onClose;

    // Animals in farms but NOT yet in any enclosure
    private List<Animals> unassignedAnimals = new ArrayList<>();

    public void setData(Farms farms, EnclosureManager manager, Enclosure enclosure) {
        this.farms = farms;
        this.enclosureManager = manager;
        this.enclosure = enclosure;

        enclosureTitleLabel.setText("🏠 " + enclosure.getName());
        refreshUnassigned();
        refreshAll();
    }

    public void setOnClose(Runnable r) { this.onClose = r; }

    private void refreshUnassigned() {
        unassignedAnimals.clear();
        List<Animals> allAssigned = enclosureManager.getAllAnimals();
        for (Animals a : farms.getMyAnimals()) {
            if (!allAssigned.contains(a)) unassignedAnimals.add(a);
        }
        animalComboBox.getItems().clear();
        for (Animals a : unassignedAnimals) {
            animalComboBox.getItems().add(a.getSpecies() + " #" + (farms.getMyAnimals().indexOf(a) + 1));
        }
    }

    private void refreshAll() {
        refreshAnimalGrid();
        updateCapacityLabel();
        updateStatus("");
    }

    private void refreshAnimalGrid() {
        animalGrid.getChildren().clear();
        List<Animals> animals = enclosure.getAnimals();

        emptyLabel.setVisible(animals.isEmpty());

        int col = 0, row = 0;
        int maxCols = 3;

        for (Animals a : new ArrayList<>(animals)) {
            VBox card = buildAnimalCard(a);
            animalGrid.add(card, col, row);
            col++;
            if (col >= maxCols) { col = 0; row++; }
        }
    }

    private VBox buildAnimalCard(Animals a) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("animal-card");

        if (a.hasProduced()) card.getStyleClass().add("animal-card-ready");
        else if (a.isHungry()) card.getStyleClass().add("animal-card-hungry");

        Label emojiLabel = new Label(getAnimalEmoji(a.getSpecies()));
        emojiLabel.getStyleClass().add("animal-emoji");

        Label nameLabel = new Label(a.getSpecies());
        nameLabel.getStyleClass().add("animal-name");

        Label statusLbl = new Label(getAnimalStatusText(a));
        if (a.hasProduced()) statusLbl.getStyleClass().add("animal-status-ready");
        else if (a.isHungry()) statusLbl.getStyleClass().add("animal-status-hungry");
        else statusLbl.getStyleClass().add("animal-status-working");

        Label foodLabel = new Label("🌾 " + a.getFoodNeeded().replace("_", " "));
        foodLabel.setStyle("-fx-text-fill: #a89070; -fx-font-size: 10px; -fx-font-family: 'Courier New', monospace;");

        card.getChildren().addAll(emojiLabel, nameLabel, statusLbl, foodLabel);

        // Click interaction
        card.setOnMouseClicked(e -> handleAnimalClick(a));

        return card;
    }

    private void handleAnimalClick(Animals a) {
        if (a.hasProduced()) {
            farms.getInventory().add(a.getProductType() + "_Crop", 1);
            farms.addXP(50);
            a.setProduced(false);
            a.setHungry(true);
            updateStatus("✅ Récolté : " + a.getProductType() + " !");
        } else if (a.isHungry()) {
            String food = a.getFoodNeeded();
            if (farms.getInventory().getQuantity(food) > 0) {
                farms.getInventory().add(food, -1);
                farms.addXP(25);
                a.setHungry(false);
                updateStatus("🍽️ " + a.getSpecies() + " nourri !");
            } else {
                updateStatus("❌ Tu n'as pas de " + food.replace("_", " ") + " !");
            }
        } else {
            updateStatus("⚙️ " + a.getSpecies() + " est en train de travailler...");
        }
        refreshAnimalGrid();
    }

    @FXML
    private void onAddAnimalToEnclosure() {
        int idx = animalComboBox.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= unassignedAnimals.size()) {
            updateStatus("❌ Sélectionne un animal dans la liste !");
            return;
        }
        if (enclosure.isFull()) {
            updateStatus("❌ Enclos plein ! Capacité max : " + enclosure.getMaxCapacity());
            return;
        }
        Animals a = unassignedAnimals.get(idx);
        enclosure.addAnimal(a);
        updateStatus("✅ " + a.getSpecies() + " ajouté dans l'enclos !");
        refreshUnassigned();
        refreshAll();
    }

    @FXML
    private void onFeedAll() {
        int fed = 0;
        for (Animals a : enclosure.getAnimals()) {
            if (a.isHungry()) {
                String food = a.getFoodNeeded();
                if (farms.getInventory().getQuantity(food) > 0) {
                    farms.getInventory().add(food, -1);
                    farms.addXP(25);
                    a.setHungry(false);
                    fed++;
                }
            }
        }
        updateStatus(fed > 0 ? "🍽️ " + fed + " animal(aux) nourri(s) !" : "❌ Pas assez de nourriture !");
        refreshAnimalGrid();
    }

    @FXML
    private void onHarvestAll() {
        int harvested = 0;
        for (Animals a : enclosure.getAnimals()) {
            if (a.hasProduced()) {
                farms.getInventory().add(a.getProductType() + "_Crop", 1);
                farms.addXP(50);
                a.setProduced(false);
                a.setHungry(true);
                harvested++;
            }
        }
        updateStatus(harvested > 0 ? "✅ " + harvested + " produit(s) récolté(s) !" : "Rien à récolter.");
        refreshAnimalGrid();
    }

    @FXML
    private void onDeleteEnclosure() {
        if (!enclosure.isEmpty()) {
            updateStatus("❌ Retire tous les animaux avant de supprimer l'enclos !");
            return;
        }
        enclosureManager.removeEnclosure(enclosure.getId());
        updateStatus("🗑️ Enclos supprimé.");
        onClose();
    }

    @FXML
    private void onClose() {
        if (onClose != null) onClose.run();
        Stage stage = (Stage) enclosureRoot.getScene().getWindow();
        stage.close();
    }

    private void updateCapacityLabel() {
        capacityLabel.setText(enclosure.getAnimalCount() + " / " + enclosure.getMaxCapacity());
    }

    private void updateStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }

    private String getAnimalEmoji(String species) {
        return switch (species) {
            case "Chicken" -> "🐔";
            case "Cow"     -> "🐄";
            case "Sheep"   -> "🐑";
            case "Pig"     -> "🐷";
            default        -> "🐾";
        };
    }

    private String getAnimalStatusText(Animals a) {
        if (a.hasProduced()) return "✅ Cliquer pour récolter";
        if (a.isHungry())    return "🍽️ Cliquer pour nourrir";
        return "⚙️ En production...";
    }
}
