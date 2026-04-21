package FarmEngine;

import Farm.Animals;
import Farm.Farms;
import Farm.Plot;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class GameTimer {
    private Timeline timeline;
    private Farms farms;
    private Runnable updateUI;
    private Timeline oneminrefr;
    private Timeline marketTimer;
    private Timeline playTimeTimer;

    public GameTimer(Farms farms, Runnable updateUI){
        this.farms = farms;
        this.updateUI = updateUI;

        this.timeline = new Timeline(new KeyFrame(Duration.millis(700), event -> {
            tick();
        }));
        this.timeline.setCycleCount(Timeline.INDEFINITE);

        this.oneminrefr = new Timeline(new KeyFrame(Duration.seconds(60),event -> {
            farms.updateWeather();
            farms.refreshSeasonIfNeeded();
            updateUI.run();
        }));
        oneminrefr.setCycleCount(Timeline.INDEFINITE);


        this.marketTimer = new Timeline(new KeyFrame(Duration.seconds(30), event -> {
            farms.updateMarketFluctuation();
            updateUI.run();
        }));
        this.marketTimer.setCycleCount(Timeline.INDEFINITE);

        this.playTimeTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> farms.addPlayTimeSeconds(1)));
        this.playTimeTimer.setCycleCount(Timeline.INDEFINITE);

    }

    public void tick(){
        double weatherMultiplier = switch (farms.getCurrentWeather()) {
            case SUNNY -> 1.0;
            case RAINY -> 1.5;
            case THUNDERSTORM -> 2.0;
            case DROUGHT -> 0.5;
            case BLESSING_RAIN -> 1.8;
            case MIST -> 0.85;
        };
        for (int i = 0; i < farms.getNbLINES(); i ++){
            for (int j = 0; j < farms.getNbCOLMUNS(); j++){
                Plot ground = farms.getField()[i][j];
                if (ground.getActualCulture() != null){
                    double fertilityFactor = ground.getFertility();
                    double seasonMultiplier = farms.getSeasonCropMultiplier(ground.getActualCulture().getName());
                    ground.getActualCulture().growing(weatherMultiplier * fertilityFactor * seasonMultiplier);
                }
            }
        }
        for (Animals animals : farms.getMyAnimals()){
            animals.update();
        }
        updateUI.run();
    }

    public void start() {
        timeline.play();
        oneminrefr.play();
        marketTimer.play();
        playTimeTimer.play();
    }
    public void stop() {
        timeline.stop();
        oneminrefr.stop();
        marketTimer.stop();
        playTimeTimer.stop();
    }
}