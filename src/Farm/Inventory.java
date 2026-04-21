package Farm;

import java.util.HashMap;
import java.util.Map;

public class Inventory {
    private Map<String, Integer> ressources;

    public Inventory(){
        this.ressources = new HashMap<>();
    }

    public void add(String name, int quantity){
        int current = ressources.getOrDefault(name, 0);
        int updated = current + quantity;
        if (updated < 0) {
            return;
        }
        if (updated == 0) {
            ressources.remove(name);
            return;
        }
        ressources.put(name, updated);
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
