package FarmController;

import Farm.Animal.*;
import Farm.Animals;
import Farm.Farms;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AnimalShopController {
    private Farms farms;
    private Runnable onUpdateCallback;

    @FXML private Label moneyLabel;

    public void setFarms(Farms farms) {
        this.farms = farms;
        updateUI();
    }

    public void setOnUpdateCallback(Runnable callback) {
        this.onUpdateCallback = callback;
    }

    private void updateUI() {
        moneyLabel.setText("Argent : " + (int)farms.getMoney() + " $");
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
        if (farms.getMoney() >= a.getBuyPrice()) {
            farms.spending(a.getBuyPrice());
            farms.addAnimals(a);
            updateUI();
            if (onUpdateCallback != null) onUpdateCallback.run();
            System.out.println(a.getSpecies() + " acheté !");
        } else {
            System.out.println("Pas assez d'argent !");
        }
    }
}