package QuickMine.resources;

import org.dreambot.api.Client;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Timer;


import java.awt.*;

@ScriptManifest(category = Category.UTILITY, name = "Quick Time", author = "find me", version = 1.1)
public class QuickTime extends AbstractScript {

    public Timer Time;
    public Timer PauseTime;
    public Font[] Fonts;
    public String[] PauseReason;
    public int CurrentPauseReason;

    @Override
    public void onStart() {
        Time = new Timer(); //initialize Script Timer

        Fonts = new Font[]{
                new Font("Times New Roman", Font.BOLD, 16),
                new Font("Times New Roman", Font.PLAIN, 14),
                new Font("Default", Font.PLAIN, 12)
        };

        PauseReason = new String[]{
                "Logging in...",
                "Client Paused."
        };
    }

    @Override
    public int onLoop() {
        if (!Client.isLoggedIn()) {
            if (!Time.isPaused()) {
                CurrentPauseReason = 0;
                Time.pause();

                if (PauseTime == null) {
                    PauseTime = new Timer();
                }
                PauseTime.reset();
            }

            return 1000;
        }

        if (Time.isPaused()) {
            Time.resume();
        }

        return 50;
    }



    @Override
    public void onPause() {
        if (!Time.isPaused()) {
            CurrentPauseReason = 1;
            Time.pause();

            if (PauseTime == null) {
                PauseTime = new Timer();
            }
            PauseTime.reset();
        }
    }

    @Override
    public void onResume() {
        if (!Client.isLoggedIn()) {CurrentPauseReason = 0; return;}
        if (Time.isPaused()) {Time.resume();}
    }




    public int ScrW;
    public int ScrH;
    public int OriginX;
    public int OriginY;
    public int FrameWidth;
    public int FrameHeight;
    public Color FrameColor;
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



    public void SetupUI() {
        ScrW = Client.getViewportWidth();
        ScrH = Client.getViewportHeight();

        LastScreenSize = ScrW+ScrH;

        FrameWidth = 184;
        FrameHeight = 24;
        FrameColor = new Color(80, 80, 80, 160);

        if (Client.isLoggedIn()) {
            OriginX = (int) (ScrW - (ScrW * 0.354)); //TopRight ChatBox X
            OriginY = (int) (ScrH - (ScrH * 0.308)); //TopRight ChatBox Y

            LoginState = true;
        } else {
            OriginX = 2+FrameWidth;
            OriginY = 2;
        }
    }

    @Override
    public void onPaint(Graphics g) {
        if (FrameColor == null || ResolutionChange() || LoginStateChange()) { //Replace null check.
            SetupUI();
        }
        
        DrawFrame(g, OriginX, OriginY);

        DrawTimerInfo(g, OriginX, OriginY);
    }




    public void DrawFrame(Graphics g, int x, int y) {
        g.setColor(FrameColor);

        if (Time.isPaused()) {
            FrameHeight = 50; //Do sizing based on font sizes.

            if (PauseTime != null) {
                FrameHeight = 58; //Do sizing based on font sizes.
            }
        } else if (FrameHeight != 24) { //ew
            FrameHeight = 24;
        }

        g.fill3DRect(x-FrameWidth, y, FrameWidth, FrameHeight, true);
    }

    public void DrawTimerInfo(Graphics g, int x, int y) {
        g.setFont(Fonts[0]); //Position texts based of their sizes.

        g.setColor(Color.lightGray);
        g.drawString("Script Run Time: ", x-FrameWidth+4, y+16);

        g.setColor(Color.GREEN);
        if (Time.isPaused()) { g.setColor(Color.ORANGE); }
        g.drawString(Time.formatTime(), x-FrameWidth+121, y+17);



        if (!Time.isPaused()) return; //Not Paused, stop

        g.setColor(Color.ORANGE);
        g.drawString("PAUSED: ", x-FrameWidth+4, y+37);

        g.setColor(Color.green);
        g.setFont(Fonts[1]);
        g.drawString(PauseReason[CurrentPauseReason], x-FrameWidth+74, y+37);

        if (PauseTime != null) {
            g.setFont(Fonts[2]);

            g.setColor(Color.ORANGE);
            g.drawString("Time Since Paused: ", x-FrameWidth+4, y+52);

            g.setColor(Color.pink);
            g.drawString(PauseTime.formatTime(), x-FrameWidth+116, y+52);
        }
    }
}