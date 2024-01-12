package QuickMine.ui;

import org.dreambot.api.Client;
import org.dreambot.api.randoms.RandomManager;
import org.dreambot.api.utilities.Timer;

import java.awt.*;

import static QuickMine.Resources.*;


public class ScriptTime {
    public ScriptTime(RandomManager manager) {
        this.randomManager = manager;

        runTime = new Timer(); //initialize Script Timer

        pauseReason = new String[]{
                "Logging in...",
                "Solving...",
                "Client Paused."
        };
    }
    public Timer runTime;
    public Timer pausedTime;
    public String[] pauseReason;
    public int currentPauseReason;
    public RandomManager randomManager;





    public void onLoop() {
        pauseCheck();

        unPauseCheck();
    }

    public void onPause() {
        if (!runTime.isPaused()) {
            currentPauseReason = 2;
            runTime.pause();

            if (pausedTime == null) {
                pausedTime = new Timer();
            }
            pausedTime.reset();
        }
    }

    public void onResume() {
        if (!Client.isLoggedIn()) {
            currentPauseReason = 0; return;
        }else if(randomManager.isSolving()) {
            currentPauseReason = 1; return;
        }

        if (runTime.isPaused()) {
            runTime.resume();
        }
    }

    public void pauseCheck() {
        if (runTime.isPaused()) return;

        if (randomManager.isSolving()) {
            currentPauseReason = 1;
            runTime.pause();

            if (pausedTime == null) {
                pausedTime = new Timer();
            }
            pausedTime.reset();
        }else if(!Client.isLoggedIn()) {
            currentPauseReason = 0;
            runTime.pause();

            if (pausedTime == null) {
                pausedTime = new Timer();
            }
            pausedTime.reset();
        }

    }

    public void unPauseCheck() {
        if (!runTime.isPaused()) return;

        if (!randomManager.isSolving() && Client.isLoggedIn()) {
            runTime.resume();
        }
    }





    public int ScrW;
    public int ScrH;
    public int originX;
    public int originY;
    public int frameWidth;
    public int frameHeight;
    public Color frameColor;
    public int lastScreenSize;
    public boolean loginState;

    public void RebuildUI() {
        ScrW = Client.getViewportWidth();
        ScrH = Client.getViewportHeight();

        lastScreenSize = cantorPairing(ScrW, ScrH);

        frameWidth = 184;
        frameHeight = 24;
        frameColor = new Color(80, 80, 80, 160);

        if (Client.isLoggedIn() && !randomManager.isSolving()) {
            originX = (int) (ScrW - (ScrW * 0.354)); //TopRight ChatBox X
            originY = (int) (ScrH - (ScrH * 0.308)); //TopRight ChatBox Y

            loginState = true;
        } else {
            originX = 2+ frameWidth;
            originY = 2;
        }
    }

    public void onPaint(Graphics g) {
        if (frameColor == null || ResolutionChange(lastScreenSize) || LoginStateChange(loginState) || (originY == 2 && !randomManager.isSolving())) { //Replace null check.
            RebuildUI();
        }
        
        DrawFrame(g, originX, originY);
        DrawContents(g, originX, originY);
    }





    public void DrawFrame(Graphics g, int x, int y) {
        g.setColor(frameColor);

        if (runTime.isPaused()) {
            frameHeight = 50; //Do sizing based on font sizes.

            if (pausedTime != null) {
                frameHeight = 58; //Do sizing based on font sizes.
            }
        } else if (frameHeight != 24) { //ew
            frameHeight = 24;
        }

        g.fill3DRect(x- frameWidth, y, frameWidth, frameHeight, true);
    }

    public void DrawContents(Graphics g, int x, int y) {
        g.setFont(fonts[0]); //Position texts based of their sizes.

        g.setColor(Color.lightGray);
        g.drawString("Script Run Time: ", x- frameWidth +4, y+16);

        g.setColor(Color.GREEN);
        if (runTime.isPaused()) { g.setColor(Color.ORANGE); }
        g.drawString(runTime.formatTime(), x- frameWidth +121, y+17);


        if (!runTime.isPaused()) return; //Not Paused, stop

        g.setColor(Color.ORANGE);
        g.drawString("PAUSED: ", x- frameWidth +4, y+37);

        g.setColor(Color.green);
        g.setFont(fonts[1]);
        g.drawString(pauseReason[currentPauseReason], x- frameWidth +74, y+37);


        if (pausedTime == null) return; //No pausedTime, stop

        g.setFont(fonts[2]);

        g.setColor(Color.ORANGE);
        g.drawString("Time Since Paused: ", x- frameWidth +4, y+52);

        g.setColor(Color.pink);
        g.drawString(pausedTime.formatTime(), x- frameWidth +116, y+52);
    }
}