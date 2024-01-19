package BotScript.Operators;

import BotScript.Elements.TextCommand;
import BotScript.TaskManager;
import BotScript.TaskManager.Locations;
import BotScript.UIManager;
import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;

import java.awt.*;
import java.util.ArrayList;

import static BotScript.TaskManager.Locations.allAccessibleLocations;

public class Positioner extends Operator implements UIManager.TextCommands {
    public TaskManager taskManager;
    public Tile targetTile;
    public boolean alreadyArrived;
    public TaskManager.Locations targetLocation;
    public UIManager uiManager;
    public Positioner(TaskManager taskManager, UIManager uiManager) {
        this.taskManager = taskManager;
        this.uiManager = uiManager;

        buildLocationInformation();
    }






    public ArrayList<String> locations;
    public ArrayList<TextCommand> locationTextCommands;
    public ArrayList<String> accessibleLocations;
    int locationsOriginX = -1;
    int locationsOriginY = -1;
    public void buildLocationInformation() {
        locations = new ArrayList<>();
        locationTextCommands = new ArrayList<>();
        accessibleLocations = new ArrayList<>();

        for (Locations location : Locations.values()) {
            locations.add(location.name); //Locations List

            //Text Elements
            locationTextCommands.add(uiManager.TextCommand(location.name, 0, 0, this)); //no handle for deletion? keep in null wrap for now.
        }

        //Accessible Locations List
        for (Locations location : allAccessibleLocations()) {
            accessibleLocations.add(location.name);
        }
    }

    public void rebuildLocations(Graphics g) {
        log("Rebuilding locations.");

        if (locationsOriginX == -1) { //fix sometime.
            locationsOriginX = Client.getViewportWidth()-254;
            locationsOriginY = Client.getViewportHeight()-185;
        }

        int i=0;
        for (String locationName : locations) {
            Font lastfont = g.getFont();
            g.setFont(locationTextCommands.get(i).font); //set font for metrics. idk if we need this
            FontMetrics metrics = g.getFontMetrics();
            int textWidth = metrics.stringWidth(locationTextCommands.get(i).text);
            int fontHeight = metrics.getFont().getSize();

            if (locationTextCommands.get(i).x != locationsOriginX) {
                locationTextCommands.get(i).x = locationsOriginX - textWidth;
            }
            if (locationTextCommands.get(i).y != locationsOriginY - i*fontHeight) {
                locationTextCommands.get(i).y = locationsOriginY - i*fontHeight;
            }
            g.setFont(lastfont);

            if (activeTargetLocation) {
                if (locationName.equalsIgnoreCase(targetLocation.name)) {
                    if (locationTextCommands.get(i).color != Color.green) {
                        locationTextCommands.get(i).color = Color.green;
                    }
                }else {
                    if (locationTextCommands.get(i).color != Color.white) {
                        locationTextCommands.get(i).color = Color.white;
                    }
                }
            }else {
                if (locationTextCommands.get(i).color != Color.white) {
                    locationTextCommands.get(i).color = Color.white;
                }
            }

            if (!accessibleLocations.contains(locationName)) {
                if (locationTextCommands.get(i).color != Color.darkGray) {
                    locationTextCommands.get(i).color = Color.darkGray;
                }
            }
            i++;
        }
    }

    public void drawLocationInformation(Graphics g) {
        if (lastActiveLocationChanged) {
            log("Rebuild");
            rebuildLocations(g);
        }

        g.setColor(Color.white);
        g.drawString("Locations:", Client.getViewportWidth()-310, Client.getViewportHeight() - (185 + locationTextCommands.getFirst().font.getSize() * locations.size()) );
    }








    public void onDualTextPressed(int id) {

    }
    public void onTextCommandPressed(int id) {
        for (TaskManager.Locations location : TaskManager.Locations.values()) {
            if (location.name.contains(uiManager.allTextCommands.get(id).text.split(" ", 2)[0])) { //ew
                if (allAccessibleLocations().contains(location)) {
                        //if (Players.getLocal().isMoving() || Players.getLocal().isAnimating()) { //this makes it lag, need to fix.
                            //Walking.walk(Players.getLocal().getTile());
                        //}
                        targetLocation = location;
                        targetTile = location.area.getRandomTile();
                        alreadyArrived = false; //lol fck

                        if (taskManager.miner != null) {
                            taskManager.miner.targetNode = null;
                        }

                }
            }
        }
    }






    public boolean lastActiveLocationChanged;
    public boolean activeTargetLocation;
    public String lastActiveLocation;
    public void prePaint(Graphics g) {
        if (targetLocation != null) { //Update last active locations early.
            activeTargetLocation = true;

            if (lastActiveLocation == null) {
                lastActiveLocation = targetLocation.name;
                lastActiveLocationChanged = true; //first time, build.
            }

            if (!lastActiveLocation.equalsIgnoreCase(targetLocation.name)) {
                lastActiveLocation = targetLocation.name;
                lastActiveLocationChanged = true;
            }
        }
    }
    public void paint(Graphics g) {
        drawLocationInformation(g);
    }
    public void postPaint(Graphics g) {
        lastActiveLocationChanged = false;
        activeTargetLocation = false;
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







    public void setDefaultLocation() {
        if (targetLocation == null) {
            double distance = 1000000;
            TaskManager.Locations random = TaskManager.Locations.random();

            while (!allAccessibleLocations().contains(random)) {
                random = TaskManager.Locations.random();
            }

            TaskManager.Locations winner = null;

            for (Locations location : TaskManager.Locations.values()) {
                if (allAccessibleLocations().contains(location)) {
                    if (location.area.getCenter().distance(Players.getLocal().getTile()) < distance) {
                        distance = location.area.getCenter().distance(Players.getLocal().getTile());
                        winner = location;
                    }
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
