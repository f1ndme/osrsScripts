package BotScript.Operators;

import BotScript.Elements.DualText;
import BotScript.TaskManager;
import BotScript.UIManager;
import org.dreambot.api.Client;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;

import java.awt.*;

public class Operator extends TaskNode implements UIManager.TextCommands {
    public UIManager uiManager;
    public TaskManager taskManager;
    public void onTextCommandPressed(int id) {

    }
    public void onDualTextPressed(int id) {

    }
    public DualText notifier;
    public void buildTextNotifier(Operator operator, UIManager uiManager) {
        notifier = uiManager.DualText("(Decider) ", States.WAITING.text, 5, Client.getViewportHeight()-185, Color.green, Color.white, operator);
    }
    public enum States {
        WAITING("Waiting...", Color.yellow, Color.white),
        PLAYEREXIST("Player does not exist...", Color.red, Color.white),
        FULLINVENTORY("Inventory is full...", Color.red, Color.white),
        NOPICKAXE("No pickaxe...", Color.red, Color.white),
        STILLMOVING("Player is moving...", Color.red, Color.white),
        TOOFAR("Current target too far...", Color.red, Color.white),
        NOMINABLE("No minable ores near...", Color.red, Color.white),
        ATTEMPTING("Attempting to mine...", Color.yellow, Color.white),
        MINING("Mining...", Color.green, Color.white),
        DECIDING("Deciding...", Color.green, Color.white),
        OPERATING("Operating...", Color.green, Color.white);

        final String text;
        final Color color;
        final Color colorTwo;

        States(String text, Color level, Color colorTwo) {
            this.text = text;
            this.color = level;
            this.colorTwo = colorTwo;
        }
    }

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

    public boolean playerStartedMoving() {
        Player pl = Players.getLocal();

        return pl != null && pl.exists() && pl.isMoving();
    }
    public boolean playerNotMoving() {
        Player pl = Players.getLocal();

        return pl != null && pl.exists() && !pl.isMoving();
    }

    public States currentState;
    public void stateChanged(States last, States current) {
        //log("(DECIDER) Last state (" + last + ") Current state (" + current + ")");
        if (notifier != null) {
            notifier.textTwo = current.text;
            notifier.color = current.color;
            notifier.colorTwo = current.colorTwo;
        }
    }
    public void setCurrentState(States currentState) {
        stateChanged(this.currentState, currentState);

        this.currentState = currentState;
    }
}
