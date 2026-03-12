package Farm;

public abstract class Culture {
    private String name;
    private double growthTime;
    private double timeLeft;
    private double buyPrice;
    private double sellPrice;
    private String imagePath;
    protected double timeSec;

    public Culture(String name, double growthTime, double buyPrice, double sellPrice,String imagePath){
        this.name = name;
        this.growthTime = growthTime;
        this.timeLeft = 0;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.imagePath = imagePath;
    }

    public void growing(double amount) {
        if (timeLeft < growthTime) {
            timeLeft += amount;
            this.timeSec = timeLeft;

            if (timeLeft > growthTime) {
                timeLeft = growthTime;
                this.timeSec = growthTime;
            }
        }
    }


    public double getTimeSec() {
        return this.timeSec;
    }

    public void setTimeSec(double time) {
        this.timeSec = time;
    }

    public boolean isReady(){
        return timeLeft >= growthTime;
    }

    public void setGrowthTime(double growthTime) {this.growthTime = growthTime;}

    public void setTimeLeft(double timeLeft) {this.timeLeft = timeLeft;}

    public double getGrowthTime() {return growthTime;}

    public String getName() {return name;}
    public double getBuyPrice() {return buyPrice;}
    public double getSellPrice() {return sellPrice;}
    public String getImagePath() {return imagePath;}
    public double getProgression() {return (double) timeLeft / growthTime ;}
}
