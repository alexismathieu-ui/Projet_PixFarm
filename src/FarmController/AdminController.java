package FarmController;

import Farm.Farms;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class AdminController {

    @FXML private BorderPane adminRoot;
    @FXML private TextField  moneyField;
    @FXML private TextField  xpField;
    @FXML private Label      feedbackLabel;

    private Farms farms;
    private Runnable onCloseCallback;

    public void setFarms(Farms farms) { this.farms = farms; }
    public void setOnCloseCallback(Runnable r) { this.onCloseCallback = r; }

    @FXML private void onAddMoney() {
        try {
            double v = Double.parseDouble(moneyField.getText().trim());
            farms.winMoney(v);
            feedback("✅ +" + (long)v + " $ ajoutés ! Total : " + (long)farms.getMoney() + " $");
        } catch (NumberFormatException e) { feedback("❌ Montant invalide."); }
    }
    @FXML private void onMaxMoney() {
        farms.winMoney(999_000_000 - farms.getMoney());
        feedback("✅ Argent max : 999 000 000 $");
    }

    @FXML private void onAddXP() {
        try {
            double v = Double.parseDouble(xpField.getText().trim());
            farms.addXP(v);
            feedback("✅ +" + (long)v + " XP ! Niveau actuel : " + farms.getLevel());
        } catch (NumberFormatException e) { feedback("❌ XP invalide."); }
    }
    @FXML private void onMaxLevel() {
        while (farms.getLevel() < 99) farms.addXP(farms.getNextLevelXP() * 10);
        feedback("✅ Niveau MAX atteint : " + farms.getLevel());
    }

    @FXML private void onGiveWheat()      { give("Wheat_Seed",      10); }
    @FXML private void onGiveCarrot()     { give("Carrot_Seed",     10); }
    @FXML private void onGivePotato()     { give("Potato_Seed",     10); }
    @FXML private void onGiveTomato()     { give("Tomato_Seed",     10); }
    @FXML private void onGiveLemon()      { give("Lemon_Seed",      10); }
    @FXML private void onGiveStrawberry() { give("Strawberry_Seed", 10); }
    @FXML private void onGiveCorn()       { give("Corn_Seed",       10); }
    @FXML private void onGivePineapple()  { give("Pineapple_Seed",  10); }
    @FXML private void onGiveAllSeeds() {
        String[] seeds = {"Wheat_Seed","Carrot_Seed","Potato_Seed","Tomato_Seed",
                          "Lemon_Seed","Strawberry_Seed","Corn_Seed","Pineapple_Seed"};
        for (String s : seeds) farms.getInventory().add(s, 50);
        feedback("✅ +50 de chaque graine !");
    }

    @FXML private void onGiveEggs()  { give("Egg_Crop",  5); }
    @FXML private void onGiveMilk()  { give("Milk_Crop", 5); }
    @FXML private void onGiveWool()  { give("Wool_Crop", 5); }
    @FXML private void onGiveTruff() { give("Truff_Crop",5); }

    @FXML private void onWeatherSun()    { farms.setCurrentWeather(Farms.Weather.SUNNY);       feedback("☀️ Météo : Soleil"); }
    @FXML private void onWeatherRain()   { farms.setCurrentWeather(Farms.Weather.RAINY);       feedback("🌧️ Météo : Pluie"); }
    @FXML private void onWeatherStorm()  { farms.setCurrentWeather(Farms.Weather.THUNDERSTORM);feedback("⚡ Météo : Orage"); }
    @FXML private void onWeatherDrought(){ farms.setCurrentWeather(Farms.Weather.DROUGHT);     feedback("🔥 Météo : Sécheresse"); }

    @FXML private void onClearInventory() {
        String[] all = {"Wheat","Tomato","Carrot","Potato","Lemon","Strawberry","Corn","Pineapple","Egg","Truff","Milk","Wool"};
        for (String t : all) {
            farms.getInventory().add(t+"_Seed", -farms.getInventory().getQuantity(t+"_Seed"));
            farms.getInventory().add(t+"_Crop", -farms.getInventory().getQuantity(t+"_Crop"));
        }
        feedback("🗑️ Inventaire vidé.");
    }
    @FXML private void onResetGame() {
        farms.setMoney(20);
        farms.setLevel(1);
        farms.setCurrentXP(0);
        farms.setNextLevelXP(100);
        onClearInventory();
        feedback("💀 Partie réinitialisée !");
    }

    @FXML private void onClose() {
        if (onCloseCallback != null) onCloseCallback.run();
        ((Stage) adminRoot.getScene().getWindow()).close();
    }

    private void give(String item, int qty) {
        farms.getInventory().add(item, qty);
        feedback("✅ +" + qty + " " + item.replace("_"," "));
    }
    private void feedback(String msg) {
        if (feedbackLabel != null) feedbackLabel.setText(msg);
    }
}
