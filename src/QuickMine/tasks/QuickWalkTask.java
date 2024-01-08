package QuickMine.tasks;

import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.walking.path.impl.GlobalPath;
import org.dreambot.api.methods.walking.pathfinding.impl.web.WebPathQuery;
import org.dreambot.api.methods.walking.pathfinding.impl.web.WebPathResponse;
import org.dreambot.api.methods.walking.web.node.AbstractWebNode;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.interactive.Player;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import static QuickMine.resources.Enums.*;

public class QuickWalkTask extends TaskNode {
    final int swagger = 5;
    final int maxStuckTrys = 12;
    final int tillTarget = 9;
    final int tillFinalTarget = 3;
    final double trysUntilRepath = 0.65;

    int stuckTrys;
    boolean shouldWalk;
    Timer tryTimer;
    Tile targetTile;
    GlobalPath<AbstractWebNode> currentPath;
    Tile currentTile;
    Player pl;
    int index;

    Locations selectedLocation;
    Tile selectedDestination;




    @Override
    public boolean accept() {
        pl = Players.getLocal();
        if (selectedLocation == null) {
            chooseStartingArea();
        }

        return (pl != null) && !selectedLocation.area.contains(pl.getTile());
    }

    @Override
    public int execute() {
        boolean finished = walkLoop(); //handle walk loop.
        if (finished) {
            log("Location reached!");
            return 1000;
        }

        return 50;
    }

    private void chooseStartingArea() {
        List<Ores> minableOres = Ores.allMineableOres();

        Ores randomOre = Collections.unmodifiableList(minableOres).get(new Random().nextInt(Collections.unmodifiableList(minableOres).size()));
        int randomKey = new Random().nextInt(randomOre.locations.size());

        selectedLocation = randomOre.locations.get(randomKey);
        selectedDestination = selectedLocation.area.getRandomTile(); //random area, random tile
        log("Ore selected to mine near: " + randomOre.name + ". Location selected to mine: " + selectedLocation + ".");

        startWalk(selectedDestination);
    }

    public void startWalk(Tile finalDestinationTile, boolean... previousStucks) {
        targetTile = finalDestinationTile;
        currentPath = buildPath(pl.getTile(), targetTile);
        currentTile = currentPath.first().getTile();

        tryTimer = new Timer();
        tryTimer.setRunTime(10000); //let it move freely for first click.

        index = 0;

        if (previousStucks.length == 0) {
            stuckTrys = 0;
        }

        shouldWalk = true;
    }

    public void tryStuckWalk(Tile toTile) {
        if (stuckTrys>=maxStuckTrys) { log("Max stuck trys reached... Something went wrong... We are killing script now. "); getScriptManager().stop(); return; }

        if (tryTimer.elapsed() <= 1500) return;
        tryTimer.reset();

        if(stuckTrys > 2 && stuckTrys < maxStuckTrys*trysUntilRepath) { //early stucks, lets warn
            log("Something is going on... Possibilities: Connection Error || Stuck || Door || NPC Block || Bumpy Terrain || Target Tile on Tree/Rock/ect || Tricky Building");
            log("Trying to move... Attempts before script kill(" + stuckTrys + "/" + maxStuckTrys +")");
        }else if (stuckTrys >= maxStuckTrys*0.65) { //maybe situational
            log("Uh oh, stuckin hard... Trying to re-path...");
            log("Trying to move... Attempts before script kill(" + stuckTrys + "/" + maxStuckTrys +")");

            startWalk(targetTile, true); //this go forever...
            return;
        }

        log("Walking...");
        Walking.walk(toTile);
        stuckTrys++;
    }


    public boolean walkLoop() {
        if (!shouldWalk) return false;

        pl = Players.getLocal();
        if (anyNull()) return false;

        if (targetTile.distance(pl.getTile()) <= tillFinalTarget) { shouldWalk = false; return true; } //finish line check. this 'fails' if already by finish, cause it loops to node wasting time. do surround check before moving.

        if (!withinTolerance(tillTarget) && pl.isMoving()) return false;

        if (withinTolerance(tillTarget)) {
            if (lastNode()) {
                if (!currentTile.equals(targetTile)) {
                    currentTile = targetTile;
                }

                if (!pl.isMoving()) {
                    tryStuckWalk(currentTile);
                }

                return false;
            }

            index = index+1;
            currentTile = currentPath.get(index).getTile().getRandomized(swagger);
            stuckTrys = 0;

            tryStuckWalk(currentTile);
            return false;
        }

        tryStuckWalk(currentTile);
        return false;
    }







    public boolean withinTolerance(int tolerance) {
        return (currentTile.distance(pl.getTile()) <= tolerance);
    }

    public boolean lastNode() {
        return (index == currentPath.size()-1);
    }

    public boolean anyNull() {
        return (targetTile == null || currentPath == null || currentTile == null || pl == null);
    }

    public GlobalPath<AbstractWebNode> buildPath(Tile startingTile, Tile destinationTile) {
        WebPathQuery.WebPathQueryBuilder builder = WebPathQuery.builder();
        builder.from(startingTile);
        builder.to(destinationTile);

        WebPathResponse calc = builder.build().calculate();
        builder.calculated(calc);

        //check if path is null, or no nodes? possibly failed once?
        return builder.build().getGlobalPath();
    }
}