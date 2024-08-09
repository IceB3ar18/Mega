package club.mega.util;


import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MathHelper;

import static club.mega.interfaces.MinecraftInterface.MC;

public class MoveUtil  {
    public MoveUtil() {
    }
    Minecraft mc = Minecraft.getMinecraft();

    public static void setMotion(double speed, double strafeDegree, float yaw) {
        setMotion(speed, strafeDegree, yaw, false);
    }

    public static void setMotion(double speed, double strafeDegree, float yaw, boolean ignoreSprint) {
        float strafe = MC.thePlayer.moveStrafing;
        float forward = MC.thePlayer.moveForward;
        float friction = (float)speed;
        if (strafe != 0.0F && forward != 0.0F) {
            if (strafe > 0.0F) {
                yaw = (float)(forward > 0.0F ? (double)yaw - strafeDegree : (double)yaw + strafeDegree);
            } else {
                yaw = (float)(forward > 0.0F ? (double)yaw + strafeDegree : (double)yaw - strafeDegree);
            }

            strafe = 0.0F;
        }

        float f1 = MathHelper.sin(yaw * 3.1415927F / 180.0F);
        float f2 = MathHelper.cos(yaw * 3.1415927F / 180.0F);
        MC.thePlayer.motionX = (double)(strafe * friction * f2 - forward * friction * f1);
        MC.thePlayer.motionZ = (double)(forward * friction * f2 + strafe * friction * f1);
        if (MC.thePlayer.isSprinting() && !ignoreSprint) {
            float f = yaw * 0.017453292F;
            EntityPlayerSP var10000 = MC.thePlayer;
            var10000.motionX -= (double)(MathHelper.sin(f) * 0.2F);
            var10000 = MC.thePlayer;
            var10000.motionZ += (double)(MathHelper.cos(f) * 0.2F);
        }

    }

    public static double[] getMotion(double speed, float strafe, float forward, float yaw) {
        float friction = (float)speed;
        float f1 = MathHelper.sin(yaw * 3.1415927F / 180.0F);
        float f2 = MathHelper.cos(yaw * 3.1415927F / 180.0F);
        double motionX = (double)(strafe * friction * f2 - forward * friction * f1);
        double motionZ = (double)(forward * friction * f2 + strafe * friction * f1);
        return new double[]{motionX, motionZ};
    }

    public static void setMotion2(double speed, double strafeDegree, float yaw) {
        float strafe = MC.thePlayer.moveStrafing;
        float forward = MC.thePlayer.moveForward;
        float friction = (float)speed;
        if (strafe != 0.0F && forward != 0.0F) {
            if (strafe > 0.0F) {
                yaw = (float)(forward > 0.0F ? (double)yaw - strafeDegree : (double)yaw + strafeDegree);
            } else {
                yaw = (float)(forward > 0.0F ? (double)yaw + strafeDegree : (double)yaw - strafeDegree);
            }

            strafe = 0.0F;
        }

        float f1 = MathHelper.sin(yaw * 3.1415927F / 180.0F);
        float f2 = MathHelper.cos(yaw * 3.1415927F / 180.0F);
        MC.thePlayer.motionX = (double)(strafe * friction * f2 - forward * friction * f1);
        MC.thePlayer.motionZ = (double)(forward * friction * f2 + strafe * friction * f1);
    }

    public static boolean isMoving() {
        return MC.thePlayer.moveForward != 0.0F || MC.thePlayer.moveStrafing != 0.0F && !MC.thePlayer.isCollidedHorizontally;
    }

    public static void setSpeed(float f2) {
        MC.thePlayer.motionX = -(Math.sin(direction()) * (double)f2);
        MC.thePlayer.motionZ = Math.cos(direction()) * (double)f2;
    }

    public static void setSpeed(float f2, boolean strafe) {
        double d = Math.toRadians((double)getYaw(strafe));
        MC.thePlayer.motionX = -(Math.sin(d) * (double)f2);
        MC.thePlayer.motionZ = Math.cos(d) * (double)f2;
    }

    public static void strafe(double d) {
        if (isMoving()) {
            double direction = direction();
            MC.thePlayer.motionX = -Math.sin(direction) * d;
            MC.thePlayer.motionZ = Math.cos(direction) * d;
        }

    }

    public static void strafe() {
        strafe(getSpeed());
    }

    public static void strafeMatrix() {
        double speed = getSpeed();
        if (isMoving()) {
            float f = getYaw();
            if (MC.thePlayer.moveForward < 0.0F) {
                f += 180.0F;
            } else {
                float f2 = 1.0F;
                if (MC.thePlayer.moveForward < 0.0F) {
                    f2 = -0.5F;
                } else if (MC.thePlayer.moveForward > 0.0F) {
                    f2 = 0.5F;
                }

                if (MC.thePlayer.moveStrafing > 0.0F) {
                    f -= 90.0F * f2;
                }

                if (MC.thePlayer.moveStrafing < 0.0F) {
                    f += 90.0F * f2;
                }
            }

            double direction = Math.toRadians((double)f);
            MC.thePlayer.motionX = -Math.sin(direction) * speed;
            MC.thePlayer.motionZ = Math.cos(direction) * speed;
        }

    }

    public static double getSpeed() {
        return Math.sqrt(MC.thePlayer.motionX * MC.thePlayer.motionX + MC.thePlayer.motionZ * MC.thePlayer.motionZ);
    }

    public static double direction() {
        float f = getYaw();
        if (MC.thePlayer.moveForward < 0.0F) {
            f += 180.0F;
        }

        float f2 = 1.0F;
        if (MC.thePlayer.moveForward < 0.0F) {
            f2 = -0.5F;
        } else if (MC.thePlayer.moveForward > 0.0F) {
            f2 = 0.5F;
        }

        if (MC.thePlayer.moveStrafing > 0.0F) {
            f -= 90.0F * f2;
        }

        if (MC.thePlayer.moveStrafing < 0.0F) {
            f += 90.0F * f2;
        }

        return Math.toRadians((double)f);
    }

    public static void addSpeed(double speed, boolean strafe) {
        float f = getYaw(strafe) * 0.017453292F;
        EntityPlayerSP var10000 = MC.thePlayer;
        var10000.motionX -= (double)MathHelper.sin(f) * speed;
        var10000 = MC.thePlayer;
        var10000.motionZ += (double)MathHelper.cos(f) * speed;
    }

    public static float getYaw() {
        float var0 = MC.thePlayer.rotationYawHead;

        return var0;
    }

    public static float getYaw(boolean strafe) {
        return strafe ? (float)Math.toDegrees(direction()) : getYaw();
    }

    public static void setSpeed2(double speed) {
        EntityPlayerSP player = MC.thePlayer;
        double yaw = (double)getYaw();
        boolean isMoving = player.moveForward != 0.0F || player.moveStrafing != 0.0F;
        boolean isMovingForward = player.moveForward > 0.0F;
        boolean isMovingBackward = player.moveForward < 0.0F;
        boolean isMovingRight = player.moveStrafing > 0.0F;
        boolean isMovingLeft = player.moveStrafing < 0.0F;
        boolean isMovingSideways = isMovingLeft || isMovingRight;
        boolean isMovingStraight = isMovingForward || isMovingBackward;
        if (isMoving) {
            if (isMovingForward && !isMovingSideways) {
                yaw += 0.0;
            } else if (isMovingBackward && !isMovingSideways) {
                yaw += 180.0;
            } else if (isMovingForward && isMovingLeft) {
                yaw += 45.0;
            } else if (isMovingForward) {
                yaw -= 45.0;
            } else if (!isMovingStraight && isMovingLeft) {
                yaw += 90.0;
            } else if (!isMovingStraight && isMovingRight) {
                yaw -= 90.0;
            } else if (isMovingBackward && isMovingLeft) {
                yaw += 135.0;
            } else if (isMovingBackward) {
                yaw -= 135.0;
            }

            yaw = Math.toRadians(yaw);
            player.motionX = -Math.sin(yaw) * speed;
            player.motionZ = Math.cos(yaw) * speed;
        }

    }

    public static void multiplyXZ(double v) {
        MC.thePlayer.motionX *= v;
        MC.thePlayer.motionZ *= v;
    }
    public void increaseSpeedWithStrafe(final double speed) {
        mc.thePlayer.motionX = -MathHelper.sin(getDirection()) * speed;
        mc.thePlayer.motionZ = MathHelper.cos(getDirection()) * speed;
    }

    public float getDirection() {
        float yaw = mc.thePlayer.rotationYaw;
        final float forward = mc.thePlayer.moveForward;
        final float strafe = mc.thePlayer.moveStrafing;
        yaw += ((forward < 0.0f) ? 180 : 0);
        final int i = (forward < 0.0f) ? -45 : ((forward == 0.0f) ? 90 : 45);
        if (strafe < 0.0f) {
            yaw += i;
        }
        if (strafe > 0.0f) {
            yaw -= i;
        }
        return yaw * 0.017453292f;
    }



}
