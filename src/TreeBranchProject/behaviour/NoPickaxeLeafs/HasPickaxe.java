package TreeBranchProject.behaviour.NoPickaxeLeafs;

import org.dreambot.api.methods.Calculations;

import static org.dreambot.api.utilities.Logger.log;

public class HasPickaxe extends NoPickaxeLeaf{

    @Override
    public boolean isValid() {
        return hasPickaxe();
    }

    @Override
    public int onLoop() {
        log("We have a pickaxe, success!");

        return (int) Calculations.nextGaussianRandom(350, 250);
    }
}
