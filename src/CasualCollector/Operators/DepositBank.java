package CasualCollector.Operators;

import CasualCollector.FrameWork.OperatorBase;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;

import java.util.ArrayList;
import java.util.List;

public class DepositBank extends OperatorBase {

    private final List<Item> typesList;
    public DepositBank(List<Item> itemsList) {
        this.typesList = typesListFrom(itemsList);
        super.locationName = "" + itemsList.size(); //lol
    }

    public List<Item> typesListFrom(List<Item> itemsList) {
        List<Item> typesList = new ArrayList<>();
        for (Item inventoryItem : itemsList) {
            if (!typesList.contains(inventoryItem)) {
                typesList.add(inventoryItem);
            }
        }
        return typesList;
    }

    public boolean operating() {
        if (Bank.open()) {
            Sleep.sleepUntil(Bank::isOpen, 1801);

            for (Item itemTypeToDeposit : typesList) {
                if (!Bank.isOpen()) return true;

                Sleep.sleepUntil(()->Bank.depositAll(itemTypeToDeposit), 1801);
                Sleep.sleep(Calculations.random(201, 401));
            }

            return false;
        }

        return true;
    }
}
