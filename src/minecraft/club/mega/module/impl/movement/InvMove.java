package club.mega.module.impl.movement;

import club.mega.event.impl.EventPreTick;
import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.BooleanSetting;
import net.minecraft.util.MouseHelper;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "InvMove", description = "Move in your inventory", category = Category.MOVEMENT)
public class InvMove extends Module {
    public final BooleanSetting clickGuiOnly = new BooleanSetting("ClickGui only", this, false);

}
