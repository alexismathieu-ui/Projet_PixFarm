package FarmController;

import Farm.Animal.*;
import Farm.Animals;
import Farm.Farms;
import FarmEngine.GameBalance;
import FarmEngine.I18n;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class AnimalShopController {
    private Farms farms;
    private Runnable onUpdateCallback;

    @FXML private Label moneyLabel;
    @FXML private Label feedbackLabel;

    @FXML private Button Chicken_Btn;
    @FXML private Button Sheep_Btn;
    @FXML private Button Cow_Btn;
    @FXML private Button Pig_Btn;

    public void setFarms(Farms farms) {
        this.farms = farms;
        updateUI();
    }

    public void setOnUpdateCallback(Runnable callback) {
        this.onUpdateCallback = callback;
    }

    private void updateButtonState(Button btn, int requiredLevel, String label, double price) {
        if (btn != null) {
            boolean isLocked = farms.getLevel() < requiredLevel;
            btn.setDisable(isLocked);
            if (isLocked) {
                btn.setText("🔒 " + I18n.tr("animalShop.locked", requiredLevel));
            } else {
                btn.setText(label + " — " + I18n.tr("animalShop.price", (int) price));
            }
        }
    }

    private void updateUI() {
        moneyLabel.setText(I18n.tr("animalShop.money", (int)farms.getMoney()));

        updateButtonState(Chicken_Btn, GameBalance.getAnimalUnlockLevel("Chicken"), "🐔  " + I18n.tr("animalShop.animal.chicken"), new Chicken().getBuyPrice());
        updateButtonState(Sheep_Btn, GameBalance.getAnimalUnlockLevel("Sheep"), "🐑  " + I18n.tr("animalShop.animal.sheep"), new Sheep().getBuyPrice());
        updateButtonState(Cow_Btn, GameBalance.getAnimalUnlockLevel("Cow"), "🐄  " + I18n.tr("animalShop.animal.cow"), new Cow().getBuyPrice());
        updateButtonState(Pig_Btn, GameBalance.getAnimalUnlockLevel("Pig"), "🐷  " + I18n.tr("animalShop.animal.pig"), new Pig().getBuyPrice());
    }

    private void setFeedback(String msg) {
        if (feedbackLabel != null) feedbackLabel.setText(msg);
    }

    @FXML
    private void buyChicken() { buyAnimal(new Chicken()); }
    @FXML
    private void buyCow() { buyAnimal(new Cow()); }
    @FXML
    private void buySheep() { buyAnimal(new Sheep()); }
    @FXML
    private void buyPig() { buyAnimal(new Pig()); }

    private void buyAnimal(Animals a) {
        int requiredLevel = GameBalance.getAnimalUnlockLevel(a.getSpecies());

        if (farms.getLevel() < requiredLevel) {
            setFeedback("🔒 " + I18n.tr("animalShop.feedback.levelRequired", requiredLevel));
            return;
        }

        if (farms.getMoney() >= a.getBuyPrice()) {
            farms.spending(a.getBuyPrice());
            farms.addAnimals(a);
            updateUI();
            if (onUpdateCallback != null) onUpdateCallback.run();
            setFeedback(I18n.tr("animalShop.feedback.bought", getSpeciesName(a.getSpecies())));
        }
        else {
            setFeedback(I18n.tr("animalShop.feedback.notEnoughMoney", (int)a.getBuyPrice()));
        }
    }

    private String getSpeciesName(String species) {
        return switch (species) {
            case "Chicken" -> I18n.tr("animalShop.animal.chicken");
            case "Sheep" -> I18n.tr("animalShop.animal.sheep");
            case "Cow" -> I18n.tr("animalShop.animal.cow");
            case "Pig" -> I18n.tr("animalShop.animal.pig");
            default -> species;
        };
    }
}