package QuickMine.tasks;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.script.TaskNode;

public class DropInventoryTask extends TaskNode {


    @Override
    public boolean accept() {
        return Inventory.isFull();
    }

    @Override
    public int execute() {
        Inventory.dropAll(item -> item.getName().contains("ore") || item.getName().contains("Clay") || item.getName().contains("Uncut") || item.getName().contains("Clue") || item.getName().contains("scroll"));

        return Calculations.random(300, 600);
    }
}