package FarmEngine;

public final class GameBalance {
    private GameBalance() {}

    public static final int BARN_UNLOCK_LEVEL = 5;
    public static final boolean AUTOSAVE_ENABLED = true;
    public static final int AUTOSAVE_INTERVAL_SECONDS = 120;

    public static int getSeedUnlockLevel(String seedId) {
        return switch (seedId) {
            case "Wheat_Seed" -> 1;
            case "Carrot_Seed" -> 2;
            case "Potato_Seed" -> 3;
            case "Tomato_Seed" -> 4;
            case "Lemon_Seed" -> 5;
            case "Strawberry_Seed" -> 6;
            case "Corn_Seed" -> 8;
            case "Pineapple_Seed" -> 11;
            default -> 1;
        };
    }

    public static double getSeedPrice(String seedId) {
        return switch (seedId) {
            case "Wheat_Seed" -> 5.0;
            case "Carrot_Seed" -> 90.0;
            case "Potato_Seed" -> 360.0;
            case "Tomato_Seed" -> 1200.0;
            case "Lemon_Seed" -> 4500.0;
            case "Strawberry_Seed" -> 13000.0;
            case "Corn_Seed" -> 40000.0;
            case "Pineapple_Seed" -> 110000.0;
            default -> 0.0;
        };
    }

    public static double getSellBasePrice(String cropId) {
        return switch (cropId) {
            case "Egg_Crop" -> 300.0;
            case "Wool_Crop" -> 2200.0;
            case "Milk_Crop" -> 9000.0;
            case "Truff_Crop" -> 40000.0;
            case "Wheat_Crop" -> 15.0;
            case "Carrot_Crop" -> 220.0;
            case "Potato_Crop" -> 760.0;
            case "Tomato_Crop" -> 2200.0;
            case "Lemon_Crop" -> 7800.0;
            case "Strawberry_Crop" -> 24000.0;
            case "Corn_Crop" -> 72000.0;
            case "Pineapple_Crop" -> 240000.0;
            default -> 0.0;
        };
    }

    public static int getAnimalUnlockLevel(String species) {
        return switch (species) {
            case "Chicken" -> 5;
            case "Sheep" -> 7;
            case "Cow" -> 9;
            case "Pig" -> 11;
            default -> 1;
        };
    }
}
