package Farm.Animal;

import Farm.Animals;

public class Pig extends Animals {
    public Pig() {
        super("Pig", 1000000.0, 20);
    }

    @Override
    public String getProductType() {
        return "Truff";
    }

    @Override
    public String getFoodNeeded() {
        return "Potato_Crop";
    }
}