package BotScript.Operators;

import BotScript.TaskManager;
import BotScript.UIManager;
import org.dreambot.api.utilities.Sleep;

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
        if (taskManager.miner.currentState == FULLINVENTORY && taskManager.banker == null) {
            executionState = FULLINVENTORY;
            return true;
        }
        if (taskManager.miner.currentState == NOMINABLE && taskManager.banker == null && taskManager.positioner == null) {
            executionState = NOMINABLE;
            return true;
        }

        return false;
    }


    @Override
    public int execute() {
        setCurrentState(DECIDING);

        switch (executionState) {
            case NOMINABLE:
                log("(DECIDER) Dispatching positioner...");
                taskManager.positioner = new Positioner(uiManager, taskManager); //construct with target too?
                taskManager.addNodes(taskManager.positioner);

                Sleep.sleep(1000);
                break;

            case FULLINVENTORY:
                log("(DECIDER) Dispatching banker...");
                taskManager.banker = new Banker(uiManager, taskManager);
                taskManager.addNodes(taskManager.banker);

                Sleep.sleep(2000);
                break;
        }

        setCurrentState(WAITING);
        return 100;
    }
}
