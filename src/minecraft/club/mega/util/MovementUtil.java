package club.mega.util;

import club.mega.event.impl.EventMovePlayer;
import club.mega.interfaces.MinecraftInterface;

public final class MovementUtil implements MinecraftInterface {

    public static void setSpeed(final EventMovePlayer event, final double speed) {
        setSpeed(event, speed, MC.thePlayer.moveForward, MC.thePlayer.moveStrafing, MC.thePlayer.rotationYaw);
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
    public static void strafe() {
        strafe(getSpeed());
    }
    public static void strafe(double d) {
        if (isMoving()) {
            double direction = direction();
            MC.thePlayer.motionX = -Math.sin(direction) * d;
            MC.thePlayer.motionZ = Math.cos(direction) * d;
        }

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

    public static float getYaw() {
        return MC.thePlayer.rotationYawHead;
    }
    public static double getSpeed() {
        return Math.sqrt(MC.thePlayer.motionX * MC.thePlayer.motionX + MC.thePlayer.motionZ * MC.thePlayer.motionZ);
    }

    public static void setSpeed(final EventMovePlayer e, final double speed, final float forward, final float strafing, float yaw) {
        if (forward == 0.0F && strafing == 0.0F) return;
        final boolean reversed = forward < 0.0f;
        float strafingYaw = 90.0f * (forward > 0.0f ? 0.5f : reversed ? -0.5f : 1.0f);
        if (reversed) yaw += 180.0f;
        if (strafing > 0.0f) yaw -= strafingYaw;
        else if (strafing < 0.0f) yaw += strafingYaw;
        final double x = Math.cos(StrictMath.toRadians(yaw + 90.0f));
        final double z = Math.cos(StrictMath.toRadians(yaw));
        e.setX(x * speed);
        e.setZ(z * speed);
    }
    

    public static boolean isMoving() {
        return MC.thePlayer.moveForward != 0 || MC.thePlayer.moveStrafing != 0;
    }

    public static void jump() {
        if (isMoving() && MC.thePlayer.onGround)
            MC.thePlayer.jump();
    }
}
