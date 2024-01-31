package TreeBranchProject.behaviour.NoPickaxeLeafs;

import TreeBranchProject.Main;
import TreeBranchProject.framework.Leaf;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;

public class NoPickaxeLeaf extends Leaf<Main> {
    public String getLastName(String input) {
        String[] splitArray = input.split("\\s+");

        return splitArray[splitArray.length-1];
    }
    public boolean hasPickaxe() {
        return (Equipment.contains(item -> getLastName(item.getName()).equals("pickaxe")) || Inventory.contains(item -> getLastName(item.getName()).equals("pickaxe")));
    }
























    @Override
    public boolean isValid() {

        return true;}
    @Override
    public int onLoop() {return (int) Calculations.nextGaussianRandom(350, 250);}

}