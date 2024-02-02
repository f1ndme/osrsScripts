package CasualCollector.Operators;

import CasualCollector.FrameWork.OperatorBase;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.GroundItem;

import java.util.List;

public class CheckPickupGroundItem extends OperatorBase {
    private final String itemName;
    public CheckPickupGroundItem(String itemName) {
        this.itemName = itemName;
        super.locationName = itemName; //lol
    }

    public boolean operating() {
        List<GroundItem> groundItems = GroundItems.all(groundItem -> groundItem.getItem().getName().equals(itemName));

        if (isValidList(groundItems)) {
            GroundItem itemToTake = groundItems.getFirst();

            if (isValidItem(itemToTake)) {
                sleepUntilItemTaken(itemToTake);
                if ( playerNotMoving() ) sleepUntilPlayerMoves();
                sleepUntilPlayerStops();
            }
        }

        return !Inventory.contains(itemName);
    }





    private boolean isValidList(List<GroundItem> groundItems) {
        return !groundItems.isEmpty() && groundItems.getFirst()!= null && groundItems.getFirst().exists();
    }

    private boolean isValidItem(GroundItem groundItem) {
        return groundItem != null && groundItem.exists();
    }

    private void sleepUntilItemTaken(GroundItem itemToTake) {
        Sleep.sleepUntil(() -> itemToTake.interact("Take"), 2401);
    }

    private void sleepUntilPlayerMoves() {
        Sleep.sleepUntil(this::playerMoving, 1201);
    }

    private void sleepUntilPlayerStops() {
        Sleep.sleepUntil(this::playerNotMoving, this::playerMoving, 1801, 300);
    }
}
