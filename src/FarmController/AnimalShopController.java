package FarmController;

import Farm.Animal.*;
import Farm.Animals;
import Farm.Farms;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class AnimalShopController {
    private Farms farms;
    private Runnable onUpdateCallback;

    @FXML private Label moneyLabel;
    @FXML private Label feedbackLabel;

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

    private void updateButtonState(Button btn, int requiredLevel) {
        if (btn != null) {
            boolean isLocked = farms.getLevel() < requiredLevel;
            btn.setDisable(isLocked);
            if (isLocked) {
                btn.setText("🔒 Niv. " + requiredLevel);
            }
        }
    }

    private void updateUI() {
        moneyLabel.setText("Argent : " + (int)farms.getMoney() + " $");

        updateButtonState(Cow_Btn, 12);
        updateButtonState(Sheep_Btn, 9);
        updateButtonState(Pig_Btn, 15);
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
        int requiredLevel = switch (a.getSpecies()) {
            case "Chicken" -> 5;
            case "Sheep" -> 9;
            case "Cow" -> 12;
            case "Pig" -> 15;
            default -> 1;
        };

        if (farms.getLevel() < requiredLevel) {
            setFeedback("🔒 Niveau " + requiredLevel + " requis !");
            return;
        }

        if (farms.getMoney() >= a.getBuyPrice()) {
            farms.spending(a.getBuyPrice());
            farms.addAnimals(a);
            updateUI();
            if (onUpdateCallback != null) onUpdateCallback.run();
            setFeedback("✅ " + a.getSpecies() + " acheté ! Placez-le dans un enclos.");
        }
        else {
            setFeedback("❌ Pas assez d'argent ! (" + (int)a.getBuyPrice() + " $ requis)");
        }
    }
}