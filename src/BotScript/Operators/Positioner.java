package BotScript.Operators;

import BotScript.TaskManager;
import BotScript.UIManager;
import org.dreambot.api.Client;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Player;

import static BotScript.Operators.Operator.States.*;
import static BotScript.Operators.Operator.States.WAITING;

public class Positioner extends Operator implements UIManager.TextCommands {
    public Positioner(UIManager uiManager, TaskManager taskManager) {
        this.uiManager = uiManager;
        this.taskManager = taskManager;

        buildTextNotifier(this, uiManager);
        setCurrentState(WAITING);
        notifier.text = "(Positioner)";
        notifier.y = Client.getViewportHeight() - 215;
    }



    public States executionState;
    @Override
    public boolean accept() {
        if (taskManager.miner.currentState == NOMINABLE && taskManager.banker == null) {
            executionState = NOMINABLE;
            return true;
        }

        return false;
    }


    public boolean arrivedAtTarget() {
        Player pl = Players.getLocal();
        Tile plTile = pl.getTile();

        if (pl.exists() && plTile != null) {
            if (plTile.distance(targetTile) <= 3) {
                return true;
            }
        }

        if (pl.exists()) {
            if (!pl.isMoving()) {
                Walking.walk(targetTile);

                Sleep.sleepWhile(this::playerNotMoving, 6001);
            }
        }

        return false;
    }

    public Tile targetTile;
    @Override
    public int execute() {
        setCurrentState(OPERATING);

        if (executionState == NOMINABLE) {
            if (targetTile == null) {
                targetTile = TaskManager.Locations.random().area.getCenter().getTile();
            }

            Sleep.sleepUntil(this::arrivedAtTarget,()->{Player pll = Players.getLocal(); return pll != null && pll.exists() && pll.isMoving();}, 1801, 300);

            if (arrivedAtTarget()) {
                taskManager.removeOperator(this);
            }
        }

        return 100;
    }


}