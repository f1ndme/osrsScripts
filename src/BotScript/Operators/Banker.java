package BotScript.Operators;

import BotScript.TaskManager;
import BotScript.UIManager;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.Shop;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;

import java.util.*;

public class Banker extends Operator {
    public TaskManager taskManager;
    public UIManager uiManager;
    public Banker(TaskManager taskManager, UIManager uiManager) {
        this.taskManager = taskManager;
        this.uiManager = uiManager;


    }







    @Override
    public boolean accept() {
        if (Players.getLocal() == null) {
            log("Player is null");
            return false;
        }

        if (!Players.getLocal().exists()) {
            log("Player does not exist");
            return false;
        }

        if (!containsOres()) {
            return false;
        }

        if (!Inventory.isFull() && !isBanking) { //isBanking fixes bank runs over night. only happens when half sold inventory, execute gets to bank then fails & doesnt retry.
            return false;
        }

        if (taskManager.positioner != null && !taskManager.positioner.alreadyArrived) {
            return false;
        }

        return true;
    }


    public boolean withinRangeGeneralStore() {
        log("Within range? " + (Players.getLocal().getTile().distance(Shops.DWARVENGENERAL.area.getCenter()) < 6));
        return Players.getLocal().getTile().distance(Shops.DWARVENGENERAL.area.getCenter()) < 6;
    }
    public boolean arrivedAtGeneralStore() {
        if (Players.getLocal() == null) return false; //ignore error connection?

        if (!Players.getLocal().isMoving()) {
            if (Walking.getDestination() != null) {
                if (Walking.getDestination().distance() < 4) {
                    if (Walking.walk(Shops.DWARVENGENERAL.area.getCenter())) {
                        Sleep.sleep(Calculations.random(301, 601));

                        return false;
                    }
                }
            }

            if (Walking.walk(Shops.DWARVENGENERAL.area.getCenter())) {
                Sleep.sleep(Calculations.random(301, 601));

                return false;
            }
        }

        return withinRangeGeneralStore();
    }



    public boolean withinRangeOreStore() {
        return Players.getLocal().getTile().distance(Shops.DWARVENORE.area.getCenter()) < 6;
    }
    public boolean arrivedAtOreStore() {
        if (Players.getLocal() == null) return false; //ignore error connection?

        if (!Players.getLocal().isMoving()) {
            if (Walking.getDestination() != null) {
                if (Walking.getDestination().distance() < 4) {
                    if (Walking.walk(Shops.DWARVENORE.area.getCenter())) {
                        Sleep.sleep(Calculations.random(301, 601));

                        return false;
                    }
                }
            }

            if (Walking.walk(Shops.DWARVENORE.area.getCenter())) {
                Sleep.sleep(Calculations.random(301, 601));

                return false;
            }
        }

        return withinRangeOreStore();
    }



    public boolean generalStoreOpen() {
        if (!Shop.isOpen()) {
            List<NPC> potentials = NPCs.all(npc -> npc.hasAction("Trade") && npc.getName().contains("Dwarf"));
            if (potentials.isEmpty()) {
                return false;
            }

            NPC dwarf = NPCs.all(npc -> npc.hasAction("Trade") && npc.getName().contains("Dwarf")).getFirst();
            if (dwarf != null) {
                if (dwarf.interact("Trade")) {
                    Sleep.sleepUntil(Shop::isOpen, 4201);

                    return false;
                }
            }
        }

        if (Players.getLocal().isMoving()) {
            return false;
        }

        return true;
    }

    public boolean oreStoreOpen() {
        if (!Shop.isOpen()) {
            List<NPC> potentials = NPCs.all(npc -> npc.hasAction("Trade") && npc.getName().contains("Drogo dwarf"));
            if (potentials.isEmpty()) {
                return false;
            }

            NPC drogo = NPCs.all(npc -> npc.hasAction("Trade") && npc.getName().contains("Drogo dwarf")).getFirst();
            if (drogo != null) {
                if (drogo.interact("Trade")) {
                    Sleep.sleepUntil(()->{return Players.getLocal() != null && Players.getLocal().isMoving();}, 1801);

                    Sleep.sleepUntil(()->{return Players.getLocal() != null && !Players.getLocal().isMoving();}, 1801);

                    Sleep.sleepUntil(Shop::isOpen, 1801);

                    return false;
                }
            }
        }

        if (Players.getLocal().isMoving()) {
            return false;
        }

        return true;
    }

    public boolean atDwarvenMines() {
        return (taskManager.positioner != null && taskManager.positioner.targetLocation != null && (taskManager.positioner.targetLocation == TaskManager.Locations.DWARVENNORTH || taskManager.positioner.targetLocation == TaskManager.Locations.DWARVENNORTHEAST || taskManager.positioner.targetLocation == TaskManager.Locations.DWARVENSOUTH));
    }

    public boolean hasUncuts() {
        return Inventory.contains(item -> item.getName().contains("Uncut"));
    }

    public boolean hasSellableOres() {
        return Inventory.contains(item -> item.getName().contains("ore") || item.getName().contains("Coal"));
    }
    public boolean soldAllOres() {
        List<Item> itemsToSell = Inventory.all(item -> item.getName().contains("ore") || item.getName().contains("Coal"));

        for (Item item : itemsToSell) {
            if (Shop.count(item.getName()) < 50) {
                if (Shop.sellFifty(item.getName())) {
                    Sleep.sleep(Calculations.random(801, 1201));
                }
            }
        }

        return true;
    }

    public boolean soldAllUncuts() {
        List<Item> itemsToSell = Inventory.all(item -> item.getName().contains("Uncut"));

        for (Item item : itemsToSell) {
            Sleep.sleepUntil(() -> { return Shop.sellFifty(item.getName()); }, 2401);
        }

        return true;
    }

    public boolean hasDroppableClay() {
        return Inventory.all(item -> item.getName().contains("Clay")).size() > 4;
    }
    public boolean droppedAllClay() {
        if (Inventory.dropAll(item -> item.getName().contains("Clay"))) {
            Sleep.sleepUntil(()->{ return !Inventory.contains(item -> item.getName().contains("Clay"));}, 12000);
        }

        return true;
    }

    public boolean isBanking;
    @Override
    public int execute() {
            // (!Calculations.chance(0, 2))


        if (atDwarvenMines() && !isBanking) {
                log("Full at Dwarven mines");

                if (hasSellableOres()) {
                    Sleep.sleepUntil(this::arrivedAtOreStore, this::playerMoving, 6001, 301);

                    Sleep.sleepUntil(this::oreStoreOpen, 3601);

                    Sleep.sleep(Calculations.random(1201, 1801));

                    Sleep.sleepUntil(this::soldAllOres, 6001);
                }

                if (hasUncuts()) {
                    Sleep.sleepUntil(this::arrivedAtGeneralStore, this::playerMoving, 6001, 301);

                    Sleep.sleepUntil(this::generalStoreOpen, 3601);

                    Sleep.sleep(Calculations.random(1201, 1801));

                    Sleep.sleepUntil(this::soldAllUncuts, 6001);
                }

                if (hasDroppableClay()) { //drop down to 4 pieces for lols.
                    if (Shop.isOpen()) {
                        Shop.close();
                        Sleep.sleep(Calculations.random(301, 601));
                    }

                    Sleep.sleepUntil(this::droppedAllClay, 12001);
                }

            if (!hasSellableOres() || Inventory.size() < 16) { //all ores sold, lets get more. Or inventory half full, get more. will continue to sell half offs until bank.
                if (taskManager.positioner != null) {
                    taskManager.positioner.alreadyArrived = false;
                }
                return 100;
            }

            isBanking = true;
        }




        Sleep.sleepUntil(this::bankScreenOpen, this::playerMoving, 15000, 601);

        if (bankScreenOpen()) {
            Sleep.sleepUntil(this::inventoryDeposited, 15000);

            taskManager.positioner.targetLocation = null;
            taskManager.positioner.alreadyArrived = false; //lol fck
            taskManager.miner.targetNode = null;
            isBanking = false;
        }

        return 100;
    }





    public boolean inventoryDeposited() {
        if (!containsOres()) {
            return true;
        }

        Bank.depositAll(item -> item.getName().contains("ore") || item.getName().contains("Clay") || item.getName().contains("Coal") || item.getName().contains("Uncut") || item.getName().contains("Clue") || item.getName().contains("scroll") || item.getName().contains("Coins"));

        return false;
    }

    public boolean containsOres() {
        return Inventory.contains(item -> item.getName().contains("ore") || item.getName().contains("Clay") || item.getName().contains("Coal") || item.getName().contains("Uncut") || item.getName().contains("Clue") || item.getName().contains("scroll") || item.getName().contains("Coins"));
    }

    public boolean bankScreenOpen() {
        if (Players.getLocal() == null) {
            return false;
        }

        if (Players.getLocal().isMoving()) {
            return false;
        }


        if (Bank.open(Bank.getClosestBankLocation(false))) {
            return true;
        }

        Sleep.sleepUntil(()->{return Players.getLocal().isMoving();}, 1801);

        Sleep.sleepUntil(() -> {return !Players.getLocal().isMoving();}, 1801);

        return false;
    }

    public boolean playerMoving() {
        if (Players.getLocal() == null) {
            log("Player is null");
            return true;
        }

        if (!Players.getLocal().exists()) {
            log("Player does not exist");
            return true;
        }

        return Players.getLocal().isMoving();
    }

    public enum Shops {
        DWARVENORE("Ore Store", new Area(3030, 9848, 3036, 9845)),
        DWARVENGENERAL("General Store", new Area(2996, 9829, 3000, 9826)),
        DWARVENPICKAXE("Pickaxe Store", new Area(2997, 9841, 3000, 9833));

        public final String name;
        public final Area area;

        Shops(String name, Area area) {
            this.name = name;
            this.area = area;
        }
    }




    public enum Pickaxes {
        BRONZE("Bronze pickaxe", 1),
        IRON("Iron pickaxe", 1),
        STEEL("Steel pickaxe", 6),
        BLACK("Black pickaxe", 11),
        MITHRIL("Mithril pickaxe", 21),
        ADAMANT("Adamant pickaxe", 31),
        RUNE("Rune pickaxe", 41),
        DRAGON("Dragon pickaxe", 61),
        INFERNAL("Infernal pickaxe", 61),
        THIRDAGE("3rd age pickaxe", 61),
        CRYSTAL("Crystal pickaxe", 71);

        public final String name;
        public final int level;

        Pickaxes(String name, int level) {
            this.name = name;
            this.level = level;
        }
    }
}

