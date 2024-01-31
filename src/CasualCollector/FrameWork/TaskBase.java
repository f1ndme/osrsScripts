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
    public LinkedList<EventBase> events;
    public TaskBase() {
        events = new LinkedList<>();
    }



    public int onLoop() {
        if (events == null || events.isEmpty()) return 100;

        if (!events.getFirst().operating()) {
            if (events == null || events.isEmpty()) return 100; //an event might come in late on sleep, an its already removed.
            removeEvent(events.getFirst());
        }

        return 100;
    }

    public void think() {}


    public void addEvent(EventBase event) {
        log("(" + event.getClass().getSimpleName() + ") event has been added.");
        events.add(event);
    }

    public void removeEvent(EventBase event) {
        log("(" + event.getClass().getSimpleName() + ") event has been removed.");

        event.clearOperators();
        events.remove(event);
    }



    long thinkDelay = 300;
    long nextThink;
    public void onPaint(Graphics g) {
        if (nextThink < System.currentTimeMillis()) {
            nextThink = System.currentTimeMillis() + thinkDelay;
            think();
        }

        if (!events.isEmpty()) {
            events.getFirst().onPaint(g, mousePosition); //paint just the first event of a task.
        }
    }
}
