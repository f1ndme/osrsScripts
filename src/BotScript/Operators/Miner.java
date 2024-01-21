package BotScript.Operators;

import BotScript.TaskManager;
import BotScript.UIManager;
import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.map.TileReference;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static BotScript.Operators.Operator.States.*;

public class Miner extends Operator implements UIManager.TextCommands {
    public Miner(UIManager uiManager, TaskManager taskManager) {
        this.uiManager = uiManager;
        this.taskManager = taskManager;

        reachDistance = 9;

        fillAllTypes();
        fillAllMinableTypes();

        buildTextNotifier(this, uiManager);
        setCurrentState(WAITING);
        notifier.text = "(MINER)";
        notifier.y = Client.getViewportHeight() - 200;
    }
    public int reachDistance;
    public String[] allTypes;
    public String[] allMinableTypes;
    public GameObject targetObject;


    public GameObject objectFrom(Tile tile) {
        TileReference tileReference = tile.getTileReference();
        if (tileReference == null || tileReference.getObjects().length == 0) {
            return null;
        }

        return tileReference.getObjects()[0];
    }
    public GameObject furthestReachable() {
        double winning = 0;
        GameObject winner = null;
        for (GameObject object : minableReachable()) {
            if (object.distance(Players.getLocal().getTile()) > winning) {
                winning = object.distance(Players.getLocal().getTile());
                winner = object;
            }
        }

        return winner;
    }
    public GameObject closestReachable() {
        double winning = 100000;
        GameObject winner = null;
        for (GameObject object : minableReachable()) {
            if (object.distance(Players.getLocal().getTile()) < winning) {
                winning = object.distance(Players.getLocal().getTile());
                winner = object;
            }
        }

        return winner;
    }
    public List<GameObject> minableReachable() {
        List<GameObject> newList = new ArrayList<>();

        for (GameObject object : allReachable()) {
            for (int i=0; i < allMinableTypes.length; i++) {
                if (object.getName().equals(allMinableTypes[i])) {
                    newList.add(object);
                }
            }
        }

        return newList;
    }
    public List<GameObject> allReachable() {
        return GameObjects.all(object -> object.hasAction("Mine") && object.distance(Players.getLocal().getTile()) <= reachDistance && object.getModelColors() != null);
    }
    public boolean containsPickaxe() {
        return Inventory.contains(item -> item.getName().contains("pickaxe")) || Equipment.contains(item -> item.getName().contains("pickaxe"));
    }

    @Override
    public boolean accept() {
        Player pl = Players.getLocal();
        Tile plTile = pl.getTile();

        if (!pl.exists()) {
            setCurrentState(PLAYEREXIST);
            return false; //player does not exist.
        }
        if (taskManager.positioner != null || taskManager.banker != null) {
            return false;
        }

        if (Inventory.isFull()) {
            setCurrentState(FULLINVENTORY);
            return false; //inventory is full.
        }
        if (!containsPickaxe()) {
            setCurrentState(NOPICKAXE);
            return false; //no pickaxe.
        }
        if (pl.isMoving()) {
            setCurrentState(STILLMOVING);
            return false; //dont mine while running.
        }
        if (targetObject != null && plTile != null && targetObject.distance(plTile) > reachDistance) {
            targetObject = null;
            targetTile = null;
            setCurrentState(TOOFAR);
            return false; //valid retry, but object might be too far, so return false.
        }

        if (minableReachable().isEmpty()) {
            setCurrentState(NOMINABLE);
            return false; //no minable ores within reach.
        }
        //Now we have, a valid player, with a pickaxe, standing still, with minable ores within reach.
        setCurrentState(ATTEMPTING);

        return true;
    }

    public void targetObjectChanged(GameObject last, GameObject current) {
        //log("Target object changed!");
    }
    GameObject lastTargetObject; //not used.
    long timeToBounce = 0;
    public Tile targetTile;
    @Override
    public int execute() {
        Player pl = Players.getLocal();
        if (!pl.exists()) {
            log("Null player");
            return 100;
        }

        if (targetObject != null && targetObject.exists()) {
            if (lastTargetObject == null) {
                lastTargetObject = targetObject;
            }else {
                if (lastTargetObject != targetObject) {
                    targetObjectChanged(lastTargetObject, targetObject);
                    lastTargetObject = targetObject;
                }else {
                    log("Mining: retry attempt on target object.");
                }
            }
        }else {
            targetObject = closestReachable();
            targetTile = targetObject.getTile();

            if (System.currentTimeMillis() > timeToBounce) {
                timeToBounce = System.currentTimeMillis() + 60000;

                targetObject = furthestReachable();
                targetTile = targetObject.getTile();
            }
        }

        if (targetObject.interact("Mine")) {
            if (!pl.isMoving()) { //sleep till moving, if started not moving. stops spam click.
                Sleep.sleepUntil(this::playerStartedMoving, 1801);
            }

            Sleep.sleepUntil(this::deadRock, this::resetIfAttempting, 1801, 301); //handle connect stops animating, here.
        }

        return 100;
    }
    public boolean resetIfAttempting() {
        Player pl = Players.getLocal();

        if (pl.exists()) {
            if (pl.isAnimating() || pl.isMoving()) {
                return true;
            }
        }

        return false;
    }
    public boolean deadRock() {
        Player pl = Players.getLocal();
        GameObject objectFrom = objectFrom(targetTile);

        if (targetTile == null || objectFrom == null || objectFrom.getName().equals("Rocks") ) {
            targetObject = null;
            targetTile = null;
            return true;
        }

        if (pl != null && pl.exists() && pl.isAnimating()) {
            if (currentState != MINING) {
                setCurrentState(MINING);
            }
        }

        return false;
    }
    public void onInventoryItemAdded(Item item) {
        //log(item.getName() + " added to inventory!");
    }

    public void prePaint(Graphics g) {

    }

    public void paint(Graphics g) {

    }

    public void postPaint(Graphics g) {

    }











    public void fillAllTypes() {
        allTypes = new String[TaskManager.Ores.values().length];

        int i=0;
        for (TaskManager.Ores ore : TaskManager.Ores.values()) {
            allTypes[i] = ore.name;

            i++;
        }
    }
    public void fillAllMinableTypes() {
        int count=0;
        for (TaskManager.Ores ore : TaskManager.Ores.values()) {
            if (ore.level <= Skills.getRealLevel(Skill.MINING)) {
                count++;
            }
        }

        allMinableTypes = new String[count];

        int i=0;
        for (TaskManager.Ores ore : TaskManager.Ores.values()) {
            if (ore.level <= Skills.getRealLevel(Skill.MINING)) {
                allMinableTypes[i] = ore.name;
                i++;
            }
        }
    }
}
