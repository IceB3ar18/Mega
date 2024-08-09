package club.mega.module.impl.misc;

import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.ListSetting;
import club.mega.util.TimeUtil;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "StaffDetector", description = "Detects Staff in vanish", category = Category.MISC)
public class StaffDetector extends Module {

    public final ListSetting mode = new ListSetting("Mode", this, new String[]{"HypeMC"});

    private TimeUtil timerUtil = new TimeUtil();

    @Handler
    public final void onTick(final EventTick eventTick) {
        if(timerUtil.hasTimePassed(20000)) {
            MC.thePlayer.sendChatMessage("/find " + MC.thePlayer.getName());
            MC.thePlayer.sendChatMessage("/find v1nd1go");
            timerUtil.reset();
        }
    }
}
