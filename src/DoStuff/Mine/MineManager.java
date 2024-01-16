package DoStuff.Mine;

import DoStuff.Mine.Tasks.*;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.script.impl.TaskScript;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Model;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;

import java.awt.*;
import java.util.*;
import java.util.List;

import static DoStuff.Mine.MineManager.Locations.*;
import static DoStuff.Mine.MineManager.Ores.*;


public class MineManager extends TaskScript {

    public enum Locations {
        LUMBRIDGE("Lumbridge", new Area(3225, 3151, 3231, 3145)),
        RIMMINGTON("Rimmington", new Area(2969, 3245, 2985, 3229)),
        DRAYNOR("Draynor", new Area(3145, 3154, 3149, 3145));

        public final String name;
        public final Area area;

        Locations(String name, Area area) {
            this.name = name;
            this.area = area;
        }

        public static Locations random() {
            return List.of(values()).get(new Random().nextInt(List.of(values()).size()));
        }

        public static List<Locations> allAccessible() {

            List<Locations> accessible = new ArrayList<>();

            for (Ores ore : List.of(Ores.values())) { //loop ORES

                if (ore.level <= Skills.getRealLevel(Skill.MINING)) {//if valid LEVEL
                    for (Locations location : ore.locations) { //loop locations.
                        if (!accessible.contains(location)) { //ADD if not in.
                            accessible.add(location);
                        }
                    }

                }

            }

            return accessible;
        }
    }

    public enum Ores {
        COPPER("Copper rocks", 1, Arrays.asList(LUMBRIDGE, RIMMINGTON) ),
        TIN("Tin rocks", 1, Arrays.asList(LUMBRIDGE, RIMMINGTON) ),
        CLAY("Clay rocks", 1, Arrays.asList(RIMMINGTON, RIMMINGTON) ),
        IRON("Iron rocks", 15, Arrays.asList(RIMMINGTON, RIMMINGTON) ),
        COAL("Coal rocks", 30, Arrays.asList(DRAYNOR, DRAYNOR) ),
        GOLD("Gold rocks", 40, Arrays.asList(RIMMINGTON, RIMMINGTON) ),
        MITHRIL("Mithril rocks", 55, Arrays.asList(DRAYNOR, DRAYNOR) ),
        ADAMANT("Adamant rocks", 70, Arrays.asList(DRAYNOR, DRAYNOR) );

        public final String name;
        public final int level;
        public final List<Locations> locations;

        Ores(String name, int level, List<Locations> location) {
            this.name = name;
            this.level = level;
            this.locations = location;
        }

        public static Ores random() {
            return List.of(values()).get(new Random().nextInt(List.of(values()).size()));
        }

        public static List<Ores> allMinable() {

            List<Ores> minable = new ArrayList<>();

            for (Ores ore : List.of(Ores.values())) { //loop ORES
                if (ore.level <= Skills.getRealLevel(Skill.MINING)) {//if valid LEVEL
                    if (!minable.contains(ore)) { //ADD if not in.
                        minable.add(ore);
                    }
                }
            }

            return minable;
        }

    }



    public Positioner positioner;
    public Miner miner;
    public Banker banker;
    public MineManager() {
        positioner = new Positioner(this);
        miner = new Miner(this);
        banker = new Banker(this);

        addNodes(miner, banker, positioner);
    }





    long uiHoldTime;
    String lastGrab;
    public Hashtable<Ores, Integer> oreCollection;
    public void onInventoryItemAdded(Item item) {
        if (oreCollection == null) { //create if not exist.
            oreCollection = new Hashtable<>();
        }

        for (Ores ore : Ores.values()) {
            String oreFirstName = ore.name.split(" ", 2)[0];

            if (item.getName().contains(oreFirstName)) {
                if (!oreCollection.containsKey(ore)) { //create if not exist.
                    oreCollection.put(ore, 0);
                }

                int oldValue = oreCollection.get(ore);
                oreCollection.put(ore, oldValue + 1);

                uiHoldTime = System.currentTimeMillis() + 2000;
                lastGrab = ore.name.split(" ", 2)[0];
            }
        }
    }

    public int allCollectedOres() {
        int count = 0;
        for (Integer myCount : oreCollection.values()) {
            count = count + myCount;
        }

        return count;
    }

















    public void drawInfo(Graphics g) {
        drawOperatingTasks(g);
        int offset = drawLocations(g);
        drawOres(g, offset);
        drawTests(g);
    }

    public void drawTests(Graphics g) {
        if (miner != null) {
            if (miner.targetNode != null && miner.targetNode.getModel() != null) {
                GameObject target = miner.targetNode;

                List<GameObject> sameTypes = GameObjects.all(object -> object.hasAction("Mine") && object.getModelColors() == null && object.getName().equals(target.getName()));

                for (GameObject node : sameTypes) {
                    g.setColor(Color.PINK);
                    node.getModel().drawWireFrame(g);
                }
            }
        }
    }



    public void drawOperatingTasks(Graphics g) {
        if (positioner != null) {
            g.setColor(Color.white);
            g.drawString("Positioner", 5, Client.getViewportHeight() - 190);

            if (getLastTaskNode() == positioner) {
                FontMetrics metrics = g.getFontMetrics();
                int stringWidth = metrics.stringWidth("Positioner ");

                g.setColor(Color.yellow);
                g.drawString("lastActive", 5 + stringWidth, Client.getViewportHeight() - 190);
            }

            if (positioner.accept()) {
                FontMetrics metrics = g.getFontMetrics();
                int stringWidth = metrics.stringWidth("Positioner ");
                if (getLastTaskNode() == positioner) {
                    stringWidth = metrics.stringWidth("Positioner lastActive ");
                }
                g.setColor(Color.green);
                g.drawString("Operating...", 5 + stringWidth, Client.getViewportHeight() - 190);
            }
        }

        if (miner != null) {
            g.setColor(Color.white);
            g.drawString("Miner", 5, Client.getViewportHeight() - 205);

            if (getLastTaskNode() == miner) {
                FontMetrics metrics = g.getFontMetrics();
                int stringWidth = metrics.stringWidth("Miner ");
                g.setColor(Color.yellow);
                g.drawString("lastActive", 5 + stringWidth, Client.getViewportHeight() - 205);
            }

            if (miner.accept()) {
                FontMetrics metrics = g.getFontMetrics();
                int stringWidth = metrics.stringWidth("Miner ");
                if (getLastTaskNode() == miner) {
                    stringWidth = metrics.stringWidth("Miner lastActive ");
                }
                g.setColor(Color.green);
                g.drawString("Operating...", 5 + stringWidth, Client.getViewportHeight() - 205);
            }
        }

        if (banker != null) {
            g.setColor(Color.white);
            g.drawString("Banker", 5, Client.getViewportHeight() - 220);

            if (getLastTaskNode() == banker) {
                FontMetrics metrics = g.getFontMetrics();
                int stringWidth = metrics.stringWidth("Banker ");
                g.setColor(Color.yellow);
                g.drawString("lastActive", 5 + stringWidth, Client.getViewportHeight() - 220);
            }

            if (banker.accept()) {
                FontMetrics metrics = g.getFontMetrics();
                int stringWidth = metrics.stringWidth("Banker ");
                if (getLastTaskNode() == banker) {
                    stringWidth = metrics.stringWidth("Banker lastActive ");
                }
                g.setColor(Color.green);
                g.drawString("Operating...", 5 + stringWidth, Client.getViewportHeight() - 220);
            }
        }
    }

    public int drawLocations(Graphics g) {
        int i = 0;
        for (Locations location : Locations.values()) {
            if (allAccessible().contains(location)) {
                g.setColor(Color.white);
                if (positioner != null) {
                    if (positioner.targetLocation != null) {
                        if (positioner.targetLocation.name.equals(location.name)) {
                            g.setColor(Color.green);
                        }
                    }
                }
                g.drawString(location.name + " Mine", 5, Client.getViewportHeight() - (250 + (i*15)));
                i++;
            }else {
                g.setColor(Color.darkGray);
                g.drawString(location.name + " Mine", 5, Client.getViewportHeight() - (250 + (i*15)));
                i++;
            }
        }

        g.setColor(Color.white);
        g.drawString("Locations:", 5, Client.getViewportHeight() - (250 + (i*15)));

        return (i*15);
    }

    public void drawOres(Graphics g, int offset) {
        int i = 0;
        for (Ores ore : Ores.values()) {
            g.setColor(Color.white);
            if (allMinable().contains(ore)) {
                if (oreCollection != null && !oreCollection.isEmpty()) {
                    if (oreCollection.containsKey(ore)) {

                        int count = oreCollection.get(ore);
                        String name = ore.name.split(" ", 2)[0];

                        String display = ore.name + " x";
                        FontMetrics metrics = g.getFontMetrics();
                        int stringWidth = metrics.stringWidth(display);

                        g.setColor(Color.white);
                        if (lastGrab != null) {
                            if (name.contains(lastGrab) && Calculations.isBefore(uiHoldTime)) {
                                g.setColor(Color.green);
                            }
                        }

                        g.drawString("x" + count, 5 + stringWidth, (int) Client.getViewportHeight() - (280 + offset + (i*15)));
                    }
                }

                g.setColor(Color.white);
                if (miner != null) {
                    if (miner.targetNode != null) {
                        if (miner.targetNode.getName().equals(ore.name)) {
                            g.setColor(Color.green);
                        }
                    }
                }
                g.drawString(ore.name, 5, Client.getViewportHeight() - (280 + offset + (i*15)));
                i++;
            }else {
                g.setColor(Color.darkGray);
                g.drawString(ore.name, 5, Client.getViewportHeight() - (280 + offset + (i*15)));
                i++;
            }
        }

        g.setColor(Color.white);
        if (oreCollection != null && !oreCollection.isEmpty()) {
            g.drawString("Ores(Collected: " + allCollectedOres() + ")", 5, Client.getViewportHeight() - (280 + offset + (i*15)));
        }else {
            g.drawString("Ores(Collected: 0):", 5, Client.getViewportHeight() - (280 + offset + (i*15)));
        }
    }
}
