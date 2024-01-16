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
    public long bounceDelay = 20000;


    public GameObject randomReachable() {
        return getReachable().get(Calculations.random(0, getReachable().size()));
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

    public GameObject findWinningOre() {
        GameObject winningObject = closestReachable();

        if (nextBounce < System.currentTimeMillis()) {
            nextBounce = System.currentTimeMillis() + bounceDelay;

            //winningObject = furthestReachable();
            winningObject = randomReachable();
        }

        return winningObject;
    }

    public List<GameObject> getReachable() {//thic
        List<GameObject> reachable = new ArrayList<>();

        for (GameObject object : GameObjects.all()) {
            if (object.hasAction("Mine")) {
                if (object.distance(Players.getLocal().getTile()) <= reachDistance) {
                    if (object.getModelColors() != null) {

                        for (Ores ore : Ores.allMinable()) {
                            if (ore.name.equals(object.getName())) {
                                reachable.add(object);
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
