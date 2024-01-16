package DoStuff.Mine.Tasks;

import DoStuff.Mine.MineManager;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Sleep;

public class Banker extends TaskNode {
    public MineManager manager;
    public Banker(MineManager manager) {
        this.manager = manager;
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

    boolean randomDropper;
    @Override
    public int execute() {
        if (!randomDropper) {
            if (Calculations.chance(0, 4)) {
                log("Chanced drop. lul");
                Sleep.sleepUntil(this::droppedInventory, 15000);
                return 100;
            }
            randomDropper = true;
        }

        Sleep.sleepUntil(this::bankScreenOpen, this::playerMoving, 15000, 601);

        if (bankScreenOpen()) {
            Sleep.sleepUntil(this::inventoryDeposited, 15000);

            manager.positioner.targetLocation = null;
            manager.positioner.alreadyArrived = false; //lol fck
            manager.miner.targetNode = null;
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
        return Bank.open(Bank.getClosestBankLocation(true));
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
