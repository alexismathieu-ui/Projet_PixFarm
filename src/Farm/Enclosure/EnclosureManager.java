package Farm.Enclosure;

import Farm.Animals;

import java.util.ArrayList;
import java.util.List;

public class EnclosureManager {
    private List<Enclosure> enclosures;
    private int maxEnclosures = 6;
    private int nextId = 1;

    public EnclosureManager() {
        this.enclosures = new ArrayList<>();
        // Create 2 default enclosures at start
        addEnclosure("Enclos 1", 4);
        addEnclosure("Enclos 2", 4);
    }

    public Enclosure addEnclosure(String name, int capacity) {
        if (enclosures.size() >= maxEnclosures) return null;
        Enclosure e = new Enclosure(nextId++, name, capacity);
        enclosures.add(e);
        return e;
    }

    public boolean removeEnclosure(int id) {
        return enclosures.removeIf(e -> e.getId() == id && e.isEmpty());
    }

    public Enclosure getEnclosure(int id) {
        return enclosures.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
    }

    public List<Enclosure> getEnclosures() {
        return enclosures; 
    }

    public boolean canAddMoreEnclosures() {
        return enclosures.size() < maxEnclosures;
    }

    public int getTotalAnimals() {
        return enclosures.stream().mapToInt(Enclosure::getAnimalCount).sum();
    }

    public int getTotalReady() {
        return enclosures.stream().mapToInt(Enclosure::getReadyCount).sum();
    }

    public int getTotalHungry() {
        return enclosures.stream().mapToInt(Enclosure::getHungryCount).sum();
    }

    public List<Animals> getAllAnimals() {
        List<Animals> all = new ArrayList<>();
        for (Enclosure e : enclosures) all.addAll(e.getAnimals());
        return all;
    }

    public void setMaxEnclosures(int max) {
        this.maxEnclosures = max;
    }

    public int getMaxEnclosures() { return maxEnclosures; }
}
