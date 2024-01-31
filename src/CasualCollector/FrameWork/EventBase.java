package CasualCollector.FrameWork;

import CasualCollector.UI.Elements.DualText;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

public class EventBase {
    Color white = new Color(255, 255, 255, 180);
    Color green = new Color(80, 255, 80, 225);
    Color lightyellow = new Color(255, 255, 80, 80);
    Font font = new Font("Default", Font.PLAIN, 12);

    public LinkedList<OperatorBase> operators;
    OperatorBase currentOperator;
    DualText EventNotifier;
    List<DualText> operatorNotifiers;
    public EventBase() {
        operators = new LinkedList<>();

        EventNotifier = new DualText("[" + this.getClass().getSimpleName() + "] ", "Waiting...", 10, 32, font, green, white);
    }

    public void think() {}

    public void rebuildOperatorNotifiers() {
        if (operators.isEmpty()) {
            operatorNotifiers.clear();
            return;
        }

        if (operatorNotifiers == null) {
            operatorNotifiers = new ArrayList<>();
        }else {
            operatorNotifiers.clear();

            if (EventNotifier != null) {
                EventNotifier.textTwo = operators.size() + " Operation(s) to complete...";
            }
        }

        int g = 0;
        for (OperatorBase operator : operators) {
            if (currentOperator == operator) {
                operatorNotifiers.add(new DualText("(" + operator.getClass().getSimpleName() + " " + operator.locationName + ") ", "Operating...", 4, 50 + g*15, font, green, white));
            }else {
                operatorNotifiers.add(new DualText("(" + operator.getClass().getSimpleName() + " " + operator.locationName + ") ", "", 4, 50 + g*15, font, lightyellow, white));
            }
            g++;
        }
    }

    public boolean operating() {
        if (operators == null || operators.isEmpty()) return false;

        if (!operators.getFirst().operating()) {
            if (operators == null || operators.isEmpty()) return false; //an operator might come in late on sleep, an its already removed.
            removeOperator(operators.getFirst());
        }

        return true;
    }

    public void operatorChanged() {
        rebuildOperatorNotifiers();
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
        EventNotifier.onPaint(g, mousePosition);

        for (DualText operatorText : operatorNotifiers) {
            operatorText.onPaint(g, mousePosition);
        }
    }
}
