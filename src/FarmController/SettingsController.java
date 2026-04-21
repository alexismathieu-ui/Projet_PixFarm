package FarmController;

import FarmEngine.GameSettings;
import FarmEngine.I18n;
import FarmEngine.SoundManager;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.stage.Stage;

public class SettingsController {
    @FXML private Slider autosaveSlider;
    @FXML private Slider volumeSlider;
    @FXML private ComboBox<String> languageCombo;
    @FXML private Label autosaveValueLabel;
    @FXML private Label volumeValueLabel;
    @FXML private Label headerLabel;
    @FXML private Label autosaveLabel;
    @FXML private Label volumeLabel;
    @FXML private Label languageLabel;
    @FXML private javafx.scene.control.Button cancelBtn;
    @FXML private javafx.scene.control.Button applyBtn;

    private Runnable onApply;

    @FXML
    public void initialize() {
        languageCombo.getItems().setAll("FR", "EN");
        autosaveSlider.setValue(GameSettings.getAutosaveIntervalSeconds());
        volumeSlider.setValue(GameSettings.getVolume() * 100.0);
        languageCombo.setValue(GameSettings.getLanguage());
        applyStaticTexts();
        refreshLabels();

        autosaveSlider.valueProperty().addListener((obs, oldVal, newVal) -> refreshLabels());
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> refreshLabels());
    }

    public void setOnApply(Runnable onApply) {
        this.onApply = onApply;
    }

    @FXML
    private void onApplySettings() {
        GameSettings.setAutosaveIntervalSeconds((int) autosaveSlider.getValue());
        GameSettings.setVolume(volumeSlider.getValue() / 100.0);
        GameSettings.setLanguage(languageCombo.getValue());
        GameSettings.save();
        SoundManager.updateVolume();
        if (onApply != null) onApply.run();
        close();
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void refreshLabels() {
        autosaveValueLabel.setText((int) autosaveSlider.getValue() + " " + I18n.tr("settings.seconds"));
        volumeValueLabel.setText((int) volumeSlider.getValue() + "%");
    }

    private void applyStaticTexts() {
        headerLabel.setText("⚙ " + I18n.tr("settings.header"));
        autosaveLabel.setText(I18n.tr("settings.autosave"));
        volumeLabel.setText(I18n.tr("settings.volume"));
        languageLabel.setText(I18n.tr("settings.language"));
        cancelBtn.setText(I18n.tr("settings.cancel"));
        applyBtn.setText(I18n.tr("settings.apply"));
    }

    private void close() {
        Stage stage = (Stage) autosaveSlider.getScene().getWindow();
        stage.close();
    }
}
