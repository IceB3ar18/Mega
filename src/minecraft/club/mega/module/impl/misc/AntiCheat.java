package club.mega.module.impl.misc;

import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.util.ChatUtil;
import club.mega.util.TimeUtil;
import club.mega.util.WorldUtil;
import org.lwjgl.input.Keyboard;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "AntiCheat", description = "Detects cheaters", category = Category.MISC)
public class AntiCheat extends Module {
    public final BooleanSetting verbose = new BooleanSetting("AlertChat", this, true);
    private final TimeUtil timer = new TimeUtil();

    @Handler
    public final void onTick(final EventTick eventTick) {
        // Grundlegende Überprüfungen, erweiterte Überprüfungen werden anders behandelt
        if (MC.theWorld == null) {
            return;
        }

        for (Object t : MC.theWorld.loadedEntityList) {
            if (t instanceof EntityPlayer) {
                final EntityPlayer p = (EntityPlayer) t;
                final String name = p.getName();

                if (!p.isRiding() && p.ticksExisted > 10) {
                    final double x = p.posX, y = p.posY, z = p.posZ;
                    final double lx = p.lastTickPosX, ly = p.lastTickPosY, lz = p.lastTickPosZ;
                    final double yDiff = y - ly;

                    final boolean groundReal = isPlayerOnGround(p);

                    final float deltaYaw = Math.abs(p.rotationYaw - p.prevRotationYaw);

                    if (deltaYaw > 1.5f) {
                        // Handle large yaw changes if needed
                    }

                    if (getYMotionOfEntity(p) > 0.53) {
                        handleFlag(name + " flagged impossible Y value: " + getYMotionOfEntity(p));
                    }

                    if (yDiff > 1.5) {
                        handleFlag(name + " flagged basic fly! : " + Math.random());
                    } else if (yDiff < -3.5) {
                        // Handle fast fall if needed
                    }

                    if (getSpeedOfEntity(p) > 1) {
                        handleFlag(name + " speed is too high! Speed of " + getSpeedOfEntity(p));
                    }

                    if (!p.onGround && !groundReal && !isPlayerInLiquid(p) && p.lastTickPosX != p.posX && p.lastTickPosZ != p.posZ) {
                        if (p.lastTickPosY == p.posY) {
                            handleFlag(name + " is not abiding by gravity!");
                        }
                    }

                    if (veloCheckXZ(p)) {
                        handleFlag(name + " flagged Velocity on the XZ axis!");
                    }

                    if (veloCheckY(p)) {
                        handleFlag(name + " flagged Velocity on the Y axis!");
                    }
                }
            }
        }
    }

    private boolean veloCheckXZ(final EntityPlayer e) {
        if (e.hurtTime < 10 && e.hurtTime > 0) {
            final double diffX = e.posX - e.lastTickPosX;
            final double diffZ = e.posZ - e.lastTickPosZ;
            return diffX > 0.9 || diffX < -0.9 || diffZ > 0.9 || diffZ < -0.9;
        }
        return false;
    }

    private boolean veloCheckY(final EntityPlayer e) {
        if (e.hurtTime < 10 && e.hurtTime > 0) {
            return e.onGround && Math.abs(e.motionY) < 0.1;
        }
        return false;
    }

    public static double getSpeedOfEntity(final EntityPlayer e) {
        final double deltaX = e.posX - e.lastTickPosX;
        final double deltaZ = e.posZ - e.lastTickPosZ;
        return Math.hypot(deltaX, deltaZ);
    }

    public static double getYMotionOfEntity(final EntityPlayer e) {
        final double mot = e.posY - e.lastTickPosY;
        return mot - 0.0784000015258789;
    }

    private boolean isPlayerOnGround(EntityPlayer player) {
        BlockPos playerPos = new BlockPos(player.posX, player.posY - 0.5, player.posZ);
        return WorldUtil.getBlock(playerPos) instanceof BlockAir;
    }

    private boolean isPlayerInLiquid(EntityPlayer player) {
        BlockPos playerPos = new BlockPos(player.posX, player.posY - 0.5, player.posZ);
        return WorldUtil.getBlock(playerPos) instanceof BlockLiquid;
    }

    private void handleFlag(String flag) {
        if (verbose.get()) {
            if (timer.hasTimePassed(1500)) {
                timer.reset();
                MC.thePlayer.sendChatMessage(flag);
            }
        } else {
            ChatUtil.sendMessage(flag);
        }
    }
}
