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

    public GameTimer(Farms farms, Runnable updateUI){
        this.farms = farms;
        this.updateUI = updateUI;

        this.timeline = new Timeline(new KeyFrame(Duration.millis(650), event -> {
            tick();
        }));
        this.timeline.setCycleCount(Timeline.INDEFINITE);

        this.oneminrefr = new Timeline(new KeyFrame(Duration.seconds(60),event -> {
            farms.updateWeather();
        }));
        oneminrefr.setCycleCount(Timeline.INDEFINITE);


        this.marketTimer = new Timeline(new KeyFrame(Duration.seconds(30), event -> {
            farms.updateMarketFluctuation();
            updateUI.run();
        }));
        this.marketTimer.setCycleCount(Timeline.INDEFINITE);

    }

    public void tick(){
        double multiplier = switch (farms.getCurrentWeather()) {
            case SUNNY -> 1.0;
            case RAINY -> 1.5;
            case THUNDERSTORM -> 2.0;
            case DROUGHT -> 0.5;
        };
        for (int i = 0; i < farms.getNbLINES(); i ++){
            for (int j = 0; j < farms.getNbCOLMUNS(); j++){
                Plot ground = farms.getField()[i][j];
                if (ground.getActualCulture() != null){
                    ground.getActualCulture().growing(multiplier);
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
    }
    public void stop() {
        timeline.stop();
        oneminrefr.stop();
        marketTimer.stop();
    }
}
