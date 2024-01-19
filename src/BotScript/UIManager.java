package BotScript;

import BotScript.Elements.*;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class UIManager {
    public ArrayList<TextCommand> allTextCommands;
    public ArrayList<DualText> allDualTexts;
    public Vector2D mousePosition;
    UIManager() {
        this.allTextCommands = new ArrayList<>();
        this.allDualTexts = new ArrayList<>();

        this.mousePosition = new Vector2D(0, 0);
    }

    public void think() {

    }

    public void onPaint(Graphics g) {
        paintTextCommands(g);
        paintDualTexts(g);
    }
    public void onMouseMoved(MouseEvent mouse) {
        mousePosition = new Vector2D(mouse.getX(), mouse.getY());
    }
    public void onMousePressed(MouseEvent mouse) {
        onMousePressedTextCommands(mouse);
        onMousePressedDualTexts(mouse);
    }
    public void onMouseReleased(MouseEvent mouse) {
        onMouseReleasedTextCommands(mouse);
        onMouseReleasedDualTexts(mouse);
    }














    public DualText DualText(String text, String textTwo, int x, int y, Color color, Color colorTwo, TextCommands receiver) {
        DualText newDualText = new DualText(text, textTwo, x, y, new Font("Default", Font.PLAIN, 12), color, colorTwo, receiver, allDualTexts.size(), this);

        allDualTexts.add(newDualText);

        return newDualText;
    }
    public TextCommand TextCommand(String text, int x, int y, TextCommands receiver) {
        TextCommand newTextCommand = new TextCommand(text, x, y, new Font("Default", Font.PLAIN, 12), new Color(255, 255, 255, 255), receiver, allTextCommands.size(), this);

        allTextCommands.add(newTextCommand);

        return newTextCommand;
    }
    public TextCommand TextCommand(String text, int x, int y, Font font, Color color, TextCommands receiver) {
        TextCommand newTextCommand = new TextCommand(text, x, y, font, color, receiver, allTextCommands.size(), this);

        allTextCommands.add(newTextCommand);

        return newTextCommand;
    }
    public void onMousePressedTextCommands(MouseEvent mouse) {
        for (TextCommand textCommand : allTextCommands) {
            textCommand.onMousePressed(mouse);
        }
    }
    public void onMouseReleasedTextCommands(MouseEvent mouse) {
        for (TextCommand textCommand : allTextCommands) {
            textCommand.onMouseReleased(mouse);
        }
    }

    public void paintTextCommands(Graphics g) {
        for (TextCommand textCommand : allTextCommands) { textCommand.onPaint(g); }
    }




    public void onMousePressedDualTexts(MouseEvent mouse) {
        for (DualText dualText : allDualTexts) {
            dualText.onMousePressed(mouse);
        }
    }
    public void onMouseReleasedDualTexts(MouseEvent mouse) {
        for (DualText dualText : allDualTexts) {
            dualText.onMouseReleased(mouse);
        }
    }

    public void paintDualTexts(Graphics g) {
        for (DualText dualText : allDualTexts) {
            dualText.onPaint(g);
        }
    }
    public static boolean withinBounds(Vector2D min, Vector2D max, Vector2D point) {
        if (point.getX() < max.getX() && point.getX() > min.getX()) {
            if (point.getY() > max.getY() && point.getY() < min.getY()) {
                return true;
            }
        }

        return false;
    }










    public interface TextCommands {
        public void onTextCommandPressed(int id);
        public void onDualTextPressed(int id);
    }
}
