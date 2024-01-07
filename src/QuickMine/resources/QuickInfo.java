package QuickMine.resources;

import org.dreambot.api.Client;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.wrappers.interactive.Player;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class QuickInfo extends AbstractScript {
    public Image Animating_Icon;
    public Image Animating_Icon_Active;
    public int ScrW;
    public int ScrH;
    public int iconX;
    public int iconY;
    public int iconSize;
    public Color iconColor;
    public int LastScreenSize;
    public boolean LoginState;

    public boolean ResolutionChange() {
        return LastScreenSize != Client.getViewportWidth() + Client.getViewportHeight();
    }

    public boolean LoginStateChange() {
        if (LoginState != Client.isLoggedIn()) {
            LoginState = Client.isLoggedIn();

            return true;
        }

        return false;
    }


    @Override
    public void onStart() {
        buildResourceDirectory();
    }

    public void SetupUI() {
        ScrW = Client.getViewportWidth();
        ScrH = Client.getViewportHeight();
        LastScreenSize = ScrW+ScrH;

        iconSize = 32;
        iconColor = new Color(80, 80, 80, 160);

        if (Client.isLoggedIn()) {
            iconX = 12; //TopRight ChatBox X
            iconY = (int) (ScrH - (ScrH * 0.308) - iconSize*1.5); //TopRight ChatBox Y

            LoginState = true;
        } else {
            iconX = 2+ iconSize;
            iconY = 2;
        }

        buildFonts();
    }

    @Override
    public int onLoop() {
        //NOT CALLED CURRENTLY.
        return 50;
    }

    public Font[] Fonts;

    public void buildFonts() {
        Fonts = new Font[]{
                new Font("Times New Roman", Font.BOLD, 16),
                new Font("Times New Roman", Font.PLAIN, 14),
                new Font("Default", Font.PLAIN, 12)
        };
    }

    public int getAnimationDelay() {
        if (Players.getLocal() == null) {
            return 0;
        }

        return Players.getLocal().getAnimationDelay();
    }

    public void drawAnimationDelay(Graphics g, Boolean... animating) {
        int animationDelay = getAnimationDelay();
        g.setFont(Fonts[1]);

        FontMetrics metrics = g.getFontMetrics(Fonts[1]);
        int stringWidth = metrics.stringWidth("Animation Delay: ");

        g.setColor(Color.gray);
        g.drawString("Animation Delay: ", 4 , iconY - 8 - 14);
        if (animating.length > 0) {
            g.setColor(Color.green);
        }
        g.drawString("" + animationDelay, 4 + stringWidth , iconY - 8 - 14);

    }

    public int getAnimationID() {
        if (Players.getLocal() == null) {
            return -1;
        }

        return Players.getLocal().getAnimation();
    }

    public void drawAnimationID(Graphics g, Boolean... animating) {
        int animationID = getAnimationID();
        g.setFont(Fonts[1]);

        FontMetrics metrics = g.getFontMetrics(Fonts[1]);
        int stringWidth = metrics.stringWidth("Current Animation ID: ");

        g.setColor(Color.gray);
        g.drawString("Current Animation ID: ", 4 , iconY - 8);
        if (animating.length > 0) {
            g.setColor(Color.green);
        }
        g.drawString("" + animationID, 4 + stringWidth , iconY - 8);

    }

    @Override
    public void onPaint(Graphics g) {
        if (iconColor == null || ResolutionChange() || LoginStateChange()) { //Replace null check.
            SetupUI();
        }

        if (Animating_Icon != null && Animating_Icon_Active != null ) {
            Player pl = Players.getLocal();

            if (pl != null) {
                log("hi");
                if (pl.isAnimating()) {
                    drawAnimationDelay(g, true);
                    drawAnimationID(g, true);
                    g.drawImage(Animating_Icon_Active, iconX, iconY, null);
                } else {
                    drawAnimationDelay(g);
                    drawAnimationID(g);
                    g.drawImage(Animating_Icon, iconX, iconY, null);
                }
            }
        }
    }

    public String Directory(ENUMS.RESOURCE... finalDirectory) {
        String sourceDirectory = System.getProperty("user.dir").replace('\\', '/') + "Scripts/" + ENUMS.RESOURCE.SOURCE.dir;

        if (finalDirectory.length > 0) {
            sourceDirectory = System.getProperty("user.dir").replace('\\', '/') + "Scripts/" + ENUMS.RESOURCE.SOURCE.dir + finalDirectory[0].dir;
        }

        return sourceDirectory;
    }

    public void buildResourceDirectory() {
        try {
            Files.createDirectories(Paths.get(Directory()));
            Files.createDirectories(Paths.get(Directory(ENUMS.RESOURCE.IMAGES)));
        } catch (IOException e) { log(e); }

        try {//Try collect Animating_Icon image resource.
            Image original = ImageIO.read(new File(Directory(ENUMS.RESOURCE.IMAGES) + "Animating_Icon.png"));
            Image resized = original.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            Animating_Icon = resized;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {//Try collect Animating_Icon_Active image resource.
            Image original = ImageIO.read(new File(Directory(ENUMS.RESOURCE.IMAGES) + "Animating_Icon_Active.png"));
            Image resized = original.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            Animating_Icon_Active = resized;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
