package Farm;

public class Plot {
    private Culture actualCulture;
    private boolean estGood;
    private boolean locked = true;
    private double fertility = 1.0;
    private String lastCropName = "";
    private int monoStreak = 0;

    public Plot(){
        this.actualCulture = null;
        this.estGood = false;
    }

    public void planting(Culture culture){
        if (this.actualCulture == null) {
            String newCrop = culture.getName();
            if (newCrop.equalsIgnoreCase(lastCropName)) {
                monoStreak++;
            } else {
                monoStreak = 1;
            }
            lastCropName = newCrop;

            this.actualCulture = culture;
            // Monoculture is punished gradually; rotation keeps soil healthy.
            double monoPenalty = Math.max(0, monoStreak - 1) * 0.02;
            fertility = Math.max(0.6, fertility - 0.03 - monoPenalty);
        }
    }

    public void collect(){
        this.actualCulture = null ;
        fertility = Math.min(1.5, fertility + 0.01);
    }

    public boolean isEmpty(){
        return this.actualCulture == null;
    }

    public Culture getActualCulture() {
        return actualCulture;
    }

    public boolean isLocked() { return locked; }

    public void setLocked(boolean locked) { this.locked = locked; }

    public boolean isEstGood(){
        return estGood ;
    }

    public void setEstGood(boolean estGood){
        this.estGood = estGood;
    }

    public double getFertility() { return fertility; }
    public void setFertility(double fertility) { this.fertility = Math.max(0.5, Math.min(1.8, fertility)); }
    public void boostFertility(double amount) { this.fertility = Math.max(0.5, Math.min(1.8, this.fertility + amount)); }
    public String getLastCropName() { return lastCropName; }
    public int getMonoStreak() { return monoStreak; }
    public void setRotationData(String lastCropName, int monoStreak) {
        this.lastCropName = lastCropName == null ? "" : lastCropName;
        this.monoStreak = Math.max(0, monoStreak);
    }
}
