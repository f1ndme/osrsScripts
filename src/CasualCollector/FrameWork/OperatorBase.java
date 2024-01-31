package CasualCollector.FrameWork;

import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.wrappers.interactive.Player;

public class OperatorBase {
    public String locationName = "";

    public boolean operating() {

        return false;
    }

    public boolean playerMoving() {
        Player pl = Players.getLocal();

        return pl.exists() && pl.isMoving();
    }

    public boolean playerNotMoving() {
        Player pl = Players.getLocal();

        return pl.exists() && !pl.isMoving();
    }

    public boolean playerAnimating() {
        Player pl = Players.getLocal();

        return pl.exists() && pl.isAnimating();
    }
}
