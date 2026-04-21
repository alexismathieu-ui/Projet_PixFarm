package Farm;

import Farm.Enclosure.EnclosureManager;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Farms {
    private double money;
    private Plot[][] field;
    private ArrayList<Animals> myAnimals = new ArrayList<>();
    private final int LINES = 7;
    private final int COLUMNS = 10;
    private Inventory inventory;
    private int level = 1;
    private double currentXP = 0;
    private double nextLevelXP = 100;
    public enum Weather { SUNNY, RAINY, THUNDERSTORM, DROUGHT, BLESSING_RAIN, MIST }
    public enum Season { SPRING, SUMMER, AUTUMN, WINTER }
    private Weather currentWeather = Weather.SUNNY;
    private Season currentSeason = Season.SPRING;
    private long nextSeasonChangeMs = 0;
    private int unlockedPlotsCount = 0;
    private HashMap<String, Integer> marketSales = new HashMap<>();
    private List<Quest> activeQuests = new ArrayList<>();
    private long nextQuestTime = 0 ;
    private List<Quest> currentQuests = new ArrayList<>();
    private long questResetTime = 0;
    private int currentSaveSlot = 1;
    private long playTimeSeconds = 0;
    private final HashMap<String, Double> cropQualityBonusPool = new HashMap<>();
    private final HashMap<String, Integer> cropQualityUnits = new HashMap<>();
    private String specialOrderItem = "Wheat";
    private double specialOrderMultiplier = 1.2;
    private long specialOrderExpiryMs = 0;
    private int specialOrderDay = 0;
    private int gameDay = 1;
    private final HashMap<String, Integer> toolDurability = new HashMap<>();
    private final HashMap<String, Integer> toolMaxDurability = new HashMap<>();
    private int harvestComboCount = 0;
    private long harvestComboExpiryMs = 0;
    private int totalHarvested = 0;
    private int totalSold = 0;
    private int totalCompostUsed = 0;
    private double permanentSellBonus = 0.0;
    private final java.util.Set<String> unlockedAchievements = new java.util.HashSet<>();




    public Farms(double initialMoney){
        this.money = initialMoney;
        this.field = new Plot[LINES][COLUMNS];
        this.inventory = new Inventory();
        this.myAnimals = new ArrayList<>();
        initTools();

        for (int i = 0; i < LINES; i++){
            for (int j = 0; j < COLUMNS ; j ++){
                field[i][j] = new Plot();

                if (i == 0 && j == 0){
                    field[i][j].setLocked(false);
                }
            }
        }
    }

    private void initTools() {
        toolMaxDurability.put("Hoe", 120);
        toolMaxDurability.put("WateringCan", 90);
        toolMaxDurability.put("Sickle", 110);
        toolMaxDurability.put("CompostShovel", 80);
        for (java.util.Map.Entry<String, Integer> entry : toolMaxDurability.entrySet()) {
            toolDurability.put(entry.getKey(), entry.getValue());
        }
    }

    public double getDemandPrice(String cultureName, double baseSellPrice) {
        int totalSold = marketSales.getOrDefault(cultureName, 0);
        double multiplier = Math.max(0.4, 1.0 - (totalSold * 0.02));
        return baseSellPrice * multiplier;
    }

    public void recordSale(String cultureName, int quantity) {
        marketSales.put(cultureName, marketSales.getOrDefault(cultureName, 0) + quantity);
    }

    public void updateMarketFluctuation() {
        for (String name : marketSales.keySet()) {
            int current = marketSales.get(name);

            if (current > -10) {
                marketSales.put(name, current - 1);
            }
        }
    }

    public void addHarvestedCrop(String cropName, double qualityMultiplier) {
        inventory.add(cropName + "_Crop", 1);
        totalHarvested++;
        double bonus = Math.max(0.0, qualityMultiplier - 1.0);
        if (bonus > 0) {
            cropQualityBonusPool.put(cropName, cropQualityBonusPool.getOrDefault(cropName, 0.0) + bonus);
        }
        cropQualityUnits.put(cropName, cropQualityUnits.getOrDefault(cropName, 0) + 1);
    }

    public double consumeQualityBonus(String cropName, int qtyRemoved) {
        if (qtyRemoved <= 0) return 0.0;
        int units = cropQualityUnits.getOrDefault(cropName, 0);
        if (units <= 0) return 0.0;
        double pool = cropQualityBonusPool.getOrDefault(cropName, 0.0);
        double avgBonusPerUnit = pool / units;
        int consumedUnits = Math.min(qtyRemoved, units);
        double consumedBonus = avgBonusPerUnit * consumedUnits;

        int remainingUnits = units - consumedUnits;
        double remainingPool = Math.max(0.0, pool - consumedBonus);
        if (remainingUnits == 0) {
            cropQualityUnits.remove(cropName);
            cropQualityBonusPool.remove(cropName);
        } else {
            cropQualityUnits.put(cropName, remainingUnits);
            cropQualityBonusPool.put(cropName, remainingPool);
        }
        return consumedBonus;
    }

    public void refreshSpecialOrderIfNeeded() {
        if (specialOrderDay == gameDay && specialOrderExpiryMs > System.currentTimeMillis()) return;
        String[] possibleItems = {"Wheat","Carrot","Potato","Tomato","Lemon","Strawberry","Corn","Pineapple","Egg","Milk","Wool","Truff"};
        Random rand = new Random();
        specialOrderItem = possibleItems[rand.nextInt(possibleItems.length)];
        specialOrderMultiplier = switch (rand.nextInt(3)) {
            case 0 -> 1.25;
            case 1 -> 1.5;
            default -> 1.75;
        };
        specialOrderDay = gameDay;
        specialOrderExpiryMs = System.currentTimeMillis() + (24 * 60 * 60 * 1000L);
    }

    public void winMoney(double gains){
        this.money += gains;
    }

    public boolean spending(double gains){
        if(money >= gains){
            money -= gains;
            return true;
        }
        return false;
    }

    public void addSoldCount(int quantity) {
        totalSold += Math.max(0, quantity);
    }

    public void addCompostUsed(int quantity) {
        totalCompostUsed += Math.max(0, quantity);
    }

    public double getPermanentSellMultiplier() {
        return 1.0 + permanentSellBonus;
    }

    public java.util.List<String> evaluateAchievements() {
        java.util.List<String> newlyUnlocked = new java.util.ArrayList<>();
        unlockAchievementIf("achv.level10", level >= 10, newlyUnlocked);
        unlockAchievementIf("achv.money100k", money >= 100_000, newlyUnlocked);
        unlockAchievementIf("achv.harvest100", totalHarvested >= 100, newlyUnlocked);
        unlockAchievementIf("achv.sold200", totalSold >= 200, newlyUnlocked);
        unlockAchievementIf("achv.compost10", totalCompostUsed >= 10, newlyUnlocked);
        unlockAchievementIf("achv.land20", unlockedPlotsCount >= 20, newlyUnlocked);
        return newlyUnlocked;
    }

    private void unlockAchievementIf(String key, boolean condition, java.util.List<String> collector) {
        if (!condition || unlockedAchievements.contains(key)) return;
        unlockedAchievements.add(key);
        permanentSellBonus += 0.02;
        collector.add(key);
    }

    public double getNextPlotCost() {
        return 500 + (unlockedPlotsCount * 250);
    }

    public void addXP(double amount) {
        this.currentXP += amount;
        while (this.currentXP >= nextLevelXP) {
            levelUp();
        }
    }

    private void levelUp() {
        this.level++;
        this.currentXP -= this.nextLevelXP;
        this.nextLevelXP *= 1.5;
        System.out.println("LEVEL UP ! Vous êtes niveau " + level);
    }

    public void resetBeforeLoad() {
        this.money = 0;
        this.level = 1;
        this.currentXP = 0;
        this.nextLevelXP = 100;
        this.unlockedPlotsCount = 0;
        this.inventory.empty();
        this.myAnimals.clear();
        this.activeQuests.clear();
        this.currentQuests.clear();
        this.nextQuestTime = 0;
        this.questResetTime = 0;
        this.playTimeSeconds = 0;
        this.marketSales.clear();
        this.cropQualityBonusPool.clear();
        this.cropQualityUnits.clear();
        this.totalHarvested = 0;
        this.totalSold = 0;
        this.totalCompostUsed = 0;
        this.permanentSellBonus = 0.0;
        this.unlockedAchievements.clear();
        this.gameDay = 1;
        this.specialOrderDay = 0;
        this.harvestComboCount = 0;
        this.harvestComboExpiryMs = 0;
        this.toolDurability.clear();
        this.toolMaxDurability.clear();
        initTools();
        this.enclosureManager = null;

        for (int i = 0; i < LINES; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                field[i][j] = new Plot();
                if (i == 0 && j == 0) {
                    field[i][j].setLocked(false);
                }
            }
        }
    }

    public void updateWeather() {
        Random random = new Random();
        // Rare weather event for phase 2.
        double roll = random.nextDouble();
        if (roll < 0.06) {
            currentWeather = Weather.BLESSING_RAIN;
            return;
        }
        if (roll < 0.1) {
            currentWeather = Weather.THUNDERSTORM;
            return;
        }
        if (roll < 0.18) {
            currentWeather = Weather.MIST;
            return;
        }
        Farms.Weather[] regular = {Weather.SUNNY, Weather.RAINY, Weather.DROUGHT};
        currentWeather = regular[random.nextInt(regular.length)];
        System.out.println("Le temps change : " + currentWeather);
    }

    public void refreshSeasonIfNeeded() {
        long now = System.currentTimeMillis();
        if (nextSeasonChangeMs == 0) {
            nextSeasonChangeMs = now + (3 * 60 * 1000L);
            return;
        }
        if (now < nextSeasonChangeMs) return;
        currentSeason = switch (currentSeason) {
            case SPRING -> Season.SUMMER;
            case SUMMER -> Season.AUTUMN;
            case AUTUMN -> Season.WINTER;
            case WINTER -> Season.SPRING;
        };
        nextSeasonChangeMs = now + (3 * 60 * 1000L);
        gameDay++;
        refreshSpecialOrderIfNeeded();
    }

    public double getSeasonCropMultiplier(String cropName) {
        String crop = cropName == null ? "" : cropName;
        return switch (currentSeason) {
            case SPRING -> (crop.equals("Wheat") || crop.equals("Strawberry")) ? 1.2 : 1.0;
            case SUMMER -> (crop.equals("Tomato") || crop.equals("Corn") || crop.equals("Lemon")) ? 1.2 : 1.0;
            case AUTUMN -> (crop.equals("Potato") || crop.equals("Carrot") || crop.equals("Pumpkin")) ? 1.2 : 1.0;
            case WINTER -> (crop.equals("Pineapple")) ? 0.9 : 0.95;
        };
    }

    public boolean useTool(String toolName, int cost) {
        if (toolName == null) return true;
        int current = toolDurability.getOrDefault(toolName, 0);
        if (current <= 0 || cost <= 0) return current > 0;
        if (current < cost) return false;
        toolDurability.put(toolName, current - cost);
        return true;
    }

    public int getToolDurability(String toolName) {
        return toolDurability.getOrDefault(toolName, 0);
    }

    public int getToolMaxDurability(String toolName) {
        return toolMaxDurability.getOrDefault(toolName, 100);
    }

    public java.util.Map<String, Integer> getToolDurabilityMap() {
        return toolDurability;
    }

    public java.util.Map<String, Integer> getToolMaxDurabilityMap() {
        return toolMaxDurability;
    }

    public double registerHarvestCombo() {
        long now = System.currentTimeMillis();
        if (now > harvestComboExpiryMs) {
            harvestComboCount = 0;
        }
        harvestComboCount = Math.min(8, harvestComboCount + 1);
        harvestComboExpiryMs = now + 6500;
        return Math.min(1.4, 1.0 + (harvestComboCount * 0.05));
    }

    public int getHarvestComboCount() { return harvestComboCount; }
    public long getHarvestComboExpiryMs() { return harvestComboExpiryMs; }
    public void setHarvestComboState(int comboCount, long expiryMs) {
        this.harvestComboCount = Math.max(0, Math.min(8, comboCount));
        this.harvestComboExpiryMs = Math.max(0, expiryMs);
    }

    public int getGameDay() { return gameDay; }
    public void setGameDay(int gameDay) { this.gameDay = Math.max(1, gameDay); }
    public int getSpecialOrderDay() { return specialOrderDay; }

    public void generalQuests(){
        activeQuests.clear();
        String[] possibleItems = {"Wheat_Crop","Carrot_Crop","Potato_Crop","Tomato_Crop","Lemon_Crop","Strawberry_Crop","Corn_Crop","Pineapple_Crop","Egg_Crop","Truff_Crop","Wool_Crop","Milk_Crop",};
        Random rand = new Random();

        for (int i = 0; i < 3; i ++){
            String items = possibleItems[rand.nextInt(possibleItems.length)];
            int amount = rand.nextInt(50) + 1;
            double reward = amount * (10 + rand.nextInt(50)) * level;
            activeQuests.add(new Quest(items, amount, reward, 20 * level));
        }
    }

    public int getNbLINES(){return LINES;};
    public int getNbCOLMUNS(){return COLUMNS;}
    public Inventory getInventory() { return inventory; }
    public Weather getCurrentWeather() { return currentWeather; }
    public void setCurrentWeather(Weather w) { this.currentWeather = w; }
    public Season getCurrentSeason() { return currentSeason; }
    public void setCurrentSeason(Season season) { this.currentSeason = season; }
    public long getNextSeasonChangeMs() { return nextSeasonChangeMs; }
    public void setNextSeasonChangeMs(long nextSeasonChangeMs) { this.nextSeasonChangeMs = nextSeasonChangeMs; }
    public int getUnlockedPlotsCount() { return unlockedPlotsCount; }
    public void incrementUnlockedPlots() { this.unlockedPlotsCount++; }
    public List<Quest> getActiveQuests() { return activeQuests; }
    public long getNextQuestTime() { return nextQuestTime; }
    public void setNextQuestTime(long time) { this.nextQuestTime = time; }
    public void setMoney(double init) {
        this.money = init;
    }
    public void setLevel(int level){this.level = level;}
    public void setCurrentXP(double currentXP){this.currentXP = currentXP;}
    public void setNextLevelXP(double nextLevelXP){this.nextLevelXP = nextLevelXP;}
    public int getLevel() { return level; }
    public double getCurrentXP() { return currentXP; }
    public double getNextLevelXP() { return nextLevelXP; }
    public double getMoney() {return money;}
    public ArrayList<Animals> getMyAnimals(){return myAnimals;}
    public void addAnimals(Animals animals){this.myAnimals.add(animals);}
    public Plot[][] getField() {return field;}
    public List<Quest> getCurrentQuests() { return currentQuests; }
    public long getQuestResetTime() { return questResetTime; }
    public void setQuestResetTime(long time) { this.questResetTime = time; }
    public int getCurrentSaveSlot() { return currentSaveSlot; }
    public void setCurrentSaveSlot(int slot) { this.currentSaveSlot = slot; }
    public long getPlayTimeSeconds() { return playTimeSeconds; }
    public void setPlayTimeSeconds(long playTimeSeconds) { this.playTimeSeconds = Math.max(0, playTimeSeconds); }
    public void addPlayTimeSeconds(long delta) { this.playTimeSeconds = Math.max(0, this.playTimeSeconds + Math.max(0, delta)); }

    // ---- Enclosure Manager ----
    private EnclosureManager enclosureManager;
    public EnclosureManager getEnclosureManager() { return enclosureManager; }
    public void setEnclosureManager(EnclosureManager mgr) { this.enclosureManager = mgr; }
    public String getSpecialOrderItem() { return specialOrderItem; }
    public double getSpecialOrderMultiplier() { return specialOrderMultiplier; }
    public long getSpecialOrderExpiryMs() { return specialOrderExpiryMs; }
    public void setSpecialOrder(String item, double multiplier, long expiryMs) {
        this.specialOrderItem = item;
        this.specialOrderMultiplier = multiplier;
        this.specialOrderExpiryMs = expiryMs;
        this.specialOrderDay = gameDay;
    }
    public void setSpecialOrder(String item, double multiplier, long expiryMs, int orderDay) {
        this.specialOrderItem = item;
        this.specialOrderMultiplier = multiplier;
        this.specialOrderExpiryMs = expiryMs;
        this.specialOrderDay = Math.max(0, orderDay);
    }
    public java.util.Map<String, Double> getCropQualityBonusPool() { return cropQualityBonusPool; }
    public java.util.Map<String, Integer> getCropQualityUnits() { return cropQualityUnits; }
    public int getTotalHarvested() { return totalHarvested; }
    public void setTotalHarvested(int totalHarvested) { this.totalHarvested = Math.max(0, totalHarvested); }
    public int getTotalSold() { return totalSold; }
    public void setTotalSold(int totalSold) { this.totalSold = Math.max(0, totalSold); }
    public int getTotalCompostUsed() { return totalCompostUsed; }
    public void setTotalCompostUsed(int totalCompostUsed) { this.totalCompostUsed = Math.max(0, totalCompostUsed); }
    public double getPermanentSellBonus() { return permanentSellBonus; }
    public void setPermanentSellBonus(double permanentSellBonus) { this.permanentSellBonus = Math.max(0.0, permanentSellBonus); }
    public java.util.Set<String> getUnlockedAchievements() { return unlockedAchievements; }
}
