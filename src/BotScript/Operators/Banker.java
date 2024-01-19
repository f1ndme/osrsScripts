package BotScript.Operators;

import BotScript.TaskManager;
import BotScript.UIManager;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.utilities.Sleep;

public class Banker extends Operator {
    public TaskManager taskManager;
    boolean randomDropper;
    public UIManager uiManager;
    public Banker(TaskManager taskManager, UIManager uiManager) {
        this.taskManager = taskManager;
        this.uiManager = uiManager;


    }







    @Override
    public boolean accept() {
        if (Players.getLocal() == null) {
            log("Player is null");
            return false;
        }

        if (!Players.getLocal().exists()) {
            log("Player does not exist");
            return false;
        }

        if (!containsOres()) {
            return false;
        }

        if (!Inventory.isFull()) {
            return false;
        }

        return true;
    }

    @Override
    public int execute() {
        if (!randomDropper) {
            if (!Calculations.chance(0, 2)) {
                log("Chanced drop. lul");
                Sleep.sleepUntil(this::droppedInventory, 15000);
                return 100;
            }
            randomDropper = true;
        }

        Sleep.sleepUntil(this::bankScreenOpen, this::playerMoving, 15000, 601);

        if (bankScreenOpen()) {
            Sleep.sleepUntil(this::inventoryDeposited, 15000);

            taskManager.positioner.targetLocation = null;
            taskManager.positioner.alreadyArrived = false; //lol fck
            taskManager.miner.targetNode = null;
            randomDropper = false;
        }

        return 100;
    }








    public boolean droppedInventory() {
        if (Inventory.dropAll(item -> item.getName().contains("ore") || item.getName().contains("Clay") || item.getName().contains("Coal") || item.getName().contains("Uncut") || item.getName().contains("Clue") || item.getName().contains("scroll"))) {
            return false;
        }

        return true;
    }

    public boolean inventoryDeposited() {
        if (containsOres()) {
            Bank.depositAll(item -> item.getName().contains("ore") || item.getName().contains("Clay") || item.getName().contains("Coal") || item.getName().contains("Uncut") || item.getName().contains("Clue") || item.getName().contains("scroll"));
            return false;
        }

        return true;
    }

    public boolean containsOres() {
        return Inventory.contains(item -> item.getName().contains("ore") || item.getName().contains("Clay") || item.getName().contains("Coal") || item.getName().contains("Uncut") || item.getName().contains("Clue") || item.getName().contains("scroll"));
    }

    public boolean bankScreenOpen() {
        return Bank.open(Bank.getClosestBankLocation(false));
    }

    public boolean playerMoving() {
        if (Players.getLocal() == null) {
            log("Player is null");
            return true;
        }

        if (!Players.getLocal().exists()) {
            log("Player does not exist");
            return true;
        }

        return Players.getLocal().isMoving();
    }
}
