package Farm.Animal;

import Farm.Animals;

public class Chicken extends Animals {
    public Chicken() {
        super("Chicken", 900.0, 4);
    }

    @Override
    public String getProductType() {
        return "Egg";
    }

    @Override
    public String getFoodNeeded() {
        return "Wheat_Seed";
    }
}