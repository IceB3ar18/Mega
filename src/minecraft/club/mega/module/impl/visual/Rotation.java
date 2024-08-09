package club.mega.module.impl.visual;

import club.mega.event.impl.EventPreTick;
import club.mega.event.impl.EventRenderPitch;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.impl.combat.KillAura;
import club.mega.module.impl.combat.RotationHandler;
import club.mega.module.impl.player.Scaffold;
import club.mega.util.AuraUtil;
import club.mega.util.RotationUtil;
import rip.hippo.lwjeb.annotation.Handler;
@Module.ModuleInfo(name = "Rotation", description = "Rotation", category = Category.VISUAL)
public class Rotation extends Module {


    private boolean isRotating() {
        boolean rotate = ((KillAura.getInstance().isToggled() && AuraUtil.getTarget() != null) ||
                (Scaffold.getInstance().isToggled()) ||
                (AuraUtil.isLeftToBackRotate()));
        return rotate;
    }
    @Handler
    public final void preTick(final EventPreTick event) {

        if(isRotating()) {
            MC.thePlayer.rotationYawHead = RotationUtil.getRotations()[0];
            MC.thePlayer.renderYawOffset = RotationUtil.getRotations()[0];

        }

    }

    @Handler
    public final void renderPitch(final EventRenderPitch event) {
        if(isRotating()) {
            event.setPitch(RotationUtil.getRotations()[1]);
        }
    }


}
