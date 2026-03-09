package Farm.Animal;

import Farm.Animals;

public class Sheep extends Animals {
    public Sheep() {
        super("Sheep", 50000.0, 10);
    }

    @Override
    public String getProductType() {
        return "Wool";
    }

    @Override
    public String getFoodNeeded() {
        return "Carrot_Crop";
    }
}