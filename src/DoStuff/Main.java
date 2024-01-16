package DoStuff;


import DoStuff.Mine.MineManager;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.ItemContainerListener;
import org.dreambot.api.wrappers.items.Item;

import java.awt.*;

@ScriptManifest(category = Category.UTILITY, name = "Do Stuff", description = "It does stuff.", author = "find me", version = 1.0)
public class Main extends AbstractScript implements ItemContainerListener {
    public int loopDelay = 100;
    public MineManager mineManager;


    @Override
    public void onStart() {
        mineManager = new MineManager();

    }

    public void onInventoryItemAdded(Item item) {
        mineManager.onInventoryItemAdded(item);
    }


    @Override
    public int onLoop() {
        mineManager.onLoop();

        return loopDelay;
    }

    @Override
    public void onPaint(Graphics g) {
        if (mineManager == null) return;
        mineManager.drawInfo(g);
    }

}
