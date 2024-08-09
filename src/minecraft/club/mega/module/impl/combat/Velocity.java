package club.mega.module.impl.combat;

import club.mega.Mega;
import club.mega.event.impl.EventPacket;
import club.mega.event.impl.EventPostTick;
import club.mega.event.impl.EventSilentMove;
import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.impl.player.Scaffold;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ListSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.util.Vec3;
import rip.hippo.lwjeb.annotation.Handler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

@Module.ModuleInfo(name = "Velocity", description = "Removes Velocity", category = Category.COMBAT)
public class Velocity extends Module {
    private final TimeUtil timeHelper = new TimeUtil();
    private final TimeUtil timeDelay = new TimeUtil();
    private final ArrayList<Packet> packets = new ArrayList();
    public final ListSetting mode = new ListSetting("Mode", this, new String[]{"Basic", "Legit", "PushGround", "Push", "Intave", "IntaveTest", "Reverse", "Spoof", "Test", "Grim", "Grim2", "BuzzReverse", "TickZero"});
    public final NumberSetting XZValue = new NumberSetting("XZVelocity", this, 0, 100, 20.0, 1, () -> mode.is("Basic"));
    public final NumberSetting YValue = new NumberSetting("YVelocity", this, 0, 100, 20.0, 1, () -> mode.is("Basic"));
    public final NumberSetting XZValueIntave = new NumberSetting("XZIntave", this, -1.0, 1.0, 0.6, 0.1, () -> mode.is("Intave"));
    public final BooleanSetting jumpIntave = new BooleanSetting("Jump", this, false, () -> mode.is("Intave"));
    public final BooleanSetting ignoreExplosion = new BooleanSetting("Explosion", this, true, () -> mode.is("Basic"));
    public final NumberSetting pushXZ = new NumberSetting("Push", this, 1.0, 10.0, 1.1, 0.1, () -> mode.is("Push"));
    public final NumberSetting pushStart = new NumberSetting("PushStart", this, 1.0, 10.0, 9.0, 1, () -> mode.is("Push"));
    public final NumberSetting pushEnd = new NumberSetting("PushEnd", this, 1.0, 10.0, 2.0, 2.0, () -> mode.is("Push"));
    public final NumberSetting reverseStart = new NumberSetting("ReverseStart", this, 1.0, 10.0, 9.0, 1, () -> mode.is("Reverse"));
    public final NumberSetting karhuStart = new NumberSetting("TickZero", this, 1.0, 10.0, 4.0, 1, () -> mode.is("TickZero"));
    public final BooleanSetting tickZeroY = new BooleanSetting("TickZeroY", this, false, () -> mode.is("TickZero"));
    public final BooleanSetting reverseStrafe = new BooleanSetting("ReverseStrafe", this, false, () -> mode.is("Reverse"));
    public final BooleanSetting pushOnGround = new BooleanSetting("OnGround", this, false, () -> mode.is("Push"));
    public final BooleanSetting hitBug = new BooleanSetting("HitBug", this, false, () -> mode.is("Basic"));

    public Vec3 position = new Vec3(0.0, 0.0, 0.0);
    private int counter = 0;
    private int grimTCancel = 0;
    private boolean grimFlag;
    private double posY;
    private double posZ;
    private double posX;



    @Handler
    public final void packet(final EventPacket event) {
        if(event.getType() == EventPacket.Type.RECEIVE) {
            Packet packet = event.getPacket();
            if (this.mode.getCurrent().equalsIgnoreCase("IntaveTest")) {
                this.posX = MC.thePlayer.posX;
                this.posY = MC.thePlayer.posY;
                this.posZ = MC.thePlayer.posZ;
            }

            Packet p;
            if (this.mode.is("Grim") && !Scaffold.getInstance().isToggled()) {
                p = event.getPacket();
                if (p instanceof S12PacketEntityVelocity && ((S12PacketEntityVelocity)p).getEntityID() == MC.thePlayer.getEntityId()) {
                    event.setCancelled(true);
                    this.grimTCancel = 6;
                }

                if (p instanceof S32PacketConfirmTransaction) {
                    event.setCancelled(true);
                    --this.grimTCancel;
                }
            }

            if (this.mode.getCurrent().equalsIgnoreCase("Basic")) {
                p = event.getPacket();
                if (p instanceof S12PacketEntityVelocity && ((S12PacketEntityVelocity)p).getEntityID() == MC.thePlayer.getEntityId()) {
                    if (!(this.XZValue.getAsDouble() > 0.0) && !(this.YValue.getAsFloat() > 0.0)) {
                        event.setCancelled(true);
                    } else {
                        ((S12PacketEntityVelocity)p).setMotionX((int)((double)((S12PacketEntityVelocity)p).getMotionX() * this.XZValue.getAsDouble() / 100.0));
                        ((S12PacketEntityVelocity)p).setMotionY((int)((double)((S12PacketEntityVelocity)p).getMotionY() * this.YValue.getAsDouble() / 100.0));
                        ((S12PacketEntityVelocity)p).setMotionZ((int)((double)((S12PacketEntityVelocity)p).getMotionZ() * this.XZValue.getAsDouble() / 100.0));
                    }
                }

                if (p instanceof S27PacketExplosion && this.ignoreExplosion.get()) {
                    if (!(this.XZValue.getAsDouble() > 0.0) && !(this.YValue.getAsDouble() > 0.0)) {
                        event.setCancelled(true);
                    } else {
                        ((S27PacketExplosion)p).setField_149152_f((float)((int)((double)((S27PacketExplosion)p).getField_149152_f() * this.XZValue.getAsDouble() / 100.0)));
                        ((S27PacketExplosion)p).setField_149153_g((float)((int)((double)((S27PacketExplosion)p).getField_149152_f() * this.YValue.getAsDouble() / 100.0)));
                        ((S27PacketExplosion)p).setField_149159_h((float)((int)((double)((S27PacketExplosion)p).getField_149152_f() * this.XZValue.getAsDouble() / 100.0)));
                    }
                }
            }

            if (packet instanceof S29PacketSoundEffect && this.hitBug.get()) {
                S29PacketSoundEffect soundEffect = (S29PacketSoundEffect)packet;
                if (soundEffect.getSoundName().equalsIgnoreCase("game.player.hurt") || soundEffect.getSoundName().equalsIgnoreCase("game.player.die")) {
                    event.setCancelled(true);
                }
            }

            if (packet instanceof S12PacketEntityVelocity && this.mode.getCurrent().equals("Spoof")) {
                S12PacketEntityVelocity s12PacketEntityVelocity = (S12PacketEntityVelocity)packet;
                if (s12PacketEntityVelocity.getEntityID() == MC.thePlayer.getEntityId()) {
                    event.setCancelled(true);
                    MC.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(MC.thePlayer.posX + (double)s12PacketEntityVelocity.getMotionX() / 8000.0, MC.thePlayer.posY + (double)s12PacketEntityVelocity.getMotionY() / 8000.0, MC.thePlayer.posZ + (double)s12PacketEntityVelocity.getMotionZ() / 8000.0, false));
                }
            }

        } else if(event.getType() == EventPacket.Type.SEND) {
            Packet packet = event.getPacket();
            switch (this.mode.getCurrent()) {
                case "IntaveTest":
                    if (MC.thePlayer.hurtTime > 7 && packet instanceof C0FPacketConfirmTransaction) {
                        event.setCancelled(true);
                    }
                    break;
                case "Grim2":
                    if (MC.thePlayer.hurtTime != 0) {
                        ((C03PacketPlayer)packet).setY(MC.thePlayer.posY - 2.0);
                    }
            }
        }
    }

    @Handler
    public void onEventSilentMove(EventSilentMove eventSilentMove) {
        switch (this.mode.getCurrent()) {
            case "BuzzReverse":
                try {
                    if (MC.thePlayer.hurtTime == 7) {
                        MoveUtil.multiplyXZ(-1.0);
                    }
                } catch (Exception var12) {
                }
                break;
            case "Legit":
                if (MC.thePlayer.hurtTime > 0 && KillAura.getInstance().isToggled()) {
                    if (AuraUtil.getTarget() != null) {
                        ArrayList<Vec3> vec3s = new ArrayList();
                        HashMap<Vec3, Integer> map = new HashMap();
                        Vec3 playerPos = new Vec3(MC.thePlayer.posX, MC.thePlayer.posY, MC.thePlayer.posZ);
                        Vec3 onlyForward = PlayerUtil.getPredictedPos(false, AuraUtil.getTarget(), 1.0F, 0.0F).add(playerPos);
                        Vec3 strafeLeft = PlayerUtil.getPredictedPos(false, AuraUtil.getTarget(), 1.0F, 1.0F).add(playerPos);
                        Vec3 strafeRight = PlayerUtil.getPredictedPos(false, AuraUtil.getTarget(), 1.0F, -1.0F).add(playerPos);
                        map.put(onlyForward, 0);
                        map.put(strafeLeft, 1);
                        map.put(strafeRight, -1);
                        vec3s.add(onlyForward);
                        vec3s.add(strafeLeft);
                        vec3s.add(strafeRight);
                        Vec3 targetVec = new Vec3(AuraUtil.getTarget().posX, AuraUtil.getTarget().posY, AuraUtil.getTarget().posZ);
                        targetVec.getClass();
                        vec3s.sort(Comparator.comparingDouble(targetVec::distanceXZTo));
                        if (!MC.thePlayer.movementInput.sneak) {
                            MC.thePlayer.movementInput.moveStrafe = (float)(Integer)map.get(vec3s.get(0));
                        }
                    }
                }
                break;
            case "Intave":
                if (this.jumpIntave.get() && MC.thePlayer.hurtTime == 9 && MC.thePlayer.onGround && this.counter++ % 2 == 0) {
                    MC.thePlayer.movementInput.jump = true;
                    if (Velocity.getInstance().isToggled() && Velocity.getInstance().mode.is("Intave") && Minecraft.getMinecraft().thePlayer.hurtTime > 0 && MC.currentScreen != null) {
                        MC.thePlayer.motionX *= Velocity.getInstance().XZValueIntave.getAsFloat();
                        MC.thePlayer.motionZ *= Velocity.getInstance().XZValueIntave.getAsFloat();
                        MC.thePlayer.setSprinting(false);
                    }
                }
                break;
            case "Test":
                if (MC.thePlayer.hurtTime > 2) {
                    MoveUtil.setSpeed(0.01F);
                    if (MC.thePlayer.hurtTime == 9 && MC.thePlayer.onGround) {
                        MC.thePlayer.movementInput.jump = true;
                    }
                }
        }

    }
    @Handler
    public final void earlyTick(final EventPostTick eventTick) {
        if (this.mode.getCurrent().equalsIgnoreCase("IntaveTest") && MC.thePlayer.hurtTime == 8) {
            MC.thePlayer.setPosition(this.posX, this.posY, this.posZ);
        }

        if (this.mode.getCurrent().equals("Reverse") && this.reverseStrafe.get() && (double)MC.thePlayer.hurtTime <= this.reverseStart.getAsDouble() && MC.thePlayer.hurtTime > 0) {
            MoveUtil.strafe();
        }
    }
    @Handler
    public final void earlyTick(final EventTick eventTick) {
        this.setTag(mode.getCurrent() );
    }

    @Handler
    public final void tick(final EventTick event) {
        switch (this.mode.getCurrent()) {
            case "Grim2":
                if (MC.thePlayer.hurtTime != 0) {
                    MC.thePlayer.setPosition(MC.thePlayer.lastTickPosX, MC.thePlayer.lastTickPosY, MC.thePlayer.lastTickPosZ);
                }
                break;
            case "TickZero":
                if ((double)MC.thePlayer.hurtTime == this.karhuStart.getAsDouble()) {
                    MoveUtil.multiplyXZ(0.0);
                    MC.thePlayer.motionY = this.tickZeroY.get() ? 0.0 : MC.thePlayer.motionY;
                }
                break;
            case "PushGround":
                this.pushGround();
                break;
            case "Push":
                this.push();
                break;
            case "Reverse":
                this.reverse();
        }
    }

    private void reverse() {
        if ((double)MC.thePlayer.hurtTime == this.reverseStart.getAsDouble()) {
            EntityPlayerSP var10000 = MC.thePlayer;
            var10000.motionX *= -1.0;
            var10000 = MC.thePlayer;
            var10000.motionZ *= -1.0;
            if (this.reverseStrafe.get() && (double)MC.thePlayer.hurtTime <= this.reverseStart.getAsDouble() && MC.thePlayer.hurtTime > 0) {
                MoveUtil.strafe();
            }
        }

    }

    private void push() {
        if ((double)MC.thePlayer.hurtTime <= Math.max(this.pushStart.getAsDouble(), this.pushEnd.getAsDouble()) && (double)MC.thePlayer.hurtTime >= Math.min(this.pushStart.getAsDouble(), this.pushEnd.getAsDouble())) {
            MC.thePlayer.moveFlying(0.0F, 0.98F, (float)(this.pushXZ.getAsDouble() / 100.0));
            if (this.pushOnGround.get()) {
                MC.thePlayer.onGround = true;
            }
        }

    }

    private void pushGround() {
        if (MC.thePlayer.hurtTime > 0) {
            MC.thePlayer.onGround = true;
        }

    }
    public static Velocity getInstance() {
        return Mega.INSTANCE.getModuleManager().getModule(Velocity.class);
    }
}
