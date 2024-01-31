package TreeBranchProject.behaviour.NoPickaxeLeafs;

import org.dreambot.api.methods.Calculations;

import static org.dreambot.api.utilities.Logger.log;

public class HasNoPickaxe extends NoPickaxeLeaf{

    @Override
    public boolean isValid() {


        return !hasPickaxe();
    }

    @Override
    public int onLoop() {
        log("We do not have a pickaxe!");

        return (int) Calculations.nextGaussianRandom(350, 250);
    }
}
