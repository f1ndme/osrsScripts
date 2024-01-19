package BotScript.Elements;

import BotScript.UIManager;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.awt.*;
import java.awt.event.MouseEvent;

import static BotScript.UIManager.withinBounds;

public class TextCommand {
    public String text;
    public int x;
    public int y;
    public Font font;
    public Color color;
    public UIManager.TextCommands receiver;
    public int id;
    public UIManager uiManager;

    public Vector2D mousePosition;
    public boolean mouseDown;
    public int textWidth;
    public int textHeight;
    public Font lastFont;
    public Font scaledFont;


    public TextCommand(String text, int x, int y, Font font, Color color, UIManager.TextCommands receiver, int id, UIManager uiManager) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.font = font;
        this.color = color;
        this.receiver = receiver;
        this.id = id;
        this.uiManager = uiManager;
    }




    public void onMousePressed(MouseEvent mouse) {
        mouseDown = true;

        if (hovering()) {
            receiver.onTextCommandPressed(id);
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
        textWidth = metrics.stringWidth(text);
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
            g.drawString(text, x, y );
        }else {
            if (mouseDown) {
                if (scaledFont == null) { //doesnt update if font is updated real time.
                    scaledFont = new Font(font.getFontName(), font.getStyle(), font.getSize() - 3);
                }
                if (g.getFont() != scaledFont) {
                    g.setFont(scaledFont);
                }

                FontMetrics metricss = g.getFontMetrics();
                int scaledTextWidth = metricss.stringWidth(text);

                g.setColor(new Color(Math.max(color.getRed() - 20, 0), Math.max(color.getGreen() - 20, 0), Math.max(color.getBlue() - 20, 0), Math.max(color.getAlpha() - 40, 0)));
                g.drawString(text, x + (textWidth - scaledTextWidth)/2, y-1 );
            }else {
                if (g.getFont() != font) {
                    g.setFont(font);
                }

                g.setColor(new Color(Math.max(color.getRed() - 20, 0), Math.max(color.getGreen() - 20, 0), Math.max(color.getBlue() - 20, 0), Math.max(color.getAlpha() - 40, 0)));
                g.drawString(text, x, y );
            }
        }

        postPaint(g);
    }
}

