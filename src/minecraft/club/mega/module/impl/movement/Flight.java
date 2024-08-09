package club.mega.module.impl.movement;

import club.mega.event.impl.*;
import club.mega.module.Category;
import club.mega.module.Module;

import club.mega.module.setting.impl.*;
import club.mega.util.*;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "Flight", description = "Flight", category = Category.MOVEMENT)
public class Flight extends Module {
    public int count;
    boolean verusdmg = false;
    boolean hasJumped = false;
    private double startY = 0.0;
    private double jumpGround;
    private double oldY;
    private double x;
    private double z;
    private final ListSetting mode = new ListSetting("Mode", this, new String[]{"Vanilla", "Motion", "AirJump", "Verus", "Verus2", "SkycaveBad", "Teleport", "FurtherTeleport", "Test", "SilentACAbuse", "Karhu", "VulcanGlide", "Grim", "Packet", "Intave12"});
    private final BooleanSetting autoJump = new BooleanSetting("AutoJump", this, true, () -> mode.is("AirJump"));
    private final BooleanSetting sendOnGroundPacket = new BooleanSetting("OnGroundPacket", this, true, () -> mode.is("AirJump"));
    private final NumberSetting speed = new NumberSetting("Speed", this, 0.1, 9, 1, 0.1, () -> !mode.is("Verus"));
    private final NumberSetting verusspeed = new NumberSetting("VerusSpeed", this, 0.1, 9, 1, 0.1, () -> mode.is("Verus"));
    private final BooleanSetting onWorld = new BooleanSetting("DisableOnWorld", this, false);

    @Handler
    public final void earlyTick(final EventTick eventTick) {
        this.setTag(mode.getCurrent() );
        double multiplier;
        switch (this.mode.getCurrent()) {
            case "Intave12":
                if (MC.thePlayer.posY == (double)((int)MC.thePlayer.posY) && !(MC.thePlayer.motionY > 0.0)) {
                    MC.thePlayer.onGround = true;
                    MC.thePlayer.noClip = true;
                    MC.thePlayer.motionY = 0.0;
                    MC.gameSettings.keyBindJump.pressed = false;
                    if (MC.thePlayer.ticksExisted % 2 == 0) {
                        multiplier = 0.01;
                        this.z = MC.thePlayer.posZ + (double) MathHelper.cos((float)MoveUtil.direction()) * multiplier * this.speed.getAsFloat();
                        this.x = MC.thePlayer.posX + (double)(-MathHelper.sin((float)MoveUtil.direction())) * multiplier * this.speed.getAsFloat();
                        MC.thePlayer.setPosition(this.x, MC.thePlayer.posY, this.z);
                    }
                    break;
                }

                return;
            case "Karhu":
                if ((double)MC.thePlayer.fallDistance >= 0.05) {
                    MC.thePlayer.motionY = 0.05;
                    MC.thePlayer.fallDistance = 0.0F;
                }
                break;
            case "VulcanGlide":
                if (MC.thePlayer.fallDistance > 2.0F) {
                    MC.thePlayer.motionY = 0.0;
                    MC.thePlayer.onGround = true;
                    MC.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
                    MC.thePlayer.fallDistance = 0.0F;
                }
                MC.thePlayer.motionY = MC.thePlayer.ticksExisted % 2 == 0 ? -0.17 : -0.1;

                break;
            case "Motion":
            case "SilentACAbuse":
                MC.thePlayer.motionY = 0.0;
                if (MC.gameSettings.keyBindJump.pressed) {
                    MC.thePlayer.motionY = this.speed.getAsFloat();
                }

                if (MC.gameSettings.keyBindSneak.pressed) {
                    MC.thePlayer.motionY = -this.speed.getAsFloat();
                }

                if (MoveUtil.isMoving()) {
                    MoveUtil.setSpeed((float)this.speed.getAsFloat());
                } else {
                    MoveUtil.setSpeed(0.0F);
                }
                break;
            case "Test":
                MoveUtil.multiplyXZ(0.0);
                MC.thePlayer.motionY = 0.0;
                multiplier = 0.01;
                if (MC.thePlayer.ticksExisted % 3 == 0) {
                    this.z = MC.thePlayer.posZ + (double)MathHelper.cos((float)MoveUtil.direction()) * multiplier * this.speed.getAsFloat();
                    this.x = MC.thePlayer.posX + (double)(-MathHelper.sin((float)MoveUtil.direction())) * multiplier * this.speed.getAsFloat();
                }

                MC.thePlayer.setPosition(this.x, this.oldY, this.z);
                if (MC.gameSettings.keyBindJump.pressed) {
                    this.oldY += multiplier * this.speed.getAsFloat();
                } else if (MC.gameSettings.keyBindSneak.pressed) {
                    this.oldY -= multiplier * this.speed.getAsFloat();
                }
                break;
            case "Vanilla":
                MC.thePlayer.capabilities.isFlying = true;
                MC.thePlayer.capabilities.setFlySpeed((float)this.speed.getAsFloat());
                break;
            case "Verus":
                if (MC.thePlayer.hurtTime != 0) {
                    this.verusdmg = true;
                }

                if (!this.verusdmg) {
                    MC.thePlayer.motionZ = 0.0;
                    MC.thePlayer.motionX = 0.0;
                    MC.gameSettings.keyBindJump.pressed = false;
                }

                if (this.verusdmg) {
                    MC.getTimer().timerSpeed = 0.3F;
                    if (MC.gameSettings.keyBindJump.pressed) {
                        MC.thePlayer.motionY = 1.5;
                    } else if (MC.gameSettings.keyBindSneak.pressed) {
                        MC.thePlayer.motionY = -1.5;
                    } else {
                        MC.thePlayer.motionY = 0.0;
                    }

                    MC.thePlayer.onGround = true;
                    MoveUtil.setSpeed(this.verusspeed.getAsFloat());
                }
                break;
            case "Teleport":
                MoveUtil.multiplyXZ(0.0);
                MC.thePlayer.motionY = 0.0;
                multiplier = 0.01;
                this.z = MC.thePlayer.posZ + (double)MathHelper.cos((float)MoveUtil.direction()) * multiplier * this.speed.getAsFloat();
                this.x = MC.thePlayer.posX + (double)(-MathHelper.sin((float)MoveUtil.direction())) * multiplier * this.speed.getAsFloat();
                MC.thePlayer.setPositionAndUpdate(this.x, this.oldY, this.z);
                if (MC.gameSettings.keyBindJump.pressed) {
                    this.oldY += multiplier * this.speed.getAsFloat();
                } else if (MC.gameSettings.keyBindSneak.pressed) {
                    this.oldY -= multiplier * this.speed.getAsFloat();
                }
                break;
            case "Packet":
            case "FurtherTeleport":
                MoveUtil.multiplyXZ(0.0);
                MC.thePlayer.motionY = 0.0;
                multiplier = 1.0;
                this.z = MC.thePlayer.posZ + (double)MathHelper.cos((float)MoveUtil.direction()) * multiplier * this.speed.getAsFloat();
                this.x = MC.thePlayer.posX + (double)(-MathHelper.sin((float)MoveUtil.direction())) * multiplier * this.speed.getAsFloat();
                MC.thePlayer.setPositionAndUpdate(this.x, this.oldY, this.z);
                if (MC.gameSettings.keyBindJump.pressed) {
                    this.oldY += multiplier * this.speed.getAsFloat();
                } else if (MC.gameSettings.keyBindSneak.pressed) {
                    this.oldY -= multiplier * this.speed.getAsFloat();
                }
                break;
            case "Verus2":
                multiplier = 0.41999998688697815;
                float constantMotionJumpGroundValue = 0.76F;
                if (MC.thePlayer.onGround) {
                    this.jumpGround = MC.thePlayer.posY;
                    MC.thePlayer.jump();
                }

                if (MC.thePlayer.posY > this.jumpGround + (double)constantMotionJumpGroundValue) {
                    MoveUtil.setMotion(0.35, 45.0, MC.thePlayer.rotationYaw, true);
                    MC.thePlayer.motionY = multiplier;
                    this.jumpGround = MC.thePlayer.posY;
                }
                break;
            case "SkycaveBad":
                if ((double)MC.thePlayer.fallDistance >= 3.8) {
                    MC.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
                    MC.thePlayer.setPosition(MC.thePlayer.posX, MC.thePlayer.posY - MC.thePlayer.motionY, MC.thePlayer.posZ);
                    MC.thePlayer.motionY = 0.8;
                    MoveUtil.setSpeed(this.speed.getAsFloat());
                    MC.thePlayer.fallDistance = 0.0F;
                }
                break;
            case "AirJump":
                if (MC.gameSettings.keyBindJump.isPressed()) {
                    if (!MC.thePlayer.onGround) {
                        MC.thePlayer.onGround = true;
                        if (this.sendOnGroundPacket.get()) {
                            MC.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
                        }
                    }
                } else if (this.autoJump.get() && MC.thePlayer.motionY < -0.44 && !MC.thePlayer.onGround) {
                    MC.thePlayer.jump();
                    if (this.sendOnGroundPacket.get()) {
                        MC.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
                    }
                }
        }

        if (MC.theWorld == null) {
            this.toggle();
        }
    }



    @Override
    public void onEnable() {
        super.onEnable();
        hasJumped = false;

        try {
            this.oldY = MC.thePlayer.posY;
            this.verusdmg = false;
            switch (this.mode.getCurrent()) {
                case "Verus":
                    PlayerUtil.verusdmg();
                    break;
                case "Collide":
                    this.startY = MC.thePlayer.posY;
                    break;
            }

            this.jumpGround = 0.0;
        } catch (NullPointerException var4) {
            var4.printStackTrace();
        }
    }


    @Handler
    public final void packet(final EventPacket packetEvent) {
        if (MC.theWorld != null) {
            if (packetEvent.getType() == EventPacket.Type.SEND) {

                switch (this.mode.getCurrent()) {

                    case "Packet":
                        if (MC.thePlayer.ticksExisted % 2 == 1 && packetEvent.getPacket() instanceof C03PacketPlayer) {
                            ((C03PacketPlayer)packetEvent.getPacket()).setX(MC.thePlayer.posX + 1000.0);
                            ((C03PacketPlayer)packetEvent.getPacket()).setZ(MC.thePlayer.posZ + 1000.0);
                        }
                        break;
                    case "Grim":

                        if (packetEvent.getPacket() instanceof C03PacketPlayer) {

                            ((C03PacketPlayer)packetEvent.getPacket()).setX(MC.thePlayer.posX + 10.0);
                            ((C03PacketPlayer)packetEvent.getPacket()).setZ(MC.thePlayer.posZ + 10.0);
                        }
                        break;
                    case "Test":
                        if (packetEvent.getPacket() instanceof C03PacketPlayer) {
                            ((C03PacketPlayer)packetEvent.getPacket()).setOnGround(false);
                        }
                        break;
                    case "SilentACAbuse":
                        if (packetEvent.getPacket() instanceof C03PacketPlayer) {
                            double newY = this.oldY - MC.thePlayer.posY;
                            double newX = this.x - MC.thePlayer.posX;
                            double newZ = this.z - MC.thePlayer.posZ;
                            double diff = Math.sqrt(newX * newX + newY * newY + newZ * newZ);
                            boolean should = diff >= 8.0;
                            if (!should) {
                                packetEvent.setCancelled(true);
                            }

                            if (should) {
                                this.x = MC.thePlayer.posX;
                                this.oldY = MC.thePlayer.posY;
                                this.z = MC.thePlayer.posZ;
                            }
                        }
                }
            }
        }
    }
    
    @Override
    public void onDisable() {
        super.onDisable();
        hasJumped = false;

        this.verusdmg = false;
        MC.thePlayer.capabilities.isFlying = false;
        MC.getTimer().timerSpeed = 1.0F;
        MC.thePlayer.setSpeedInAir(0.02F);
        switch (this.mode.getCurrent()) {
            case "Vanilla":
                MC.thePlayer.motionX = 0.0;
                MC.thePlayer.motionZ = 0.0;
                break;
            case "Verus":
                MoveUtil.setSpeed(0.0F);
        }
    }

}
