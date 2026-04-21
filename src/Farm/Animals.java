package Farm;

public abstract class Animals {
    private String species;
    private double buyPrice;
    private boolean isHungry = false;
    private int productionTime;
    private int currentTime = 0;
    private boolean hasProduced = false;
    private double health = 100.0;
    private double happiness = 100.0;

    public Animals(String species, double buyPrice, int productionTime){
        this.species = species;
        this.buyPrice = buyPrice;
        this.productionTime = productionTime;
    }

    public void update() {
        if (isHungry) {
            health = Math.max(30.0, health - 0.35);
            happiness = Math.max(20.0, happiness - 0.45);
        } else {
            health = Math.min(100.0, health + 0.12);
            happiness = Math.min(100.0, happiness + 0.2);
        }
        if (!isHungry && !hasProduced) {
            currentTime++;
            double productionFactor = 0.75 + ((health + happiness) / 200.0) * 0.5;
            int adjustedProductionTime = Math.max(2, (int) Math.round(productionTime / productionFactor));
            if (currentTime >= adjustedProductionTime){
                hasProduced = true;
                currentTime = 0;
            }
        }
    }

    public void feed() {
        this.isHungry = false;
        this.health = Math.min(100.0, this.health + 4.0);
        this.happiness = Math.min(100.0, this.happiness + 8.0);
    }

    public void collectProduct(){
        this.hasProduced = false;
        this.isHungry = true;
        this.happiness = Math.max(20.0, this.happiness - 2.0);
    }

    public String getSpecies(){return species;}
    public double getBuyPrice() {return buyPrice;}
    public boolean isHungry() { return isHungry; }
    public boolean hasProduced(){return hasProduced;}
    public void setHungry(boolean hungry) { this.isHungry = hungry; }
    public void setProduced(boolean produced) { this.hasProduced = produced; }
    public double getHealth() { return health; }
    public double getHappiness() { return happiness; }
    public void setHealth(double health) { this.health = Math.max(0.0, Math.min(100.0, health)); }
    public void setHappiness(double happiness) { this.happiness = Math.max(0.0, Math.min(100.0, happiness)); }
    public abstract String getProductType();
    public abstract String getFoodNeeded();
}