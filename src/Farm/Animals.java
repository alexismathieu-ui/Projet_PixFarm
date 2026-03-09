package Farm;

public abstract class Animals {
    private String species;
    private double buyPrice;
    private boolean isHungry = false;
    private int productionTime;
    private int currentTime = 0;
    private boolean hasProduced = false;

    public Animals(String species, double buyPrice, int productionTime){
        this.species = species;
        this.buyPrice = buyPrice;
        this.productionTime = productionTime;
    }

    public void update() {
        if (!isHungry && !hasProduced) {
            currentTime++;
            if (currentTime >= productionTime){
                hasProduced = true;
                currentTime = 0;
            }
        }
    }

    public void feed() {
        this.isHungry = false;
    }

    public void collectProduct(){
        this.hasProduced = false;
        this.isHungry = true;
    }

    public String getSpecies(){return species;}
    public double getBuyPrice() {return buyPrice;}
    public boolean isHungry() { return isHungry; }
    public boolean hasProduced(){return hasProduced;}
    public void setHungry(boolean hungry) { this.isHungry = hungry; }
    public void setProduced(boolean produced) { this.hasProduced = produced; }
    public abstract String getProductType();
    public abstract String getFoodNeeded();
}