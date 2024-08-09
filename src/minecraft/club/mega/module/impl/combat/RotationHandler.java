package club.mega.module.impl.combat;

import club.mega.Mega;
import club.mega.event.impl.*;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.impl.player.BedFucker;
import club.mega.module.impl.player.NoFall;
import club.mega.module.impl.player.Scaffold;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.module.setting.impl.RangeSetting;
import club.mega.util.AuraUtil;
import club.mega.util.ChatUtil;
import club.mega.util.RandomUtil;
import club.mega.util.RotationUtil;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "RotationHandler", description = "Backrotates smoothly to normal rotations", category = Category.MISC)
public class RotationHandler extends Module {
    private final RangeSetting yawSpeed = new RangeSetting("YawSpeed", this, 1.0, 180.0, 30.0, 50.0, 1.0);
    private final RangeSetting pitchSpeed = new RangeSetting("PitchSpeed", this, 1.0, 180.0, 30.0, 50.0, 1.0);

    public final BooleanSetting moveFix = new BooleanSetting("MoveFix", this, false);
    public final BooleanSetting silentMoveFix = new BooleanSetting("SilentMoveFix", this, false, moveFix::get);

    private RotationUtil rotationUtil = new RotationUtil();
    private boolean backRotated = false;

    private boolean shouldBackRotate() {
        boolean killaura = (AuraUtil.getTarget() == null && KillAura.getInstance().isToggled()) || !KillAura.getInstance().isToggled();

        boolean scaffold = !Scaffold.getInstance().isToggled();
        boolean nofall = !Mega.INSTANCE.getModuleManager().getModule(NoFall.class).isToggled() || !Mega.INSTANCE.getModuleManager().getModule(NoFall.class).mode.is("Legit");
        boolean bedfucker = !Mega.INSTANCE.getModuleManager().getModule(BedFucker.class).isToggled() || Mega.INSTANCE.getModuleManager().getModule(BedFucker.class).b == null;
        return killaura && scaffold && bedfucker && !isBackRotated() && RotationUtil.getRotations() != null;
    }

    private boolean shouldUsePlayerRotations() {
        boolean killaura = !KillAura.getInstance().isToggled() || AuraUtil.getTarget() == null;
        boolean scaffold = !Scaffold.getInstance().isToggled();
        boolean nofall = !Mega.INSTANCE.getModuleManager().getModule(NoFall.class).isToggled() || !Mega.INSTANCE.getModuleManager().getModule(NoFall.class).mode.is("Legit");
        boolean bedfucker = !Mega.INSTANCE.getModuleManager().getModule(BedFucker.class).isToggled() || Mega.INSTANCE.getModuleManager().getModule(BedFucker.class).b == null;

        return killaura && scaffold && bedfucker && isBackRotated();
    }

    @Handler
    public final void moveFlying(final EventMoveFlying event) {
        if (shouldBackRotate() || shouldUsePlayerRotations()) {
            event.setYaw(RotationUtil.getRotations()[0]);
        }
    }

    @Handler
    public void onSilent(EventSilentMove event) {
        if (shouldBackRotate() || shouldUsePlayerRotations()) {
            event.setSilent(true);
            event.setAdvanced(true);

        }

    }

    @Handler
    public final void look(final EventLook event) {
        if (shouldBackRotate() && !shouldUsePlayerRotations()) {
            event.setRotations(RotationUtil.getRotations());
        }
    }

    @Handler
    public final void tick(final EventTick event) {
        if (shouldBackRotate()) {

            float deltaYaw = (float) RandomUtil.getRandomNumber(yawSpeed.getCurrentMin(), yawSpeed.getCurrentMax());
            float deltaPitch = (float) RandomUtil.getRandomNumber(pitchSpeed.getCurrentMin(), pitchSpeed.getCurrentMax());
            RotationUtil rotationUtil = new RotationUtil();
            RotationUtil.setPrevRotations(RotationUtil.getRotations());

            RotationUtil.setRotations(rotationUtil.backRotate(deltaYaw, deltaPitch, RotationUtil.getPrevRotations()[0], RotationUtil.getPrevRotations()[1]));

            MC.thePlayer.rotationYawHead = RotationUtil.getRotations()[0];

            if (Math.abs((Math.abs(RotationUtil.getRotations()[0] % 360)) - (Math.abs(MC.thePlayer.rotationYaw % 360))) < 10F &&
                    Math.abs((Math.abs(RotationUtil.getRotations()[1] % 360)) - (Math.abs(MC.thePlayer.rotationPitch % 360))) < 10F) {
                backRotated = true;

            }
        } else if (shouldUsePlayerRotations()) {
            RotationUtil.setPrevRotations(RotationUtil.getRotations());
            RotationUtil.setRotations(new float[]{MC.thePlayer.rotationYaw, MC.thePlayer.rotationPitch});

        }
    }

    @Handler
    public final void preTick(final EventPreTick event) {
        if(shouldBackRotate() || shouldUsePlayerRotations()) {
            event.setRotations(RotationUtil.getRotations());
        }
    }
    @Handler
    public void onPitch(EventRenderPitch event) {
        if (shouldBackRotate() && !shouldUsePlayerRotations()) {
            event.setPitch(RotationUtil.getRotations()[1]);
        }
    }


    @Handler
    public final void velocity(final EventVelocity event) {
        if (AuraUtil.isLeftToBackRotate() && AuraUtil.getTarget() == null && !Scaffold.getInstance().isToggled()) {
            event.setYaw(RotationUtil.getRotations()[0]);
        }
    }

    public boolean isBackRotated() {
        return backRotated;
    }

    public void setBackRotated(boolean backRotated) {
        this.backRotated = backRotated;
    }
}
