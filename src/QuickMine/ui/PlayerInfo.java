package QuickMine.ui;

import QuickMine.Resources;
import org.dreambot.api.Client;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.wrappers.interactive.Character;
import org.dreambot.api.wrappers.interactive.Player;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;

import static QuickMine.Resources.*;
import static org.dreambot.api.utilities.Logger.log;

public class PlayerInfo {
    public Image animatingIcon;
    public Image animatingIconActive;
    public int ScrW;
    public int ScrH;
    public int iconX;
    public int iconY;
    public int iconSize;
    public Color iconColor;
    public int lastScreenSize;
    public boolean loginState;






    public void rebuildUI() {
        getImages();

        ScrW = Client.getViewportWidth();
        ScrH = Client.getViewportHeight();

        lastScreenSize = cantorPairing(ScrW, ScrH);

        iconSize = 32;
        iconColor = new Color(80, 80, 80, 160);

        if (Client.isLoggedIn()) {
            iconX = 8; //TopRight ChatBox X
            iconY = (int) (ScrH - (ScrH * 0.308) - iconSize*1.5 -14); //TopRight ChatBox Y

            loginState = true;
        } else {
            iconX = 2+ iconSize;
            iconY = 2;
        }
    }


    public void getImages() {
        try {//Try collect Animating_Icon image resource.
            Image original = ImageIO.read(new File(Directory(Resources.FileResources.IMAGES) + "Animating_Icon.png"));
            Image resizedImage = original.getScaledInstance(32, 32, Image.SCALE_SMOOTH);

            animatingIcon = resizedImage;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {//Try collect Animating_Icon_Active image resource.
            Image original = ImageIO.read(new File(Directory(Resources.FileResources.IMAGES) + "Animating_Icon_Active.png"));
            Image resized = original.getScaledInstance(32, 32, Image.SCALE_SMOOTH);

            animatingIconActive = resized;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void drawAnimationID(Graphics g, Boolean... animating) {
        int animationID = Players.getLocal().getAnimation();
        g.setFont(fonts[1]);

        FontMetrics metrics = g.getFontMetrics(fonts[1]);
        int stringWidth = metrics.stringWidth("Current Animation ID: ");


        if (animating.length > 0) {
            g.setColor(Color.white);
            g.drawString("Current Animation ID: ", iconX + iconSize + 4, iconY + iconSize -24);
            g.setColor(Color.green);
        }else {
            g.setColor(Color.gray);
            g.drawString("Current Animation ID: ", iconX + iconSize + 4, iconY + iconSize -24);
        }
        g.drawString("" + animationID, iconX + iconSize + 4 + stringWidth, iconY + iconSize -24);
    }
    public void drawInCombat(Graphics g) {
        if (Players.getLocal().isInCombat()) {
            g.setFont(fonts[1]);

            FontMetrics metrics = g.getFontMetrics(fonts[1]);
            int previousStringWidth = metrics.stringWidth("Current Animation ID: " + Players.getLocal().getAnimation());

            g.setColor(Color.red);
            g.drawString("In Combat", iconX + iconSize + 10 + previousStringWidth, iconY + iconSize -24);
        }
    }
    public void drawInteractingID(Graphics g) {
        Player pl = Players.getLocal();
        Character interactingCharacter = pl.getInteractingCharacter();
        long interactingID = pl.getInteractingIndex();
        g.setFont(fonts[1]);

        FontMetrics metrics = g.getFontMetrics(fonts[1]);
        int stringWidth = metrics.stringWidth("Interacting ID: ");

        g.setColor(Color.gray);
        if (pl.isInteracting(interactingCharacter)) {
            g.setColor(Color.white);
        }
        g.drawString("Interacting ID: ", iconX + iconSize + 4, iconY + iconSize -10);

        if (pl.isInteracting(interactingCharacter)) {
            g.setColor(Color.green);
            if (pl.isInCombat()) {
                g.setColor(Color.red);
            }
        }
        g.drawString("" + interactingID, iconX + iconSize + 4 + stringWidth, iconY + iconSize -10);
    }
    public void drawInteractingCharacter(Graphics g) {
        Player pl = Players.getLocal();
        Character interactingCharacter = pl.getInteractingCharacter();
        g.setFont(fonts[1]);

        String intermediateText;
        if (pl.isInCombat()) {
            intermediateText = "Fighting";
        }else {
            intermediateText = "Interacting";
        }

        FontMetrics metrics = g.getFontMetrics(fonts[1]);
        int stringWidth = metrics.stringWidth(intermediateText + " with: ");

        int previousStringWidth = metrics.stringWidth("Interacting ID: " + pl.getInteractingIndex());

        g.setColor(Color.gray);
        if (pl.isInteracting(interactingCharacter)) {
            g.setColor(Color.white);
        }

        g.drawString(intermediateText + " with: ", iconX + iconSize + 10 + previousStringWidth , iconY + iconSize -10);

        if (pl.isInteracting(interactingCharacter)) {
            g.setColor(Color.green);
            if (pl.isInCombat()) {
                g.setColor(Color.red);
            }
            g.drawString("" + interactingCharacter.getName(), iconX + iconSize + 10 + previousStringWidth + stringWidth , iconY + iconSize -10);
        }
    }
    public void drawFacingDirection(Graphics g) {
        Player pl = Players.getLocal();
        g.setFont(fonts[1]);

        FontMetrics metrics = g.getFontMetrics(fonts[1]);

        String intermediateText;
        if (pl.isStandingStill()) {
            intermediateText = "Facing";
        }else if (Walking.isRunEnabled()) {
            intermediateText = "Running";
        }else {
            intermediateText = "Walking";
        }

        int stringWidth = metrics.stringWidth("Currently " + intermediateText + ": ");

        if (pl.isMoving()) {
            g.setColor(Color.white);
            g.drawString("Currently " + intermediateText + ": ", iconX + iconSize + 4 , iconY + iconSize +4);
            g.setColor(Color.green);
        }else {
            g.setColor(Color.gray);
            g.drawString("Currently " + intermediateText + ": ", iconX + iconSize + 4 , iconY + iconSize +4);
        }
        g.drawString("" + pl.getFacingDirection(), iconX + iconSize + 4 + stringWidth , iconY + iconSize +4);
    }
    public void drawCoordinates(Graphics g) {
        Player pl = Players.getLocal();
        int x = pl.getX();
        int y = pl.getY();
        g.setFont(fonts[1]);

        String intermediateText;
        if (pl.isStandingStill()) {
            intermediateText = "Facing";
        }else if (Walking.isRunEnabled()) {
            intermediateText = "Running";
        }else {
            intermediateText = "Walking";
        }

        FontMetrics metrics = g.getFontMetrics(fonts[1]);
        int stringWidth = metrics.stringWidth("@ Coordinates:");

        int previousStringWidth = metrics.stringWidth("Currently " + intermediateText + ": " + pl.getFacingDirection());

        g.setColor(Color.gray);
        g.drawString("@ Coordinates: ", iconX + iconSize + 10 + previousStringWidth, iconY + iconSize +4);
        g.drawString(" (" + x + ", " + y + ")", iconX + iconSize + 10 + previousStringWidth + stringWidth , iconY + iconSize +4);
    }






    public void onPaint(Graphics g) {
        if (!Client.isLoggedIn()) return;

        if (iconColor == null || ResolutionChange(lastScreenSize) || LoginStateChange(loginState)) { //Replace null check.
            rebuildUI();
        }

        if (animatingIcon != null && animatingIconActive != null ) {
            Player pl = Players.getLocal();

            if (pl != null) {
                drawFacingDirection(g);
                drawCoordinates(g);
                drawInteractingID(g);
                drawInteractingCharacter(g);
                drawInCombat(g);

                if (pl.isAnimating()) {
                    drawAnimationID(g, true);
                    g.drawImage(animatingIconActive, iconX, iconY +2, null);
                } else {
                    drawAnimationID(g);
                    g.drawImage(animatingIcon, iconX, iconY +2, null);
                }
            }
        }
    }
}














//what would you even use these for...
/*
                drawUserID(g);
                drawWalkingAnimation(g);
                drawStandingAnimation(g);
                drawAnimationDelay(g, true);

    public int getAnimationDelay() {
        if (Players.getLocal() == null) {
            return 0;
        }

        return Players.getLocal().getAnimationDelay();
    }

    public void drawUserID(Graphics g) {
        Player pl = Players.getLocal();
        long userID = pl.getUID();
        g.setFont(Fonts[1]);

        FontMetrics metrics = g.getFontMetrics(Fonts[1]);
        int stringWidth = metrics.stringWidth("Player ID: ");

        g.setColor(Color.gray);
        g.drawString("Player ID: ", iconX + iconSize + 4 , iconY + iconSize -28);
        g.drawString("" + userID, iconX + iconSize + 4 + stringWidth , iconY + iconSize -28);
    }

    public void drawAnimationDelay(Graphics g, Boolean... animating) {
        int animationDelay = getAnimationDelay();
        g.setFont(Fonts[1]);

        FontMetrics metrics = g.getFontMetrics(Fonts[1]);
        int stringWidth = metrics.stringWidth("Animation Delay: ");

        g.setColor(Color.gray);
        g.drawString("Animation Delay: ", 4 , iconY - 12 - 14);
        if (animating.length > 0) {
            g.setColor(Color.green);
        }
        g.drawString("" + animationDelay, 4 + stringWidth , iconY - 12 - 14);

    }

    public void drawWalkingAnimation(Graphics g) {
        Player pl = Players.getLocal();
        int walkingAnimation = pl.getWalkAnimation();
        g.setFont(Fonts[1]);

        FontMetrics metrics = g.getFontMetrics(Fonts[1]);
        int stringWidth = metrics.stringWidth("Walking Animation ID: ");

        g.setColor(Color.gray);
        g.drawString("Walking Animation ID: ", iconX + iconSize + 4 , iconY + iconSize -14);
        g.drawString(" " + walkingAnimation, iconX + iconSize + 4 + stringWidth , iconY + iconSize -14);
    }

    public void drawStandingAnimation(Graphics g) {
        Player pl = Players.getLocal();
        int standingAnimation = pl.getStandAnimation();
        g.setFont(Fonts[1]);

        FontMetrics metrics = g.getFontMetrics(Fonts[1]);
        int stringWidth = metrics.stringWidth("Standing Animation ID: ");

        int previousStringWidth = metrics.stringWidth("Walking Animation ID: " + pl.getWalkAnimation());

        g.setColor(Color.gray);
        g.drawString("Standing Animation ID: ", iconX + iconSize + 12 + previousStringWidth, iconY + iconSize -14);
        g.drawString(" " + standingAnimation, iconX + iconSize + 12 + previousStringWidth + stringWidth , iconY + iconSize -14);
    }

*/