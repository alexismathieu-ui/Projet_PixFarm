package Farm;

public class Plot {
    private Culture actualCulture;
    private boolean estGood;
    private boolean locked = true;

    public Plot(){
        this.actualCulture = null;
        this.estGood = false;
    }

    public void planting(Culture culture){
        if (this.actualCulture == null) {
            this.actualCulture = culture;
        }
    }

    public void collect(){
        this.actualCulture = null ;
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
}
