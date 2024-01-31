package CasualCollector.MiningTask.Events;

import BotScript.Operators.Operator;
import CasualCollector.FrameWork.EventBase;
import CasualCollector.Operators.GoTo;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.wrappers.interactive.Player;

import java.util.ArrayList;
import java.util.List;

public class FindOres extends EventBase {

    public FindOres() {
        buildMinableLocationsEnum();

        addOperator(new GoTo(closestMinableLocation()));
        operatorChanged();
    }

    public boolean operating() {
        return super.operating();
    }

    public Tile closestMinableLocation() {
        Player pl = Players.getLocal();
        Tile plTile = pl.getTile();

        Operator.Locations winner = Operator.Locations.LUMBRIDGE; //this saves us when falling in barbarian trap. dont remove it.
        if (!pl.exists() || plTile == null) return winner.area.getCenter().getTile();

        double winning = 1000000;
        for (Operator.Locations location : minableLocationsEnum) {
            if (plTile.distance(location.area.getCenter().getTile()) < winning) {
                winning = plTile.distance(location.area.getCenter().getTile());
                winner = location;
            }
        }

        return winner.area.getCenter().getTile();
    }

    public List<Operator.Locations> minableLocationsEnum;
    public void buildMinableLocationsEnum() {
        minableLocationsEnum = new ArrayList<>();

        for (Operator.Objects rocks : Operator.Objects.values()) {
            if (rocks.name.contains("rocks")) {
                if (rocks.requiredLevel <= Skills.getRealLevel(Skill.MINING)) {
                    for (Operator.Locations location : rocks.locations) {
                        if (!minableLocationsEnum.contains(location)) {
                            minableLocationsEnum.add(location);
                        }
                    }
                }
            }
        }
    }
}
