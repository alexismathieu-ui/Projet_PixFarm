package Farm.Enclosure;

import Farm.Animals;

import java.util.ArrayList;
import java.util.List;

public class Enclosure {
    private String name;
    private String enclosureType; // "Chicken", "Sheep", "Cow", "Pig", or "Mixed"
    private int maxCapacity;
    private List<Animals> animals;
    private int id;

    public Enclosure(int id, String name, int maxCapacity) {
        this.id = id;
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.enclosureType = "Mixed";
        this.animals = new ArrayList<>();
    }

    public boolean addAnimal(Animals animal) {
        if (animals.size() >= maxCapacity) return false;
        animals.add(animal);
        return true;
    }

    public boolean removeAnimal(Animals animal) {
        return animals.remove(animal);
    }

    public boolean isFull() {
        return animals.size() >= maxCapacity;
    }

    public boolean isEmpty() {
        return animals.isEmpty();
    }

    public int getAnimalCount() {
        return animals.size();
    }

    public int getReadyCount() {
        return (int) animals.stream().filter(Animals::hasProduced).count();
    }

    public int getHungryCount() {
        return (int) animals.stream().filter(Animals::isHungry).count();
    }

    public String getStatusEmoji() {
        if (isEmpty()) return "🌿";
        if (getReadyCount() > 0) return "✅";
        if (getHungryCount() > 0) return "🍽️";
        return "⚙️";
    }

    public String getDominantSpecies() {
        if (animals.isEmpty()) return "Vide";
        return animals.get(0).getSpecies();
    }

    // Getters & Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getMaxCapacity() { return maxCapacity; }
    public List<Animals> getAnimals() { return animals; }
    public String getEnclosureType() { return enclosureType; }
    public void setEnclosureType(String type) { this.enclosureType = type; }
}
