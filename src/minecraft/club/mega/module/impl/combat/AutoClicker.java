package club.mega.module.impl.combat;

import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.util.RandomUtil;
import club.mega.util.TimeUtil;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "AutoClicker", description = "Spams leftclick", category = Category.COMBAT)
public class AutoClicker extends Module {
    private static final TimeUtil placeTimer = new TimeUtil();
    private static int  finalCps = 0;
    public final NumberSetting maxAPS = new NumberSetting("Max APS", this, 5, 40, 14, 1);
    public final NumberSetting minAPS = new NumberSetting("Min APS", this, 5, 40, 12, 1);
    @Handler
    public final void ontick(final EventTick event) {
        if(MC.gameSettings.keyBindAttack.pressed) {

                int aps = (int) Math.round(RandomUtil.getRandomNumber(minAPS.getAsInt(), maxAPS.getAsInt()));

                if (finalCps < aps)
                    finalCps += (int) Math.round(RandomUtil.getRandomNumber(0.8D, 2D));
                if (finalCps <= aps)
                    finalCps--;

                if (!placeTimer.hasTimePassed(1000 / aps))
                    return;
                MC.clickMouse();
                placeTimer.reset();
         
        }
    }
}
