package CasualCollector.Operators;

import CasualCollector.FrameWork.OperatorBase;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Player;

public class GoTo extends OperatorBase {
    private final Tile location;
    public GoTo(Tile tile) {
        location = tile;
        super.locationName = tile.toString();
    }
    public GoTo(Area area) {
        location = area.getCenter().getTile();
        super.locationName = area.getTile().toString();
    }
    public GoTo(BankLocation bankLocation) {
        location = bankLocation.getCenter().getTile();
        super.locationName = bankLocation.name();
    }


    public boolean operating() {
        if (!arrivedAtLocation()) return true;

        return false;
    }

    public boolean arrivedAtLocation() {
        if (location == null) return true; //die if no location.

        if (locationContainsPlayer()) {
            return true;
        }

        if (playerNotMoving() || (Walking.isRunEnabled()? Walking.shouldWalk(9) : Walking.shouldWalk(5))) {
            if (Walking.walk(location)) {
                if ( playerNotMoving() ) {
                    Sleep.sleepUntil(this::playerMoving, 1201);
                }else {
                    Sleep.sleepUntil(this::playerNotMoving, 1201);
                }
            }
        }

        return false;
    }

    public boolean locationContainsPlayer() {
        Player pl = Players.getLocal();
        if (!pl.exists() || location == null) return false;

        int locationFinishRadius = 3;
        return location.getArea(locationFinishRadius).contains(pl);
    }
}
