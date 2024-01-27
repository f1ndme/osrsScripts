package BotScript;

import org.dreambot.api.methods.container.impl.Shop;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.event.impl.ExperienceEvent;
import org.dreambot.api.script.listener.ExperienceListener;
import org.dreambot.api.script.listener.HumanMouseListener;
import org.dreambot.api.script.listener.ItemContainerListener;
import org.dreambot.api.wrappers.items.Item;

import java.awt.*;
import java.awt.event.MouseEvent;

@ScriptManifest(category = Category.UTILITY, name = "Bot Script", description = "It does cool stuff.", author = "find me", version = 1.0)
public class Main extends AbstractScript implements HumanMouseListener, ItemContainerListener, ExperienceListener {

    public UIManager uiManager;
    public TaskManager taskManager;
    @Override
    public void onStart() {
        uiManager = new UIManager();
        taskManager = new TaskManager(uiManager);
    }

    @Override
    public int onLoop() {
        return taskManager.onLoop();
    }

    public void think() {

        if (uiManager != null) {
            uiManager.think();
        }

        if (taskManager != null) {
            taskManager.think();
        }
    }

    @Override
    public void onPaint(Graphics g) {
        think();

        if (uiManager != null) {
            uiManager.onPaint(g);
        }

        if (taskManager != null) {
            taskManager.onPaint(g);
        }
    }





    public void onMouseMoved(MouseEvent mouse) {
        uiManager.onMouseMoved(mouse);
    }
    public void onMousePressed(MouseEvent mouse) {
        uiManager.onMousePressed(mouse);
    }
    public void onMouseReleased(MouseEvent mouse) {
        uiManager.onMouseReleased(mouse);
    }
    public void onInventoryItemAdded(Item item) {
        taskManager.onInventoryItemAdded(item);
    }
    public void onLevelUp(ExperienceEvent event) {taskManager.onLevelUp(event);}
}
