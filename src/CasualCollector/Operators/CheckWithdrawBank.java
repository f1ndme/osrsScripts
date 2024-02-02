package CasualCollector.Operators;

import CasualCollector.FrameWork.OperatorBase;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;

import java.util.Optional;

import static org.dreambot.api.utilities.Logger.log;

public class CheckWithdrawBank extends OperatorBase {
    private Optional<PreBankCheckFunction> bankCheckOption = Optional.empty();
    public interface PreBankCheckFunction {
        Item getHighestBankedPickaxe();
    }

    private String itemName;
    public CheckWithdrawBank(String itemName, PreBankCheckFunction onBankCheckFunction) {
        this.itemName = itemName;
        super.locationName = itemName; //lol
        this.bankCheckOption = Optional.of(onBankCheckFunction);
    }

    public boolean operating() {
        if (Bank.open()) {
            doExtraBankFunction();
            Sleep.sleepUntil(Bank::isOpen, 1801);

            return proceedWithCheckOperation();
        }

        return true;
    }





    private void doExtraBankFunction() {
        bankCheckOption.ifPresent(check -> {
            Item itemToWithdraw = check.getHighestBankedPickaxe();
            if (itemToWithdraw != null) {
                itemName = itemToWithdraw.getName();
                super.locationName = itemToWithdraw.getName();
            }
        });
    }

    private boolean proceedWithCheckOperation() {
        Item itemToCheck = Bank.get(item -> item.getName().contains(itemName));
        if (isValidItem(itemToCheck)) {
            return proceedWithWithdrawal();
        } else {
            return false;
        }
    }

    private boolean proceedWithWithdrawal() {
        if (Bank.withdraw(itemName)) {
            sleepUntilContainsItem();
            return !Inventory.contains(itemName);
        }
        return false;
    }

    private void sleepUntilContainsItem() {
        Sleep.sleepUntil(()-> Inventory.contains(itemName), 2401);
        Sleep.sleep(Calculations.random(801, 1201));
    }

    private boolean isValidItem(Item item) {
        return item != null && item.isValid();
    }


}