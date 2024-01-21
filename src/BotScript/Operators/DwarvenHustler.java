package BotScript.Operators;

import BotScript.TaskManager;
import BotScript.UIManager;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.Shop;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;

import java.util.ArrayList;
import java.util.List;

import static BotScript.Operators.Operator.States.*;
import static BotScript.Operators.Operator.States.WAITING;

public class DwarvenHustler extends Operator implements UIManager.TextCommands {
    public DwarvenHustler(UIManager uiManager, TaskManager taskManager) {
        this.uiManager = uiManager;
        this.taskManager = taskManager;

        buildTextNotifier(this, uiManager);
        setCurrentState(WAITING);
        notifier.text = "(HUSTLER)";
        notifier.y = Client.getViewportHeight() - 215;
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

    @Override
    public boolean accept() {

        return true;
    }



    public boolean hasSellableOres() {
        return Inventory.contains(item -> item.getName().contains("ore") || item.getName().contains("Coal"));
    }
    public boolean hasUncuts() {
        return Inventory.contains(item -> item.getName().contains("Uncut"));
    }





    public boolean withinRangeGeneralStore() {
        Player pl = Players.getLocal();
        Tile plTile = pl.getTile();
        if (!pl.exists() || plTile == null) return false;

        return plTile.distance(Shops.DWARVENGENERAL.area.getCenter()) < 3;
    }
    public boolean arrivedAtGeneralStore() {
        Player pl = Players.getLocal();

        if (!pl.exists()) return false;

        if (!pl.isMoving()) {
            if (Walking.getDestination() != null) {
                if (Walking.getDestination().distance() < 5) {
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
        Player pl = Players.getLocal();
        Tile plTile = pl.getTile();
        if (!pl.exists() || plTile == null) return false;

        return plTile.distance(Shops.DWARVENORE.area.getCenter()) < 3;
    }
    public boolean arrivedAtOreStore() {
        Player pl = Players.getLocal();

        if (!pl.exists()) return false;

        if (!pl.isMoving()) {
            if (Walking.getDestination() != null) {
                if (Walking.getDestination().distance() < 5) {
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

    public boolean oreStoreOpen() {
        Player pl = Players.getLocal();
        if (!pl.exists()) return false;

        if (!Shop.isOpen()) {
            List<NPC> potentials = NPCs.all(npc -> npc.hasAction("Trade") && npc.getName().contains("Drogo dwarf"));
            if (potentials.isEmpty()) {
                return false;
            }

            NPC drogo = NPCs.all(npc -> npc.hasAction("Trade") && npc.getName().contains("Drogo dwarf")).getFirst();
            if (drogo != null && drogo.exists()) {
                if (drogo.interact("Trade")) {
                    Sleep.sleepUntil(this::playerStartedMoving, 1801);

                    Sleep.sleepUntil(this::playerNotMoving, 1801);

                    Sleep.sleepUntil(Shop::isOpen, 1801);

                    return true;
                }
            }
        }

        if (pl.isMoving()) {
            return false;
        }

        return false;
    }

    public boolean generalStoreOpen() {
        Player pl = Players.getLocal();
        if (!pl.exists()) return false;

        if (!Shop.isOpen()) {
            List<NPC> potentials = NPCs.all(npc -> npc.hasAction("Trade") && npc.getName().contains("Dwarf"));
            if (potentials.isEmpty()) {
                return false;
            }

            NPC dwarf = NPCs.all(npc -> npc.hasAction("Trade") && npc.getName().contains("Dwarf")).getFirst();
            if (dwarf != null && dwarf.exists()) {
                if (dwarf.interact("Trade")) {
                    Sleep.sleepUntil(this::playerStartedMoving, 1801);

                    Sleep.sleepUntil(this::playerNotMoving, 1801);

                    Sleep.sleepUntil(Shop::isOpen, 1801);

                    return true;
                }
            }
        }

        if (pl.isMoving()) {
            return false;
        }

        return false;
    }

    public enum Sell {
        COPPER("Copper ore", 9),
        TIN("Tin ore", 9),
        IRON("Iron ore", 50),
        COAL("Coal", 50);

        final String name;
        final int limit;

        Sell(String name, int limit) {
            this.name = name;
            this.limit = limit;
        }
    }

    public int limitFromName(String name) {
        for (Sell thing : Sell.values()) {
            if (thing.name.equals(name)) {
                return thing.limit;
            }
        }

        return 50;
    }

    public boolean soldAllOres() {
        if (Shop.isOpen()) {
            List<Item> itemsList = Inventory.all(item -> item.getName().contains("ore") || item.getName().contains("Coal"));

            ArrayList<String> typesList = new ArrayList<>();
            for (Item item : itemsList) {
                if (!typesList.contains(item.getName()) && Inventory.contains(item.getName())) {
                    typesList.add(item.getName());
                }
            }

            ArrayList<Item> sellList = new ArrayList<>();
            for (Item item : Inventory.all()) {
                if (item != null) {
                    if (!sellList.contains(item) && typesList.contains(item.getName())) {
                        sellList.add(item);
                    }
                }
            }

            for (Item item : sellList) {
                if (!Shop.isOpen()) return false;

                if (Shop.count(item.getName()) < limitFromName(item.getName())) {
                    oresSold++;
                    Sleep.sleepUntil(() -> {return Shop.sellFifty(item);}, 1801);
                    Sleep.sleep(Calculations.random(801, 1201));
                }
            }

            //could do inv check, but it might have left over if shop is full.
            return true;

        }

        return false;
    }

    public boolean soldAllUncuts() {
            if (Shop.isOpen()) {
                List<Item> itemsList = Inventory.all(item -> item.getName().contains("Uncut"));

                ArrayList<String> typesList = new ArrayList<>();
                for (Item item : itemsList) {
                    if (!typesList.contains(item.getName()) && Inventory.contains(item.getName())) {
                        typesList.add(item.getName());
                    }
                }

                ArrayList<Item> sellList = new ArrayList<>();
                for (Item item : Inventory.all()) {
                    if (item != null) {
                        if (!sellList.contains(item) && typesList.contains(item.getName())) {
                            sellList.add(item);
                        }
                    }
                }

                for (Item item : sellList) {
                    if (!Shop.isOpen()) return false;

                    if (Shop.count(item.getName()) < 50) {
                        uncutsSold++;
                        Sleep.sleepUntil(() -> {return Shop.sellFifty(item);}, 1801);
                        Sleep.sleep(Calculations.random(801, 1201));
                    }
                }


                //could do inv check, but it might have left over if shop is full.
                return true;

            }

            return false;
    }

    public boolean hasDroppableClay() {
        return Inventory.all(item -> item.getName().contains("Clay")).size() > 4;
    }
    public boolean droppedAllClay() {
        if (Inventory.all(item -> item.getName().contains("Clay")).size() < 5) {
            return true;
        }

        if (Inventory.drop(item -> item.getName().contains("Clay"))) {
            Sleep.sleep(Calculations.random(801, 1201));
        }

        return false;
    }

    public boolean oresChecked;
    public boolean uncutsChecked;
    public int oresSold;
    public int uncutsSold;
    @Override
    public int execute() {
        setCurrentState(OPERATING);

        if (!oresChecked) {
            if (hasSellableOres()) {
                Sleep.sleepUntil(this::arrivedAtOreStore, this::playerStartedMoving, 6001, 301);

                if (!withinRangeOreStore()) return 100;

                Sleep.sleepUntil(this::oreStoreOpen, 3601);

                if (!Shop.isOpen()) return 100;

                Sleep.sleep(Calculations.random(1201, 1801));

                Sleep.sleepUntil(this::soldAllOres, 6001);

                if (Shop.isOpen()) {
                    Shop.close();
                    Sleep.sleep(Calculations.random(301, 601));
                }
            }

            oresChecked = true;
        }


        if (!uncutsChecked) {
            if (hasUncuts()) {
                Sleep.sleepUntil(this::arrivedAtGeneralStore, this::playerStartedMoving, 6001, 301);

                if (!withinRangeGeneralStore()) return 100;

                Sleep.sleepUntil(this::generalStoreOpen, 3601);

                if (!Shop.isOpen()) return 100;

                Sleep.sleep(Calculations.random(1201, 1801));

                Sleep.sleepUntil(this::soldAllUncuts, 6001);

                if (Shop.isOpen()) {
                    Shop.close();
                    Sleep.sleep(Calculations.random(301, 601));
                }
            }

            uncutsChecked = true;
        }


        if (uncutsSold < 1 && oresSold < 1 || (Inventory.emptySlotCount() < 5)) {
            log("(HUSTLER) Dispatching banker...");
            taskManager.banker = new Banker(uiManager, taskManager);
            taskManager.addNodes(taskManager.banker);
        }else {
            taskManager.miner.setCurrentState(WAITING);

            if (hasDroppableClay()) { //drop down to 4 pieces, get more to sell, so we move through things quicker.
                if (Shop.isOpen()) {
                    Shop.close();
                    Sleep.sleep(Calculations.random(301, 601));
                }

                Sleep.sleepUntil(this::droppedAllClay, 12001);
            }

            log("(HUSTLER) Dispatching positioner...");
            int random = Calculations.random(1, 3);
            if (random == 1) {
                taskManager.positioner = new Positioner(uiManager, taskManager, TaskManager.Locations.DWARVENSOUTH);
            }else if (random == 2) {
                taskManager.positioner = new Positioner(uiManager, taskManager, TaskManager.Locations.DWARVENNORTH);
            }else if (random == 3) {
                taskManager.positioner = new Positioner(uiManager, taskManager, TaskManager.Locations.DWARVENNORTHEAST);
            }
            taskManager.addNodes(taskManager.positioner);
        }

        //if it banks, why drop it.
        /*if (hasDroppableClay()) { //drop down to 4 pieces for lols.
            if (Shop.isOpen()) {
                Shop.close();
                Sleep.sleep(Calculations.random(301, 601));
            }

            Sleep.sleepUntil(this::droppedAllClay, 12001);
        }*/



        taskManager.removeOperator(this); //sold what we can, bail.

        setCurrentState(WAITING);
        return 100;
    }
}
