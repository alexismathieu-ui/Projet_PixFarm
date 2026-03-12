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
    public enum Weather { SUNNY, RAINY, THUNDERSTORM, DROUGHT }
    private Weather currentWeather = Weather.SUNNY;
    private int unlockedPlotsCount = 0;
    private HashMap<String, Integer> marketSales = new HashMap<>();
    private List<Quest> activeQuests = new ArrayList<>();
    private long nextQuestTime = 0 ;
    private List<Quest> currentQuests = new ArrayList<>();
    private long questResetTime = 0;
    private int currentSaveSlot = 1;




    public Farms(double initialMoney){
        this.money = initialMoney;
        this.field = new Plot[LINES][COLUMNS];
        this.inventory = new Inventory();
        this.myAnimals = new ArrayList<>();

        for (int i = 0; i < LINES; i++){
            for (int j = 0; j < COLUMNS ; j ++){
                field[i][j] = new Plot();

                if (i == 0 && j == 0){
                    field[i][j].setLocked(false);
                }
            }
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

    public double getNextPlotCost() {
        return 500 + (unlockedPlotsCount * 250);
    }

    public void addXP(double amount) {
        this.currentXP += amount;
        if (this.currentXP >= nextLevelXP) {
            levelUp();
        }
    }

    private void levelUp() {
        this.level++;
        this.currentXP = 0;
        this.nextLevelXP *= 1.5;
        System.out.println("LEVEL UP ! Vous êtes niveau " + level);
    }

    public void updateWeather() {
        Farms.Weather[] allWeather = Farms.Weather.values();
        currentWeather = allWeather[new Random().nextInt(allWeather.length)];
        System.out.println("Le temps change : " + currentWeather);
    }

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

    // ---- Enclosure Manager ----
    private EnclosureManager enclosureManager;
    public EnclosureManager getEnclosureManager() { return enclosureManager; }
    public void setEnclosureManager(EnclosureManager mgr) { this.enclosureManager = mgr; }
}
