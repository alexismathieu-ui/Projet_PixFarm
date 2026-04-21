package FarmEngine;

import Farm.*;
import Farm.Animal.Chicken;
import Farm.Animal.Cow;
import Farm.Animal.Pig;
import Farm.Animal.Sheep;
import Farm.Crops.*;
import Farm.Enclosure.Enclosure;
import Farm.Enclosure.EnclosureManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

public class SaveSystem {
    private static final int SAVE_VERSION = 3;
    private static final String VERSION_PREFIX = "SAVE_VERSION=";
    private static final String CHECKSUM_PREFIX = "CHECKSUM=";
    private static final Path SAVE_DIR = Path.of("saves");
    private static final String[] ITEM_TYPES = {"Wheat","Tomato","Carrot","Potato","Lemon","Strawberry","Corn","Pineapple","Egg","Truff","Milk","Wool"};
    private static final DateTimeFormatter SAVE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    private static String getFilePath(int slot){
        return "saves/save" + slot + ".txt";
    }

    private static void ensureSaveDirectory() throws IOException {
        Files.createDirectories(SAVE_DIR);
    }

    private static long computeChecksum(List<String> lines) {
        CRC32 crc = new CRC32();
        for (String line : lines) {
            crc.update((line + "\n").getBytes(StandardCharsets.UTF_8));
        }
        return crc.getValue();
    }

    public static void saves(Farms farms, int slot){
        Path savePath = Path.of(getFilePath(slot));
        Path tempPath = Path.of(getFilePath(slot) + ".tmp");
        Path backupPath = Path.of(getFilePath(slot) + ".bak");

        try {
            ensureSaveDirectory();
            List<String> lines = new ArrayList<>();
            lines.add(VERSION_PREFIX + SAVE_VERSION);
            lines.add(String.valueOf(farms.getMoney()));
            lines.add(String.valueOf(farms.getLevel()));
            lines.add(String.valueOf(farms.getCurrentXP()));
            lines.add(String.valueOf(farms.getNextLevelXP()));
            lines.add(String.valueOf(farms.getUnlockedPlotsCount()));
            lines.add(String.valueOf(farms.getPlayTimeSeconds()));
            lines.add(String.valueOf(System.currentTimeMillis()));

            for (String type : ITEM_TYPES) {
                lines.add(String.valueOf(farms.getInventory().getQuantity(type + "_Seed")));
                lines.add(String.valueOf(farms.getInventory().getQuantity(type + "_Crop")));
            }

            for (int i = 0; i < farms.getNbLINES(); i++) {
                for (int j = 0; j < farms.getNbCOLMUNS(); j++) {
                    Plot p = farms.getField()[i][j];
                    lines.add(String.valueOf(p.isLocked()));
                    if (p.isEmpty()) {
                        lines.add("EMPTY");
                    } else {
                        lines.add(p.getActualCulture().getName().toUpperCase() + "|" + p.getActualCulture().getTimeSec());
                    }
                }
            }

            lines.add("ANIMALS_STARTS");
            for (Animals a : farms.getMyAnimals()) {
                lines.add(a.getSpecies() + "|" + a.isHungry() + "|" + a.hasProduced());
            }
            lines.add("ANIMALS_END");

            lines.add("ENCLOSURES_START");
            EnclosureManager mgr = farms.getEnclosureManager();
            if (mgr != null) {
                lines.add(String.valueOf(mgr.getEnclosures().size()));
                for (Enclosure enc : mgr.getEnclosures()) {
                    lines.add(enc.getId() + "|" + enc.getName() + "|" + enc.getMaxCapacity());
                    lines.add(String.valueOf(enc.getAnimals().size()));
                    for (Animals a : enc.getAnimals()) {
                        lines.add(String.valueOf(farms.getMyAnimals().indexOf(a)));
                    }
                }
            } else {
                lines.add("0");
            }
            lines.add("ENCLOSURES_END");

            lines.add("QUESTS_DATA");
            lines.add(String.valueOf(farms.getNextQuestTime()));
            lines.add(String.valueOf(farms.getActiveQuests().size()));
            for (Quest q : farms.getActiveQuests()) {
                lines.add(q.getTargetItem() + "|" + q.getAmountNeeded() + "|" + q.getRewardMoney() + "|" + q.getRewardXP());
            }

            lines.add(CHECKSUM_PREFIX + computeChecksum(lines));
            Files.write(tempPath, lines, StandardCharsets.UTF_8);

            if (Files.exists(savePath)) {
                Files.copy(savePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.move(tempPath, savePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            System.out.println("Partie sauvegardee dans " + savePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load(Farms farms, int slot) {
        File file = new File(getFilePath(slot));
        if (!file.exists()) return;
        farms.resetBeforeLoad();

        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            if (lines.isEmpty()) return;

            int startIndex = 0;
            int dataEndExclusive = lines.size();
            int parsedVersion = 1;

            if (lines.get(0).startsWith(VERSION_PREFIX)) {
                parsedVersion = Integer.parseInt(lines.get(0).substring(VERSION_PREFIX.length()));
                startIndex = 1;
            }

            if (!lines.isEmpty() && lines.get(lines.size() - 1).startsWith(CHECKSUM_PREFIX)) {
                long expectedChecksum = Long.parseLong(lines.get(lines.size() - 1).substring(CHECKSUM_PREFIX.length()));
                List<String> checksumSource = lines.subList(0, lines.size() - 1);
                long actualChecksum = computeChecksum(checksumSource);
                if (expectedChecksum != actualChecksum) {
                    System.out.println("Checksum invalide pour save slot " + slot);
                    return;
                }
                dataEndExclusive = lines.size() - 1;
            }

            if (parsedVersion > SAVE_VERSION) {
                System.out.println("Version de sauvegarde non supportee : " + parsedVersion);
                return;
            }

            int idx = startIndex;
            if (idx < dataEndExclusive) farms.setMoney(Double.parseDouble(lines.get(idx++)));
            if (idx < dataEndExclusive) farms.setLevel(Integer.parseInt(lines.get(idx++)));
            if (idx < dataEndExclusive) farms.setCurrentXP(Double.parseDouble(lines.get(idx++)));
            if (idx < dataEndExclusive) farms.setNextLevelXP(Double.parseDouble(lines.get(idx++)));
            if (idx < dataEndExclusive) {
                int count = Integer.parseInt(lines.get(idx++));
                for (int i = 0; i < count; i++) farms.incrementUnlockedPlots();
            }
            if (parsedVersion >= 3 && idx < dataEndExclusive) {
                farms.setPlayTimeSeconds(Long.parseLong(lines.get(idx++)));
            }
            if (parsedVersion >= 3 && idx < dataEndExclusive) {
                // reserve read for last saved timestamp metadata
                idx++;
            }

            for (String type : ITEM_TYPES) {
                if (idx < dataEndExclusive) farms.getInventory().add(type + "_Seed", Integer.parseInt(lines.get(idx++)));
                if (idx < dataEndExclusive) farms.getInventory().add(type + "_Crop", Integer.parseInt(lines.get(idx++)));
            }

            for (int i = 0; i < farms.getNbLINES(); i++) {
                for (int j = 0; j < farms.getNbCOLMUNS(); j++) {
                    if (idx >= dataEndExclusive) break;
                    farms.getField()[i][j].setLocked(Boolean.parseBoolean(lines.get(idx++)));
                    if (idx >= dataEndExclusive) break;
                    String line = lines.get(idx++);
                    if (!line.equals("EMPTY")) {
                        String[] parts = line.split("\\|");
                        Culture c = switch (parts[0]) {
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
                        if (c != null && parts.length > 1) {
                            double t = Double.parseDouble(parts[1]);
                            c.setTimeSec(t);
                            c.setTimeLeft(t);
                            farms.getField()[i][j].planting(c);
                        }
                    }
                }
            }

            while (idx < dataEndExclusive) {
                String line = lines.get(idx++);
                if (line.equals("ANIMALS_STARTS")) continue;
                if (line.equals("ANIMALS_END")) break;
                String[] parts = line.split("\\|");
                Animals a = switch (parts[0]) {
                    case "Chicken" -> new Chicken();
                    case "Sheep" -> new Sheep();
                    case "Cow" -> new Cow();
                    case "Pig" -> new Pig();
                    default -> null;
                };
                if (a != null && parts.length > 2) {
                    a.setHungry(Boolean.parseBoolean(parts[1]));
                    a.setProduced(Boolean.parseBoolean(parts[2]));
                    farms.addAnimals(a);
                }
            }

            EnclosureManager mgr = new EnclosureManager(false);
            farms.setEnclosureManager(mgr);

            while (idx < dataEndExclusive) {
                String line = lines.get(idx++);
                if (line.equals("ENCLOSURES_START")) {
                    if (idx >= dataEndExclusive) break;
                    int enclCount = Integer.parseInt(lines.get(idx++));
                    for (int e = 0; e < enclCount && idx < dataEndExclusive; e++) {
                        String[] hp = lines.get(idx++).split("\\|");
                        Enclosure enc = mgr.addEnclosureWithId(
                                Integer.parseInt(hp[0]), hp[1], Integer.parseInt(hp[2])
                        );
                        int animalCount = Integer.parseInt(lines.get(idx++));
                        for (int k = 0; k < animalCount && idx < dataEndExclusive; k++) {
                            int animalIdx = Integer.parseInt(lines.get(idx++));
                            if (animalIdx >= 0 && animalIdx < farms.getMyAnimals().size()) {
                                enc.addAnimal(farms.getMyAnimals().get(animalIdx));
                            }
                        }
                    }
                    continue;
                }
                if (line.equals("ENCLOSURES_END")) break;
            }

            while (idx < dataEndExclusive) {
                String line = lines.get(idx++);
                if (line.equals("QUESTS_DATA")) {
                    if (idx < dataEndExclusive) {
                        long savedNextTime = Long.parseLong(lines.get(idx++));
                        long now = System.currentTimeMillis();
                        if (savedNextTime != 0 && now >= savedNextTime) {
                            farms.setNextQuestTime(0);
                            farms.generalQuests();
                            break;
                        } else {
                            farms.setNextQuestTime(savedNextTime);
                        }
                    }
                    if (idx < dataEndExclusive) {
                        int questCount = Integer.parseInt(lines.get(idx++));
                        farms.getActiveQuests().clear();
                        for (int i = 0; i < questCount && idx < dataEndExclusive; i++) {
                            String[] qp = lines.get(idx++).split("\\|");
                            if (qp.length < 4) continue;
                            farms.getActiveQuests().add(new Quest(qp[0], Integer.parseInt(qp[1]),
                                    Double.parseDouble(qp[2]), Integer.parseInt(qp[3])));
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("No Saves Found");
        } catch (Exception e) {
            System.out.println("Erreur de chargement de la sauvegarde slot " + slot + " : " + e.getMessage());
        }
    }

    public static String getSaveSummary(int slot) {
        File file = new File("saves/save" + slot + ".txt");
        if (!file.exists()) return "Nouvelle Partie";
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            if (lines.isEmpty()) return "Sauvegarde vide";

            int idx = lines.get(0).startsWith(VERSION_PREFIX) ? 1 : 0;
            int version = lines.get(0).startsWith(VERSION_PREFIX)
                    ? Integer.parseInt(lines.get(0).substring(VERSION_PREFIX.length()))
                    : 1;

            if (idx + 4 >= lines.size()) return "Sauvegarde corrompue";
            double money = Double.parseDouble(lines.get(idx));
            int level = Integer.parseInt(lines.get(idx + 1));
            long playTime = 0;
            long lastSaved = 0;
            if (version >= 3 && idx + 6 < lines.size()) {
                playTime = Long.parseLong(lines.get(idx + 5));
                lastSaved = Long.parseLong(lines.get(idx + 6));
            }

            String dateText = lastSaved > 0 ? SAVE_DATE_FORMATTER.format(Instant.ofEpochMilli(lastSaved)) : "date inconnue";
            return "Niv " + level + " — " + (int) money + " $ — " + formatDuration(playTime) + " — " + dateText + " — v" + version;
        } catch (Exception e) { return "Sauvegarde corrompue"; }
        return "Nouvelle Partie";
    }

    private static String formatDuration(long totalSeconds) {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        if (h > 0) return h + "h" + String.format("%02d", m);
        return m + "min";
    }

    public static int getMostRecentSaveSlot() {
        long latestTimestamp = -1;
        int latestSlot = -1;
        for (int slot = 1; slot <= 3; slot++) {
            File file = new File(getFilePath(slot));
            if (!file.exists()) continue;
            long ts = extractLastSavedTimestamp(file.toPath());
            if (ts > latestTimestamp) {
                latestTimestamp = ts;
                latestSlot = slot;
            }
        }
        return latestSlot;
    }

    private static long extractLastSavedTimestamp(Path path) {
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            if (lines.isEmpty()) return 0;
            if (!lines.get(0).startsWith(VERSION_PREFIX)) return 0;
            int version = Integer.parseInt(lines.get(0).substring(VERSION_PREFIX.length()));
            int idx = 1;
            if (version >= 3 && idx + 6 < lines.size()) {
                return Long.parseLong(lines.get(idx + 6));
            }
        } catch (Exception ignored) {
        }
        return 0;
    }
}
