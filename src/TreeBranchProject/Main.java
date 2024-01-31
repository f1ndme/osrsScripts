package TreeBranchProject;

import TreeBranchProject.behaviour.NoPickaxeLeafs.HasNoPickaxe;
import TreeBranchProject.behaviour.NoPickaxeLeafs.HasPickaxe;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import TreeBranchProject.behaviour.NoPickaxeBranch;
import TreeBranchProject.framework.Branch;
import TreeBranchProject.framework.Tree;
import TreeBranchProject.paint.CustomPaint;
import TreeBranchProject.paint.PaintInfo;
import TreeBranchProject.utilities.API;

import java.awt.*;

@ScriptManifest(category = Category.UTILITY, author = "find me", name = "TreeBranchExample", description = "Very cool example", version = 1)
public class Main extends AbstractScript implements PaintInfo {

    @Override
    public void onStart(String... args) {
        instantiateMiningTree();
    }

    @Override
    public void onStart() {
        instantiateMiningTree();
    }


    private final Tree<Main> miningTree = new Tree<>();
    private Branch<Main> noPickaxeBranch;

    private void instantiateMiningTree() {
        noPickaxeBranch = new NoPickaxeBranch();

        miningTree.addBranches(
                noPickaxeBranch.addLeafs(new HasNoPickaxe(), new HasPickaxe())
        );
    }

    @Override
    public int onLoop() {

        return this.miningTree.onLoop();
    }

    @Override
    public String[] getPaintInfo() {
        return new String[] {
                getManifest().name() + " V" + getManifest().version(),
                "Current Branch: " + API.currentBranch,
                "Current Leaf: " + API.currentLeaf
        };
    }

    /**
     * Instantiate the paint object, can be customized to liking.
     */
    private final CustomPaint CUSTOM_PAINT = new CustomPaint(this,
            CustomPaint.PaintLocations.TOP_LEFT_PLAY_SCREEN, new Color[]{new Color(255, 251, 255)},
            "Trebuchet MS",
            new Color[]{new Color(50, 50, 50, 175)},
            new Color[]{new Color(28, 28, 29)},
            1, false, 5, 3, 0);

    private final RenderingHints aa = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


    /**
     * paint for the script
     */
    @Override
    public void onPaint(Graphics g) {
        Graphics2D gg = (Graphics2D) g;
        gg.setRenderingHints(aa);

        CUSTOM_PAINT.paint(gg);
    }

}
