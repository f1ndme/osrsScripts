package CasualCollector.Operators;

import CasualCollector.FrameWork.OperatorBase;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;

import static org.dreambot.api.utilities.Logger.log;

public class CheckWithdrawBank extends OperatorBase {

    private String itemName;
    public CheckWithdrawBank(String itemName) {
        this.itemName = itemName;
        super.locationName = itemName; //lol
    }

    public CheckWithdrawBank(String itemName, PreBankCheckFunction onBankOpenFunction) {
        this.itemName = itemName;
        super.locationName = itemName; //lol

        this.onBankOpenFunction = onBankOpenFunction;
    }
    PreBankCheckFunction onBankOpenFunction;
    public interface PreBankCheckFunction {
        public Item getHighestBankedPickaxe();
    }
    public void doExtraBankFunction() {
        if (onBankOpenFunction != null) {
            Item itemToWithdraw = onBankOpenFunction.getHighestBankedPickaxe();
            if (itemToWithdraw != null) {
                itemName = itemToWithdraw.getName();
                locationName = itemToWithdraw.getName(); //lol
            }
        }
    }



    public boolean operating() {
        if (Bank.open()) {
            doExtraBankFunction();

            Sleep.sleepUntil(Bank::isOpen, 1801);
            Item itemToCheck = Bank.get(item -> item.getName().contains(itemName));

            if (itemToCheck != null && itemToCheck.isValid()) {
                if (Bank.withdraw(itemToCheck.getName())) {
                    Sleep.sleepUntil(()-> Inventory.contains(itemToCheck), 2401);
                    Sleep.sleep(Calculations.random(801, 1201));

                    if (Inventory.contains(itemToCheck)) {
                        return false;
                    }
                }
            }else {
                return false;
            }
        }

        return true;
    }


}