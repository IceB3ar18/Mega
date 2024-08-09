package club.mega.module.impl.visual;

import club.mega.event.impl.EventPreTick;
import club.mega.module.Category;
import club.mega.module.Module;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "NoBob", description = "Remove BOB effect", category = Category.VISUAL)
public class NoBob extends Module {

    private boolean toggle;
    @Handler
    public final void tick(final EventPreTick event) {
        this.MC.gameSettings.viewBobbing = true;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        MC.gameSettings.viewBobbing = toggle;
    }
    @Override
    public void onEnable() {
        super.onEnable();
        toggle =  MC.gameSettings.viewBobbing;
    }

}
