package QuickMine.resources;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;

import java.util.*;

public class Enums {

    public enum Priority { JUSTGO, EXPERIENCE, GOLD }

    public enum Resources {
        SOURCE("findme/"),
        IMAGES("images/");

        public final String dir;

        Resources(String dir) { this.dir = dir; };
    }

    public enum Locations {
        COPPER_LUMBRIDGE(new Area(3227, 3149, 3231, 3145)),
        COPPER_RIMMINGTON(new Area(2975, 3248, 2980, 3244)),
        TIN_LUMBRIDGE(new Area(3223, 3149, 3226, 3145)),
        TIN_RIMMINGTON(new Area(2983, 3238, 2987, 3234)),
        CLAY_RIMMINGTON(new Area(2985, 3241, 2988, 3238)),
        IRON_RIMMINGTON(new Area(2967, 3243, 2972, 3236)),
        IRON2_RIMMINGTON(new Area(2980, 3235, 2983, 3232)),
        COAL_DRAYNOR(new Area(3145, 3154, 3147, 3148)),
        GOLD_RIMMINGTON(new Area(2974, 3235, 2978, 3232)),
        MITHRIL_DRAYNOR(new Area(3144, 3147, 3148, 3144)),
        ADAMANT_DRAYNOR(new Area(3146, 3148, 3149, 3145));

        public final Area area;

        Locations(Area area) {
            this.area = area;
        }

        public static Locations randomArea() {
            return Collections.unmodifiableList(Arrays.asList(values())).get(new Random().nextInt(Collections.unmodifiableList(Arrays.asList(values())).size()));
        }
    }


    public enum Ores {
        COPPER("Copper rocks", 1, Arrays.asList(Locations.COPPER_LUMBRIDGE, Locations.COPPER_RIMMINGTON) ),
        TIN("Tin rocks", 1, Arrays.asList(Locations.TIN_LUMBRIDGE, Locations.TIN_RIMMINGTON) ),
        CLAY("Clay rocks", 1, Arrays.asList(Locations.CLAY_RIMMINGTON, Locations.CLAY_RIMMINGTON) ),
        IRON("Iron rocks", 15, Arrays.asList(Locations.IRON_RIMMINGTON, Locations.IRON2_RIMMINGTON) ),
        COAL("Coal rocks", 30, Arrays.asList(Locations.COAL_DRAYNOR, Locations.COAL_DRAYNOR) ),
        GOLD("Gold rocks", 40, Arrays.asList(Locations.GOLD_RIMMINGTON, Locations.GOLD_RIMMINGTON) ),
        MITHRIL("Mithril rocks", 55, Arrays.asList(Locations.MITHRIL_DRAYNOR, Locations.MITHRIL_DRAYNOR) ),
        ADAMANT("Adamant rocks", 70, Arrays.asList(Locations.ADAMANT_DRAYNOR, Locations.ADAMANT_DRAYNOR) );

        public final String name;
        public final int level;
        public final List<Locations> locations;

        Ores(String name, int level, List<Locations> location) {
            this.name = name;
            this.level = level;
            this.locations = location;
        }

        public static Ores randomOre() {
            return Collections.unmodifiableList(Arrays.asList(values())).get(new Random().nextInt(Collections.unmodifiableList(Arrays.asList(values())).size()));
        }

        public static Ores highestMineableOre() {
            Ores lastOre = Ores.COPPER; //consider multiple ores @ same level, return random of?

            for (Ores ore : Ores.values()) {
                if (Skills.getRealLevel(Skill.MINING) < lastOre.level) return lastOre;

                lastOre = ore;
            }

            return lastOre;
        }

        public static List<Ores> allMineableOres() {
            List<Ores> oreList = new ArrayList<>();

            for (Ores ore : Ores.values()) {
                if (Skills.getRealLevel(Skill.MINING) >= ore.level) {
                    oreList.add(ore);
                }
            }

            return oreList;
        }
    }

    public enum Pickaxes {
        BRONZE("Bronze pickaxe", 1),
        IRON("Iron pickaxe", 1),
        STEEL("Steel pickaxe", 6),
        BLACK("Black pickaxe", 11),
        MITHRIL("Mithril pickaxe", 21),
        ADAMANT("Adamant pickaxe", 31),
        RUNE("Rune pickaxe", 41),
        DRAGON("Dragon pickaxe", 61),
        INFERNAL("Infernal pickaxe", 61),
        THIRDAGE("3rd age pickaxe", 61),
        CRYSTAL("Crystal pickaxe", 71);

        public final String name;
        public final int level;

        Pickaxes(String name, int level) {
            this.name = name;
            this.level = level;
        }
        public static Pickaxes randomPickaxe() {
            return Collections.unmodifiableList(Arrays.asList(values())).get(new Random().nextInt(Collections.unmodifiableList(Arrays.asList(values())).size()));
        }

        public static Pickaxes highestUsablePickaxe() { //NO MINING LEVEL SETUP FOR USE YET
            Pickaxes lastPickaxe = Pickaxes.BRONZE;

            for (Pickaxes pickaxe : Pickaxes.values()) {
                if (Skills.getRealLevel(Skill.MINING) < pickaxe.level) return lastPickaxe;
                lastPickaxe = pickaxe;
            }

            return lastPickaxe;
        }

        public static boolean hasHighestUsablePickaxe() { //NO MINING LEVEL SETUP FOR USE YET
            Pickaxes highestUsable = highestUsablePickaxe();

            return (Equipment.contains(highestUsable) || Inventory.contains(highestUsable));
        }

        public static boolean hasUsablePickaxe() {
            for (Pickaxes pickaxe : Pickaxes.values()) {
                String name = pickaxe.name;

                if (Equipment.contains(name) || Inventory.contains(name))
                {
                    if (Skills.getRealLevel(Skill.MINING) >= pickaxe.level) return true;
                }
            }

            return false;
        }
    }
}







//Ticks @ 70 mining. RS ticks == 600ms
//Pickaxes:

//Drag 0.6seconds, 1 tick
//Rune 1.15seconds, 2 ticks
//Addy 1.85seconds, 3 ticks
//Mith 2.35seconds?, 4 ticks
//Black 3seconds?, 5 ticks
//Steel 3.55seconds?, 6 ticks
//Iron 4.25seconds?, 7 ticks
//Bron 4.75seconds?, 8 ticks //test with bronze an expand.
//https://rune-server.org/runescape-development/rs2-server/informative-threads/383556-runescape-mining-success-rate-formula.html