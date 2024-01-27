package BotScript.Operators;

import BotScript.Elements.DualText;
import BotScript.TaskManager;
import BotScript.UIManager;
import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.world.World;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.script.event.impl.ExperienceEvent;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class Operator extends TaskNode implements UIManager.TextCommands {
    public boolean stillSolving;
    public Locations currentLocation;
    public Operator(UIManager uiManager, TaskManager taskManager) {
        this.uiManager = uiManager;
        this.taskManager = taskManager;

        buildEventNotifier(this, uiManager);
        setEventState(EventState.WAITING);
        eventNotifier.text = "(Event)";
        buildExecutionNotifier(this, uiManager);
        executionNotifier.text = "(Operator)";
        setExecutionCase(ExecutionCase.WAITING);

        generateAccessorArrays();

        currentLocation = closestLocation();
    }


    public UIManager uiManager;
    public TaskManager taskManager;
    public DualText executionNotifier;
    public DualText eventNotifier;
    public ExecutionCase executionCase;
    public EventState eventState;
    public enum ExecutionCase {
        SMELTBRONZEBAR("Going to smelt a bronze bar...", Color.green, Color.white),
        COLLECTBRONZEORES("No ores for bronze bar...", Color.yellow, Color.white),
        NOAXE("No axe...", Color.yellow, Color.white),
        WAITING("Waiting...", Color.red, Color.white),
        ORESTOMINE("Ores to mine...", Color.green, Color.white),
        FULLINVENTORYDWARVEN("Selling ores & uncuts...", Color.green, Color.white),
        FULLINVENTORY("Inventory is full...", Color.yellow, Color.white),
        NOPICKAXE("No pickaxe...", Color.yellow, Color.white),
        NOMINABLE("No minable ores near...", Color.yellow, Color.white);
        final String text;
        final Color color;
        final Color colorTwo;
        ExecutionCase(String text, Color level, Color colorTwo) {
            this.text = text;
            this.color = level;
            this.colorTwo = colorTwo;
        }
    }
    public enum EventState {
        SELLINGCOAL("Selling coal at dwarven ore store...", Color.green, Color.white),
        PURCHASEPICKAXE("Purchasing a better pickaxe from dwarven pickaxe store...", Color.green, Color.white),
        WAITING("Waiting...", Color.yellow, Color.white),
        LOCATIONCLOSEST("Going to closest minable location...", Color.green, Color.white),
        LOCATIONRANDOM("Going to random minable location...", Color.green, Color.white),
        GOINGTOBANK("Going to the bank...", Color.green, Color.white),
        MININGATTEMPT("Attempting to mine...", Color.yellow, Color.white),
        MINING("Mining...", Color.green, Color.white),
        PICKAXEBANKCHECK("Checking bank for pickaxe...", Color.green, Color.white),
        PICKAXEFREECHECK("Checking free bronze pickaxe spot...", Color.green, Color.white),
        PICKAXEFREEWAIT("Pickaxe will spawn on table very shortly... Then we will grab it.", Color.green, Color.white),
        FULLORESHOP("Ore shop is full. Going to bank what we have...", Color.green, Color.white),
        SELLINGORES("Checking if Drogo will pay for any of these ores...", Color.green, Color.white),
        SELLINGUNCUTS("Checking if General Dwarf will buy any of these uncuts...", Color.green, Color.white),
        DROPPINGCLAY("We made profit. Dropping clay, so we have more space for profit...", Color.green, Color.white),
        LOCATIONDWARVEN("Going back to Dwarven minable location for profit...", Color.green, Color.white);

        final String text;
        final Color color;
        final Color colorTwo;

        EventState(String text, Color level, Color colorTwo) {
            this.text = text;
            this.color = level;
            this.colorTwo = colorTwo;
        }
    }
    public enum Shops {
        DWARVENORE("Dwarven Ore Store", Locations.DWARVENORESHOP,  5895, asList(Items.COPPER, Items.TIN, Items.IRON, Items.COAL)),
        DWARVENGENERAL("Dwarven General Store", Locations.DWARVENGENERALSHOP,  5904, asList(Items.UNCUTSAPPHIRE, Items.UNCUTEMERALD, Items.UNCUTRUBY, Items.UNCUTDIAMOND)),
        DWARVENPICKAXE("Dwarven Pickaxe Store", Locations.DWARVENPICKAXESHOP,  8686, asList(Items.BRONZEPICKAXE, Items.IRONPICKAXE, Items.STEELPICKAXE, Items.MITHRILPICKAXE, Items.ADAMANTPICKAXE, Items.RUNEPICKAXE));

        public final String name;
        public final Locations location;
        public final int shopID;
        public final List<Items> containingItems;

        Shops(String name, Locations location, int shopID, List<Items> containingItems) {
            this.name = name;
            this.location = location;
            this.shopID = shopID;
            this.containingItems = containingItems;
        }
    }
    public enum Locations {
        VARROCKSOUTHEAST("Varrock SE", new Area(3282, 3363, 3289, 3361)),
        BARBARIANVILLAGE("Barbarian Village", new Area(3078, 3424, 3084, 3417)),
        DWARVENNORTHEAST("NE Dwarven Cave", new Area(3051, 9827, 3054, 9817)),
        DWARVENNORTH("N Dwarven Cave", new Area(3029, 9828, 3032, 9823)),
        DWARVENSOUTH("S Dwarven Cave", new Area(3023, 9806, 3027, 9800)),
        RIMMINGTON("Rimmington", new Area(2969, 3245, 2985, 3229)),
        LUMBRIDGE("Lumbridge", new Area(3225, 3151, 3231, 3145)),
        DRAYNOR("Draynor", new Area(3145, 3154, 3149, 3145)),
        DWARVENORESHOP("Dwarven Ore Store", new Area(3030, 9848, 3036, 9845)),
        DWARVENGENERALSHOP("Dwarven General Store", new Area(2996, 9829, 3000, 9826)),
        DWARVENPICKAXESHOP("Dwarven Pickaxe Store", new Area(2995, 9849, 2999, 9841)),
        FREEPICKAXE("Free Pickaxe Barbarian Village", new Area(3082, 3431, 3084, 3429));

        public final String name;
        public final Area area;

        Locations(String name, Area area) {
            this.name = name;
            this.area = area;
        }
    }
    public enum Objects {
        COPPER("Copper rocks", 1, asList(Locations.LUMBRIDGE, Locations.RIMMINGTON, Locations.DWARVENNORTH, Locations.DWARVENSOUTH, Locations.VARROCKSOUTHEAST), Items.COPPER),
        TIN("Tin rocks", 1, asList(Locations.LUMBRIDGE, Locations.RIMMINGTON, Locations.DWARVENNORTHEAST, Locations.DWARVENNORTH, Locations.BARBARIANVILLAGE, Locations.VARROCKSOUTHEAST), Items.TIN),
        CLAY("Clay rocks", 1, asList(Locations.RIMMINGTON, Locations.RIMMINGTON, Locations.DWARVENNORTHEAST, Locations.DWARVENSOUTH), Items.IRON),
        IRON("Iron rocks", 15, asList(Locations.RIMMINGTON, Locations.RIMMINGTON, Locations.DWARVENNORTHEAST, Locations.DWARVENNORTH, Locations.VARROCKSOUTHEAST), Items.IRON),
        COAL("Coal rocks", 30, asList(Locations.DRAYNOR, Locations.DRAYNOR, Locations.BARBARIANVILLAGE), Items.COAL),
        GOLD("Gold rocks", 40, asList(Locations.RIMMINGTON, Locations.RIMMINGTON), Items.GOLD),
        MITHRIL("Mithril rocks", 55, asList(Locations.DRAYNOR, Locations.DRAYNOR), Items.MITHRIL),
        ADAMANT("Adamant rocks", 70, asList(Locations.DRAYNOR, Locations.DRAYNOR), Items.ADAMANT);

        public final String name;
        public final int requiredLevel;
        public final List<Locations> locations;
        public final Items producingItem;

        Objects(String name, int requiredLevel, List<Locations> location, Items producingItem) {
            this.name = name;
            this.requiredLevel = requiredLevel;
            this.locations = location;
            this.producingItem = producingItem;
        }
    }

    public enum Items {
        COPPER(436, "Copper ore", 11, -1, -1, 3),
        TIN(438, "Tin ore", 11, -1, -1, 3),
        CLAY(434, "Clay", 50, -1, -1, -1),
        IRON(440, "Iron ore", 14, -1, -1, 11),
        COAL(453, "Coal", 11, -1, -1, 31),
        GOLD(444, "Gold ore", 50, -1, -1, -1),
        MITHRIL(447, "Mithril ore", 50, -1, -1, -1),
        ADAMANT(449, "Adamant ore", 50, -1, -1, -1),
        BRONZEPICKAXE(1265, "Bronze pickaxe", 50, 1, 1, -1),
        IRONPICKAXE(1267, "Iron pickaxe", 50, 1, 140, -1),
        STEELPICKAXE(1269, "Steel pickaxe", 50, 6, 500, -1),
        BLACKPICKAXE(12297, "Black pickaxe", 50, 11, -1, -1),
        MITHRILPICKAXE(1273, "Mithril pickaxe", 50, 21, 1300, -1),
        ADAMANTPICKAXE(1271, "Adamant pickaxe", 50, 31, 3200, -1),
        RUNEPICKAXE(1275, "Rune pickaxe", 50, 41, 32000, -1),
        DRAGONPICKAXE(11920, "Dragon pickaxe", 50, 61, -1, -1),
        INFERNALPICKAXE(13243, "Infernal pickaxe", 50, 61, -1, -1),
        THIRDAGEPICKAXE(20014, "3rd age pickaxe", 50, 61, -1, -1),
        CRYSTALPICKAXE(23680, "Crystal pickaxe", 50, 71, -1, -1),
        UNCUTSAPPHIRE(23680, "Uncut sapphire", 20, -1, -1, 15),
        UNCUTEMERALD(23680, "Uncut emerald", 20, -1, -1, 30),
        UNCUTRUBY(23680, "Uncut ruby", 20, -1, -1, 60),
        UNCUTDIAMOND(23680, "Uncut diamond", 20, -1, -1, 120),
        BRONZEAXE(1, "Bronze axe", 50, 1, 1, -1),
        IRONAXE(1, "Iron axe", 50, 1, -1, -1),
        STEELAXE(1, "Steel axe", 50, 6, -1, -1),
        BLACKAXE(1, "Black axe", 50, 11, -1, -1),
        MITHRILAXE(1, "Mithril axe", 50, 21, -1, -1),
        ADAMANTAXE(1, "Adamant axe", 50, 31, -1, -1),
        RUNEAXE(1, "Rune axe", 50, 41, -1, -1),
        DRAGONAXE(1, "Dragon axe", 50, 61, -1, -1),
        CRYSTALAXE(1, "Crystal axe", 50, 71, -1, -1);

        public final int id;
        public final String name;
        public final int storeBuyLimit;
        public final int requiredLevel;
        public final int cost;
        public final int worth;

        Items(int id, String name, int storeBuyLimit, int requiredLevel, int cost, int worth) {
            this.id = id;
            this.name = name;
            this.storeBuyLimit = storeBuyLimit;
            this.requiredLevel = requiredLevel;
            this.cost = cost;
            this.worth = worth;
        }
    }
    public void buildExecutionNotifier(Operator operator, UIManager uiManager) {
        executionNotifier = uiManager.DualText("(Miner) ", ExecutionCase.WAITING.text, 5, Client.getViewportHeight()-200, Color.green, Color.white, operator);
    }
    public void buildEventNotifier(Operator operator, UIManager uiManager) {
        eventNotifier = uiManager.DualText("(Miner) ", EventState.WAITING.text, 5, Client.getViewportHeight()-185, Color.green, Color.white, operator);
    }
    public void executionStateChanged(ExecutionCase last, ExecutionCase current) {
        if (executionNotifier != null) {
            executionNotifier.textTwo = current.text;
            executionNotifier.color = current.color;
            executionNotifier.colorTwo = current.colorTwo;
        }
    }
    public void eventStateChanged(EventState last, EventState current) {
        if (eventNotifier != null) {
            eventNotifier.textTwo = current.text;
            eventNotifier.color = current.color;
            eventNotifier.colorTwo = current.colorTwo;
        }
    }
    public void setExecutionCase(ExecutionCase executionCase) {
        executionStateChanged(this.executionCase, executionCase);

        this.executionCase = executionCase;
    }
    public void setEventState(EventState eventState) {
        eventStateChanged(this.eventState, eventState);

        this.eventState = eventState;
    }

    public void onTextCommandPressed(int id) {}
    public void onDualTextPressed(int id) {}

    @Override
    public boolean accept() {

        return true;
    }

    @Override
    public int execute() {

        return 100;
    }

    public void prePaint(Graphics g) {}
    public void paint(Graphics g) {}
    public void postPaint(Graphics g) {}
    public void onInventoryItemAdded(Item item) {}
    public void onLevelUp(ExperienceEvent event) {}







































    //FUNCTIONS///////////////////////////////////////////////////// todo check these.
    public Locations closestLocation() {
        Player pl = Players.getLocal();
        Tile plTile = pl.getTile();
        if (!pl.exists() || plTile == null) return null;

        double winning = 100000;
        Locations winner = null;
        for (Locations location : allLocations) {
            if (location.area.getCenter().getTile().distance(plTile) < winning) {
                winning = location.area.getCenter().getTile().distance(plTile);
                winner = location;
            }
        }

        return winner;
    }

    public Locations closestLocationNotThis() {
        Player pl = Players.getLocal();
        Tile plTile = pl.getTile();
        if (!pl.exists() || plTile == null) return null;

        double winning = 100000;
        Locations winner = null;
        for (Locations location : allLocations) {
            if (location != currentLocation) {
                if (location.area.getCenter().getTile().distance(plTile) < winning) {
                    winning = location.area.getCenter().getTile().distance(plTile);
                    winner = location;
                }
            }
        }

        return winner;
    }
    public boolean atCenterOf(Locations targetLocation) {
        Player pl = Players.getLocal();
        Tile plTile = pl.getTile();
        Tile targetTile = targetLocation.area.getCenter().getTile();
        if (!pl.exists() || plTile == null || targetTile == null) return false;

        return plTile.distance(targetTile) <= 3;
    }
    public boolean arrivedAt(Locations targetLocation) {
        Player pl = Players.getLocal();
        Tile plTile = pl.getTile();
        Tile targetTile = targetLocation.area.getCenter().getTile();
        if (!pl.exists() || plTile == null || targetTile == null) return false;

        if (atCenterOf(targetLocation)) {
            return true;
        }

        if (!pl.isMoving() || Walking.isRunEnabled()? Walking.shouldWalk(9) : Walking.shouldWalk(5)) {
            if (Walking.walk(targetTile)) {
                if ( !pl.isMoving() ) {
                    Sleep.sleepUntil(this::playerMoving, 1201);
                }else {
                    Sleep.sleepUntil(this::playerNotMoving, 1201);
                }
            }
        }

        return false;
    }

    public boolean atCenterOf(BankLocation targetLocation) {
        Player pl = Players.getLocal();
        Tile plTile = pl.getTile();
        Tile targetTile = targetLocation.getCenter().getTile();
        if (!pl.exists() || plTile == null || targetTile == null) return false;

        return plTile.distance(targetTile) <= 3;
    }
    public boolean arrivedAt(BankLocation targetLocation) {
        Player pl = Players.getLocal();
        Tile plTile = pl.getTile();
        Tile targetTile = targetLocation.getCenter().getTile();
        if (!pl.exists() || plTile == null || targetTile == null) return false;

        if (atCenterOf(targetLocation)) {
            return true;
        }

        if (!pl.isMoving() || (Walking.isRunEnabled()? Walking.shouldWalk(9) : Walking.shouldWalk(5))) {
            if (Walking.walk(targetTile)) {
                if ( !pl.isMoving() ) {
                    Sleep.sleepUntil(this::playerMoving, 1201);
                }else {
                    Sleep.sleepUntil(this::playerNotMoving, 1201);
                }
            }
        }

        return false;
    }

    public boolean playerMoving() {
        Player pl = Players.getLocal();

        return pl.exists() && pl.isMoving();
    }

    public boolean playerNotMoving() {
        Player pl = Players.getLocal();

        return pl.exists() && !pl.isMoving();
    }
    public boolean playerAnimating() {
        Player pl = Players.getLocal();

        return pl.exists() && pl.isAnimating();
    }

    public void hopRandomFreeWorld() {
        List<World> validWorlds = new ArrayList<>();

        for (World world : Worlds.getNormalizedWorlds()) {
            if (Worlds.f2p().contains(world) && Worlds.noMinimumLevel().contains(world) && !validWorlds.contains(world)) {
                validWorlds.add(world);
            }
        }
        World randomWorld = Worlds.getRandomWorld(validWorlds);

        WorldHopper.hopWorld(randomWorld);
    }


































    public Items enumFromName(String itemName) { //todo good
        for (Items item : Items.values()) {
            if (itemName.equals(item.name)) {
                return item;
            }
        }

        return null;
    }








    public void generateAccessorArrays() {
        buildAllLocations();
        buildAllItems();

    }

    public List<Locations> allLocations; //todo good
    public void buildAllLocations() { //todo Locations List
        allLocations = new ArrayList<>();

        allLocations.addAll(asList(Locations.values()));
    }

    public List<Items> allItems;  //todo good
    public void buildAllItems() { //todo Items List
        allItems = new ArrayList<>();

        allItems.addAll(asList(Items.values()));
    }


}
