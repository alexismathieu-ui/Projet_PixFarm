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
            case "Tomato_Seed" -> 5;
            case "Lemon_Seed" -> 6;
            case "Strawberry_Seed" -> 7;
            case "Corn_Seed" -> 10;
            case "Pineapple_Seed" -> 15;
            default -> 1;
        };
    }

    public static double getSeedPrice(String seedId) {
        return switch (seedId) {
            case "Wheat_Seed" -> 5.0;
            case "Carrot_Seed" -> 180.0;
            case "Potato_Seed" -> 850.0;
            case "Tomato_Seed" -> 3500.0;
            case "Lemon_Seed" -> 15000.0;
            case "Strawberry_Seed" -> 60000.0;
            case "Corn_Seed" -> 250000.0;
            case "Pineapple_Seed" -> 950000.0;
            default -> 0.0;
        };
    }

    public static double getSellBasePrice(String cropId) {
        return switch (cropId) {
            case "Egg_Crop" -> 500.0;
            case "Wool_Crop" -> 3000.0;
            case "Milk_Crop" -> 20000.0;
            case "Truff_Crop" -> 150000.0;
            case "Wheat_Crop" -> 15.0;
            case "Carrot_Crop" -> 400.0;
            case "Potato_Crop" -> 2100.0;
            case "Tomato_Crop" -> 6500.0;
            case "Lemon_Crop" -> 24000.0;
            case "Strawberry_Crop" -> 150000.0;
            case "Corn_Crop" -> 575000.0;
            case "Pineapple_Crop" -> 3000000.0;
            default -> 0.0;
        };
    }

    public static int getAnimalUnlockLevel(String species) {
        return switch (species) {
            case "Chicken" -> 5;
            case "Sheep" -> 9;
            case "Cow" -> 12;
            case "Pig" -> 15;
            default -> 1;
        };
    }
}
