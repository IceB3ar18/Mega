package club.mega.module.impl.visual;

import club.mega.Mega;
import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.*;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "Animations", description = "Block Animations", category = Category.VISUAL)
public class Animations extends Module {

    public final ListSetting mode = new ListSetting("Mode", this, new String[]{"Mega", "Mega2", "Mega3", "1.8"});
    public final NumberSetting swingSlowness = new NumberSetting("Swing slowness", this, 3, 100, 11, 1);
    public final BooleanSetting equipProgress = new BooleanSetting("EquipProgress", this, true);
    public final NumberSetting equipProgressMultiplier = new NumberSetting("Progress Multiplier", this, 0, 2, 1, 0.05, equipProgress::get);

    public static Animations getInstance() {
        return Mega.INSTANCE.getModuleManager().getModule(Animations.class);
    }

    @Handler
    public final void earlyTick(final EventTick eventTick) {
        this.setTag(mode.getCurrent());
    }

}
