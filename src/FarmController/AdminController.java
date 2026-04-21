package FarmController;

import Farm.Farms;
import FarmEngine.AudioPaths;
import FarmEngine.I18n;
import FarmEngine.SoundManager;
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
    @FXML private javafx.scene.control.Button closeBtn;

    private Farms farms;
    private Runnable onCloseCallback;

    public void setFarms(Farms farms) {
        this.farms = farms;
        applyStaticTexts();
    }
    public void setOnCloseCallback(Runnable r) { this.onCloseCallback = r; }

    @FXML private void onAddMoney() {
        SoundManager.playSfx(AudioPaths.SFX_CLICK);
        try {
            double v = Double.parseDouble(moneyField.getText().trim());
            farms.winMoney(v);
            feedback(I18n.tr("admin.feedback.moneyAdded", (long) v, (long) farms.getMoney()));
        } catch (NumberFormatException e) { feedback(I18n.tr("admin.feedback.invalidAmount")); }
    }
    @FXML private void onMaxMoney() {
        SoundManager.playSfx(AudioPaths.SFX_CLICK);
        farms.winMoney(999_000_000 - farms.getMoney());
        feedback(I18n.tr("admin.feedback.moneyMax"));
    }

    @FXML private void onAddXP() {
        SoundManager.playSfx(AudioPaths.SFX_CLICK);
        try {
            double v = Double.parseDouble(xpField.getText().trim());
            farms.addXP(v);
            feedback(I18n.tr("admin.feedback.xpAdded", (long) v, farms.getLevel()));
        } catch (NumberFormatException e) { feedback(I18n.tr("admin.feedback.invalidXp")); }
    }
    @FXML private void onMaxLevel() {
        SoundManager.playSfx(AudioPaths.SFX_CLICK);
        while (farms.getLevel() < 99) farms.addXP(farms.getNextLevelXP() * 10);
        feedback(I18n.tr("admin.feedback.levelMax", farms.getLevel()));
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
        SoundManager.playSfx(AudioPaths.SFX_CLICK);
        String[] seeds = {"Wheat_Seed","Carrot_Seed","Potato_Seed","Tomato_Seed",
                          "Lemon_Seed","Strawberry_Seed","Corn_Seed","Pineapple_Seed"};
        for (String s : seeds) farms.getInventory().add(s, 50);
        feedback(I18n.tr("admin.feedback.allSeeds"));
    }

    @FXML private void onGiveEggs()  { give("Egg_Crop",  5); }
    @FXML private void onGiveMilk()  { give("Milk_Crop", 5); }
    @FXML private void onGiveWool()  { give("Wool_Crop", 5); }
    @FXML private void onGiveTruff() { give("Truff_Crop",5); }

    @FXML private void onWeatherSun()    { farms.setCurrentWeather(Farms.Weather.SUNNY);       feedback(I18n.tr("admin.feedback.weather.sunny")); }
    @FXML private void onWeatherRain()   { farms.setCurrentWeather(Farms.Weather.RAINY);       feedback(I18n.tr("admin.feedback.weather.rainy")); }
    @FXML private void onWeatherStorm()  { farms.setCurrentWeather(Farms.Weather.THUNDERSTORM);feedback(I18n.tr("admin.feedback.weather.storm")); }
    @FXML private void onWeatherDrought(){ farms.setCurrentWeather(Farms.Weather.DROUGHT);     feedback(I18n.tr("admin.feedback.weather.drought")); }

    @FXML private void onClearInventory() {
        SoundManager.playSfx(AudioPaths.SFX_CLICK);
        String[] all = {"Wheat","Tomato","Carrot","Potato","Lemon","Strawberry","Corn","Pineapple","Egg","Truff","Milk","Wool"};
        for (String t : all) {
            farms.getInventory().add(t+"_Seed", -farms.getInventory().getQuantity(t+"_Seed"));
            farms.getInventory().add(t+"_Crop", -farms.getInventory().getQuantity(t+"_Crop"));
        }
        feedback(I18n.tr("admin.feedback.inventoryCleared"));
    }
    @FXML private void onResetGame() {
        SoundManager.playSfx(AudioPaths.SFX_CLICK);
        farms.setMoney(20);
        farms.setLevel(1);
        farms.setCurrentXP(0);
        farms.setNextLevelXP(100);
        onClearInventory();
        feedback(I18n.tr("admin.feedback.reset"));
    }

    @FXML private void onClose() {
        SoundManager.playSfx(AudioPaths.SFX_CLICK);
        if (onCloseCallback != null) onCloseCallback.run();
        ((Stage) adminRoot.getScene().getWindow()).close();
    }

    private void give(String item, int qty) {
        SoundManager.playSfx(AudioPaths.SFX_CLICK);
        farms.getInventory().add(item, qty);
        feedback(I18n.tr("admin.feedback.give", qty, item.replace("_", " ")));
    }
    private void feedback(String msg) {
        if (feedbackLabel != null) feedbackLabel.setText(msg);
    }

    private void applyStaticTexts() {
        if (closeBtn != null) closeBtn.setText("✖ " + I18n.tr("admin.close"));
        if (moneyField != null) moneyField.setPromptText(I18n.tr("admin.prompt.money"));
        if (xpField != null) xpField.setPromptText(I18n.tr("admin.prompt.xp"));
    }
}
