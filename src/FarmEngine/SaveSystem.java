package FarmEngine;

import Farm.*;
import Farm.Animal.Chicken;
import Farm.Animal.Cow;
import Farm.Animal.Pig;
import Farm.Animal.Sheep;
import Farm.Crops.*;
import Farm.Enclosure.Enclosure;
import Farm.Enclosure.EnclosureManager;
import FarmController.StoreController;

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
    private static final int SAVE_VERSION = 11;
    private static final String VERSION_PREFIX = "SAVE_VERSION=";
    private static final String CHECKSUM_PREFIX = "CHECKSUM=";
    private static final Path SAVE_DIR = Path.of("saves");
    private static final String[] ITEM_TYPES = {"Wheat","Tomato","Carrot","Potato","Lemon","Strawberry","Corn","Pineapple","Egg","Truff","Milk","Wool","Compost"};
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
            lines.add("PLOT_FERTILITY_START");
            for (int i = 0; i < farms.getNbLINES(); i++) {
                for (int j = 0; j < farms.getNbCOLMUNS(); j++) {
                    lines.add(String.valueOf(farms.getField()[i][j].getFertility()));
                }
            }
            lines.add("PLOT_FERTILITY_END");
            lines.add("PLOT_ROTATION_START");
            for (int i = 0; i < farms.getNbLINES(); i++) {
                for (int j = 0; j < farms.getNbCOLMUNS(); j++) {
                    Plot p = farms.getField()[i][j];
                    lines.add(p.getLastCropName() + "|" + p.getMonoStreak());
                }
            }
            lines.add("PLOT_ROTATION_END");

            lines.add("ANIMALS_STARTS");
            for (Animals a : farms.getMyAnimals()) {
                lines.add(a.getSpecies() + "|" + a.isHungry() + "|" + a.hasProduced() + "|" + a.getHealth() + "|" + a.getHappiness());
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

            lines.add("META_QUALITY_START");
            java.util.Map<String, Double> qualityPool = farms.getCropQualityBonusPool();
            java.util.Map<String, Integer> qualityUnits = farms.getCropQualityUnits();
            lines.add(String.valueOf(qualityPool.size()));
            for (java.util.Map.Entry<String, Double> e : qualityPool.entrySet()) {
                int units = qualityUnits.getOrDefault(e.getKey(), 0);
                lines.add(e.getKey() + "|" + e.getValue() + "|" + units);
            }
            lines.add("META_QUALITY_END");

            lines.add("META_SPECIAL_ORDER");
            lines.add(farms.getSpecialOrderItem() + "|" + farms.getSpecialOrderMultiplier() + "|" + farms.getSpecialOrderExpiryMs() + "|" + farms.getSpecialOrderDay());

            lines.add("META_STORE_STOCK_START");
            java.util.Map<String, Integer> stock = StoreController.snapshotSeedStocks();
            lines.add(String.valueOf(stock.size()));
            for (java.util.Map.Entry<String, Integer> e : stock.entrySet()) {
                lines.add(e.getKey() + "|" + e.getValue());
            }
            lines.add(String.valueOf(StoreController.getNextRestockMs()));
            lines.add("META_STORE_STOCK_END");

            lines.add("META_SEASON");
            lines.add(farms.getCurrentSeason().name() + "|" + farms.getNextSeasonChangeMs() + "|" + farms.getGameDay());

            lines.add("META_TOOLS_START");
            lines.add(String.valueOf(farms.getToolDurabilityMap().size()));
            for (java.util.Map.Entry<String, Integer> e : farms.getToolDurabilityMap().entrySet()) {
                int max = farms.getToolMaxDurabilityMap().getOrDefault(e.getKey(), 100);
                lines.add(e.getKey() + "|" + e.getValue() + "|" + max);
            }
            lines.add("META_TOOLS_END");

            lines.add("META_COMBO");
            lines.add(farms.getHarvestComboCount() + "|" + farms.getHarvestComboExpiryMs());

            lines.add("META_ACHIEVEMENTS");
            lines.add(farms.getTotalHarvested() + "|" + farms.getTotalSold() + "|" + farms.getTotalCompostUsed() + "|" + farms.getPermanentSellBonus());
            lines.add(String.valueOf(farms.getUnlockedAchievements().size()));
            for (String achv : farms.getUnlockedAchievements()) {
                lines.add(achv);
            }

            lines.add("META_TALENTS");
            lines.add(farms.getTalentPoints() + "|" + farms.getNarrativeChapter() + "|" + farms.getGuildWeekIndex() + "|" + farms.getGuildWeekStartDay());
            lines.add(String.valueOf(farms.getTalentRanks().size()));
            for (java.util.Map.Entry<String, Integer> e : farms.getTalentRanks().entrySet()) {
                lines.add(e.getKey() + "|" + e.getValue());
            }

            lines.add("META_NARRATIVE");
            lines.add(String.valueOf(farms.getCompletedNarrativeQuests().size()));
            for (String id : farms.getCompletedNarrativeQuests()) {
                lines.add(id);
            }

            lines.add("META_GUILD");
            lines.add(String.valueOf(farms.getWeeklyGuildQuests().size()));
            for (Farms.GuildQuest gq : farms.getWeeklyGuildQuests()) {
                lines.add(gq.id + "|" + gq.targetItem + "|" + gq.amountNeeded + "|" + gq.rewardMoney + "|" + gq.rewardXP + "|" + gq.claimed);
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
        Path mainPath = Path.of(getFilePath(slot));
        Path backupPath = Path.of(getFilePath(slot) + ".bak");
        boolean loaded = tryLoadFromPath(farms, slot, mainPath, false);
        if (!loaded) {
            tryLoadFromPath(farms, slot, backupPath, true);
        }
    }

    private static boolean tryLoadFromPath(Farms farms, int slot, Path path, boolean fromBackup) {
        if (!Files.exists(path)) return false;
        farms.resetBeforeLoad();

        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            if (lines.isEmpty()) return false;

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
                    System.out.println("Checksum invalide pour save slot " + slot + (fromBackup ? " (.bak)" : ""));
                    return false;
                }
                dataEndExclusive = lines.size() - 1;
            }

            if (parsedVersion > SAVE_VERSION) {
                System.out.println("Version de sauvegarde non supportee : " + parsedVersion);
                return false;
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

            if (parsedVersion >= 5 && idx < dataEndExclusive && lines.get(idx).equals("PLOT_FERTILITY_START")) {
                idx++;
                for (int i = 0; i < farms.getNbLINES(); i++) {
                    for (int j = 0; j < farms.getNbCOLMUNS(); j++) {
                        if (idx >= dataEndExclusive) break;
                        String line = lines.get(idx++);
                        if ("PLOT_FERTILITY_END".equals(line)) break;
                        farms.getField()[i][j].setFertility(Double.parseDouble(line));
                    }
                }
                while (idx < dataEndExclusive && !lines.get(idx).equals("PLOT_FERTILITY_END")) idx++;
                if (idx < dataEndExclusive) idx++;
            }
            if (idx < dataEndExclusive && lines.get(idx).equals("PLOT_ROTATION_START")) {
                idx++;
                for (int i = 0; i < farms.getNbLINES(); i++) {
                    for (int j = 0; j < farms.getNbCOLMUNS(); j++) {
                        if (idx >= dataEndExclusive) break;
                        String line = lines.get(idx++);
                        if ("PLOT_ROTATION_END".equals(line)) break;
                        String[] rot = line.split("\\|");
                        if (rot.length >= 2) {
                            farms.getField()[i][j].setRotationData(rot[0], Integer.parseInt(rot[1]));
                        }
                    }
                }
                while (idx < dataEndExclusive && !lines.get(idx).equals("PLOT_ROTATION_END")) idx++;
                if (idx < dataEndExclusive) idx++;
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
                    if (parts.length > 4) {
                        a.setHealth(Double.parseDouble(parts[3]));
                        a.setHappiness(Double.parseDouble(parts[4]));
                    }
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
                    continue;
                }
                if (line.equals("META_QUALITY_START")) {
                    if (idx < dataEndExclusive) {
                        int count = Integer.parseInt(lines.get(idx++));
                        for (int i = 0; i < count && idx < dataEndExclusive; i++) {
                            String[] qp = lines.get(idx++).split("\\|");
                            if (qp.length < 3) continue;
                            farms.getCropQualityBonusPool().put(qp[0], Double.parseDouble(qp[1]));
                            farms.getCropQualityUnits().put(qp[0], Integer.parseInt(qp[2]));
                        }
                    }
                    while (idx < dataEndExclusive && !lines.get(idx).equals("META_QUALITY_END")) idx++;
                    if (idx < dataEndExclusive) idx++;
                    continue;
                }
                if (line.equals("META_SPECIAL_ORDER") && idx < dataEndExclusive) {
                    String[] sp = lines.get(idx++).split("\\|");
                    if (sp.length >= 3) {
                        int orderDay = sp.length >= 4 ? Integer.parseInt(sp[3]) : farms.getGameDay();
                        farms.setSpecialOrder(sp[0], Double.parseDouble(sp[1]), Long.parseLong(sp[2]), orderDay);
                    }
                    continue;
                }
                if (line.equals("META_STORE_STOCK_START")) {
                    java.util.Map<String, Integer> restoredStock = new java.util.HashMap<>();
                    long restoredNextRestock = System.currentTimeMillis() + (5 * 60 * 1000L);
                    if (idx < dataEndExclusive) {
                        int count = Integer.parseInt(lines.get(idx++));
                        for (int i = 0; i < count && idx < dataEndExclusive; i++) {
                            String[] st = lines.get(idx++).split("\\|");
                            if (st.length < 2) continue;
                            restoredStock.put(st[0], Integer.parseInt(st[1]));
                        }
                        if (idx < dataEndExclusive) {
                            restoredNextRestock = Long.parseLong(lines.get(idx++));
                        }
                    }
                    StoreController.restoreSeedStocks(restoredStock, restoredNextRestock);
                    while (idx < dataEndExclusive && !lines.get(idx).equals("META_STORE_STOCK_END")) idx++;
                    if (idx < dataEndExclusive) idx++;
                    continue;
                }
                if (line.equals("META_SEASON") && idx < dataEndExclusive) {
                    String[] sp = lines.get(idx++).split("\\|");
                    if (sp.length >= 2) {
                        farms.setCurrentSeason(Farms.Season.valueOf(sp[0]));
                        farms.setNextSeasonChangeMs(Long.parseLong(sp[1]));
                        if (sp.length >= 3) {
                            farms.setGameDay(Integer.parseInt(sp[2]));
                        }
                    }
                    continue;
                }
                if (line.equals("META_TOOLS_START")) {
                    farms.getToolDurabilityMap().clear();
                    farms.getToolMaxDurabilityMap().clear();
                    if (idx < dataEndExclusive) {
                        int count = Integer.parseInt(lines.get(idx++));
                        for (int i = 0; i < count && idx < dataEndExclusive; i++) {
                            String[] tool = lines.get(idx++).split("\\|");
                            if (tool.length < 3) continue;
                            farms.getToolDurabilityMap().put(tool[0], Integer.parseInt(tool[1]));
                            farms.getToolMaxDurabilityMap().put(tool[0], Integer.parseInt(tool[2]));
                        }
                    }
                    while (idx < dataEndExclusive && !lines.get(idx).equals("META_TOOLS_END")) idx++;
                    if (idx < dataEndExclusive) idx++;
                    continue;
                }
                if (line.equals("META_COMBO") && idx < dataEndExclusive) {
                    String[] combo = lines.get(idx++).split("\\|");
                    if (combo.length >= 2) {
                        farms.setHarvestComboState(Integer.parseInt(combo[0]), Long.parseLong(combo[1]));
                    }
                    continue;
                }
                if (line.equals("META_ACHIEVEMENTS") && idx < dataEndExclusive) {
                    String[] meta = lines.get(idx++).split("\\|");
                    if (meta.length >= 4) {
                        farms.setTotalHarvested(Integer.parseInt(meta[0]));
                        farms.setTotalSold(Integer.parseInt(meta[1]));
                        farms.setTotalCompostUsed(Integer.parseInt(meta[2]));
                        farms.setPermanentSellBonus(Double.parseDouble(meta[3]));
                    }
                    if (idx < dataEndExclusive) {
                        int count = Integer.parseInt(lines.get(idx++));
                        for (int i = 0; i < count && idx < dataEndExclusive; i++) {
                            farms.getUnlockedAchievements().add(lines.get(idx++));
                        }
                    }
                    continue;
                }
                if (line.equals("META_TALENTS") && idx < dataEndExclusive) {
                    String[] meta = lines.get(idx++).split("\\|");
                    if (meta.length >= 4) {
                        farms.setTalentPoints(Integer.parseInt(meta[0]));
                        farms.setNarrativeChapter(Integer.parseInt(meta[1]));
                        farms.setGuildWeekIndex(Integer.parseInt(meta[2]));
                        farms.setGuildWeekStartDay(Integer.parseInt(meta[3]));
                    }
                    if (idx < dataEndExclusive) {
                        int count = Integer.parseInt(lines.get(idx++));
                        farms.getTalentRanks().clear();
                        for (int i = 0; i < count && idx < dataEndExclusive; i++) {
                            String[] t = lines.get(idx++).split("\\|");
                            if (t.length >= 2) {
                                farms.getTalentRanks().put(t[0], Integer.parseInt(t[1]));
                            }
                        }
                    }
                    continue;
                }
                if (line.equals("META_NARRATIVE") && idx < dataEndExclusive) {
                    farms.getCompletedNarrativeQuests().clear();
                    int count = Integer.parseInt(lines.get(idx++));
                    for (int i = 0; i < count && idx < dataEndExclusive; i++) {
                        farms.getCompletedNarrativeQuests().add(lines.get(idx++));
                    }
                    continue;
                }
                if (line.equals("META_GUILD") && idx < dataEndExclusive) {
                    farms.getWeeklyGuildQuests().clear();
                    int count = Integer.parseInt(lines.get(idx++));
                    for (int i = 0; i < count && idx < dataEndExclusive; i++) {
                        String[] g = lines.get(idx++).split("\\|");
                        if (g.length >= 6) {
                            Farms.GuildQuest quest = new Farms.GuildQuest(
                                    g[0], g[1], Integer.parseInt(g[2]), Double.parseDouble(g[3]), Integer.parseInt(g[4])
                            );
                            quest.claimed = Boolean.parseBoolean(g[5]);
                            farms.getWeeklyGuildQuests().add(quest);
                        }
                    }
                }
            }
            if (fromBackup) {
                System.out.println("Chargement effectue depuis le backup pour le slot " + slot);
            }
            return true;
        } catch (IOException e) {
            System.out.println("No Saves Found");
            return false;
        } catch (Exception e) {
            System.out.println("Erreur de chargement de la sauvegarde slot " + slot + (fromBackup ? " (.bak)" : "") + " : " + e.getMessage());
            return false;
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
