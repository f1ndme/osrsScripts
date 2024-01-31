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

        if (!groundItems.isEmpty() && groundItems.getFirst().exists()) {
            GroundItem itemToTake = groundItems.getFirst();

            if (itemToTake.exists()) {
                Sleep.sleepUntil(()->itemToTake.interact("Take"), 2401);
                if ( playerNotMoving() ) {Sleep.sleepUntil(this::playerMoving, 1201);}
                Sleep.sleepUntil(this::playerNotMoving, this::playerMoving, 1801, 300);

            }
        }

        if (Inventory.contains(itemName)) {
            return false;
        }

        return true;
    }
}
