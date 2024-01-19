package BotScript.Elements;

import BotScript.UIManager;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;

import java.awt.*;
import java.awt.event.MouseEvent;

import static BotScript.UIManager.withinBounds;

public class DualText {
    public String text;
    public String textTwo;
    public int x;
    public int y;
    public Font font;
    public Color color;
    public Color colorTwo;
    public UIManager.TextCommands receiver;
    public int id;
    public UIManager uiManager;

    public Vector2D mousePosition;
    public boolean mouseDown;
    public int textWidth;
    public int textHeight;
    public Font lastFont;
    public Font scaledFont;


    public DualText(String text, String textTwo, int x, int y, Font font, Color color, Color colorTwo, UIManager.TextCommands receiver, int id, UIManager uiManager) {
        this.text = text;
        this.textTwo = textTwo;
        this.x = x;
        this.y = y;
        this.font = font;
        this.color = color;
        this.colorTwo = colorTwo;
        this.receiver = receiver;
        this.id = id;
        this.uiManager = uiManager;
    }




    public void onMousePressed(MouseEvent mouse) {
        mouseDown = true;

        if (hovering()) {
            receiver.onDualTextPressed(id);
        }
    }

    public void onMouseReleased(MouseEvent mouse) {
        mouseDown = false;
    }

    public boolean hovering() {
        return withinBounds(new Vector2D(x, y+2), new Vector2D(x+textWidth, y - (textHeight-6) ), mousePosition);
    }

    public void prePaint(Graphics g) {
        lastFont = g.getFont();

        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        textWidth = metrics.stringWidth(text + textTwo);
        textHeight = metrics.getHeight();

        mousePosition = uiManager.mousePosition;
    }
    public void postPaint(Graphics g) {
        if (g.getFont() != lastFont) {
            g.setFont(lastFont);
        }
    }

    public void onPaint(Graphics g) {
        prePaint(g);

        if (!hovering()) {
            if (g.getFont() != font) {
                g.setFont(font);
            }

            g.setColor(color);
        }else {
            if (mouseDown) {
                if (scaledFont == null) { //doesnt update if font is updated real time.
                    scaledFont = new Font(font.getFontName(), font.getStyle(), font.getSize() - 3);
                }
                if (g.getFont() != scaledFont) {
                    g.setFont(scaledFont);
                }

                g.setColor(new Color(Math.max(color.getRed() - 20, 0), Math.max(color.getGreen() - 20, 0), Math.max(color.getBlue() - 20, 0), Math.max(color.getAlpha() - 40, 0)));
            }else {
                if (g.getFont() != font) {
                    g.setFont(font);
                }

                g.setColor(new Color(Math.max(color.getRed() - 20, 0), Math.max(color.getGreen() - 20, 0), Math.max(color.getBlue() - 20, 0), Math.max(color.getAlpha() - 40, 0)));
            }
        }

        FontMetrics metrics = g.getFontMetrics();
        int stringWidth = metrics.stringWidth(text);

        g.drawString(text, x, y);
        g.setColor(colorTwo);
        g.drawString(textTwo, x + stringWidth, y);

        postPaint(g);
    }
}