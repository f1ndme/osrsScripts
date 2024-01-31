package BotScript.Operators;

import BotScript.TaskManager;
import BotScript.UIManager;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;

import java.util.ArrayList;
import java.util.List;

import static BotScript.Operators.Operator.ExecutionCase.*;

public class WoodCutter extends Operator implements UIManager.TextCommands {
    public WoodCutter(UIManager uiManager, TaskManager taskManager) {
        super(uiManager, taskManager);

        buildUsableAxeEnums();
        buildUsableAxeNames();
        buildHighestUsableAxeEnum();
    }

    public List<String> usableAxeNames;
    public void buildUsableAxeNames() { //todo Names List
        usableAxeNames = new ArrayList<>();

        for (Items axe : Items.values()) {
            if (getLastName(axe.name).contains("axe")) {
                if (axe.requiredLevel <= Skills.getRealLevel(Skill.WOODCUTTING)) {
                    usableAxeNames.add(axe.name);
                }
            }
        }
    }
    public List<Items> usableAxeEnums;
    public void buildUsableAxeEnums() { //todo Enums List
        usableAxeEnums = new ArrayList<>();

        for (Items axe : Items.values()) {
            if (getLastName(axe.name).contains("axe")) {
                if (axe.requiredLevel <= Skills.getRealLevel(Skill.WOODCUTTING)) {
                    usableAxeEnums.add(axe);
                }
            }
        }
    }
    Items highestUsableAxeEnum;
    public void buildHighestUsableAxeEnum() { //todo Enum

        int winning = 0;
        Items winner = Items.BRONZEAXE;
        for (Items axeEnum : usableAxeEnums) {
            if (axeEnum.requiredLevel >= winning && axeEnum.requiredLevel <= Skills.getRealLevel(Skill.WOODCUTTING)) {
                winning = axeEnum.requiredLevel;
                winner = axeEnum;
            }
        }

        highestUsableAxeEnum = winner;
    }

    public String getLastName(String input) {
        String[] splitArray = input.split("\\s+");

        return splitArray[splitArray.length-1];
    }
    public boolean hasAxe() {
        List<Item> inventoryAxes = Inventory.all(item -> getLastName(item.getName()).equals("axe"));
        List<Item> equipmentAxes = Equipment.all(item -> getLastName(item.getName()).equals("axe"));

        if (!inventoryAxes.isEmpty() || !equipmentAxes.isEmpty()) {
            return true;
        }

        return false;
    }
    public boolean bankHasAxe() {
        List<Item> bankAxes = Bank.all(item -> getLastName(item.getName()).equals("axe"));

        if (!bankAxes.isEmpty()) {
            return true;
        }

        return false;
    }










    @Override
    public boolean accept() {
        if (stillSolving) {
            return true;
        }

        if (!hasAxe()) {
            setExecutionCase(NOAXE);
            return true;
        }

        return false;
    }
    @Override
    public int execute() {
        stillSolving = true;
        switch (executionCase) {
            case NOAXE: if (collectBestAxeFromBank()) {return 100;} else {break;}
        }
        stillSolving = false;
        return 100;
    }

    public boolean collectBestAxeFromBank() {
        if (hasAxe()) return false;

        log("No axe, solving");

        if (!arrivedAtBank(Bank.getClosestBankLocation(false))) return true;

        if (!Bank.open() || !Bank.isOpen()) return true;

        if (bankHasAxe() && Bank.withdraw(highestBankedAxe().getName())) {
            Sleep.sleepUntil(this::hasAxe, 1801);
            log("We have our best axe!");
            return false;
        }

        return true;
    }
















    public Item highestBankedAxe() {
        List<Item> bankedAxes = Bank.all(item -> getLastName(item.getName()).equals("axe"));
        Item axeToWithdraw = null;

        if (!bankedAxes.isEmpty()) {
            if (bankedAxes.size() == 1) {
                return bankedAxes.getFirst();
            }

            List<Item> bankedUsableAxes = new ArrayList<>();
            for (Item bankedAxe : bankedAxes) { //Found highest usable pickaxes in bank, grab it.
                if (bankedAxe.getName().equals(highestUsableAxeEnum.name)) {
                    return bankedAxe;
                }

                if (usableAxeNames.contains(bankedAxe.getName())) {
                    bankedUsableAxes.add(bankedAxe);
                }
            }


            if (!bankedUsableAxes.isEmpty()) { //winner of usable in bank.
                int winning = 0;
                Item winner = null;

                for (Item bankedUsableAxe : bankedUsableAxes) {
                    String axeName = bankedUsableAxe.getName();
                    Items axeEnum = enumFromName(axeName);
                    if (axeEnum.requiredLevel > winning) {
                        winning = axeEnum.requiredLevel;
                        winner = bankedUsableAxe;
                    }
                }

                if (winner != null) {
                    axeToWithdraw = winner;
                }
            }

        }

        return axeToWithdraw;
    }
    public boolean bankContains(BankLocation bank) {
        Player pl = Players.getLocal();
        Tile targetTile = bank.getCenter().getTile();
        if (!pl.exists() || targetTile == null) return false;

        return targetTile.getArea(5).contains(pl);
    }
    public boolean arrivedAtBank(BankLocation bank) {
        Tile bankCenterTile = bank.getCenter().getTile();
        if (bankCenterTile == null) return false;

        if (bankContains(bank)) {
            return true;
        }

        if (playerNotMoving() || (Walking.isRunEnabled()? Walking.shouldWalk(9) : Walking.shouldWalk(5))) {
            if (Walking.walk(bankCenterTile)) {
                if ( playerNotMoving() ) {
                    Sleep.sleepUntil(this::playerMoving, 1201);
                }else {
                    Sleep.sleepUntil(this::playerNotMoving, 1201);
                }
            }
        }

        return false;
    }
}
