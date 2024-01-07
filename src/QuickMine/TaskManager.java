package QuickMine;

import QuickMine.resources.ENUMS.*;
import QuickMine.tasks.DropInventoryTask;
import QuickMine.tasks.QuickMineTask;
import QuickMine.tasks.QuickWalkTask;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.SkillTracker;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.script.impl.TaskScript;

import java.awt.*;

import static QuickMine.resources.ENUMS.PRIORITY.JUSTGO;

public class TaskManager extends TaskScript {
    public PRIORITY priority;

    @Override
    public void onStart() {
        SkillTracker.start(Skill.MINING);

        setPriority(JUSTGO);
    }

    public void onSetup(PRIORITY... priorities) {
        if (priorities.length > 0) {
            priority = priorities[0];
        } else {
            priority = JUSTGO;
        }

        setPriority(priority);
    }

    public void setPriority(PRIORITY priority) {
        switch (priority) {
            case JUSTGO:
                addNodes(new QuickWalkTask(), new QuickMineTask(), new DropInventoryTask());

                break;
            case EXPERIENCE:
                //addNodes(new MiningTask());

                break;
            case GOLD:
                //addNodes();

                break;
        }
    }

    @Override
    public void onPaint(Graphics g) {
        String miningLevel = String.format(
                "Mining Level: %d", // The paint's text format. '%d' will be replaced with the next two arguments.
                Skills.getRealLevel(Skill.MINING)
        );

        String experienceGainedText = String.format(
                "Mining Experience: %d (%d per hour)", // The paint's text format. '%d' will be replaced with the next two arguments.
                SkillTracker.getGainedExperience(Skill.MINING),
                SkillTracker.getGainedExperiencePerHour(Skill.MINING)
        );

        g.setColor(Color.gray);
        g.drawString(miningLevel, 5, 55);
        g.drawString(experienceGainedText, 5, 70);
    }

}
