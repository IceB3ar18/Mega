package club.mega.module.impl.player;

import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.NumberSetting;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "Range", description = "More range", category = Category.PLAYER)
public class Range extends Module {



    public final NumberSetting range = new NumberSetting("Range", this, 3, 6, 3, 0.1);
    @Handler
    public final void earlyTick(final EventTick eventTick) {
        this.setTag(String.valueOf(range.getAsDouble()));
    }


}
