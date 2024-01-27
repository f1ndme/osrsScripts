package BotScript.Operators;

import BotScript.TaskManager;
import BotScript.UIManager;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.script.event.impl.ExperienceEvent;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.map.TileReference;

import java.util.List;
import java.util.ArrayList;

public class MinerBase extends Operator implements UIManager.TextCommands {
    public MinerBase(UIManager uiManager, TaskManager taskManager) {
        super(uiManager, taskManager);

        generateMiningAccessorArrays();
    }
    public void onLevelUp(ExperienceEvent event) {
        if (event.getSkill() == Skill.MINING) {
            log("Mining level increased. Updating accessors");
            generateMiningAccessorArrays();
        }
    }









    public void generateMiningAccessorArrays() {
        buildUsablePickaxeNames();
        buildUsablePickaxeEnums();
        buildMinableRockNames();

        buildHighestUsablePickaxeEnum();
        buildMinableLocationsEnum();
    }





    public List<String> usablePickaxeNames; //todo GOOD
    public void buildUsablePickaxeNames() { //todo STRING Names List
        usablePickaxeNames = new ArrayList<>();

        for (Items pickaxe : Items.values()) {
            if (pickaxe.name.contains("pickaxe")) {
                if (pickaxe.requiredLevel <= Skills.getRealLevel(Skill.MINING)) {
                    usablePickaxeNames.add(pickaxe.name);
                }
            }
        }
    }

    public List<Items> usablePickaxeEnums; //todo GOOD
    public void buildUsablePickaxeEnums() { //todo ENUM List
        usablePickaxeEnums = new ArrayList<>();

        for (Items pickaxe : Items.values()) {
            if (pickaxe.name.contains("pickaxe")) {
                if (pickaxe.requiredLevel <= Skills.getRealLevel(Skill.MINING)) {
                    usablePickaxeEnums.add(pickaxe);
                }
            }
        }
    }

    public List<String> minableRockNames; //todo GOOD
    public void buildMinableRockNames() { //todo STRING Names List
        minableRockNames = new ArrayList<>();

        for (Objects rock : Objects.values()) {
            if (rock.name.contains("rocks")) {
                if (rock.requiredLevel <= Skills.getRealLevel(Skill.MINING)) {
                    minableRockNames.add(rock.name);
                }
            }
        }
    }

    Items highestUsablePickaxeEnum; //todo GOOD I THINK
    public void buildHighestUsablePickaxeEnum() { //todo ENUM

        int winning = 0;
        Items winner = Items.BRONZEPICKAXE;
        for (Items pickaxeEnum : usablePickaxeEnums) {
            if (pickaxeEnum.requiredLevel >= winning && pickaxeEnum.requiredLevel <= Skills.getRealLevel(Skill.MINING)) {
                winning = pickaxeEnum.requiredLevel;
                winner = pickaxeEnum;
            }
        }

        highestUsablePickaxeEnum = winner;
    }

    public List<Locations> minableLocationsEnum; //todo I think this is good. this is better as enum list? should they all just be like normally. check usages
    public void buildMinableLocationsEnum() {
        minableLocationsEnum = new ArrayList<>();

        for (Objects rocks : Objects.values()) {
            if (rocks.name.contains("rocks")) {
                if (rocks.requiredLevel <= Skills.getRealLevel(Skill.MINING)) {
                    for (Locations location : rocks.locations) {
                        if (!minableLocationsEnum.contains(location)) {
                            minableLocationsEnum.add(location);
                        }
                    }
                }
            }
        }
    }


















//todo these down here are all good to be here. vvvvvvvvvvvvvvvvvvvv


    public boolean clayIsUnder(int minDropAmount) {
        return Inventory.all(item -> item.getName().contains("Clay")).size() > minDropAmount;
    }
    public boolean droppedAllClay(int minDropAmount) {
        if (!clayIsUnder(minDropAmount)) {
            return true;
        }

        Inventory.drop(item -> item.getName().contains("Clay"));
        Sleep.sleep(Calculations.random(801, 1201));
        return false;
    }
    public boolean hasPickaxe() {
        return (Equipment.contains(item -> item.getName().contains("pickaxe")) || Inventory.contains(item -> item.getName().contains("pickaxe")));
    }
    public boolean atDwarvenMines() { //check closest, is of 3 dwarven mines.
        Player pl = Players.getLocal();
        Tile plTile = pl.getTile();
        if (!pl.exists() || plTile == null) return false;

        double winning = 100000;
        Locations winner = null;
        for (Locations location : minableLocationsEnum) {
            Tile center = location.area.getCenter().getTile();
            if (center.distance(plTile) < winning) {
                winning = center.distance(plTile);
                winner = location;
            }
        }
        if (winner != null) {
            if (winner == Locations.DWARVENNORTH || winner == Locations.DWARVENNORTHEAST || winner == Locations.DWARVENSOUTH) {
                return true;
            }
        }

        return false;
    }
    public Locations RandomDwarvenMine() {
        Locations dwarvenLocation = Locations.DWARVENNORTH;

        int random = Calculations.random(1, 3);
        if (random == 1) {
            dwarvenLocation = Locations.DWARVENSOUTH;
        }else if (random == 2) {
            dwarvenLocation = Locations.DWARVENNORTH;
        }else if (random == 3) {
            dwarvenLocation = Locations.DWARVENNORTHEAST;
        }

        return dwarvenLocation;
    }
    public Item highestPickaxeInInventory() {
        if (Inventory.contains(item -> item.getName().contains("pickaxe"))) {
            List<Item> heldPickaxes = Inventory.all(item -> item.getName().contains("pickaxe"));

            int winning = 0;
            Item winner = null;
            if (!heldPickaxes.isEmpty()) {
                for (Item pickaxe : heldPickaxes) {
                    Items pickaxeEnum = enumFromName(pickaxe.getName());
                    if (pickaxeEnum.requiredLevel > winning) {
                        winning = pickaxeEnum.requiredLevel;
                        winner = pickaxe;
                    }
                }
                return winner;
            }
        }

        return null;
    }
    public void hopOnCongestedMining() {
        Player pl = Players.getLocal();
        Tile plTile = pl.getTile();
        if (!pl.exists() || plTile == null) return;

        java.util.List<Player> playersList = Players.all(player -> player.getTile().distance(plTile) < 2);

        if (playersList.size() >= 3) {
            log("Too many players, hopping world.");
            hopRandomFreeWorld();
        }
    }
    public Locations randomLocation;
    public void setRandomLocationToMine() {
        int randomKey = Calculations.random(0, minableLocationsEnum.size());
        randomLocation = minableLocationsEnum.get(randomKey);
    }
    public Locations closestMinableLocation() {
        Player pl = Players.getLocal();
        Tile plTile = pl.getTile();

        Locations winner = Locations.LUMBRIDGE; //this saves us when falling in barbarian trap. dont remove it.
        if (!pl.exists() || plTile == null) return winner;

        double winning = 1000000;
        for (Locations location : minableLocationsEnum) {
            if (plTile.distance(location.area.getCenter().getTile()) < winning) {
                winning = plTile.distance(location.area.getCenter().getTile());
                winner = location;
            }
        }

        return winner;
    }
    public List<GameObject> allReachable() {
        return GameObjects.all(object -> object.hasAction("Mine") && object.distance(Players.getLocal().getTile()) <= 9 && object.getModelColors() != null);
    }
    public List<GameObject> minableReachable() {
        List<GameObject> newList = new ArrayList<>();

        for (GameObject object : allReachable()) {
            if (minableRockNames.contains(object.getName())) {
                newList.add(object);
            }
        }

        return newList;
    }
    public GameObject furthestReachable() {
        Player pl = Players.getLocal();
        Tile plTile = pl.getTile();
        if (!pl.exists() || plTile == null) return null;

        double winning = 0;
        GameObject winner = null;
        for (GameObject object : minableReachable()) {
            if (object.distance(plTile) > winning) {
                winning = object.distance(plTile);
                winner = object;
            }
        }

        return winner;
    }
    public GameObject closestReachable() {
        Player pl = Players.getLocal();
        Tile plTile = pl.getTile();
        if (!pl.exists() || plTile == null) return null;

        double winning = 100000;
        GameObject winner = null;
        for (GameObject object : minableReachable()) {
            if (object.distance(plTile) < winning) {
                winning = object.distance(plTile);
                winner = object;
            }
        }

        return winner;
    }
    public GameObject objectFrom(Tile tile) {
        TileReference tileReference = tile.getTileReference();
        if (tileReference == null || tileReference.getObjects().length == 0) {
            return null;
        }

        return tileReference.getObjects()[0];
    }






}
