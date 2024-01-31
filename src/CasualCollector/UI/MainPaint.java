package CasualCollector.UI;

import CasualCollector.FrameWork.TaskBase;
import CasualCollector.Main;
import CasualCollector.UI.Elements.DualText;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.dreambot.api.utilities.Logger.log;

public class MainPaint implements Main.Info {
    Color lightyellow = new Color(255, 255, 80, 80);
    Color white = new Color(255, 255, 255, 180);
    Color green = new Color(80, 255, 80, 225);
    Font font = new Font("Default", Font.PLAIN, 12);
    Map<TaskBase, DualText> taskNotifiers;
    public Vector2D mousePosition = new Vector2D(0, 0);
    public MainPaint() {
        taskNotifiers = new HashMap<TaskBase, DualText>();
    }


    public void rebuildMainInfo() {
        log("MainPaint: Notifiers count: " + taskNotifiers.size());

    }

    public DualText newDualText(String text, int x, int y) {
        return new DualText(text, "", x, y, font, lightyellow, white);
    }
    public DualText newDualText(String text, String textTwo, int x, int y, Font font, Color color, Color colorTwo) {
        return new DualText(text, textTwo, x, y, font, color, colorTwo);
    }




    public void onTaskAdded(TaskBase task) {
        //log(task.getClass().getSimpleName() + " task added!");
        addTaskNotifier(task);
    }
    public void onTaskRemoved(TaskBase task) {
        //log(task.getClass().getSimpleName() + " task removed!");
        removeTaskNotifier(task);
    }
    public void onTasksCleared() {
        //log("All tasks were cleared!");
        clearTaskNotifiers();
    }

    public void addTaskNotifier(TaskBase task) {
        String niceName = task.getClass().getSimpleName().replaceAll("(.)([A-Z])", "$1 $2");
        taskNotifiers.put(task, newDualText("[" + niceName + "]",4,50 + (taskNotifiers.size() * (2 + font.getSize()))));
        rebuildMainInfo();
    }

    public void removeTaskNotifier(TaskBase task) {
        taskNotifiers.remove(task);
        rebuildMainInfo();
    }

    public void clearTaskNotifiers() {
        taskNotifiers.clear();
        rebuildMainInfo();
    }





    public void onMouseMoved(Vector2D mousePosition) {
        this.mousePosition = mousePosition;
    }
    public void onPaint(Graphics g) {
        if (taskNotifiers == null) return;

        for (DualText dualText : taskNotifiers.values()) {
            dualText.onPaint(g, mousePosition);
        }
    }

}
