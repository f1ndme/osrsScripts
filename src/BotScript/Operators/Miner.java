package BotScript.Operators;

import BotScript.TaskManager;
import BotScript.UIManager;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.Shop;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.world.World;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.script.event.impl.ExperienceEvent;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;

import java.util.ArrayList;
import java.util.List;

import static BotScript.Operators.Operator.ExecutionCase.*;

public class Miner extends MinerBase {
    public Miner(UIManager uiManager, TaskManager taskManager) {
        super(uiManager, taskManager);
    }

    public interface MinerInterface {
        public void onFullInventory();
        public void onBankedInventory();
    }

    @Override
    public boolean accept() {
        if (stillSolving) {
            return true;
        }

        if (Inventory.isFull() && atDwarvenMines()) {
            taskManager.onFullInventory();
            setExecutionCase(FULLINVENTORYDWARVEN);
            return true;
        }
        if (Inventory.isFull()) {
            taskManager.onFullInventory();
            setExecutionCase(FULLINVENTORY);
            return true;
        }
        if (!hasPickaxe()) {
            setExecutionCase(NOPICKAXE);
            return true;
        }

        if (minableReachable().isEmpty()) {
            setExecutionCase(NOMINABLE);
            return true;
        }else {
            setExecutionCase(ORESTOMINE);
            return true;
        }
    }
    @Override
    public int execute() {
        stillSolving = true;
        switch (executionCase) {
            case NOPICKAXE: if (solvingNoPickaxe()) {return 100;} else {break;}
            case NOMINABLE: if (solvingNoMinable()) {return 100;} else {break;}
            case FULLINVENTORY: if (solvingFullInventory()) {return 100;} else {break;}
            case FULLINVENTORYDWARVEN: if (solvingDwarvenFullInventory()) {return 100;} else {break;}
            case ORESTOMINE: if (solvingOresToMine()) {return 100;} else {break;}
        }
        stillSolving = false;
        return 100;
    }


    //todo SOLVING Dwarven Full Inventory
    public boolean soldAllPossibleOres;
    public boolean soldAllUncuts;
    public boolean bankedRemainingInventory;
    public boolean solvingDwarvenFullInventory() {
        if (!soldAllPossibleOres) {
            if (hasSellableOres()) {
                setEventState(EventState.SELLINGORES);
                if (arrivedAt(Locations.DWARVENORESHOP) && Shop.open(Shops.DWARVENORE.shopID) && Shop.isOpen() && soldAllOres()) {
                    Shop.close();
                    soldAllPossibleOres = true;
                }
                return true;
            }

            soldAllPossibleOres = true;
        }

        if (!soldAllUncuts) {
            if (hasSellableUncuts()) {
                setEventState(EventState.SELLINGUNCUTS);
                if (arrivedAt(Locations.DWARVENGENERALSHOP) && Shop.open(Shops.DWARVENGENERAL.shopID) && Shop.isOpen() && soldAllUncuts()) {
                    Shop.close();
                    soldAllUncuts = true;
                }
                return true;
            }

            soldAllUncuts = true;
        }

        if (Inventory.emptySlotCount() < 5 || bankedRemainingInventory) {
            if (!bankedRemainingInventory) {
                if (eventState != EventState.FULLORESHOP) {setEventState(EventState.FULLORESHOP); }
                if (arrivedAt(Bank.getClosestBankLocation(false)) && Bank.open() && Bank.isOpen()) {
                    Item inventoryPickaxe = highestPickaxeInInventory();
                    String bestPickaxe = "Bronze pickaxe"; //should deposit smaller pickaxes.
                    if (inventoryPickaxe != null) {
                        bestPickaxe = inventoryPickaxe.getName();
                    }

                    if (Bank.depositAllExcept(bestPickaxe)) {
                        setRandomLocationToMine();
                        setEventState(EventState.LOCATIONRANDOM);
                        bankedRemainingInventory = true;
                        taskManager.onBankedInventory(); //Banked fire.
                    }
                }
                return true;
            }

            if (!arrivedAt(randomLocation)) return true;

            soldAllPossibleOres = false;
            soldAllUncuts = false;
            bankedRemainingInventory = false;
            return false;
        }

        if (clayIsUnder(4)) {
            setEventState(EventState.DROPPINGCLAY);
            if (!droppedAllClay(4)) {
                return true;
            }
        }

        if (eventState != EventState.LOCATIONDWARVEN) {setEventState(EventState.LOCATIONDWARVEN);}
        if (!arrivedAt(RandomDwarvenMine())) return true;

        soldAllPossibleOres = false;
        soldAllUncuts = false;
        bankedRemainingInventory = false;
        return false;
    }


    //todo SOLVING Full Inventory
    public boolean bankedInventory;
    public Items betterPickaxeToPurchase;
    public boolean coalToSell;
    public boolean solvingFullInventory() {
        if (!bankedInventory) {
            setEventState(EventState.GOINGTOBANK);
            if (arrivedAt(Bank.getClosestBankLocation(false)) && Bank.open() && Bank.isOpen()) {
                Item inventoryPickaxe = highestPickaxeInInventory();
                String bestPickaxe = "Bronze pickaxe";
                if (inventoryPickaxe != null) {
                    bestPickaxe = inventoryPickaxe.getName();
                }

                if (Bank.depositAllExcept(bestPickaxe)) {
                    betterPickaxeToPurchase = checkForPurchasablePickaxe();
                    if (betterPickaxeToPurchase != null) {
                        if (Bank.withdrawAll("Coins")) {
                            setEventState(EventState.PURCHASEPICKAXE);
                            setRandomLocationToMine();
                            bankedInventory = true;
                        }
                    }else {
                        setEventState(EventState.LOCATIONRANDOM);
                        setRandomLocationToMine();

                        if (Bank.contains("Coal") && Bank.get("Coal").getAmount() > 51) {
                            if (Bank.withdrawAll("Coal")) {
                                setEventState(EventState.SELLINGCOAL);
                                coalToSell = true;
                            }
                        }

                        bankedInventory = true;
                        taskManager.onBankedInventory(); //Banked fire.
                    }
                }
            }
            return true;
        }

        if (betterPickaxeToPurchase != null) {
            if (!arrivedAt(Locations.DWARVENPICKAXESHOP)) return true;

            if (!Inventory.contains("Coins") || Inventory.count("Coins") < betterPickaxeToPurchase.cost) { //We might have died on the way here. stop solving, no money.
                log("Arrived to purchase pickaxe, but no money? We might have died. Relocating to minable area.");
                setEventState(EventState.LOCATIONRANDOM);
                betterPickaxeToPurchase = null;
            }

            if (Shop.open(Shops.DWARVENPICKAXE.shopID) && Shop.isOpen()) {
                Sleep.sleepUntil(() -> {return Shop.purchase(betterPickaxeToPurchase.name, 1);}, 1801);
                if (Inventory.contains(betterPickaxeToPurchase.name)) {
                    log("Better pickaxe purchased!");
                    setEventState(EventState.LOCATIONRANDOM);
                    betterPickaxeToPurchase = null;
                    Sleep.sleep(Calculations.random(801, 1201));
                }else {
                    log("Couldn't buy pickaxe, shop must be out, hopping worlds.");
                    World randomWorld = Worlds.getRandomWorld(Worlds.getNormalizedWorlds());
                    WorldHopper.hopWorld(randomWorld);
                }
            }

            return true;
        }

        if (coalToSell) {
            if (!arrivedAt(Locations.DWARVENORESHOP)) return true;

            if (Inventory.contains("Coal")) {
                if (Shop.open(Shops.DWARVENORE.shopID) && Shop.isOpen()) {
                    if (Shop.count("Coal") < Items.COAL.storeBuyLimit) {
                        if (Shop.sellFifty("Coal")) {
                            log("Coal sold!");
                            coalToSell = false;
                            setEventState(EventState.LOCATIONRANDOM);
                            Sleep.sleep(Calculations.random(801, 1201));
                        }
                    }else {
                        log("Shop has no space to buy our coal, hopping worlds.");
                        hopRandomFreeWorld();
                    }
                }
                return true;
            }
        }

        if (!arrivedAt(randomLocation)) return true;

        bankedInventory = false;
        return false;
    }


    //todo SOLVING No Pickaxe
    public boolean checkedBankForPickaxe;
    public boolean solvingNoPickaxe() {
        if (hasPickaxe()) { checkedBankForPickaxe = false; return false; }

        if (!checkedBankForPickaxe) {
            setEventState(EventState.PICKAXEBANKCHECK);
            if (arrivedAt(Bank.getClosestBankLocation(false)) && Bank.open() && Bank.isOpen()) {
                if (!Bank.contains(item -> item.getName().contains("pickaxe"))) {
                    checkedBankForPickaxe = true;
                    setEventState(EventState.PICKAXEFREECHECK);
                    return true;
                }

                Item pickaxeToWithdraw = highestBankedPickaxe();
                if (pickaxeToWithdraw != null) {
                    if (Bank.withdraw(pickaxeToWithdraw.getName())) {
                        Sleep.sleepUntil(this::hasPickaxe, 2401);
                        if (hasPickaxe()) {
                            checkedBankForPickaxe = true;
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        if (arrivedAt(Locations.FREEPICKAXE)) {
            if (foundFreePickaxe()) {
                if (pickedUpFreePickaxe()) {
                    Sleep.sleepUntil(this::hasPickaxe, 2401);
                    if (hasPickaxe()) {
                        checkedBankForPickaxe = false;
                        return false;
                    }
                }
            }else {
                if (eventState != EventState.PICKAXEFREEWAIT) {
                    setEventState(EventState.PICKAXEFREEWAIT);
                }
                Sleep.sleep(5000);
            }
        }

        return true;
    }



    //todo SOLVING No Minable
    public boolean solvingNoMinable() {
        setEventState(EventState.LOCATIONCLOSEST);
        return !arrivedAt(closestMinableLocation());//true to keep thinking.
    }


    //todo SOLVING Ores To Mine
    long timeToBounce = 0;
    public Tile targetTile;
    public GameObject targetObject;
    public boolean solvingOresToMine() {
        hopOnCongestedMining();

        if (System.currentTimeMillis() > timeToBounce) {
            timeToBounce = System.currentTimeMillis() + 60000;

            targetObject = furthestReachable();
            targetTile = targetObject.getTile();
            return true;
        }

        if (targetObject != null && targetTile != null) {
            GameObject node = objectFrom(targetTile);

            if (node == null || node.getName().equals("Rocks") || Inventory.isFull()) {
                targetObject = null;
                targetTile = null;
                return false;
            }

            if (!playerAnimating()) {
                setEventState(EventState.MININGATTEMPT);
                if (targetObject.interact("Mine")) {
                    Sleep.sleepUntil(this::playerAnimating, 3601);
                    setEventState(EventState.MINING);
                }else {
                    targetObject = null;
                    targetTile = null;
                    return false;
                }
            }
            return true;
        }


        targetObject = closestReachable();
        targetTile = targetObject.getTile();
        return true;
    }













    public Items checkForPurchasablePickaxe() {
        Item coins = Bank.get("Coins");
        int coinsCount = coins.getAmount();

        if (coins.isValid()) {
            log("Coins: " + coinsCount);

            Item bankedPickaxe = highestBankedPickaxe();
            Item inventoryPickaxe = Inventory.get(item -> item.getName().contains("pickaxe")); //todo get all picks, check for best.
            Item equipmentPickaxe = Equipment.get(item -> item.getName().contains("pickaxe")); //todo get all picks, check for best.

            Items highestUsablePurchasable = Items.BRONZEPICKAXE;
            int winning = 0;
            for (Items pickaxeEnum : usablePickaxeEnums) {
                if (pickaxeEnum.cost <= coinsCount && pickaxeEnum.cost > winning) {
                    winning = pickaxeEnum.cost;
                    highestUsablePurchasable = pickaxeEnum;
                }
            }

            Items bestEnumPickaxe = Items.BRONZEPICKAXE;
            if (bankedPickaxe != null) {
                for (Items pickaxeEnum : usablePickaxeEnums) {
                    if (pickaxeEnum.name.equals(bankedPickaxe.getName())) {
                        if (bestEnumPickaxe.requiredLevel < pickaxeEnum.requiredLevel) {
                            bestEnumPickaxe = pickaxeEnum;
                        }
                    }
                }
            }
            if (inventoryPickaxe != null && inventoryPickaxe.isValid()) {
                for (Items pickaxeEnum : usablePickaxeEnums) {
                    if (pickaxeEnum.name.equals(inventoryPickaxe.getName())) {
                        if (bestEnumPickaxe.requiredLevel < pickaxeEnum.requiredLevel) {
                            bestEnumPickaxe = pickaxeEnum;
                        }
                    }
                }
            }
            if (equipmentPickaxe != null) {
                for (Items pickaxeEnum : usablePickaxeEnums) {
                    if (pickaxeEnum.name.equals(equipmentPickaxe.getName())) {
                        if (bestEnumPickaxe.requiredLevel < pickaxeEnum.requiredLevel) {
                            bestEnumPickaxe = pickaxeEnum;
                        }
                    }
                }
            }

            if (highestUsablePurchasable != bestEnumPickaxe && highestUsablePurchasable.requiredLevel >= bestEnumPickaxe.requiredLevel) {
                //todo go buy pickaxe.
                return highestUsablePurchasable;
            }
        }

        return null;
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
                        pickaxe = bankedPickaxe; //found A usable pickaxe. probably the lowest cause indexing.
                        bankedUsables.add(bankedPickaxe);
                    }
                }
                if (bankedUsables.size() > 1) {
                    int winning = 0;
                    Item winner = null;
                    for (Item usablePickaxe : bankedUsables) {
                        String pickaxeName = usablePickaxe.getName();
                        if (usablePickaxeNames.contains(pickaxeName)) {
                            Items pickaxeEnum = enumFromName(pickaxeName);
                            if (pickaxeEnum.requiredLevel > winning) {
                                winning = pickaxeEnum.requiredLevel;
                                winner = usablePickaxe;
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
    public boolean foundFreePickaxe() {
        List<GroundItem> groundItems = GroundItems.all(groundItem -> groundItem.getItem().getName().equals("Bronze pickaxe"));
        if (!groundItems.isEmpty() && groundItems.getFirst().exists()) {
            return true;
        }

        return false;
    }
    public boolean pickedUpFreePickaxe() {
        List<GroundItem> groundItems = GroundItems.all(groundItem -> groundItem.getItem().getName().equals("Bronze pickaxe"));
        if (!groundItems.isEmpty()) {
            GroundItem bronzePickaxe = groundItems.getFirst();
            if (bronzePickaxe.exists()) {
                if (bronzePickaxe.interact("Take")) {
                    return true;
                }
            }
        }

        return false;
    }
    public boolean hasSellableOres() {
        return Inventory.contains(item -> item.getName().contains("ore") || item.getName().contains("Coal"));
    }
    public boolean soldAllOres() {
        if (Shop.isOpen()) {
            List<Items> toSell = Shops.DWARVENORE.containingItems;

            for (Items itemEnum : toSell) {
                if (!Shop.isOpen()) return true;

                if (Inventory.contains(itemEnum.name) && Shop.count(itemEnum.name) < itemEnum.storeBuyLimit) {
                    Item inventoryItem = Inventory.get(itemEnum.name);

                    if (inventoryItem != null && inventoryItem.isValid()) {
                        Sleep.sleepUntil(() -> {return Shop.sellFifty(inventoryItem);}, 1801);
                        Sleep.sleep(Calculations.random(801, 1201));
                    }
                }
            }

            return true;
        }

        return false;
    }
    public boolean hasSellableUncuts() {
        return Inventory.contains(item -> item.getName().contains("Uncut"));
    }
    public boolean soldAllUncuts() {
        if (Shop.isOpen()) {
            List<Items> toSell = Shops.DWARVENGENERAL.containingItems;

            for (Items itemEnum : toSell) {
                if (!Shop.isOpen()) return true;

                if (Inventory.contains(itemEnum.name) && Shop.count(itemEnum.name) < itemEnum.storeBuyLimit) {
                    Item inventoryItem = Inventory.get(itemEnum.name);

                    if (inventoryItem != null && inventoryItem.isValid()) {
                        Sleep.sleepUntil(() -> {return Shop.sellFifty(inventoryItem);}, 1801);
                        Sleep.sleep(Calculations.random(801, 1201));
                    }
                }
            }

            return true;
        }

        return false;
    }









    public void onLevelUp(ExperienceEvent event) {
        super.onLevelUp(event);
    }
}
