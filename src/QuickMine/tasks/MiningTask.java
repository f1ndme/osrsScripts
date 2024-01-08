package QuickMine.tasks;

import QuickMine.resources.Enums.Ores;
import QuickMine.resources.Enums.Pickaxes;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;

public class MiningTask extends TaskNode {

    final int miningTolerance = 12;

    Player pl;
    int miningLevel;
    Ores highestMinableOre;
    GameObject attemptingOre;
    Tile playerTile;

    @Override
    public boolean accept() {
        pl = Players.getLocal();
        playerTile = pl.getTile();
        if (pl == null || playerTile == null) return false;

        if (!Pickaxes.hasHighestUsablePickaxe()) return false;
        highestMinableOre = Ores.highestMineableOre();

        attemptingOre = getClosestOre(highestMinableOre.name);
        if (attemptingOre == null) return false;

        boolean nearHighestMinable = attemptingOre.distance(playerTile) <= miningTolerance;
        if (!nearHighestMinable) return false;

        return !Inventory.isFull() && !isMining();
    }

    @Override
    public int execute() {
        if (attemptingOre.interact("Mine")) {
            Sleep.sleepUntil(this::isMining, 4200);
        }

        return Calculations.random(300, 600);
    }

    private boolean isMining() {
        boolean playerAnimating = pl.isAnimating();
        boolean attemptingOreExists = attemptingOre.getModelColors() != null;

        return playerAnimating && attemptingOreExists;
    }

    private GameObject getClosestOre(String... name) {
        if (name != null && name.length >0) {
            return GameObjects.closest(object -> object.getName().equalsIgnoreCase(name[0]) && object.hasAction("Mine") && object.getModelColors() != null);
        }else {
            return GameObjects.closest(object -> object.getName().equalsIgnoreCase("Rocks") && object.hasAction("Mine") && object.getModelColors() != null);
        }
    }
}