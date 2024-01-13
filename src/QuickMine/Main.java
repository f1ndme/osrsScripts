package QuickMine;

import QuickMine.ui.PlayerInfo;
import QuickMine.ui.ScriptTime;
import org.dreambot.api.Client;
import org.dreambot.api.ClientSettings;
import org.dreambot.api.data.ClientLayout;
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
import org.dreambot.api.script.listener.ItemContainerListener;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;

import java.awt.*;
import java.util.ArrayList;
import java.util.Hashtable;

import static QuickMine.Resources.*;
import static QuickMine.Resources.Pickaxes.hasUsablePickaxe;

//every 3 min or something, check fail rate on ores, an exclude.
//when none, switch ore type. higher/lower/world-hop?

@ScriptManifest(category = Category.MINING, name = "Quick Mine 2.1", description = "Mines stuff.", author = "find me", version = 1.0)
public class Main extends AbstractScript implements ItemContainerListener {
    final int miningTolerance = 14; //How far can we see ores.
    final long locationChangeMinTime = 10 * 60000; //Minutes
    final long locationChangeMaxTime = 15 * 60000; //Minutes
    final int preMineDelay = Calculations.random(1201, 1801); //seems to retry less with this higher
    final int preWalkDelay = Calculations.random(300, 601); //but this matters in another case.
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
    GameObject attemptingLocationObject;





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
            //log("We have arrived at our mining location!");
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

        //log("Mining location set to: " + miningLocation.name());
    }
    public boolean readyToMine() {
        return hasUsablePickaxe();
    }

    public GameObject getObjectFromTile(Tile tile) {
        if (tile.getTileReference() == null) {
            return null;
        }

        if (tile.getTileReference().getObjects().length <= 0) {
            return null;
        }

        return tile.getTileReference().getObjects()[0];
    } //fixed i think? no more error here?

    public boolean shouldTryMine() {//delicate lol
        if (Players.getLocal() == null) {
            return false;
        }

        if (!Players.getLocal().exists()) {
            return false;
        }

        if (Players.getLocal().isMoving()) {
            return false;
        }

        if (Calculations.isBefore(nextMineTry)) {
            if (attemptingOre != null) {
                if (attemptingOre.getTile() == null) {
                    return false;
                }//we might not need this. it might also break stuff. lets see. MIGHT NEED TO SET TRUE AN NULL STUFF.

                attemptingLocationObject = getObjectFromTile(attemptingOre.getTile());

                if (attemptingLocationObject != null) {
                    if (attemptingLocationObject.getName().equalsIgnoreCase("rocks")) {
                        retryRequest = false;
                        allRetryRequests = 0;

                        attemptingOre = null;
                        nextMineTry = preMineDelay;

                        return true;
                    }
                }
            }

            return false;
        }

        if (attemptingOre != null) {
            if (attemptingOre.getTile() == null) {
                return false;
            }//we might not need this. it might also break stuff. lets see. MIGHT NEED TO SET TRUE AN NULL STUFF.

            attemptingLocationObject = getObjectFromTile(attemptingOre.getTile());

            if (attemptingLocationObject != null) {
                if (attemptingLocationObject.getName().equalsIgnoreCase("rocks")) {
                    retryRequest = false;
                    allRetryRequests = 0;

                    attemptingOre = null;
                    nextMineTry = preMineDelay;

                    return true;
                }
            }
        }

        if (!Players.getLocal().isAnimating()) {
            if (attemptingOre != null) {
                //retry same? already checked, not rocks.

                retryRequest = true;
                return true;
            }

            nextMineTry = preMineDelay;

            return true;
        }else if (attemptingOre == null) { //and animating?
            nextMineTry = preMineDelay;

            return true;
        }

        return false;
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

    boolean lastAction;
    boolean retryRequest;
    int allRetryRequests;
    final int maxRetryRequests = 3;

    public int readyLoop() {
        //if (ClientSettings.getClientLayout() != ClientLayout.RESIZABLE_CLASSIC) {
        //    ClientSettings.setClientLayout(ClientLayout.RESIZABLE_CLASSIC);
        //}

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

            if (retryRequest) {
                //attemptingOre is valid?
                if (allRetryRequests > maxRetryRequests) {
                    log("No More Retry Requests... Finding new target.");
                    attemptingOre = null;
                    nextMineTry = preMineDelay;

                    retryRequest = false;
                    allRetryRequests = 0;
                }else {
                    log("Retry Request...");

                    if (Players.getLocal().isAnimating()) {
                        retryRequest = false;
                        allRetryRequests = 0;

                        return loopDelay;
                    }

                    if (attemptingOre == null) {
                        retryRequest = false;
                        allRetryRequests = 0;

                        return loopDelay;
                    }

                    if (attemptingOre.interact("Mine")) {
                        nextMineTry = System.currentTimeMillis() + Calculations.random(preMineDelay, preMineDelay+300);
                        attemptingLocationObject = getObjectFromTile(attemptingOre.getTile());
                        Sleep.sleepUntil(this::shouldTryMine, 15000); //FIX pl NULL STUFF.
                    }

                    Sleep.sleep(Calculations.random(601, 1201));

                    allRetryRequests++;

                    return loopDelay;
                }
            }else {
                int randomKey = Calculations.random(0, minableOresNear.size());
                attemptingOre = minableOresNear.get(randomKey);
            }

            if (attemptingOre == null) return loopDelay;

            if (attemptingOre.interact("Mine")) {
                nextMineTry = System.currentTimeMillis() + Calculations.random(preMineDelay, preMineDelay+300);
                attemptingLocationObject = getObjectFromTile(attemptingOre.getTile());
                Sleep.sleepUntil(this::shouldTryMine, 15000); //FIX pl NULL STUFF.
            }
        }

        return loopDelay;
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
        log(getRandomManager().getCurrentSolver());
        log(getRandomManager().getCurrentSolver());

        if (getRandomManager().getCurrentSolver() != null) {
            log(getRandomManager().getCurrentSolver().getEventString());
            log(getRandomManager().getCurrentSolver());
            log(getRandomManager());
        }

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

        drawDistanceTo(g, miningLocationTile);
        drawAttemptingObject(g);
        drawOreCollection(g);
    }


    long uiHoldTime;
    String lastGrab;
    public void drawOreCollection(Graphics g) {
        if (oreCollection == null) {
                oreCollection = new Hashtable<>();

                for (Ores o: Ores.values()) {
                    oreCollection.put(o, 0);
                }
        }

        int stringWidth = g.getFontMetrics().stringWidth("Adamant: 10");

        int i = 0;
        for (Ores o: Ores.values()) {
            int count = oreCollection.get(o);
            String[] nameSplit = o.name.split(" ", 2);
            String name = nameSplit[0];

            String display = name + ": ";

            FontMetrics metrics = g.getFontMetrics();
            stringWidth = metrics.stringWidth(display);

            g.setColor(Color.gray);

            if (lastGrab != null) {
                if (display.contains(lastGrab) && Calculations.isBefore(uiHoldTime)) {
                    g.setColor(Color.green);
                    g.drawString("" + count, (int) (Client.getViewportWidth() -265), (int) (Client.getViewportHeight() -290)+(i*15));
                    g.setColor(Color.white);
                }else if(count >0) {
                    g.setColor(Color.white);
                    g.drawString("" + count, (int) (Client.getViewportWidth() -265), (int) (Client.getViewportHeight() -290)+(i*15));
                    g.setColor(Color.gray);
                }else {
                        g.setColor(Color.gray);
                        g.drawString("" + count, (int) (Client.getViewportWidth() -265), (int) (Client.getViewportHeight() -290)+(i*15));
                }
            }else {
                g.setColor(Color.gray);
                g.drawString("" + count, (int) (Client.getViewportWidth() -265), (int) (Client.getViewportHeight() -290)+(i*15));
            }

            g.drawString(display, (int) (Client.getViewportWidth() -265 -stringWidth), (int) (Client.getViewportHeight() -290)+(i*15));
            i++;
        }

        g.setColor(Color.white);

        int totalOresCollected = 0;
        for (Integer total : oreCollection.values()) {
            totalOresCollected = totalOresCollected + total;
        }

        FontMetrics metrics = g.getFontMetrics();
        int totalStringWidth = metrics.stringWidth("Total Ores Mined: " + totalOresCollected);

        g.drawString("Total Ores Mined: " + totalOresCollected, (int) (Client.getViewportWidth() -258 -totalStringWidth), (int) (Client.getViewportHeight() - 305));
    }


    Hashtable<Ores, Integer> oreCollection;
    public void onInventoryItemChanged(Item incoming, Item existing) {}
    public void onInventoryItemAdded(Item item) {
        for (Ores ore : Ores.values()) {
            String[] enumSplit = ore.name.split(" ", 2);
            String realOreName = enumSplit[0];


            if (item.getName().contains(realOreName)) {
                int oldValue = oreCollection.get(ore);
                oreCollection.put(ore, oldValue + 1);
                uiHoldTime = System.currentTimeMillis() + 6000;

                String[] oreEnumNameSplit = ore.name.split(" ", 2);
                lastGrab = oreEnumNameSplit[0];
            }
        }
    }


    public void drawAttemptingObject(Graphics g) {
        FontMetrics metrics = g.getFontMetrics();
        int stringWidth = metrics.stringWidth("Currently Attempting Object: ");

        g.setColor(Color.gray);

        if (attemptingLocationObject != null) {
            g.setColor(Color.green);
            g.drawString("" + attemptingLocationObject.getName(), 5 + stringWidth, Client.getViewportHeight() - 260);
            g.setColor(Color.white);
        }

        g.drawString("Currently Attempting Object: ", 5, Client.getViewportHeight() - 260);
    }

    public void drawDistanceTo(Graphics g, Tile tile) {
        FontMetrics metrics = g.getFontMetrics();
        int stringWidth = metrics.stringWidth("Distance Until Arrival: ");

        g.setColor(Color.darkGray);

        if (Players.getLocal().getTile().walkingDistance(tile) > 25) {
            g.setColor(Color.green);
            g.drawString("" + (int)Players.getLocal().getTile().walkingDistance(tile), 5 + stringWidth, Client.getViewportHeight() - 290);
            g.setColor(Color.white);
            g.drawString("Distance Till Location: ", 5, Client.getViewportHeight() - 290);
        } else {
            g.drawString("Distance Till Location: ", 5, Client.getViewportHeight() - 305);
        }
    }

    public void drawMiningInformation(Graphics g) {
        //SkillTracker.getGainedExperiencePerHour(Skill.MINING)

        g.setColor(Color.white);
        g.drawString("(" + miningLocation.realName + ")", 5, Client.getViewportHeight() - 275);

        if (locationTimer != null && miningLocation != null) {
            FontMetrics metrics = g.getFontMetrics();
            int stringWidth = metrics.stringWidth("Time spent mining:");

            g.setColor(Color.gray);
            g.drawString("Time spent mining:", 5, Client.getViewportHeight() - 290);
            g.setColor(Color.white);
            g.drawString("" + locationTimer.formatTime(), 5 + stringWidth, Client.getViewportHeight() - 290);
        }

        g.setColor(Color.gray);

        FontMetrics metrics = g.getFontMetrics();
        int experienceStringWidth = metrics.stringWidth("Mining Experience Gained:" + SkillTracker.getGainedExperience(Skill.MINING));
        int levelStringWidth = metrics.stringWidth("Current Mining Level:" + Skills.getRealLevel(Skill.MINING));

        g.drawString("Mining Experience Gained: " + SkillTracker.getGainedExperience(Skill.MINING), (int) (Client.getViewportWidth() -262 -experienceStringWidth), (int) (Client.getViewportHeight() - 335));
        g.drawString("Current Mining Level: " + Skills.getRealLevel(Skill.MINING), (int) (Client.getViewportWidth() -262 -levelStringWidth), (int) (Client.getViewportHeight() - 350));
    }
}
