package BotScript.Operators;

import BotScript.TaskManager;
import BotScript.UIManager;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Player;

import static BotScript.Operators.Operator.States.*;

public class Decider extends Operator implements UIManager.TextCommands {
    public Decider(UIManager uiManager, TaskManager taskManager) {
        this.uiManager = uiManager;
        this.taskManager = taskManager;

        buildTextNotifier(this, uiManager);
        setCurrentState(WAITING);
        notifier.text = "(DECIDER)";
    }



    public States executionState;
    @Override
    public boolean accept() {
        if (taskManager.miner.currentState == FULLINVENTORY && taskManager.banker == null && taskManager.dwarvenHustler == null && taskManager.positioner == null) {
            executionState = FULLINVENTORY;
            return true;
        }
        if (taskManager.miner.currentState == NOMINABLE && taskManager.banker == null && taskManager.positioner == null) {
            executionState = NOMINABLE;
            return true;
        }

        return false;
    }

    public boolean atDwarvenMines() {
        Player pl = Players.getLocal();
        Tile plTile = pl.getTile();
        if (!pl.exists() || plTile == null) return false;

        double winning = 100000;
        TaskManager.Locations winner = null;
        for (TaskManager.Locations location : TaskManager.Locations.values()) {
            Tile center = location.area.getCenter().getTile();
            if (center.distance(plTile) < winning) {
                winning = center.distance(plTile);
                winner = location;
            }
        }
        if (winner != null) {
            if (winner == TaskManager.Locations.DWARVENNORTH || winner == TaskManager.Locations.DWARVENNORTHEAST || winner == TaskManager.Locations.DWARVENSOUTH) {
                return true;
            }
        }


        return false;
    }


    public boolean hustlerWasDispatched;
    @Override
    public int execute() {
        setCurrentState(DECIDING);

        switch (executionState) {
            case NOMINABLE:
                log("(DECIDER) Dispatching positioner...");
                taskManager.positioner = new Positioner(uiManager, taskManager);
                taskManager.addNodes(taskManager.positioner);

                Sleep.sleep(1000);
                break;

            case FULLINVENTORY:
                if (atDwarvenMines()) {
                    log("at dwarven mines");
                    if (!hustlerWasDispatched) {
                        log("(DECIDER) Dispatching Dwarven Mine Hustler...");
                        taskManager.dwarvenHustler = new DwarvenHustler(uiManager, taskManager);
                        taskManager.addNodes(taskManager.dwarvenHustler);

                        setCurrentState(WAITING);
                        return 100;
                    }
                }

                log("(DECIDER) Dispatching banker...");
                taskManager.banker = new Banker(uiManager, taskManager);
                taskManager.addNodes(taskManager.banker);

                hustlerWasDispatched = false; //i think this is ok?

                Sleep.sleep(2000);
                break;
        }

        setCurrentState(WAITING);
        return 100;
    }
}
