package BotScript;

import BotScript.Operators.Banker;
import BotScript.Operators.Miner;
import BotScript.Operators.Operator;
import BotScript.Operators.Positioner;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.script.impl.TaskScript;
import org.dreambot.api.wrappers.items.Item;

import java.awt.*;
import java.util.*;
import java.util.List;

public class TaskManager extends TaskScript {
    public UIManager uiManager;
    public Positioner positioner;
    public Miner miner;
    public Banker banker;
    TaskManager(UIManager uiManager) {
        this.uiManager = uiManager;

        positioner = new Positioner(this, uiManager);
        miner = new Miner(this, uiManager);
        banker = new Banker(this, uiManager);

        addNodes(miner, banker, positioner);
    }


    public void think() {

    }



    @Override
    public void onPaint(Graphics g) {
        prePaint(g);
        paint(g);
        postPaint(g);

        //drawOperatingTasks(g);
    }

    public void prePaint(Graphics g) {
        for (TaskNode node : getNodes()) {
            Operator op = (Operator)node;

            op.prePaint(g);
        }
    }

    public void paint(Graphics g) {
        for (TaskNode node : getNodes()) {
            Operator op = (Operator)node;

            op.paint(g);
        }
    }

    public void postPaint(Graphics g) {
        for (TaskNode node : getNodes()) {
            Operator op = (Operator)node;

            op.postPaint(g);
        }
    }

    public void onInventoryItemAdded(Item item) {
        for (TaskNode node : getNodes()) {
            Operator op = (Operator)node;

            op.onInventoryItemAdded(item);
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
        DRAYNOR("Draynor", new Area(3145, 3154, 3149, 3145));

        public final String name;
        public final Area area;

        Locations(String name, Area area) {
            this.name = name;
            this.area = area;
        }

        public static Locations random() {
            return java.util.List.of(values()).get(new Random().nextInt(java.util.List.of(values()).size()));
        }

        public static java.util.List<Locations> allAccessibleLocations() {

            java.util.List<Locations> accessible = new ArrayList<>();

            for (Ores ore : java.util.List.of(Ores.values())) { //loop ORES

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
        COPPER("Copper rocks", 1, Arrays.asList(Locations.LUMBRIDGE, Locations.RIMMINGTON, Locations.DWARVENNORTH, Locations.DWARVENSOUTH, Locations.VARROCKSOUTHEAST) ),
        TIN("Tin rocks", 1, Arrays.asList(Locations.LUMBRIDGE, Locations.RIMMINGTON, Locations.DWARVENNORTHEAST, Locations.DWARVENNORTH, Locations.BARBARIANVILLAGE, Locations.VARROCKSOUTHEAST) ),
        CLAY("Clay rocks", 1, Arrays.asList(Locations.RIMMINGTON, Locations.RIMMINGTON, Locations.DWARVENNORTHEAST, Locations.DWARVENSOUTH) ),
        IRON("Iron rocks", 15, Arrays.asList(Locations.RIMMINGTON, Locations.RIMMINGTON, Locations.DWARVENNORTHEAST, Locations.DWARVENNORTH, Locations.VARROCKSOUTHEAST) ),
        COAL("Coal rocks", 30, Arrays.asList(Locations.DRAYNOR, Locations.DRAYNOR, Locations.BARBARIANVILLAGE) ),
        GOLD("Gold rocks", 40, Arrays.asList(Locations.RIMMINGTON, Locations.RIMMINGTON) ),
        MITHRIL("Mithril rocks", 55, Arrays.asList(Locations.DRAYNOR, Locations.DRAYNOR) ),
        ADAMANT("Adamant rocks", 70, Arrays.asList(Locations.DRAYNOR, Locations.DRAYNOR) );

        public final String name;
        public final int level;
        public final java.util.List<Locations> locations;

        Ores(String name, int level, java.util.List<Locations> location) {
            this.name = name;
            this.level = level;
            this.locations = location;
        }

        public static Ores random() {
            return java.util.List.of(values()).get(new Random().nextInt(java.util.List.of(values()).size()));
        }

        public static java.util.List<Ores> allMinable() {

            java.util.List<Ores> minable = new ArrayList<>();

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
}