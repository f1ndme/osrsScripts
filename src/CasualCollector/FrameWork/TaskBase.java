package CasualCollector.FrameWork;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.dreambot.api.script.listener.HumanMouseListener;

import java.awt.*;
import java.util.LinkedList;

import static org.dreambot.api.utilities.Logger.log;

public class TaskBase implements HumanMouseListener {
    public void onMouseMoved(Vector2D mousePosition) {
        this.mousePosition = mousePosition;
    }
    Vector2D mousePosition;
    public LinkedList<EventBase> taskEvents;
    public TaskBase() {
        taskEvents = new LinkedList<>();
    }


    public boolean hasTaskEvents() {
        return taskEvents != null && !taskEvents.isEmpty();
    }

    public int onLoop() {
        if (!hasTaskEvents()) return 100;

        if (!taskEvents.getFirst().operating()) {
            if (taskEvents == null || taskEvents.isEmpty()) return 100; //an event might come in late on sleep, an its already removed.
            removeEvent(taskEvents.getFirst());
        }

        return 100;
    }

    public void think() {}


    public void addEvent(EventBase event) {
        log("(" + event.getClass().getSimpleName() + ") event has been added.");
        taskEvents.add(event);
    }

    public void removeEvent(EventBase event) {
        log("(" + event.getClass().getSimpleName() + ") event has been removed.");

        event.clearOperators();
        taskEvents.remove(event);
    }



    long thinkDelay = 300;
    long nextThink;
    public void onPaint(Graphics g) {
        if (nextThink < System.currentTimeMillis()) {
            nextThink = System.currentTimeMillis() + thinkDelay;
            think();
        }

        if (!taskEvents.isEmpty()) {
            taskEvents.getFirst().onPaint(g, mousePosition); //paint just the first event of a task.
        }
    }
}
