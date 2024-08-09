package club.mega.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

public class SigmaMoveUtils {
    private static Minecraft mc = Minecraft.getMinecraft();

    public SigmaMoveUtils() {
    }

    public static double defaultSpeed() {
        double baseSpeed = 0.2873;
        if (Minecraft.getMinecraft().thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amplifier = Minecraft.getMinecraft().thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (double)(amplifier + 1);
        }

        return baseSpeed;
    }

    public static void strafe(double speed) {
        float a = mc.thePlayer.rotationYaw * 0.017453292F;
        float l = mc.thePlayer.rotationYaw * 0.017453292F - 4.712389F;
        float r = mc.thePlayer.rotationYaw * 0.017453292F + 4.712389F;
        float rf = mc.thePlayer.rotationYaw * 0.017453292F + 0.5969026F;
        float lf = mc.thePlayer.rotationYaw * 0.017453292F + -0.5969026F;
        float lb = mc.thePlayer.rotationYaw * 0.017453292F - 2.3876104F;
        float rb = mc.thePlayer.rotationYaw * 0.017453292F - -2.3876104F;
        EntityPlayerSP var10000;
        if (mc.gameSettings.keyBindForward.pressed) {
            if (mc.gameSettings.keyBindLeft.pressed && !mc.gameSettings.keyBindRight.pressed) {
                var10000 = mc.thePlayer;
                var10000.motionX -= (double)MathHelper.sin(lf) * speed;
                var10000 = mc.thePlayer;
                var10000.motionZ += (double)MathHelper.cos(lf) * speed;
            } else if (mc.gameSettings.keyBindRight.pressed && !mc.gameSettings.keyBindLeft.pressed) {
                var10000 = mc.thePlayer;
                var10000.motionX -= (double)MathHelper.sin(rf) * speed;
                var10000 = mc.thePlayer;
                var10000.motionZ += (double)MathHelper.cos(rf) * speed;
            } else {
                var10000 = mc.thePlayer;
                var10000.motionX -= (double)MathHelper.sin(a) * speed;
                var10000 = mc.thePlayer;
                var10000.motionZ += (double)MathHelper.cos(a) * speed;
            }
        } else if (mc.gameSettings.keyBindBack.pressed) {
            if (mc.gameSettings.keyBindLeft.pressed && !mc.gameSettings.keyBindRight.pressed) {
                var10000 = mc.thePlayer;
                var10000.motionX -= (double)MathHelper.sin(lb) * speed;
                var10000 = mc.thePlayer;
                var10000.motionZ += (double)MathHelper.cos(lb) * speed;
            } else if (mc.gameSettings.keyBindRight.pressed && !mc.gameSettings.keyBindLeft.pressed) {
                var10000 = mc.thePlayer;
                var10000.motionX -= (double)MathHelper.sin(rb) * speed;
                var10000 = mc.thePlayer;
                var10000.motionZ += (double)MathHelper.cos(rb) * speed;
            } else {
                var10000 = mc.thePlayer;
                var10000.motionX += (double)MathHelper.sin(a) * speed;
                var10000 = mc.thePlayer;
                var10000.motionZ -= (double)MathHelper.cos(a) * speed;
            }
        } else if (mc.gameSettings.keyBindLeft.pressed && !mc.gameSettings.keyBindRight.pressed && !mc.gameSettings.keyBindForward.pressed && !mc.gameSettings.keyBindBack.pressed) {
            var10000 = mc.thePlayer;
            var10000.motionX += (double)MathHelper.sin(l) * speed;
            var10000 = mc.thePlayer;
            var10000.motionZ -= (double)MathHelper.cos(l) * speed;
        } else if (mc.gameSettings.keyBindRight.pressed && !mc.gameSettings.keyBindLeft.pressed && !mc.gameSettings.keyBindForward.pressed && !mc.gameSettings.keyBindBack.pressed) {
            var10000 = mc.thePlayer;
            var10000.motionX += (double)MathHelper.sin(r) * speed;
            var10000 = mc.thePlayer;
            var10000.motionZ -= (double)MathHelper.cos(r) * speed;
        }

    }

    public static void setMotion(double speed) {
        double forward = (double)mc.thePlayer.movementInput.moveForward;
        double strafe = (double)mc.thePlayer.movementInput.moveStrafe;
        float yaw = mc.thePlayer.rotationYaw;
        if (forward == 0.0 && strafe == 0.0) {
            mc.thePlayer.motionX = 0.0;
            mc.thePlayer.motionZ = 0.0;
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (float)(forward > 0.0 ? -45 : 45);
                } else if (strafe < 0.0) {
                    yaw += (float)(forward > 0.0 ? 45 : -45);
                }

                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }

            mc.thePlayer.motionX = forward * speed * Math.cos(Math.toRadians((double)(yaw + 90.0F))) + strafe * speed * Math.sin(Math.toRadians((double)(yaw + 90.0F)));
            mc.thePlayer.motionZ = forward * speed * Math.sin(Math.toRadians((double)(yaw + 90.0F))) - strafe * speed * Math.cos(Math.toRadians((double)(yaw + 90.0F)));
        }

    }

    public static boolean checkTeleport(double x, double y, double z, double distBetweenPackets) {
        double var10000 = mc.thePlayer.posX - x;
        var10000 = mc.thePlayer.posY - y;
        var10000 = mc.thePlayer.posZ - z;
        double dist = Math.sqrt(mc.thePlayer.getDistanceSq(x, y, z));
        double nbPackets = (double)(Math.round(dist / distBetweenPackets + 0.49999999999) - 1L);
        double xtp = mc.thePlayer.posX;
        double ytp = mc.thePlayer.posY;
        double ztp = mc.thePlayer.posZ;

        for(int i = 1; (double)i < nbPackets; ++i) {
            double xdi = (x - mc.thePlayer.posX) / nbPackets;
            xtp += xdi;
            double zdi = (z - mc.thePlayer.posZ) / nbPackets;
            ztp += zdi;
            double ydi = (y - mc.thePlayer.posY) / nbPackets;
            ytp += ydi;
            AxisAlignedBB bb = new AxisAlignedBB(xtp - 0.3, ytp, ztp - 0.3, xtp + 0.3, ytp + 1.8, ztp + 0.3);
            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public static boolean isOnGround(double height) {
        return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0, -height, 0.0)).isEmpty();
    }

    public static int getJumpEffect() {
        return mc.thePlayer.isPotionActive(Potion.jump) ? mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1 : 0;
    }

    public static int getSpeedEffect() {
        return mc.thePlayer.isPotionActive(Potion.moveSpeed) ? mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1 : 0;
    }

    public static Block getBlockUnderPlayer(EntityPlayer inPlayer, double height) {
        return Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(inPlayer.posX, inPlayer.posY - height, inPlayer.posZ)).getBlock();
    }

    public static Block getBlockAtPosC(double x, double y, double z) {
        EntityPlayerSP entityPlayerSP = Minecraft.getMinecraft().thePlayer;
        return Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(entityPlayerSP.posX + x, entityPlayerSP.posY + y, entityPlayerSP.posZ + z)).getBlock();
    }

    public static float getDistanceToGround(Entity e) {
        if (mc.thePlayer.isCollidedVertically && mc.thePlayer.onGround) {
            return 0.0F;
        } else {
            for(float a = (float)e.posY; a > 0.0F; --a) {
                int[] stairs = new int[]{53, 67, 108, 109, 114, 128, 134, 135, 136, 156, 163, 164, 180};
                int[] exemptIds = new int[]{6, 27, 28, 30, 31, 32, 37, 38, 39, 40, 50, 51, 55, 59, 63, 65, 66, 68, 69, 70, 72, 75, 76, 77, 83, 92, 93, 94, 104, 105, 106, 115, 119, 131, 132, 143, 147, 148, 149, 150, 157, 171, 175, 176, 177};
                Block block = mc.theWorld.getBlockState(new BlockPos(e.posX, (double)(a - 1.0F), e.posZ)).getBlock();
                if (!(block instanceof BlockAir)) {
                    if (Block.getIdFromBlock(block) != 44 && Block.getIdFromBlock(block) != 126) {
                        int[] arrayOfInt1 = stairs;
                        int j = stairs.length;

                        int i;
                        int id;
                        for(i = 0; i < j; ++i) {
                            id = arrayOfInt1[i];
                            if (Block.getIdFromBlock(block) == id) {
                                return (float)(e.posY - (double)a - 1.0) < 0.0F ? 0.0F : (float)(e.posY - (double)a - 1.0);
                            }
                        }

                        arrayOfInt1 = exemptIds;
                        j = exemptIds.length;

                        for(i = 0; i < j; ++i) {
                            id = arrayOfInt1[i];
                            if (Block.getIdFromBlock(block) == id) {
                                return (float)(e.posY - (double)a) < 0.0F ? 0.0F : (float)(e.posY - (double)a);
                            }
                        }

                        return (float)(e.posY - (double)a + block.getBlockBoundsMaxY() - 1.0);
                    }

                    return (float)(e.posY - (double)a - 0.5) < 0.0F ? 0.0F : (float)(e.posY - (double)a - 0.5);
                }
            }

            return 0.0F;
        }
    }

    public static float[] getRotationsBlock(BlockPos block, EnumFacing face) {
        double x = (double)block.getX() + 0.5 - mc.thePlayer.posX + (double)face.getFrontOffsetX() / 2.0;
        double z = (double)block.getZ() + 0.5 - mc.thePlayer.posZ + (double)face.getFrontOffsetZ() / 2.0;
        double y = (double)block.getY() + 0.5;
        double d1 = mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight() - y;
        double d3 = (double)MathHelper.sqrt_double(x * x + z * z);
        float yaw = (float)(Math.atan2(z, x) * 180.0 / Math.PI) - 90.0F;
        float pitch = (float)(Math.atan2(d1, d3) * 180.0 / Math.PI);
        if (yaw < 0.0F) {
            yaw += 360.0F;
        }

        return new float[]{yaw, pitch};
    }

    public static boolean isBlockAboveHead() {
        AxisAlignedBB bb = new AxisAlignedBB(mc.thePlayer.posX - 0.3, mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ + 0.3, mc.thePlayer.posX + 0.3, mc.thePlayer.posY + 2.5, mc.thePlayer.posZ - 0.3);
        return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty();
    }

    public static boolean isCollidedH(double dist) {
        AxisAlignedBB bb = new AxisAlignedBB(mc.thePlayer.posX - 0.3, mc.thePlayer.posY + 2.0, mc.thePlayer.posZ + 0.3, mc.thePlayer.posX + 0.3, mc.thePlayer.posY + 3.0, mc.thePlayer.posZ - 0.3);
        if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb.offset(0.3 + dist, 0.0, 0.0)).isEmpty()) {
            return true;
        } else if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb.offset(-0.3 - dist, 0.0, 0.0)).isEmpty()) {
            return true;
        } else if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb.offset(0.0, 0.0, 0.3 + dist)).isEmpty()) {
            return true;
        } else {
            return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb.offset(0.0, 0.0, -0.3 - dist)).isEmpty();
        }
    }

    public static boolean isRealCollidedH(double dist) {
        AxisAlignedBB bb = new AxisAlignedBB(mc.thePlayer.posX - 0.3, mc.thePlayer.posY + 0.5, mc.thePlayer.posZ + 0.3, mc.thePlayer.posX + 0.3, mc.thePlayer.posY + 1.9, mc.thePlayer.posZ - 0.3);
        if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb.offset(0.3 + dist, 0.0, 0.0)).isEmpty()) {
            return true;
        } else if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb.offset(-0.3 - dist, 0.0, 0.0)).isEmpty()) {
            return true;
        } else if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb.offset(0.0, 0.0, 0.3 + dist)).isEmpty()) {
            return true;
        } else {
            return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb.offset(0.0, 0.0, -0.3 - dist)).isEmpty();
        }
    }
}
