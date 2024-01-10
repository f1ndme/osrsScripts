package QuickMine.tasks;

import QuickMine.resources.Enums.*;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static QuickMine.resources.Enums.Ores.allMineableOres;
import static QuickMine.resources.Enums.Pickaxes.hasUsablePickaxe;

public class QuickMineTask extends TaskNode {
    final int miningTolerance = 18;

    Player pl;
    GameObject attemptingOre;
    Tile playerTile;

    @Override
    public boolean accept() {
        pl = Players.getLocal();
        if (pl == null) return false;

        playerTile = pl.getTile();
        if (playerTile == null) return false;

        return hasUsablePickaxe() && !Inventory.isFull();
    }

    public long nextMineTry;
    public final int preMineDelay = 3001;
    public boolean shouldTryMine() {
        return !Calculations.isBefore(nextMineTry) && !isMining() && !pl.isMoving();
    }

    @Override
    public int execute() {
        attemptingOre = getClosestMinableOre();
        if (attemptingOre == null) return 50;

        if (isMining()) return Calculations.random(300, 601);
        if (pl.isMoving()) return Calculations.random(300, 601); //ore distance based delay.

        if (attemptingOre.interact("Mine")) {
            nextMineTry = System.currentTimeMillis() + Calculations.random(preMineDelay, preMineDelay+300);
            Sleep.sleepUntil(this::shouldTryMine, 30000);
        }

        return Calculations.random(300, 601);
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
        List<GameObject> availableNodes = new ArrayList<>();

        for (int i = allMineableOres().size(); i-- > 0; ) {
            Ores ore = allMineableOres().get(i);

            List<GameObject> obj = GameObjects.all(object -> object.getName().equalsIgnoreCase(ore.name) &&
                    object.hasAction("Mine") &&
                    object.getModelColors() != null &&
                    object.distance(pl.getTile()) <= miningTolerance);

            if (obj != null) { //god kill this dumpster fire.
                for (int k=obj.size(); k-- > 0; ) {
                    availableNodes.add(obj.get(k));
                }
            }
        }

        GameObject randomOre = Collections.unmodifiableList(availableNodes).get(new Random().nextInt(Collections.unmodifiableList(availableNodes).size()));

        if (randomOre != null) {
            return randomOre;
        }

        return null;
    }
}