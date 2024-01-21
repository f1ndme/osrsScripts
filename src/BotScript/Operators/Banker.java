package BotScript.Operators;

import BotScript.TaskManager;
import BotScript.UIManager;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;

import java.util.ArrayList;
import java.util.List;

import static BotScript.Operators.Operator.States.*;

public class Banker extends Operator implements UIManager.TextCommands {
    public UIManager uiManager;
    public TaskManager taskManager;
    public List<Item> toBank;
    public Banker(UIManager uiManager, TaskManager taskManager, List<Item>... toBankList) {
        this.uiManager = uiManager;
        this.taskManager = taskManager;
        if (toBankList.length > 0) {
            this.toBank = toBankList[0];
        }

        buildTextNotifier(this, uiManager);
        setCurrentState(WAITING);
        notifier.text = "(BANKER)";
        notifier.y = Client.getViewportHeight() - 215;
    }


    @Override
    public boolean accept() {
        Player pl = Players.getLocal();

        if (!pl.exists()) {
            log("Player is null");
            return false;
        }

        return true;
    }

    public boolean arrivedAtBank() {
        Player pl = Players.getLocal();
        Tile plTile = pl.getTile();
        Tile targetTile = Bank.getClosestBankLocation(false).getCenter().getTile();

        if (pl.exists() && plTile != null && targetTile != null) {
            if (plTile.distance(targetTile) <= 3) {
                if (Bank.open(Bank.getClosestBankLocation(false))) {
                    return true;
                }
            }

            if (!pl.isMoving()) {
                Walking.walk(targetTile);

                Sleep.sleepWhile(this::playerNotMoving, 6001);
            }
        }

        if (pl.exists() && targetTile != null) {
            if (!pl.isMoving()) {
                Walking.walk(targetTile);

                Sleep.sleepWhile(this::playerNotMoving, 6001);
            }
        }

        return false;
    }

    @Override
    public int execute() {
        Player pl = Players.getLocal();

        if (!pl.exists()) {
            log("Player is null");
            return 100;
        }

        setCurrentState(OPERATING);

        if (!Bank.isOpen()) {
            Sleep.sleepUntil(this::arrivedAtBank, this::playerStartedMoving, 1801, 300);
        }

        if (!Bank.isOpen()) {
            return 100;
        }else {
            if (!Bank.open(Bank.getClosestBankLocation(false))) {
                return 100;
            }
        }
        //bank is open now...

        Sleep.sleepUntil(this::playerNotMoving, 1801);
        //slept until stopped moving...

        log("We should be standing still, with bank screen open now. Waiting a second...");
        Sleep.sleep(Calculations.random(801, 1201));

        if (toBank == null) {
            Sleep.sleepUntil(Bank::depositAllItems, 12001);

            Sleep.sleep(Calculations.random(1201, 1801));
            if (!Bank.isOpen()) {
                return 100;
            }

            Sleep.sleepUntil(Bank::close, 2401);

            if (!Bank.isOpen()) {
                log("Banker removed.");
                taskManager.removeOperator(this);
            }

            return 100;
        }

        //log("We have stuff to bank..."); //lol this is dumb.

        ArrayList<String> typesToDeposit = new ArrayList<>();
        for (Item item : toBank) {
            if (!typesToDeposit.contains(item.getName()) && Inventory.contains(item.getName())) {
                typesToDeposit.add(item.getName());
            }
        }

        ArrayList<Item> itemsToDeposit = new ArrayList<>();
        for (Item item : Inventory.all()) {
            if (item != null) {
                if (!itemsToDeposit.contains(item) && typesToDeposit.contains(item.getName())) {
                    itemsToDeposit.add(item);
                }
            }
        }


        for (Item item : itemsToDeposit) {
            if (!Bank.isOpen()) {
                return 100;
            }

            Sleep.sleepUntil(() -> {return Bank.depositAll(item);}, 1801);
            Sleep.sleep(Calculations.random(801, 1201));
        }

        if (Bank.isOpen()) {

            Sleep.sleepUntil(Bank::close, 2401);

            Sleep.sleep(Calculations.random(301, 601));

            log("Banker removed.");
            taskManager.removeOperator(this);
        }

        return 100;
    }
}
