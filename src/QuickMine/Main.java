package QuickMine;

import QuickMine.ui.PlayerInfo;
import QuickMine.ui.ScriptTime;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.SkillTracker;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;

import java.awt.*;
import java.util.ArrayList;

import static QuickMine.Resources.*;
import static QuickMine.Resources.Pickaxes.hasUsablePickaxe;

@ScriptManifest(category = Category.MINING, name = "Quick Mine 2.1", description = "Mines stuff.", author = "find me", version = 1.0)
public class Main extends AbstractScript {
    final int preWalkDelay = 1501;
    final int miningTolerance = 14; //How far can we see ores.
    final int preMineDelay = 4801; //Extra high to reach far away ores, resets on completed mine though. Could adjust on isRunning.
    final long locationChangeMinTime = 10 * 60000; //Minutes
    final long locationChangeMaxTime = 15 * 60000; //Minutes
    final int loopDelay = 100;
    boolean loginReady;
    ScriptTime scriptTime;
    PlayerInfo playerInfo;
    Locations miningLocation;
    Tile miningLocationTile;
    boolean atMiningLocation;
    Player pl;
    long nextWalkTry;
    GameObject attemptingOre;
    long nextMineTry;
    Timer locationTimer;
    long timeTillLocationChange;





    public void setTimeTillLocationChange() {
        timeTillLocationChange = Calculations.random(locationChangeMinTime, locationChangeMaxTime);
    }
    public void runOnCombat() {
        if (pl.isInCombat() && !Walking.isRunEnabled() && Walking.getRunEnergy() > 0) {
            Walking.toggleRun();
        }
    }
    public void walkingChecks() {
        runOnCombat();

        if (miningLocationTile.distance(pl.getTile()) > 4) {
            Walking.walk(miningLocationTile);
            nextWalkTry = System.currentTimeMillis() + Calculations.random(preWalkDelay, preWalkDelay+300);
            Sleep.sleepUntil(this::shouldTryWalk, 15000);
        }else if ((miningLocationTile.distance(pl.getTile()) <= 4)) {
            atMiningLocation = true;
            log("We have arrived at our mining location!");
        }
    }
    public boolean shouldTryWalk() {
        double distance = pl.getTile().distance(Walking.getDestination());
        boolean nearTarget = distance <= ((Walking.isRunEnabled()) ? 5 : 2);

        return !Calculations.isBefore(nextWalkTry) && (!pl.isMoving() || nearTarget);
    }
    public void findMiningLocation() {
        setTimeTillLocationChange();
        atMiningLocation = false;
        locationTimer = null;
        miningLocation = Locations.randomMinableLocation();
        miningLocationTile = miningLocation.area.getRandomTile();

        log("Mining location set to: " + miningLocation.name());
    }
    public boolean readyToMine() {
        return hasUsablePickaxe();
    }

    private boolean isMining() {
        if (attemptingOre != null) {
            GameObject closest = GameObjects.closest(object -> object.getName().equalsIgnoreCase("rocks") && object.distance(pl.getTile()) <= 2);
            if (closest != null) { //Get mined Rock from attemptingOre Tile instead of trying to find near.
                if (closest.getTile().equals(attemptingOre.getTile())) {
                    nextMineTry = 0;

                    return false;
                }
            }
        }

        return pl.isAnimating();
    }

    public boolean shouldTryMine() {
        return !Calculations.isBefore(nextMineTry) && !isMining() && !pl.isMoving();
    }

    public boolean droppingInventory() {
        return Inventory.dropAll(item -> item.getName().contains("ore") || item.getName().contains("Clay") || item.getName().contains("Coal") || item.getName().contains("Uncut") || item.getName().contains("Clue") || item.getName().contains("scroll"));
    }

    public void firstLogin() {
        log("Logged in(not solving anything) Main is now setting up.");

        SkillTracker.start(Skill.MINING);

        playerInfo = new PlayerInfo();

        findMiningLocation();

        loginReady = true;
    }

    public int readyLoop() {
        if (readyToMine()) {
            if (!atMiningLocation) {
                //go to mining location.

                walkingChecks();
                return loopDelay;
            }
            //Walk code has gotten us here, mine stuff.

            if (locationTimer == null) {
                locationTimer = new Timer();
            }
            if (locationTimer.elapsed() >= timeTillLocationChange) {
                findMiningLocation();
                return loopDelay;
            }

            if (!shouldTryMine()) return loopDelay;

            if (Inventory.isFull()) {
                Sleep.sleepWhile(this::droppingInventory, 15000);
            }

            ArrayList<GameObject> minableOresNear = Ores.getMinableOresNear(pl, miningTolerance);
            if (minableOresNear.isEmpty()) { //Might be something weird in method, might just not be collecting GameObjects cause connection error or something? Should TryCheck this & change location.
                log("No minable ores found! Switch locations? This really shouldn't happen? But it does, care.");
                return loopDelay;
            }

            int randomKey = Calculations.random(0, minableOresNear.size());
            attemptingOre = minableOresNear.get(randomKey);
            if (attemptingOre == null) return loopDelay;

            if (attemptingOre.interact("Mine")) {
                nextMineTry = System.currentTimeMillis() + Calculations.random(preMineDelay, preMineDelay+300);
                Sleep.sleepUntil(this::shouldTryMine, 15000); //Is Sleep broke on connection error? shouldTryMine returns true after connection error(tested), but Sleep doesn't stop till timeout. weird.
            }
        }

        return 20;
    }

    @Override
    public void onStart() {
        buildFileDirectories();

        scriptTime = new ScriptTime(getRandomManager());

        if (!loginReady) return;

        playerInfo = new PlayerInfo();
    }

    @Override
    public void onPause() {
        if (scriptTime != null) {
            scriptTime.onPause();
        }

        if (!loginReady) return;
    }

    @Override
    public void onResume() {
        if (scriptTime != null) {
            scriptTime.onResume();
        }

        if (!loginReady) return;
    }

    @Override
    public int onLoop() {
        pl = Players.getLocal();
        if (pl == null) return loopDelay;

        if (scriptTime != null) {
            scriptTime.onLoop();
        }

        if (!loginReady) {
            if (Client.isLoggedIn() && !getRandomManager().isSolving()) {
                firstLogin();
            }

            return loopDelay;
        }

        return readyLoop();
    }

    @Override
    public void onPaint(Graphics g) {
        if (scriptTime != null) {
            scriptTime.onPaint(g);
        }

        if (!loginReady) return;

        playerInfo.onPaint(g);

        drawMiningInformation(g);
    }

    public void drawMiningInformation(Graphics g) {
        String miningLevel = String.format(
                "Mining Level: %d",
                Skills.getRealLevel(Skill.MINING)
        );

        String experienceGainedText = String.format(
                "Mining Experience: %d (%d per hour)",
                SkillTracker.getGainedExperience(Skill.MINING),
                SkillTracker.getGainedExperiencePerHour(Skill.MINING)
        );

        if (locationTimer != null && miningLocation != null) {
            String text = "(" + miningLocation.name() + "): ";

            FontMetrics metrics = g.getFontMetrics();
            int stringWidth = metrics.stringWidth(text);

            g.setColor(Color.gray);
            g.drawString("Time spent mining at:", 5, 200);
            g.setColor(Color.white);
            g.drawString("(" + miningLocation.name() + "):", 5, 215);
            g.setColor(Color.green);
            g.drawString("" + locationTimer.formatTime(), 5 + stringWidth, 215);
        }

        g.setColor(Color.gray);
        g.drawString(miningLevel, 5, 155);
        g.drawString(experienceGainedText, 5, 170);
    }
}
