package QuickMine.tasks;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Player;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import static QuickMine.resources.Enums.*;

public class QuickWalkTask extends TaskNode {
    Player pl;
    Locations selectedLocation;
    Tile selectedDestination;
    static long nextWalkTry;
    final static int preWalkDelay = 1501;

    @Override
    public boolean accept() {
        pl = Players.getLocal();
        if (pl == null) return false;
        if (pl.getTile() == null) return false;

        if (selectedLocation == null) {
            chooseStartingArea();
        }

        if (selectedDestination == null) return false;

        return !selectedLocation.area.contains(pl.getTile());
    }

    @Override
    public int execute() {
        if (selectedDestination.distance(pl.getTile()) >= 6) {
            Walking.walk(selectedDestination);
            nextWalkTry = System.currentTimeMillis() + Calculations.random(preWalkDelay, preWalkDelay+300);
            Sleep.sleepUntil(this::shouldTryWalk, 30000);
        }

        return 50;
    }

    public boolean shouldTryWalk() {
        double distance = pl.getTile().distance(Walking.getDestination());
        boolean nearTarget = distance <= ((Walking.isRunEnabled()) ? 5 : 2);

        return !Calculations.isBefore(nextWalkTry) && (!pl.isMoving() || nearTarget);
    }

    private void chooseStartingArea() {
        List<Ores> minableOres = Ores.allMineableOres();

        Ores randomOre = Collections.unmodifiableList(minableOres).get(new Random().nextInt(Collections.unmodifiableList(minableOres).size()));
        int randomKey = new Random().nextInt(randomOre.locations.size());

        selectedLocation = randomOre.locations.get(randomKey);
        selectedDestination = selectedLocation.area.getRandomTile(); //random area, random tile
        log("Ore selected to mine near: " + randomOre.name + ". Location selected to mine: " + selectedLocation + ".");
    }
}