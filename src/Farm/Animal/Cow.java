package Farm.Animal;

import Farm.Animals;

public class Cow extends Animals {
    public Cow() {
        super("Cow", 22000.0, 10);
    }

    @Override
    public String getProductType() {
        return "Milk";
    }

    @Override
    public String getFoodNeeded() {
        return "Wheat_Crop";
    }
}