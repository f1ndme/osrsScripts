package CasualCollector.MiningTask.Events;

import CasualCollector.FrameWork.EventBase;
import CasualCollector.Operators.DepositBank;
import CasualCollector.Operators.GoTo;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;

public class BankOres extends EventBase {
    public BankOres() {
        addOperator(new GoTo(Bank.getClosestBankLocation(false)));
        addOperator(new DepositBank(Inventory.all(item -> item.getName().contains("ore") || item.getName().contains("Coal") || item.getName().contains("Clay") || item.getName().contains("Uncut") || item.getName().contains("Clue") || item.getName().contains("Coins"))));
        operatorChanged();
    }

    public boolean operating() {
        return super.operating();
    }

}
