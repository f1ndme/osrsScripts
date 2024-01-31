package CasualCollector.Operators;

import BotScript.Operators.Operator;
import CasualCollector.FrameWork.OperatorBase;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.map.TileReference;

import java.util.ArrayList;
import java.util.List;

public class Mine extends OperatorBase {

    GameObject targetObject;
    Tile targetTile;
    public Mine() {
        buildMinableRockNames();
        this.targetObject = closestReachable();
        this.targetTile = targetObject.getTile();

        super.locationName = targetObject.getName();
    }

    public List<String> minableRockNames;
    public void buildMinableRockNames() {
        minableRockNames = new ArrayList<>();

        for (Operator.Objects rock : Operator.Objects.values()) {
            if (rock.name.contains("rocks")) {
                if (rock.requiredLevel <= Skills.getRealLevel(Skill.MINING)) {
                    minableRockNames.add(rock.name);
                }
            }
        }
    }

    public boolean operating() {
        GameObject node = objectFrom(targetTile); //check on target.
        if (node == null || node.getName().equals("Rocks")) {
            return false;
        }

        if (!playerAnimating()) { //check on player.
            if (targetObject.interact("Mine")) {
                Sleep.sleepUntil(this::playerAnimating, 3601);
            }else {
                return false;
            }
        }
        return true;
    }









    public java.util.List<GameObject> allReachable() {
        return GameObjects.all(object -> object.hasAction("Mine") && object.distance(Players.getLocal().getTile()) <= 9 && object.getModelColors() != null);
    }
    public java.util.List<GameObject> minableReachable() {
        List<GameObject> newList = new ArrayList<>();

        for (GameObject object : allReachable()) {
            if (minableRockNames.contains(object.getName())) {
                newList.add(object);
            }
        }

        return newList;
    }
    public GameObject furthestReachable() {
        Player pl = Players.getLocal();
        Tile plTile = pl.getTile();
        if (!pl.exists() || plTile == null) return null;

        double winning = 0;
        GameObject winner = null;
        for (GameObject object : minableReachable()) {
            if (object.distance(plTile) > winning) {
                winning = object.distance(plTile);
                winner = object;
            }
        }

        return winner;
    }
    public GameObject closestReachable() {
        Player pl = Players.getLocal();
        Tile plTile = pl.getTile();
        if (!pl.exists() || plTile == null) return null;

        double winning = 100000;
        GameObject winner = null;
        for (GameObject object : minableReachable()) {
            if (object.distance(plTile) < winning) {
                winning = object.distance(plTile);
                winner = object;
            }
        }

        return winner;
    }
    public GameObject objectFrom(Tile tile) {
        TileReference tileReference = tile.getTileReference();
        if (tileReference == null || tileReference.getObjects().length == 0) {
            return null;
        }

        return tileReference.getObjects()[0];
    }
}
