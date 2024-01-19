package BotScript.Operators;

import org.dreambot.api.script.TaskNode;
import org.dreambot.api.wrappers.items.Item;

import java.awt.*;

public class Operator extends TaskNode {

    @Override
    public boolean accept() {

        return true;
    }

    @Override
    public int execute() {

        return 100;
    }

    public void prePaint(Graphics g) {

    }
    public void paint(Graphics g) {

    }
    public void postPaint(Graphics g) {

    }
    public void onInventoryItemAdded(Item item) {

    }
}
