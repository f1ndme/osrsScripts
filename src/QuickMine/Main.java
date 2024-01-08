package QuickMine;

import QuickMine.resources.QuickInfo;
import QuickMine.resources.QuickTime;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

import java.awt.*;

import static QuickMine.resources.Enums.Priority.*;

@ScriptManifest(category = Category.MINING, name = "Quick Mine 2.0", description = "Mines stuff.", author = "find me", version = 1.0)
public class Main extends AbstractScript {
    QuickTime quickTime;
    QuickInfo quickInfo;
    TaskManager taskManager;


    @Override
    public void onStart() {
        quickTime = new QuickTime();
        quickTime.setup();

        quickInfo = new QuickInfo();

        taskManager = new TaskManager();
        taskManager.onStart();
        taskManager.onSetup(JUSTGO);
    }

    @Override
    public void onPause() {
        quickTime.onPause();
    }

    @Override
    public void onResume() {
        quickTime.onResume();
    }


    @Override
    public int onLoop() {
        quickTime.onLoop();

        taskManager.onLoop();

        return 20;
    }

    @Override
    public void onPaint(Graphics g) {
        quickTime.onPaint(g);

        quickInfo.onPaint(g);

        taskManager.onPaint(g);
    }
}
