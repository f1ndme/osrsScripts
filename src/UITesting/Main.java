package UITesting;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.HumanMouseListener;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

@ScriptManifest(category = Category.UTILITY, name = "PAINT Testing", description = "It paints stuff.", author = "find me", version = 1.0)
public class Main extends AbstractScript implements HumanMouseListener {

    //LinkedList<HashMap<>>
    @Override
    public void onStart() {

    }


    public void addTask() {

    }
    public void removeTask() {

    }
    public void clearTasks() {

    }












    @Override
    public int onLoop() {

        return 100;
    }
    public Vector2D mousePosition;
    public void onMouseMoved(MouseEvent mouse) {
        mousePosition = new Vector2D(mouse.getX(), mouse.getY());
    }
    @Override
    public void onPaint(Graphics g) {

    }

}