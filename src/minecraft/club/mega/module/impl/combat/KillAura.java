package club.mega.module.impl.combat;

import club.mega.Mega;
import club.mega.event.impl.*;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.impl.player.Scaffold;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ListSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.module.setting.impl.RangeSetting;
import club.mega.util.*;
import net.minecraft.util.Vec3;
import rip.hippo.lwjeb.annotation.Handler;

import java.security.SecureRandom;


@Module.ModuleInfo(name = "KillAura", description = "Automatically attacks enemies", category = Category.COMBAT)
public class KillAura extends Module {

    public final ListSetting mode = new ListSetting("Mode", this, new String[]{"Single", "Switch"});
    public final NumberSetting switchDelay = new NumberSetting("Switch delay", this, 200, 1000, 300, 100, () -> mode.is("switch"));
    public final ListSetting priority = new ListSetting("Priority", this, new String[]{"Health", "Distance"});
    public final ListSetting attackMode = new ListSetting("Attack mode", this, new String[]{"Normal", "Click"});
    public final ListSetting attackOn = new ListSetting("Attack on", this, new String[]{"Legit", "Tick", "Pre", "Post", "Frame"});
    public final NumberSetting preRange = new NumberSetting("Pre Range", this, 0, 3, 1, 0.1);
    public final BooleanSetting randomizePreRange = new BooleanSetting("Randomize pre Range", this, true, () -> preRange.getAsDouble() != 0);
    public final NumberSetting range = new NumberSetting("Range", this, 3, 7, 3.4, 0.1);
    public final BooleanSetting smoothRots = new BooleanSetting("SmoothRotations", this, true);
    public final BooleanSetting calcSpeed = new BooleanSetting("CalcRotSpeed", this, false, ()-> smoothRots.get());
    private final RangeSetting yawSpeed = new RangeSetting("YawSpeed", this, 1.0, 180.0, 30.0, 50.0, 1.0, ()->!calcSpeed.get() && smoothRots.get());
    private final RangeSetting pitchSpeed = new RangeSetting("PitchSpeed", this, 1.0, 180.0, 30.0, 50.0, 1.0, ()->!calcSpeed.get() && smoothRots.get());
    public final BooleanSetting bestHitVec = new BooleanSetting("BestHitVec", this, true);
    public final NumberSetting inaccuracy = new NumberSetting("Inaccuracy", this, 0, 100, 0, 1, bestHitVec::get);
    public final BooleanSetting a3Fix = new BooleanSetting("A3Fix", this, true);
    public final BooleanSetting mouseFix = new BooleanSetting("MouseFix", this, true);
    public final BooleanSetting predict = new BooleanSetting("Predict", this, true);
    public final BooleanSetting heuristics = new BooleanSetting("Heuristics", this, false);
    public final BooleanSetting randomizeRange = new BooleanSetting("Randomize range", this, false);
    public final NumberSetting minAPS = new NumberSetting("Min APS", this, 1, 1000, 80, 1);
    public final NumberSetting maxAPS = new NumberSetting("Max APS", this, 1, 1000, 50, 1);
    public final BooleanSetting perfectHit = new BooleanSetting("PerfectHit", this, true);
    public final NumberSetting perfectHitChance = new NumberSetting("PHChance", this, 1, 100, 100, 1, perfectHit::get);
    public final BooleanSetting autoBlock = new BooleanSetting("Auto. block", this, false);
    public final NumberSetting blockRange = new NumberSetting("Block range", this, 2, 10, 4, 0.1, autoBlock::get);
    public final NumberSetting blockChance = new NumberSetting("Block chance", this, 5, 100, 20, 5, autoBlock::get);
    public final NumberSetting minUnBlockTicks = new NumberSetting("Min unblock ticks", this, 1, 20, 8, 1, autoBlock::get);
    public final NumberSetting maxUnBlockTicks = new NumberSetting("Max unblock ticks", this, 1, 20, 13, 1, autoBlock::get);
    public final BooleanSetting fakeBlock = new BooleanSetting("Fake block", this, false);
    public final BooleanSetting preSwing = new BooleanSetting("Pre swing", this, true);
    public final BooleanSetting moveFix = new BooleanSetting("MoveFix", this, true);
    public final BooleanSetting silentMoveFix = new BooleanSetting("SilentMoveFix", this, true, moveFix::get);
    public final BooleanSetting velocityFix = new BooleanSetting("VelocityFix", this, true);
    public final BooleanSetting keepSprint = new BooleanSetting("Keep sprint", this, false);
    public final BooleanSetting shopAttack = new BooleanSetting("Shop", this, false, false);
    public final BooleanSetting deathDisable = new BooleanSetting("Disable on death", this, true, true);
    public final BooleanSetting attackDeath = new BooleanSetting("Attack death", this, false);
    public final BooleanSetting player = new BooleanSetting("Players", this, true, true);
    public final BooleanSetting mobs = new BooleanSetting("Mobs", this, true, false);
    public final BooleanSetting villagers = new BooleanSetting("Villagers", this, false, false);
    public final BooleanSetting lockView = new BooleanSetting("LockView", this, false);
    public final BooleanSetting auraEsp = new BooleanSetting("LookESP", this, false);


    public Vec3 best;

    public boolean b = false;


    @Handler
    public final void tick(final EventTick event) {

        AuraUtil.setTargets();
        if ((!MC.thePlayer.isEntityAlive() || MC.theWorld == null && MC.thePlayer != null) && deathDisable.get())
            toggle();
        SecureRandom secureRandom = new SecureRandom();
        float deltaYaw = (RandomUtil.nextFloat(this.yawSpeed.getCurrentMin() - 0.0010000000474974513, this.yawSpeed.getCurrentMax()) / 2.0F + secureRandom.nextFloat() + RandomUtil.nextFloat(this.yawSpeed.getCurrentMin() - 0.0010000000474974513, this.yawSpeed.getCurrentMax()) / 2.0F) * 0.5F;
        float deltaPitch = (RandomUtil.nextFloat(this.pitchSpeed.getCurrentMin() - 0.0010000000474974513, this.pitchSpeed.getCurrentMax()) / 2.0F + secureRandom.nextFloat() + RandomUtil.nextFloat(this.pitchSpeed.getCurrentMin() - 0.0010000000474974513, this.pitchSpeed.getCurrentMax()) / 2.0F) * 0.5F;

        RotationUtil auraRots = new RotationUtil();
        if (AuraUtil.getTarget() == null) {
            AuraUtil.setPreRange(preRange.getAsFloat());
            AuraUtil.setRange(range.getAsFloat());
            Mega.INSTANCE.getModuleManager().getModule(RotationHandler.class).setBackRotated(false);
            AuraUtil.resetCps();
            AuraUtil.forceUnblock();
            return;
        }

        RotationUtil.setPrevRotations(RotationUtil.getRotations());
        float[] rotations = auraRots.auraRots(AuraUtil.getTarget(),RotationUtil.getRotations()[0], RotationUtil.getRotations()[1], mouseFix.get(), a3Fix.get(), bestHitVec.get(), inaccuracy.getAsFloat() / 100F, heuristics.get(), predict.get(), calcSpeed.get(), deltaYaw, deltaPitch);
        AuraUtil.setLeftToBackRotate(true);
        RotationUtil.setRotations(rotations);

        AuraUtil.addBlockTick();
        if (attackOn.is("tick"))
            AuraUtil.attack();

    }

    @Handler
    public final void preTick(final EventRender3D event) {
        if(AuraUtil.getTarget() != null && auraEsp.get()) {
            RenderUtil renderUtil = new RenderUtil();
            renderUtil.drawTargetBox(AuraUtil.getTarget(), RotationUtil.getRotations());
        }
    }

    @Handler
    public final void preTick(final EventPreTick event) {
        if (RotationUtil.getRotations() == null || Scaffold.getInstance().isToggled() || AuraUtil.getTarget() == null)
            return;
        if(lockView.get()) {
            MC.thePlayer.rotationYaw = RotationUtil.getRotations()[0];
            MC.thePlayer.rotationPitch = RotationUtil.getRotations()[1];
        }
        event.setRotations(RotationUtil.getRotations());

        if (attackOn.is("pre") && !Scaffold.getInstance().isToggled() && AuraUtil.getTarget() != null)
            AuraUtil.attack();
    }



    @Handler
    public void onClick(EventClickMouse eventClickMouse) {

        if (attackOn.is("Legit") && !Scaffold.getInstance().isToggled() && AuraUtil.getTarget() != null)
            AuraUtil.attack();
    }

    @Handler
    public final void postTick(final EventPostTick event) {
        if (attackOn.is("post") && !Scaffold.getInstance().isToggled() && AuraUtil.getTarget() != null)
            AuraUtil.attack();
    }

    @Handler
    public final void render2D(final EventRender2D event) {
        if (attackOn.is("frame") && !Scaffold.getInstance().isToggled() && AuraUtil.getTarget() != null)
            AuraUtil.attack();
    }

    @Handler
    public final void look(final EventLook event) {
        if (RotationUtil.getRotations() != null && !Scaffold.getInstance().isToggled() && AuraUtil.getTarget() != null)
            event.setRotations(RotationUtil.getRotations());
    }

    @Handler
    public final void moveFlying(final EventMoveFlying event) {
        if ((RotationUtil.getRotations() == null || !moveFix.get()) || Scaffold.getInstance().isToggled() || AuraUtil.getTarget() == null)
            return;

        event.setYaw(RotationUtil.getRotations()[0]);
    }


    @Handler
    public final void onSilent(final EventSilentMove event) {
        if (RotationUtil.getRotations() == null || !silentMoveFix.get() || !silentMoveFix.isVisible() || AuraUtil.getTarget() == null)
            return;
        event.setAdvanced(true);
        event.setSilent(true);
    }
    @Handler
    public final void velocity(final EventVelocity event) {
        if ((RotationUtil.getRotations() == null || !velocityFix.get()) || Scaffold.getInstance().isToggled() || AuraUtil.getTarget() == null)
            return;

        event.setYaw(RotationUtil.getRotations()[0]);
    }



    @Override
    public void onEnable() {
        super.onEnable();
        AuraUtil.setPreRange(preRange.getAsFloat());
        AuraUtil.setRange(range.getAsFloat());
        AuraUtil.resetCps();
        AuraUtil.setRange(range.getAsFloat());
        if (MC.thePlayer != null)
            RotationUtil.setRotations(new float[]{MC.thePlayer.rotationYaw, MC.thePlayer.rotationPitch});
    }

    @Override
    public void onDisable() {
        super.onDisable();
        AuraUtil.setTarget(null);
        AuraUtil.forceUnblock();
    }

    public final float[] getRots() {
        return (KillAura.getInstance().isToggled() && !Scaffold.getInstance().isToggled()) ? RotationUtil.getRotations() : new float[]{MC.thePlayer.rotationYaw, MC.thePlayer.rotationPitch};
    }

    public static KillAura getInstance() {
        return Mega.INSTANCE.getModuleManager().getModule(KillAura.class);
    }
}