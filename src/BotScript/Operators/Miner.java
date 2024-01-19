package BotScript.Operators;

import BotScript.Elements.DualText;
import BotScript.TaskManager;
import BotScript.UIManager;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;

import java.awt.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class Miner extends Operator implements UIManager.TextCommands {
    public TaskManager taskManager;
    public GameObject targetNode;
    public int reachDistance = 14;
    public long nextBounce = 0;
    public long bounceDelay = 60000;
    public UIManager uiManager;
    public Miner(TaskManager taskManager, UIManager uiManager) {
        this.taskManager = taskManager;
        this.uiManager = uiManager;

        generateMiningInformation();
    }





    public ArrayList<String> allNodes;
    public ArrayList<DualText> allNodeDualTexts;
    public ArrayList<String> accessibleNodes;
    public ArrayList<String> exclusionList;
    public Hashtable<TaskManager.Ores, Integer> oreCollection;
    public void generateMiningInformation() {
        allNodes = new ArrayList<>();
        allNodeDualTexts = new ArrayList<>();
        accessibleNodes = new ArrayList<>();
        exclusionList = new ArrayList<>();
        oreCollection = new Hashtable<>();

        for (TaskManager.Ores ore : TaskManager.Ores.values()) {
            allNodes.add(ore.name);

            oreCollection.put(ore, 0);

            allNodeDualTexts.add(uiManager.DualText(ore.name, "", 0, 0, Color.white, Color.white, this)); //no handle for deletion?
        }

        for (TaskManager.Ores ore : TaskManager.Ores.allMinable()) {
            accessibleNodes.add(ore.name);
        }

        itemListShouldUpdate = true;
    }









    int totalCollectedOres;
    long uiHoldTime;
    String lastGrab;
    public void onInventoryItemAdded(Item pickupItem) {
        totalCollectedOres = 0; //update collected items total.
        for (Integer myCount : oreCollection.values()) {
            totalCollectedOres = totalCollectedOres + myCount;
        }

        for (TaskManager.Ores ore : TaskManager.Ores.values()) { //change to items, and to real item names.
            String oreFirstName = ore.name.split(" ", 2)[0];

            if (pickupItem.getName().contains(oreFirstName)) {
                oreCollection.put(ore, oreCollection.get(ore) + 1);

                uiHoldTime = System.currentTimeMillis() + 2000;
                lastGrab = ore.name.split(" ", 2)[0];
            }
        }

        itemListShouldUpdate = true;
    }

    public void onTextCommandPressed(int id) {}
    public void onDualTextPressed(int id) {
        if (!exclusionList.contains(uiManager.allDualTexts.get(id).text)) {
            exclusionList.add(uiManager.allDualTexts.get(id).text);
        }else {
            exclusionList.remove(uiManager.allDualTexts.get(id).text);
        }

        itemListShouldUpdate = true;
    }






    public boolean itemListShouldUpdate;
    public boolean activeTargetNode;
    public void prePaint(Graphics g) {
        if (targetNode != null) {
            activeTargetNode = true;
        }
    }
    public void paint(Graphics g) {
        drawMiningInformation(g);
    }
    public void postPaint(Graphics g) {
        activeTargetNode = false;
    }









    public int MiningInformationXOrigin = -1;
    public int MiningInformationYOrigin = -1;
    public void rebuildMiningInformation(Graphics g) {
        if (MiningInformationXOrigin == -1) { //fix sometime.
            MiningInformationXOrigin = 5;
            MiningInformationYOrigin = Client.getViewportHeight()-185;
        }

        int i=0;
        for (String itemName : allNodes) {
            Font lastfont = g.getFont();
            g.setFont(allNodeDualTexts.get(i).font); //set font for metrics. idk if we need this
            FontMetrics metrics = g.getFontMetrics();
            int fontHeight = metrics.getFont().getSize();

            if (allNodeDualTexts.get(i).x != MiningInformationXOrigin) {
                allNodeDualTexts.get(i).x = MiningInformationXOrigin;
            }
            if (allNodeDualTexts.get(i).y != MiningInformationYOrigin - i*fontHeight) {
                allNodeDualTexts.get(i).y = MiningInformationYOrigin - i*fontHeight;
            }
            g.setFont(lastfont);

            if (oreCollection != null && !oreCollection.isEmpty()) { //fix this.
                for (TaskManager.Ores ore : TaskManager.Ores.values()) {
                    if (ore.name.contains(itemName.split(" ", 2)[0])) {
                        if (oreCollection.containsKey(ore)) {
                            int count = oreCollection.get(ore);
                            String name = ore.name.split(" ", 2)[0];
                            if (count > 0) {
                                allNodeDualTexts.get(i).textTwo = " x" + count;
                            }

                            if (lastGrab != null) {
                                if (name.contains(lastGrab) && Calculations.isBefore(uiHoldTime)) {
                                    if (allNodeDualTexts.get(i).colorTwo != Color.green) {
                                        allNodeDualTexts.get(i).colorTwo = Color.green;
                                    }
                                }else {
                                    if (allNodeDualTexts.get(i).colorTwo != Color.white) {
                                        allNodeDualTexts.get(i).colorTwo = Color.white;
                                    }
                                }
                            }

                        }
                    }
                }
            }

            if (activeTargetNode) {
                if (itemName.equalsIgnoreCase(targetNode.getName())) {
                    if (allNodeDualTexts.get(i).color != Color.green) {
                        allNodeDualTexts.get(i).color = Color.green;
                    }
                }else {
                    if (allNodeDualTexts.get(i).color != Color.white) {
                        allNodeDualTexts.get(i).color = Color.white;
                    }
                }
            }else {
                if (allNodeDualTexts.get(i).color != Color.white) {
                    allNodeDualTexts.get(i).color = Color.white;
                }
            }

            if (!accessibleNodes.contains(itemName)) { //skill level says no
                if (allNodeDualTexts.get(i).color != Color.darkGray) {
                    allNodeDualTexts.get(i).color = Color.darkGray;
                }
            }

            if (exclusionList.contains(itemName)) { //Client says no
                if (allNodeDualTexts.get(i).color != Color.pink) {
                    allNodeDualTexts.get(i).color = Color.pink;
                }
            }
            i++;
        }
    }

    public void drawMiningInformation(Graphics g) {
        if (itemListShouldUpdate) {
            rebuildMiningInformation(g);
            itemListShouldUpdate = false;
        }

        if (oreCollection != null && !oreCollection.isEmpty()) {
            g.setColor(Color.white);
            g.drawString("Ores(Collected: " + totalCollectedOres + ")", 5, Client.getViewportHeight() - (185 + (allNodeDualTexts.getFirst().font.getSize() * allNodes.size())));
        }else {
            g.setColor(Color.white);
            g.drawString("Ores(Collected: 0):", 5, Client.getViewportHeight() - (185 + (allNodeDualTexts.getFirst().font.getSize() * allNodes.size())));
        }
    }













    @Override
    public boolean accept() {
        if (Players.getLocal() == null) {
            return false;
        }

        if (!Players.getLocal().exists()) {
            return false;
        }

        if (!taskManager.positioner.alreadyArrived) {
            return false;
        }

        if (Inventory.isFull()) {
            return false;
        }

        if (Players.getLocal().isMoving()) {
            return false;
        }

        if (getReachable().isEmpty()) {
            return false;
        }

        if (isNodeRocks()) {
            return true;
        }

        if (Players.getLocal().isAnimating()) {
            return false;
        }

        if (getReachable().isEmpty()) {
            return false; //NEW if problems. start here.
        }

        return true;
    }

    @Override
    public int execute() {
        GameObject winningOre = findWinningOre();

        targetNode = winningOre;
        itemListShouldUpdate = true;
        uiHoldTime = 0;

        if (targetNode.interact("Mine")) {
            Sleep.sleepUntil(this::playerAnimating, this::playerMoving, 1201, 300);

            Sleep.sleepUntil(this::isNodeRocks, 1500);
        }

        return 100;
    }

















    public List<GameObject> getReachable() {
        List<GameObject> reachable = new ArrayList<>();

        for (GameObject object : GameObjects.all(object -> object.hasAction("Mine") && object.distance(Players.getLocal().getTile()) <= reachDistance && object.getModelColors() != null)) {
            for (TaskManager.Ores ore : TaskManager.Ores.allMinable()) {
                if (!exclusionList.contains(ore.name)) {
                    if (ore.name.equals(object.getName())) {
                        reachable.add(object);
                    }
                }
            }
        }

        return reachable;
    }

    public GameObject randomReachable() {
        if (getReachable().isEmpty()) {
            return null;
        }

        return getReachable().get(Calculations.random(0, getReachable().size()));
    }
    public GameObject closestReachable() {
        if (getReachable().isEmpty()) {
            return null;
        }

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

    public GameObject findWinningOre() {
        GameObject winningObject = closestReachable();

        if (nextBounce < System.currentTimeMillis()) {
            nextBounce = System.currentTimeMillis() + bounceDelay;

            winningObject = randomReachable();
        }

        return winningObject;
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
