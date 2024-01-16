package DoStuff.Mine.Tasks;

import DoStuff.Mine.MineManager;
import DoStuff.Mine.MineManager.Ores;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Model;
import org.dreambot.api.wrappers.interactive.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Miner extends TaskNode {
    public MineManager manager;
    public Miner(MineManager manager) {
        this.manager = manager;
    }
    public GameObject targetNode;
    public int reachDistance = 14;
    public long nextBounce = 0;
    public long bounceDelay = 60000;


    public GameObject randomReachable(GameObject... exluding) {
        return getReachable(exluding).get(Calculations.random(0, getReachable(exluding).size()-1));
    }
    public GameObject closestReachable() {
        GameObject winningObject = null;
        double winner = 1000000;

        for (GameObject obj : getReachable()) {
            if (obj.distance(Players.getLocal().getTile()) < winner) {
                winner = obj.distance(Players.getLocal().getTile());
                winningObject = obj;
            }
        }

        return winningObject;
    }

    public GameObject furthestReachable() {
        GameObject winningObject = null;
        double winner = 0;

        for (GameObject obj : getReachable()) {
            if (obj.distance(Players.getLocal().getTile()) > winner) {
                winner = obj.distance(Players.getLocal().getTile());
                winningObject = obj;
            }
        }

        return winningObject;
    }

    public GameObject closestSmartType() { //brokenish
        GameObject winningObject = null;
        double winner = 1000000;

        for (GameObject obj : getSmartTypes()) {
            if (obj.distance(Players.getLocal().getTile()) < winner) {
                winner = obj.distance(Players.getLocal().getTile());
                winningObject = obj;
            }
        }

        return winningObject;
    }

    public GameObject findWinningOre() {
        GameObject winningObject = closestReachable();

        if (nextBounce < System.currentTimeMillis()) {
            nextBounce = System.currentTimeMillis() + bounceDelay;

            //winningObject = furthestReachable();
            winningObject = randomReachable(targetNode);
        }

        return winningObject;
    }

    public List<GameObject> getSmartTypes() { //brokenish
        GameObject target = targetNode;

        List<GameObject> sameTypes = GameObjects.all(object -> object.hasAction("Mine") && object.distance(Players.getLocal().getTile()) <= reachDistance && object.getModelColors() != null && object.getName().equals(target.getName()));
        List<GameObject> toBeRemoved = new ArrayList<>();

        for (GameObject node : sameTypes) {
            for (Player pl : Players.all()) {
                log(pl.getAnimation());
                if (pl != Players.getLocal()) {
                    if (pl.getTile().distance(node.getTile()) < 3) {
                        if (pl.isAnimating()) {
                            toBeRemoved.add(node);
                        }
                    }
                }
            }
        }

        if (!toBeRemoved.isEmpty() && toBeRemoved.size() != sameTypes.size()) {
            for (GameObject obj : toBeRemoved) {
                sameTypes.remove(obj);
            }
        }

        return sameTypes;
    }

    public List<GameObject> getReachable(GameObject... excluding) {//thic

        String exclusionName = "";
        if (excluding.length > 0) {
            if (excluding[0] != null) {
                exclusionName = excluding[0].getName();
            }
        }

        if (!exclusionName.equals("")) {
            //log("Excluding: " + exclusionName);
        }

        List<GameObject> reachable = new ArrayList<>();

        for (GameObject object : GameObjects.all()) {
            if (object.hasAction("Mine")) {
                if (!object.getName().equals(exclusionName)) {
                    if (object.distance(Players.getLocal().getTile()) <= reachDistance) {
                        if (object.getModelColors() != null) {
                            for (Ores ore : Ores.allMinable()) {
                                if (ore.name.equals(object.getName())) {
                                    reachable.add(object); //takeoff
                                }
                            }
                        }
                    }
                }
            }
        }

        return reachable;
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

        if (!manager.positioner.alreadyArrived) {
            return false;
        }

        if (Inventory.isFull()) {
            return false;
        }

        if (Players.getLocal().isMoving()) {
            return false;
        }

        if (getReachable().isEmpty()) {//BEFORE isRocks check.
            return false;
        }

        if (isNodeRocks()) {
            return true;
        }

        if (Players.getLocal().isAnimating()) {//AFTER isRocks check.
            return false;
        }

        return true;
    }

    @Override
    public int execute() {
        GameObject winningOre = findWinningOre();

        if (winningOre == null) {
            log("No closest object found.");
            return 100;
        }

        targetNode = winningOre;

        if (targetNode.interact("Mine")) {
            Sleep.sleepUntil(this::playerAnimating, this::playerMoving, 1201, 300);

            Sleep.sleepUntil(this::isNodeRocks, 1500);
        }

        return 100;
    }







    public boolean playerMoving() {
        if (Players.getLocal() == null) { //True on null, to ignore connection error.
            log("Player is null");
            return true;
        }

        if (!Players.getLocal().exists()) { //True on null, to ignore connection error.
            log("Player does not exist");
            return true;
        }

        return Players.getLocal().isMoving();
    }

    public boolean playerAnimating() {
        if (Players.getLocal() == null) { //True on null, to ignore connection error.
            log("Player is null");
            return true;
        }

        if (!Players.getLocal().exists()) { //True on null, to ignore connection error.
            log("Player does not exist");
            return true;
        }

        return Players.getLocal().isAnimating();
    }


    public GameObject objectFrom(Tile tile) {
        if (tile.getTileReference() == null) {
            return null;
        }

        if (tile.getTileReference().getObjects().length == 0) {
            return null;
        }

        return tile.getTileReference().getObjects()[0];
    }

    public boolean isNodeRocks() {
        return targetNode == null || targetNode.getTile() == null || objectFrom(targetNode.getTile()) == null || objectFrom(targetNode.getTile()).getName().equals("Rocks");
    }

}
