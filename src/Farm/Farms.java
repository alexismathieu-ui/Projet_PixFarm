package Farm;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Farms {
    private double money;
    private Plot[][] field;
    private ArrayList<Animals> myAnimals = new ArrayList<>();
    private final int LINES = 5;
    private final int COLUMNS = 5;
    private Inventory inventory;

    public Farms(double initialMoney){
        this.money = initialMoney;
        this.field = new Plot[LINES][COLUMNS];
        this.inventory = new Inventory();
        this.myAnimals = new ArrayList<>();

        for (int i = 0; i < LINES; i++){
            for (int j = 0; j < COLUMNS ; j ++){
                field[i][j] = new Plot();
            }
        }
    }

    public void setMoney(double init) {
        this.money = init;
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

    public double getMoney() {
        return money;
    }

    public ArrayList<Animals> getMyAnimals(){
        return myAnimals;
    }

    public void addAnimals(Animals animals){
        this.myAnimals.add(animals);
    }

    public Plot[][] getField() {
        return field;
    }

    public int getNbLINES(){return LINES;};
    public int getNbCOLMUNS(){return COLUMNS;}
    public Inventory getInventory() { return inventory; }

}
