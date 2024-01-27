package BotScript.Operators;

import BotScript.TaskManager;
import BotScript.UIManager;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.pushingpixels.substance.internal.utils.WidgetUtilities;

import java.util.List;

import static BotScript.Operators.Operator.ExecutionCase.*;

public class Smelter extends Operator {
    public Smelter(UIManager uiManager, TaskManager taskManager) {
        super(uiManager, taskManager);
    }





    public boolean hasOresForBronze() {
        List<Item> inventoryCopper = Inventory.all(item -> item.getName().equals("Copper ore"));
        List<Item> inventoryTin = Inventory.all(item -> item.getName().equals("Tin ore"));

        if (!inventoryCopper.isEmpty() && !inventoryTin.isEmpty()) {
            return true;
        }

        return false;
    }
    public boolean bankHasOresForBronze() {
        List<Item> bankCopper = Bank.all(item -> item.getName().equals("Copper ore"));
        List<Item> bankTin = Bank.all(item -> item.getName().equals("Tin ore"));

        if (!bankCopper.isEmpty() && !bankTin.isEmpty()) {
            return true;
        }

        return false;
    }



    @Override
    public boolean accept() {
        if (stillSolving) {
            return true;
        }

        if (!hasOresForBronze()) {
            setExecutionCase(COLLECTBRONZEORES);
            return true;
        }else {
            setExecutionCase(SMELTBRONZEBAR);
            return true;
        }
    }
    @Override
    public int execute() {
        stillSolving = true;
        switch (executionCase) {
            case COLLECTBRONZEORES: if (collectOresForBronze()) {return 100;} else {break;}
            case SMELTBRONZEBAR: if (smeltBronzeBar()) {return 100;} else {break;}
        }
        stillSolving = false;
        return 100;
    }


    public boolean smeltBronzeBar() {

        if (!arrivedAtFurnace(new Tile(3109, 3499))) return true;

        GameObject furnace = GameObjects.closest("Furnace");

        if (furnace == null || !furnace.exists()) return true;

        if (!playerAnimating()) {
            if (Widgets.get(270, 14) == null) {
                Sleep.sleepUntil(()->furnace.interact("Smelt"), 2401);
                Sleep.sleep(Calculations.random(801, 1201));

            }

            if (hasOresForBronze()) {
                WidgetChild bronzeButton = Widgets.get(270, 14);

                if (bronzeButton != null && bronzeButton.getActions()[0].equals("Smelt")) {
                    Sleep.sleepUntil(()->bronzeButton.interact("Smelt"), 1801);
                    Sleep.sleep(Calculations.random(801, 1201));
                }
            }

            return false;
        }

        Sleep.sleep(Calculations.random(3001, 3601));

        return true;
    }

    public boolean collectOresForBronze() {
        if (hasOresForBronze()) return false;

        log("No ores for bronze bar, solving");

        if (!arrivedAtBank(Bank.getClosestBankLocation(false))) return true;

        if (!Bank.open() || !Bank.isOpen()) return true;

        if (!Inventory.isEmpty()) Sleep.sleepUntil(Bank::depositAllItems, 1801);
        Sleep.sleep(Calculations.random(801, 1201));

        if (bankHasOresForBronze() && Bank.withdraw("Copper ore", 14) && Bank.withdraw("Tin ore", 14)) {
            Sleep.sleepUntil(this::hasOresForBronze, 1801);
            log("We have ores to smelt a bronze bar!");
            Bank.close();
            return false;
        }

        return true;
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

    public boolean arrivedAtFurnace(Tile tile) {
        Player pl = Players.getLocal();
        Area furnaceArea = tile.getArea(3);
        if (!pl.exists() || tile == null || furnaceArea == null) return false;

        if (furnaceArea.contains(pl)) {
            return true;
        }

        if (playerNotMoving() || (Walking.isRunEnabled()? Walking.shouldWalk(9) : Walking.shouldWalk(5))) {
            if (Walking.walk(tile)) {
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
