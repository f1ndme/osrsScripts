package BotScript;

import BotScript.Operators.*;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.script.event.impl.ExperienceEvent;
import org.dreambot.api.script.impl.TaskScript;
import org.dreambot.api.wrappers.items.Item;

import java.awt.*;

public class TaskManager extends TaskScript implements Miner.MinerInterface {
    public UIManager uiManager;
    public MinerBase miner;
    public WoodCutter woodCutter;
    public Smelter smelter;
    TaskManager(UIManager uiManager) {
        this.uiManager = uiManager;

        miner = new Miner(uiManager, this);
        woodCutter = new WoodCutter(uiManager, this);
        smelter = new Smelter(uiManager, this);

        //decider = new Decider(uiManager, this);

        addNodes(smelter); //miner, woodCutter
    }

    public void onFullInventory() {
        log("Full inventory!");
    }

    public void onBankedInventory() {
        log("Banked inventory!");
    }

    public void removeOperator(Operator operator) {
        if (uiManager.allDualTexts.contains(operator.eventNotifier)) {
            uiManager.allDualTexts.remove(operator.eventNotifier);
        }
        removeNodes((TaskNode)operator);
        if (operator == miner) {
            miner = null;
        }else if(operator == woodCutter) {
            woodCutter = null;
        }else if(operator == smelter) {
            smelter = null;
        }
    }


    public void think() {

    }



    @Override
    public void onPaint(Graphics g) {
        prePaint(g);
        paint(g);
        postPaint(g);
    }

    public void prePaint(Graphics g) {
        for (TaskNode node : getNodes()) {
            Operator op = (Operator)node;

            op.prePaint(g);
        }
    }

    public void paint(Graphics g) {
        for (TaskNode node : getNodes()) {
            Operator op = (Operator)node;

            op.paint(g);
        }
    }

    public void postPaint(Graphics g) {
        for (TaskNode node : getNodes()) {
            Operator op = (Operator)node;

            op.postPaint(g);
        }
    }

    public void onInventoryItemAdded(Item item) {
        for (TaskNode node : getNodes()) {
            Operator op = (Operator)node;

            op.onInventoryItemAdded(item);
        }
    }

    public void onLevelUp(ExperienceEvent event) {
        for (TaskNode node : getNodes()) {
            Operator op = (Operator)node;

            op.onLevelUp(event);
        }
    }
}