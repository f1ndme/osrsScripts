package CasualCollector.MiningTask.Events;

import BotScript.Operators.Operator;
import CasualCollector.FrameWork.EventBase;
import CasualCollector.Operators.CheckPickupGroundItem;
import CasualCollector.Operators.CheckWithdrawBank;
import CasualCollector.Operators.GoTo;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.wrappers.items.Item;

import java.util.ArrayList;
import java.util.List;


public class CollectPickaxe extends EventBase implements CheckWithdrawBank.PreBankCheckFunction {

    public CollectPickaxe() {
        addOperator(new GoTo(Bank.getClosestBankLocation(false)));
        addOperator(new CheckWithdrawBank("pickaxe", this));
        addOperator(new GoTo(new Area(3082, 3431, 3084, 3429)));
        addOperator(new CheckPickupGroundItem("Bronze pickaxe"));
        operatorChanged();
    }

    public boolean operating() {
        return super.operating();
    }















    public Item getHighestBankedPickaxe() { //call from operator, when bank is open, to update a better pickaxe to withdraw, if possible. Otherwise it gets the original name checked.
        buildUsablePickaxeNames();
        buildUsablePickaxeEnums();
        buildHighestUsablePickaxeEnum();

        return highestBankedPickaxe();
    }

    public Operator.Items enumFromName(String itemName) {
        for (Operator.Items item : Operator.Items.values()) {
            if (itemName.equals(item.name)) {
                return item;
            }
        }

        return null;
    }
    public List<String> usablePickaxeNames;
    public void buildUsablePickaxeNames() {
        usablePickaxeNames = new ArrayList<>();

        for (Operator.Items pickaxe : Operator.Items.values()) {
            if (pickaxe.name.contains("pickaxe")) {
                if (pickaxe.requiredLevel <= Skills.getRealLevel(Skill.MINING)) {
                    usablePickaxeNames.add(pickaxe.name);
                }
            }
        }
    }

    public List<Operator.Items> usablePickaxeEnums;
    public void buildUsablePickaxeEnums() {
        usablePickaxeEnums = new ArrayList<>();

        for (Operator.Items pickaxe : Operator.Items.values()) {
            if (pickaxe.name.contains("pickaxe")) {
                if (pickaxe.requiredLevel <= Skills.getRealLevel(Skill.MINING)) {
                    usablePickaxeEnums.add(pickaxe);
                }
            }
        }
    }
    Operator.Items highestUsablePickaxeEnum;
    public void buildHighestUsablePickaxeEnum() {

        int winning = 0;
        Operator.Items winner = Operator.Items.BRONZEPICKAXE;
        for (Operator.Items pickaxeEnum : usablePickaxeEnums) {
            if (pickaxeEnum.requiredLevel >= winning && pickaxeEnum.requiredLevel <= Skills.getRealLevel(Skill.MINING)) {
                winning = pickaxeEnum.requiredLevel;
                winner = pickaxeEnum;
            }
        }

        highestUsablePickaxeEnum = winner;
    }

    public Item highestBankedPickaxe() {
        List<Item> bankedPickaxes = Bank.all(item -> item.getName().contains("pickaxe"));
        Item pickaxe = null;

        if (!bankedPickaxes.isEmpty()) {
            for (Item bankedpickaxe : bankedPickaxes) {
                if (bankedpickaxe.getName().equals(highestUsablePickaxeEnum.name)) {
                    pickaxe = bankedpickaxe; //found best usable pickaxes in bank.
                }
            }
            if (pickaxe == null) { //if we found best, dont continue.
                List<Item> bankedUsables = new ArrayList<>();

                for (Item bankedPickaxe : bankedPickaxes) {
                    if (usablePickaxeNames.contains(bankedPickaxe.getName())) {
                        pickaxe = bankedPickaxe;
                        bankedUsables.add(bankedPickaxe);
                    }
                }

                if (bankedUsables.size() > 1) {
                    int winning = 0;
                    Item winner = null;
                    for (Item usablePickaxe : bankedUsables) {
                        String pickaxeName = usablePickaxe.getName();
                        if (usablePickaxeNames.contains(pickaxeName)) {
                            Operator.Items pickaxeEnum = enumFromName(pickaxeName);

                            if (pickaxeEnum.requiredLevel >= winning) {
                                if (!pickaxeEnum.name.contains("Bronze")) {
                                    winning = pickaxeEnum.requiredLevel;
                                    winner = usablePickaxe;
                                }else if (winner == null) {
                                    winning = pickaxeEnum.requiredLevel; //todo. stops confusion with lvl 1 iron & bronze. need to fix in table. assuming bronze gets processed first, <= would work to pass iron. but the order isnt consistent.
                                    winner = usablePickaxe;
                                }
                            }
                        }
                    }
                    if (winner != null) {
                        pickaxe = winner;
                    }
                }
            }
        }

        return pickaxe;
    }
}
