package CasualCollector;

import CasualCollector.FrameWork.TaskBase;
import CasualCollector.MiningTask.MiningTask;
import CasualCollector.UI.MainPaint;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.HumanMouseListener;

import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.event.MouseEvent;

import static org.dreambot.api.utilities.Logger.log;

@ScriptManifest(category = Category.UTILITY, name = "Casual Collector", description = "It collects stuff.", author = "find me", version = 1.0)
public class Main extends AbstractScript implements HumanMouseListener {
    public List<TaskBase> tasks;
    public MainPaint paint;

    @Override
    public void onStart() {
        tasks = new ArrayList<>();

        paint = new MainPaint();

        addTask(new MiningTask());
        addTask(new MiningTask());
        addTask(new MiningTask());
        addTask(new MiningTask());
        addTask(new MiningTask());
        addTask(new MiningTask());
        addTask(new MiningTask());
        addTask(new MiningTask());
        addTask(new MiningTask());
    }




    public interface Info {
        public void onTaskAdded(TaskBase task);
        public void onTaskRemoved(TaskBase task);
        public void onTasksCleared();
    }

    public void addTask(TaskBase task) {
        tasks.add(task);
        log("Main: Task count: " + tasks.size());
        paint.onTaskAdded(task);
    }

    public void removeTask(TaskBase task) {
        tasks.remove(task);
        log("Main: Task count: " + tasks.size());
        paint.onTaskRemoved(task);
    }

    public void clearTasks() {
        tasks.clear();
        log("Main: Task count: " + tasks.size());
        paint.onTasksCleared();
    }









    @Override
    public int onLoop() {
        if (tasks == null) return 100;

        for (TaskBase task : tasks) {
            if (task != null) {
                task.onLoop();
            }
        }

        return 100;
    }
    public Vector2D mousePosition;
    public void onMouseMoved(MouseEvent mouse) {
        mousePosition = new Vector2D(mouse.getX(), mouse.getY());

        if (tasks == null) return;

        for (TaskBase task : tasks) {
            if (task != null) {
                task.onMouseMoved(mousePosition);
            }
        }

        if (paint == null) return;
        paint.onMouseMoved(mousePosition);
    }
    @Override
    public void onPaint(Graphics g) {
        if (tasks == null) return;

        for (TaskBase task : tasks) {
            if (task != null) {
                task.onPaint(g);
            }
        }

        if (paint == null) return;
        paint.onPaint(g);
    }

}
