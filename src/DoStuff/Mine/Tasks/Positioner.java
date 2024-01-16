package DoStuff.Mine.Tasks;

import DoStuff.Mine.*;
import DoStuff.Mine.MineManager.Locations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Sleep;

public class Positioner extends TaskNode {
    public MineManager manager;
    public Positioner(MineManager manager) {
        this.manager = manager;
    }

    public Tile targetTile;
    public boolean alreadyArrived;
    public Locations targetLocation;


    public void setDefaultLocation() {
        if (targetLocation == null) {
            double distance = 1000000;
            Locations random = Locations.random();
            Locations winner = null;

            for (Locations location : Locations.values()) {
                if (location.area.getCenter().distance(Players.getLocal().getTile()) < distance) {
                    distance = location.area.getCenter().distance(Players.getLocal().getTile());
                    winner = location;
                }
            }
            if (winner == random) { //random location selection was closest to us, check if we are within range an finish.
                if (random.area.getCenter().distance(Players.getLocal().getTile()) <= 10) {
                    alreadyArrived = true;
                }
            }

            targetLocation = random;
            targetTile = targetLocation.area.getRandomTile();
        }
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

        setDefaultLocation();

        if (Inventory.isFull()) {
            return false;
        }

        if (Players.getLocal().isMoving()) {
            return false;
        }

        if (alreadyArrived) { //half inv & lost gets stuck.
            return false;
        }

        return true;
    }

    @Override
    public int execute() {
        if (Walking.walk(targetTile)) {
            Sleep.sleepUntil(this::playerMoving, 1801);

            Sleep.sleepUntil(this::playerNotMoving, this::playerMoving, 1801, 300);

            if (arrivedAtTarget()) {
                alreadyArrived = true;
            }
        }

        return 100;
    }





    public boolean playerNotMoving() {
        if (Players.getLocal() == null) {
            log("Player is null");
            return false;
        }

        if (!Players.getLocal().exists()) {
            log("Player does not exist");
            return false;
        }

        return !Players.getLocal().isMoving();
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

    public boolean arrivedAtTarget() {
        return (targetTile.distance(Players.getLocal().getTile()) <= 4);
    }
}
