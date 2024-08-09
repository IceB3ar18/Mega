package club.mega.util;

import club.mega.interfaces.MinecraftInterface;
import club.mega.module.impl.combat.KillAura;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class RotationUtil implements MinecraftInterface {

    public float pitch;

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
    private static float[] rotations;
    private static float[] prevRotations;

    public static float[] getRotations() {
        return rotations;
    }

    public static void setRotations(final float[] rotations) {
        RotationUtil.rotations = rotations;
    }

    public static float[] getPrevRotations() {
        return prevRotations;
    }

    public static void setPrevRotations(final float[] rotations) {
        RotationUtil.prevRotations = rotations;
    }

    private static final Random random = new Random();
    public Vec3 getBestVector(Vec3 look, AxisAlignedBB axisAlignedBB) {
        return new Vec3(
                MathHelper.clamp_double(look.xCoord, axisAlignedBB.minX, axisAlignedBB.maxX),
                MathHelper.clamp_double(look.yCoord, axisAlignedBB.minY, axisAlignedBB.maxY),
                MathHelper.clamp_double(look.zCoord, axisAlignedBB.minZ, axisAlignedBB.maxZ)
        );
    }

    public float[] auraRots(Entity entity, float currentYaw, float currentPitch, boolean mouseFix, boolean a3Fix, boolean bestHitVec, float inaccuracy,
                            boolean applyHeuristics, boolean prediction, boolean calcSpeed, float yawSpeed, float pitchSpeed) {

        final double eyeX = MC.thePlayer.posX;
        final double eyeY = MC.thePlayer.posY + MC.thePlayer.getEyeHeight();
        final double eyeZ = MC.thePlayer.posZ;

        Vec3 targetVec;

        if (bestHitVec) {
            targetVec = getBestVector(MC.thePlayer.getPositionEyes(MC.timer.renderPartialTicks), entity.getEntityBoundingBox())
                    .addVector(-inaccuracy / 10, -inaccuracy / 10, -inaccuracy / 10);
        } else {
            targetVec = new Vec3(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
        }

        // Neue Logik zur Behandlung verdeckter Entitäten und Anwendung von Heuristiken
        if (!MC.thePlayer.canEntityBeSeen(entity) && applyHeuristics) {
            AxisAlignedBB boundingBox = entity.getEntityBoundingBox();
            double centerX = (boundingBox.minX + boundingBox.maxX) / 2.0;
            double centerY = (boundingBox.minY + boundingBox.maxY) / 2.0;
            double centerZ = (boundingBox.minZ + boundingBox.maxZ) / 2.0;

            // Zielposition ist der Mittelpunkt der Hitbox
            targetVec = new Vec3(centerX, centerY, centerZ);
        }

        double x = targetVec.xCoord - eyeX;
        double y = targetVec.yCoord - eyeY;
        double z = targetVec.zCoord - eyeZ;

        // Berechnung der Bezierkurve für menschlichere Mausbewegung
        if (!MC.thePlayer.canEntityBeSeen(entity) || applyHeuristics) {
            // Bezierkurve zwischen aktueller Position und Zielposition der Hitbox
            double controlX = eyeX + x * 0.75 + (random.nextDouble() - 0.5) * inaccuracy * 0.5; // Adjusted randomness
            double controlY = eyeY + y * 0.75 + (random.nextDouble() - 0.5) * inaccuracy * 0.5; // Adjusted randomness
            double controlZ = eyeZ + z * 0.75 + (random.nextDouble() - 0.5) * inaccuracy * 0.5; // Adjusted randomness

            double bezierX = controlX + (targetVec.xCoord - controlX) * random.nextDouble() * 0.5; // Adjusted randomness
            double bezierY = controlY + (targetVec.yCoord - controlY) * random.nextDouble() * 0.5; // Adjusted randomness
            double bezierZ = controlZ + (targetVec.zCoord - controlZ) * random.nextDouble() * 0.5; // Adjusted randomness

            x = bezierX - eyeX;
            y = bezierY - eyeY;
            z = bezierZ - eyeZ;
        }

        // Fortsetzen der bestehenden Berechnungen
        if (prediction) {
            boolean sprinting = entity.isSprinting();
            boolean sprintingPlayer = MC.thePlayer.isSprinting();

            float walkingSpeed = 0.10000000149011612f;

            float sprintMultiplication = sprinting ? 1.25f : walkingSpeed;
            float sprintMultiplicationPlayer = sprintingPlayer ? 1.25f : walkingSpeed;

            float xMultiplication = (float) ((entity.posX - entity.prevPosX) * sprintMultiplication);
            float zMultiplication = (float) ((entity.posZ - entity.prevPosZ) * sprintMultiplication);

            float xMultiplicationPlayer = (float) ((MC.thePlayer.posX - MC.thePlayer.prevPosX) * sprintMultiplicationPlayer);
            float zMultiplicationPlayer = (float) ((MC.thePlayer.posZ - MC.thePlayer.prevPosZ) * sprintMultiplicationPlayer);

            if (xMultiplication != 0.0f && zMultiplication != 0.0f || xMultiplicationPlayer != 0.0f && zMultiplicationPlayer != 0.0f) {
                x += xMultiplication + xMultiplicationPlayer;
                z += zMultiplication + zMultiplicationPlayer;
            }
        }

        float angle = MathHelper.sqrt_double(x * x + z * z);
        float calcYaw = (float) (MathHelper.atan2(z, x) * 180.0D / Math.PI) - 90.0F;
        float calcPitch = (float) (-(MathHelper.atan2(y, angle) * 180.0D / Math.PI));

        double rangeToEntity = MC.thePlayer.getDistanceToEntity(entity);
        double rangeSetting = KillAura.getInstance().range.getAsDouble() + KillAura.getInstance().preRange.getAsDouble() + 1;

        double rotationDelta = Math.hypot(MC.thePlayer.rotationYaw - calcYaw, MC.thePlayer.rotationPitch - calcPitch);
        double speed = rotationDelta * ((rangeSetting - rangeToEntity) / rangeSetting) * 0.6; // Increase the speed multiplier
        float yaw = updateRotation(currentYaw, calcYaw, calcSpeed ? (float) speed : yawSpeed);
        float pitch = updateRotation(currentPitch, calcPitch, calcSpeed ? (float) speed : pitchSpeed);

        if (!mouseFix)
            return new float[]{yaw, clampPitch(pitch)};

        final float[] mouseSensitivity = applyMouseSensitivity(yaw, pitch, a3Fix);
        return new float[]{mouseSensitivity[0], clampPitch(mouseSensitivity[1])};
    }



    public float clampPitch(float pitch) {
        return MathHelper.clamp_float(pitch, -90, 90);
    }

    public float[] applyMouseSensitivity(float yaw, float pitch, boolean a3) {
        float sensitivity = MC.gameSettings.mouseSensitivity;
        if (sensitivity == 0) {
            sensitivity = 0.0070422534F; // 1% Sensitivity to fix 0.0 sensitivity
        }
        sensitivity = Math.max(0.1F, sensitivity);
        int deltaYaw = (int) ((yaw - RotationUtil.getRotations()[0]) / (sensitivity / 2));
        int deltaPitch = (int) ((pitch - RotationUtil.getRotations()[1]) / (sensitivity / 2)) * -1;

        if (a3) {
            deltaYaw -= deltaYaw % 0.5 + 0.25;
            deltaPitch -= deltaPitch % 0.5 + 0.25;
        }
        float f = sensitivity * 0.6F + 0.2F;
        float f1 = f * f * f * 8F;
        float f2 = (float) deltaYaw * f1;
        float f3 = (float) deltaPitch * f1;

        float endYaw = (float) ((double) RotationUtil.getRotations()[0] + (double) f2 * 0.15);
        float endPitch = (float) ((double) RotationUtil.getRotations()[1] - (double) f3 * 0.15);
        return new float[]{endYaw, endPitch};
    }

    public float[] backRotate(float yawSpeed, float pitchSpeed, float currentYaw, float currentPitch) {
        float yawAngle = updateRotation(currentYaw, MC.thePlayer.rotationYaw, yawSpeed);
        float pitchAngle = updateRotation(currentPitch, MC.thePlayer.rotationPitch, pitchSpeed);

        final float[] mouseSensitivity = applyMouseSensitivity(yawAngle, pitchAngle, true);
        // Rückgabe der berechneten Rotationen
        return new float[] {mouseSensitivity[0], mouseSensitivity[1]};
    }



    public float[] rotateToBlockPos(double bx, double by, double bz, float lastYaw, float lastPitch, float yawSpeed, float pitchSpeed, boolean random) {
        double x = bx - MC.thePlayer.posX;
        double y = by - (MC.thePlayer.posY + (double)MC.thePlayer.getEyeHeight());
        double z = bz - MC.thePlayer.posZ;
        float calcYaw = (float)(Math.toDegrees(MathHelper.atan2(z, x)) - 90.0);
        float calcPitch = (float)(-(MathHelper.atan2(y, (double)MathHelper.sqrt_double(x * x + z * z)) * 180.0 / Math.PI));
        float pitch = updateRotation(lastPitch, calcPitch, pitchSpeed + RandomUtil.nextFloat(0.0F, 15.0F));
        float yaw = updateRotation(lastYaw, calcYaw, yawSpeed + RandomUtil.nextFloat(0.0F, 15.0F));
        if (random) {
            yaw = (float)((double)yaw + ThreadLocalRandom.current().nextDouble(-2.0, 2.0));
            pitch = (float)((double)pitch + ThreadLocalRandom.current().nextDouble(-0.2, 0.2));
        }

        return new float[]{yaw, pitch};
    }

    public static float[] mouseSens(float yaw, float pitch, float lastYaw, float lastPitch) {
        if ((double) MC.gameSettings.mouseSensitivity == 0.5) {
            MC.gameSettings.mouseSensitivity = 0.47887325F;
        }

        if (yaw == lastYaw && pitch == lastPitch) {
            return new float[]{yaw, pitch};
        } else {
            float f1 = MC.gameSettings.mouseSensitivity * 0.6F + 0.2F;
            float f2 = f1 * f1 * f1 * 8.0F;
            int deltaX = (int) ((6.667 * (double) yaw - 6.667 * (double) lastYaw) / (double) f2);
            int deltaY = (int) ((6.667 * (double) pitch - 6.667 * (double) lastPitch) / (double) f2) * -1;
            float f5 = (float) deltaX * f2;
            float f3 = (float) deltaY * f2;
            yaw = (float) ((double) lastYaw + (double) f5 * 0.15);
            float f4 = (float) ((double) lastPitch - (double) f3 * 0.15);
            pitch = MathHelper.clamp_float(f4, -90.0F, 90.0F);
            return new float[]{yaw, pitch};
        }
    }
    float updateRotation(float currentRotation, float nextRotation, float rotationSpeed) {
        float f = MathHelper.wrapAngleTo180_float(nextRotation - currentRotation);
        if (f > rotationSpeed) {
            f = rotationSpeed;
        }
        if (f < -rotationSpeed) {
            f = -rotationSpeed;
        }
        return currentRotation + f;
    }


}
