package club.mega.module.impl.movement;

import club.mega.event.impl.TickEvent;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.NumberSetting;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "Timer", description = "Timer", category = Category.MOVEMENT)
public class Timer extends Module{
    private final NumberSetting speed = new NumberSetting("Speed", this, 0.1, 10, 1, 0.1);
    @Handler
    public final void movePlayer(final TickEvent event) {
        MC.timer.timerSpeed = speed.getAsFloat();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        MC.timer.timerSpeed = 1.0f;
    }
}
