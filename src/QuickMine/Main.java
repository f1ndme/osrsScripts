package QuickMine;

import QuickMine.ui.PlayerInfo;
import QuickMine.ui.ScriptTime;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

import java.awt.*;

import static QuickMine.resources.Enums.Priority.*;

@ScriptManifest(category = Category.MINING, name = "Quick Mine 2.0", description = "Mines stuff.", author = "find me", version = 1.0)
public class Main extends AbstractScript {
    ScriptTime scriptTime;
    PlayerInfo playerInfo;
    TaskManager taskManager;


    @Override
    public void onStart() {
        scriptTime = new ScriptTime(getRandomManager());

        playerInfo = new PlayerInfo();

        taskManager = new TaskManager(JUSTGO);
    }

    @Override
    public void onPause() {
        scriptTime.onPause();
    }

    @Override
    public void onResume() {
        scriptTime.onResume();
    }


    @Override
    public int onLoop() {
        scriptTime.onLoop();

        taskManager.onLoop();

        return 20;
    }

    @Override
    public void onPaint(Graphics g) {
        scriptTime.onPaint(g);

        playerInfo.onPaint(g);

        taskManager.onPaint(g);
    }
}
