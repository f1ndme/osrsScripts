package QuickMine.tasks;

import QuickMine.resources.ENUMS;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;

import java.awt.*;

public class MiningTask extends TaskNode {

    final int miningTolerance = 12;

    Player pl;
    int miningLevel;
    ENUMS.ORE highestMinableOre;
    GameObject attemptingOre;

    @Override
    public boolean accept() {
        miningLevel = Skills.getRealLevel(Skill.MINING);
        highestMinableOre = ENUMS.ORE.highestMinable(miningLevel);
        attemptingOre = getClosestOre(highestMinableOre.name);
        pl = Players.getLocal();

        boolean hasHighestUsable = ENUMS.PICKAXE.hasHighestUsable(miningLevel);
        boolean nearHighestMinable = getClosestOre(highestMinableOre.name).distance(Players.getLocal().getTile()) <= miningTolerance;
        boolean playerNull = (pl == null);

        return !Inventory.isFull() && !isMining() && hasHighestUsable && nearHighestMinable && !playerNull;
    }

    @Override
    public int execute() {
        if (attemptingOre == null) return Calculations.random(300, 600);

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