package CasualCollector.UI.Elements;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.awt.*;

import static org.dreambot.api.utilities.Logger.log;

public class DualText {
    public String text;
    public String textTwo;
    public int x;
    public int y;
    public Font font;
    public Color color;
    public Color colorTwo;

    public Vector2D mousePosition;
    public boolean mouseDown;
    public int textWidth;
    public int textHeight;
    public Font lastFont;
    public Font scaledFont;


    public DualText(String text, String textTwo, int x, int y, Font font, Color color, Color colorTwo) {
        this.text = text;
        this.textTwo = textTwo;
        this.x = x;
        this.y = y;
        this.font = font;
        this.color = color;
        this.colorTwo = colorTwo;
    }






    public static boolean withinBounds(Vector2D min, Vector2D max, Vector2D point) {
        if (point.getX() < max.getX() && point.getX() > min.getX()) {
            if (point.getY() > max.getY() && point.getY() < min.getY()) {
                return true;
            }
        }

        return false;
    }

    public boolean hovering() {
        return withinBounds(new Vector2D(x, y+2), new Vector2D(x+textWidth, y - (textHeight-6) ), mousePosition );
    }

    public void prePaint(Graphics g) {
        lastFont = g.getFont();

        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        textWidth = metrics.stringWidth(text + textTwo);
        textHeight = metrics.getHeight();
    }
    public void postPaint(Graphics g) {
        if (g.getFont() != lastFont) {
            g.setFont(lastFont);
        }
    }

    Vector2D defaultPos = new Vector2D(0, 0);
    public void onPaint(Graphics g, Vector2D mpos) {
        mousePosition = mpos;
        if (mousePosition == null || mousePosition.getX() < 0 || mousePosition.getY() < 0) {
            mousePosition = defaultPos;
        }

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
        int fullStringWidth = metrics.stringWidth(text + textTwo);
        int fontHeight = metrics.getFont().getSize();

        Color oldColor = g.getColor();
        g.setColor(new Color(40, 40, 40, 180));
        g.fillRect(x, y - (fontHeight-2), fullStringWidth, g.getFont().getSize()+1);
        g.setColor(oldColor);

        g.drawString(text, x, y);
        g.setColor(colorTwo);
        g.drawString(textTwo, x + stringWidth, y);

        postPaint(g);
    }
}