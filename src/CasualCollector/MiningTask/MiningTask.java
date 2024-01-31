package CasualCollector.MiningTask;

import BotScript.Operators.Operator;
import CasualCollector.MiningTask.Events.BankOres;
import CasualCollector.MiningTask.Events.CollectOres;
import CasualCollector.MiningTask.Events.CollectPickaxe;
import CasualCollector.MiningTask.Events.FindOres;
import CasualCollector.FrameWork.TaskBase;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.wrappers.interactive.GameObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MiningTask extends TaskBase {

    CollectPickaxe collectPickaxeEvent;
    CollectOres collectOresEvent;
    BankOres bankOresEvent;
    FindOres findOresEvent;
    public MiningTask() {
        super();

        buildMinableRockNames();
    }

    public void think() {
        if (Inventory.isFull()) {
            if (!events.contains(bankOresEvent)) {
                bankOresEvent = new BankOres();
                addEvent(bankOresEvent);
            }
        }else { //todo. Inventory has space
            if (hasPickaxe()) {
                if (events.contains(collectPickaxeEvent)) {
                    removeEvent(collectPickaxeEvent);
                }

                if (!minableReachable().isEmpty() && !events.contains(findOresEvent)) { //dont try mine en-route to location.
                    if (!events.contains(collectOresEvent)) {
                        collectOresEvent = new CollectOres();
                        addEvent(collectOresEvent);
                    }
                }else { //todo. No Reachable Ores
                    if (!events.contains(findOresEvent)) {
                        findOresEvent = new FindOres();
                        addEvent(findOresEvent);
                    }
                }
            }else { //todo. No Pickaxe
                if (!events.contains(collectPickaxeEvent)) {
                    collectPickaxeEvent = new CollectPickaxe();
                    addEvent(collectPickaxeEvent);
                }
                if (events.contains(collectOresEvent)) {
                    removeEvent(collectOresEvent);
                }
                if (events.contains(findOresEvent)) {
                    removeEvent(findOresEvent);
                }
            }
        }
    }

    public int onLoop() {
        return super.onLoop();
    }

    public void onPaint(Graphics g) {
        super.onPaint(g);
    }

    public boolean hasPickaxe() {
        return (Equipment.contains(item -> item.getName().contains("pickaxe")) || Inventory.contains(item -> item.getName().contains("pickaxe")));
    }

    public java.util.List<GameObject> allReachable() {
        return GameObjects.all(object -> object.hasAction("Mine") && object.distance(Players.getLocal().getTile()) <= 9 && object.getModelColors() != null);
    }
    public java.util.List<GameObject> minableReachable() {
        List<GameObject> newList = new ArrayList<>();

        for (GameObject object : allReachable()) {
            if (minableRockNames.contains(object.getName())) {
                newList.add(object);
            }
        }

        return newList;
    }

    public List<String> minableRockNames;
    public void buildMinableRockNames() {
        minableRockNames = new ArrayList<>();

        for (Operator.Objects rock : Operator.Objects.values()) {
            if (rock.name.contains("rocks")) {
                if (rock.requiredLevel <= Skills.getRealLevel(Skill.MINING)) {
                    minableRockNames.add(rock.name);
                }
            }
        }
    }
}
