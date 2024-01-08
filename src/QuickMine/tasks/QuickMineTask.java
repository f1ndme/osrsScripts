package QuickMine.tasks;

import QuickMine.resources.Enums.*;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;

import static QuickMine.resources.Enums.Ores.allMineableOres;
import static QuickMine.resources.Enums.Pickaxes.hasUsablePickaxe;

public class QuickMineTask extends TaskNode {

    final int miningTolerance = 6;

    Player pl;
    GameObject attemptingOre;

    @Override
    public boolean accept() {
        pl = Players.getLocal();

        return hasUsablePickaxe() && !Inventory.isFull() && !isMining() && (pl != null) && !pl.isMoving();
    }

    @Override
    public int execute() {
        attemptingOre = getClosestMinableOre();
        if (attemptingOre == null) return 50;

        if (attemptingOre.interact("Mine")) {
            Sleep.sleepUntil(this::isMining, Calculations.random(200, 400));
        }

        return Calculations.random(601, 1100);
    }

    private boolean isMining() {
        if (pl == null) return false;

        if (attemptingOre != null) {
            GameObject closest = GameObjects.closest(object -> object.getName().equalsIgnoreCase("rocks") && object.distance(pl.getTile()) <= 2);
            if (closest != null) {
                if (closest.getTile().equals(attemptingOre.getTile())) {
                    return false;
                }
            }
        }

        return pl.isAnimating();
    }

    private GameObject getClosestMinableOre() {
            for (int i = allMineableOres().size(); i-- > 0; ) { //backwards iterate gets top ore first. cheeky
                Ores ore = allMineableOres().get(i);

                GameObject obj = GameObjects.closest(object -> object.getName().equalsIgnoreCase(ore.name) &&
                        object.hasAction("Mine") &&
                        object.getModelColors() != null &&
                        object.distance(pl.getTile()) <= miningTolerance);

                if (obj != null) {
                    return obj;
                }
        }

        return null;
    }
}