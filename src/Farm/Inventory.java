package Farm;

import java.util.HashMap;
import java.util.Map;

public class Inventory {
    private Map<String, Integer> ressources;

    public Inventory(){
        this.ressources = new HashMap<>();
    }

    public void add(String name, int quantity){
        ressources.put(name, ressources.getOrDefault(name, 0) + quantity);
    }

    public int getQuantity(String name){
        return ressources.getOrDefault(name, 0);
    }

    public Map<String, Integer> getItems() {
        return ressources;
    }

    public void empty(){
        ressources.clear();
    }
}
