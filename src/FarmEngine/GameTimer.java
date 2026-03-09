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

    public GameTimer(Farms farms, Runnable updateUI){
        this.farms = farms;
        this.updateUI = updateUI;

        this.timeline = new Timeline(new KeyFrame(Duration.millis(650), event -> {
            tick();
        }));
        this.timeline.setCycleCount(Timeline.INDEFINITE);
    }

    public void tick(){
        for (int i = 0; i < farms.getNbLINES(); i ++){
            for (int j = 0; j < farms.getNbCOLMUNS(); j++){
                Plot ground = farms.getField()[i][j];
                if (ground.getActualCulture() != null){
                    ground.getActualCulture().growing();
                }
            }
        }
        for (Animals animals : farms.getMyAnimals()){
            animals.update();
        }
        updateUI.run();
    }

    public void start() {timeline.play();}
    public void stop() {timeline.stop();}
}
