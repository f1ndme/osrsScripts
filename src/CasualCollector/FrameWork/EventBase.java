package CasualCollector.FrameWork;

import CasualCollector.UI.Elements.DualText;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

public class EventBase {
    private static final Color white = new Color(255, 255, 255, 180);
    private static final Color green = new Color(80, 255, 80, 225);
    private static final Color yellow = new Color(255, 255, 80, 80);
    private static final Font defaultFont = new Font("Default", Font.PLAIN, 12);

    public LinkedList<OperatorBase> operators;
    OperatorBase currentOperator;
    DualText defaultNotification;
    List<DualText> Notifications;
    public EventBase() {
        operators = new LinkedList<>();
        defaultNotification = new DualText("[" + this.getClass().getSimpleName() + "] ", "Waiting...", 10, 32, defaultFont, green, white);
        Notifications = new ArrayList<>();
    }

    public void think() {}

    public void rebuildNotifications() {
        Notifications.clear();
        if (operators.isEmpty()) return;

        defaultNotification.textTwo = operators.size() + " Operation(s) to complete...";

        int offset = 0;
        for (OperatorBase operator : operators) {
            Notifications.add(createOperatorNotification(offset, operator));
            offset++;
        }
    }

    private DualText createOperatorNotification(int offset, OperatorBase operator) {
        String label = "(" + operator.getClass().getSimpleName() + " " + operator.locationName + ") ";
        String text = currentOperator == operator ? "Operating..." : "";
        return new DualText(label, text, 4, 50 + offset * 15, defaultFont, green, white);
    }

    private boolean hasOperators() {
        return operators != null && !operators.isEmpty();
    }

    public boolean operating() {
        if (!hasOperators()) return false;

        if (!operators.getFirst().operating()) {
            if (!hasOperators()) return false; //an operator might come in late on sleep, an its already removed.
            removeOperator(operators.getFirst());
        }

        return true;
    }

    public void operatorChanged() {
        rebuildNotifications();
    }

    public void operatorAdded() {
        currentOperator = operators.getFirst();
        operatorChanged();
    }
    public void operatorRemoved() {

        if (operators.isEmpty()) {
            currentOperator = null;
        }else {
            currentOperator = operators.getFirst();
        }
        operatorChanged();
    }

    public void addOperator(OperatorBase operator) {
        //log("(" + operator.getClass().getSimpleName() + ") operator has been added.");
        operators.add(operator);

        operatorAdded();
    }

    public void removeOperator(OperatorBase operator) {
        //log("(" + operator.getClass().getSimpleName() + ") operator has been removed.");
        operators.remove(operator);

        operatorRemoved();
    }

    public void clearOperators() {
        operators.clear();
        operators = null;
    }








    public void onPaint(Graphics g, Vector2D mousePosition) { //paint dual texts
        defaultNotification.onPaint(g, mousePosition);

        for (DualText operatorText : Notifications) {
            operatorText.onPaint(g, mousePosition);
        }
    }
}
