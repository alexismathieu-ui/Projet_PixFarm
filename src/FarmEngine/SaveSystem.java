package FarmEngine;

import Farm.*;
import Farm.Animal.Chicken;
import Farm.Animal.Cow;
import Farm.Animal.Pig;
import Farm.Animal.Sheep;
import Farm.Crops.*;

import java.io.FileWriter;
import java.io.PrintWriter;

import java.io.*;
import java.util.Scanner;

public class SaveSystem {

    private static String getFilePath(int slot){
        return "saves/save" + slot + ".txt";
    }

    public static void saves(Farms farms, int slot){
        String path = getFilePath(slot);
        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
            writer.println(farms.getMoney());
            writer.println(farms.getLevel());
            writer.println(farms.getCurrentXP());
            writer.println(farms.getNextLevelXP());

            writer.println(farms.getUnlockedPlotsCount());

            String[] types = {"Wheat", "Tomato", "Carrot", "Potato", "Lemon", "Strawberry", "Corn", "Pineapple", "Egg", "Truff", "Milk", "Wool"};
            for(String type : types) {
                writer.println(farms.getInventory().getQuantity(type + "_Seed"));
                writer.println(farms.getInventory().getQuantity(type + "_Crop"));
            }

            for (int i = 0; i < farms.getNbLINES(); i++) {
                for (int j = 0; j < farms.getNbCOLMUNS(); j++) {
                    Plot p = farms.getField()[i][j];

                    writer.println(p.isLocked());

                    if (p.isEmpty()) {
                        writer.println("EMPTY");
                    } else {
                        String name = p.getActualCulture().getName().toUpperCase();
                        double time = p.getActualCulture().getTimeSec();
                        writer.println(name + "|" + time);
                    }
                }
            }
            writer.println("ANIMALS_STARTS");
            for(Animals animals : farms.getMyAnimals()){
                writer.println(animals.getSpecies() + "|" + animals.isHungry() + "|" + animals.hasProduced());
            }
            writer.println("ANIMALS_END");
            writer.println("QUESTS_DATA");
            writer.println(farms.getNextQuestTime());

            writer.println(farms.getActiveQuests().size());
            for (Quest q : farms.getActiveQuests()) {
                writer.println(q.getTargetItem() + "|" + q.getAmountNeeded() + "|" + q.getRewardMoney() + "|" + q.getRewardXP());
            }

            System.out.println("Partie Sauvegardée dans " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void load(Farms farms, int slot) {
        File file = new File(getFilePath(slot));
        if (!file.exists()) return;

        try (Scanner scanner = new Scanner(file)) {

            if (scanner.hasNextLine()) {farms.setMoney(Double.parseDouble(scanner.nextLine()));}
            if (scanner.hasNextLine()) farms.setLevel(Integer.parseInt(scanner.nextLine()));
            if (scanner.hasNextLine()) farms.setCurrentXP(Double.parseDouble(scanner.nextLine()));
            if (scanner.hasNextLine()) farms.setNextLevelXP(Double.parseDouble(scanner.nextLine()));

            if (scanner.hasNextLine()) {
                int count = Integer.parseInt(scanner.nextLine());
                for(int i = 0; i < count; i++) farms.incrementUnlockedPlots();
            }

            String[] types = {"Wheat", "Tomato", "Carrot", "Potato", "Lemon", "Strawberry", "Corn", "Pineapple", "Egg", "Truff", "Milk", "Wool"};
            for(String type : types) {
                if(scanner.hasNextLine()) farms.getInventory().add(type + "_Seed", Integer.parseInt(scanner.nextLine()));
                if(scanner.hasNextLine()) farms.getInventory().add(type + "_Crop", Integer.parseInt(scanner.nextLine()));
            }


            for (int i = 0; i < farms.getNbLINES(); i++) {
                for (int j = 0; j < farms.getNbCOLMUNS(); j++) {
                    if (scanner.hasNextLine()) {
                        boolean isLocked = Boolean.parseBoolean(scanner.nextLine());
                        farms.getField()[i][j].setLocked(isLocked);

                        String line = scanner.nextLine();

                        if (!line.equals("EMPTY")) {

                            String[] parts = line.split("\\|");
                            String type = parts[0];
                            double savedTime = Double.parseDouble(parts[1]);

                            Culture c = switch (type) {
                                case "WHEAT" -> new Wheat();
                                case "CARROT" -> new Carrot();
                                case "POTATO" -> new Potato();
                                case "TOMATO" -> new Tomato();
                                case "LEMON" -> new Lemon();
                                case "STRAWBERRY" -> new Strawberry();
                                case "CORN" -> new Corn();
                                case "PINEAPPLE" -> new Pineapple();
                                default -> null;
                            };

                            if (c != null) {
                                c.setTimeSec(savedTime);
                                c.setTimeLeft(savedTime);
                                farms.getField()[i][j].planting(c);
                            }
                        }
                    }
                }
            }
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                if (line.equals("ANIMALS_STARTS")) continue;
                if (line.equals("ANIMALS_END")) break;

                String[] parts = line.split("\\|");
                Animals animals = switch (parts[0]){
                    case "Chicken" -> new Chicken();
                    case "Sheep" -> new Sheep();
                    case "Cow" -> new Cow();
                    case "Pig" -> new Pig();
                    default -> null;
                };

                if (animals != null){
                    animals.setHungry(Boolean.parseBoolean(parts[1]));
                    animals.setProduced(Boolean.parseBoolean(parts[2]));
                    farms.addAnimals(animals);
                }
            }
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.equals("QUESTS_DATA")) {
                    if (scanner.hasNextLine()) {
                        long savedNextTime = Long.parseLong(scanner.nextLine());
                        long now = System.currentTimeMillis();

                        if (savedNextTime != 0 && now >= savedNextTime) {
                            farms.setNextQuestTime(0);
                            farms.generalQuests();
                            break;
                        } else {
                            farms.setNextQuestTime(savedNextTime);
                        }
                    }

                    if (scanner.hasNextLine()) {
                        int questCount = Integer.parseInt(scanner.nextLine());
                        farms.getActiveQuests().clear();

                        for (int i = 0; i < questCount; i++) {
                            if (scanner.hasNextLine()) {
                                String questLine = scanner.nextLine();
                                String[] qParts = questLine.split("\\|");
                                if(qParts.length < 4) continue;

                                Quest q = new Quest(
                                        qParts[0],
                                        Integer.parseInt(qParts[1]),
                                        Double.parseDouble(qParts[2]),
                                        Integer.parseInt(qParts[3])
                                );
                                farms.getActiveQuests().add(q);
                            }
                        }
                    }
                }
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("No Saves Founds");
        }
    }
    public static String getSaveSummary(int slot) {
        File file = new File("saves/save" + slot + ".txt");
        if (!file.exists()) {
            return "Nouvelle Partie\nEmplacement vide";
        }

        try (Scanner scanner = new Scanner(file)) {
            if (scanner.hasNextLine()) {
                double money = Double.parseDouble(scanner.nextLine());
                int level = Integer.parseInt(scanner.nextLine());
                return "Niveau " + level + " — " + (int)money + " $";
            }
        } catch (Exception e) {
            return "Sauvegarde corrompue";
        }
        return "Nouvelle Partie";
    }
}



