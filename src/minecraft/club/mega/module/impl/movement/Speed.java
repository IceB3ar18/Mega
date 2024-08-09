package club.mega.module.impl.movement;

import club.mega.Mega;
import club.mega.event.impl.*;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.impl.combat.KillAura;
import club.mega.module.impl.player.Scaffold;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ColorSetting;
import club.mega.module.setting.impl.ListSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.util.*;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import rip.hippo.lwjeb.annotation.Handler;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

@Module.ModuleInfo(name = "Speed", description = "Speed", category = Category.MOVEMENT)
public class Speed extends Module {


    public final ListSetting mode = new ListSetting("Mode", this, new String[]{"Vanilla", "VanillaHop", "LegitHop", "LegitAbuse", "Matrix", "TeleportAbuse", "NCP", "Test", "Verus", "NoCheatMinus", "GamerCheatOG", "GamerCheatBHop", "CheatmineSafe", "CheatmineRage", "NCPRace", "NCPWtf", "NCPLow", "GroundStrafe", "Strafe", "FixedStrafe"});
    public final NumberSetting vanillaSpeed = new NumberSetting("Speed", this, 0.0, 10.0, 1.0, 0.1);
    public final NumberSetting vanillaHeight = new NumberSetting("Height", this, 0.01, 0.42, 0.2, 0.01);
    public final NumberSetting dmgSpeed = new NumberSetting("DMGSpeed", this, 0.0, 2.0, 1.0, 0.1);

    public final BooleanSetting damageBoost = new BooleanSetting("DamageBoost", this, false);
    public final BooleanSetting strafe = new BooleanSetting("Strafe", this, false);

    private int tickCounter;
    private int tickCounter2;
    private int ticks = 0;
    private float speedYaw;
    private boolean wasOnGround;
    public int stage = 0;
    public boolean collided = false;
    public double stair = 0.0;
    private double speed = 0.0;
    private boolean lessSlow = false;
    public double less = 0.0;
    private TimeUtil timer = new TimeUtil();
    private TimeUtil lastCheck = new TimeUtil();
    private boolean shouldslow;
    private boolean polared;
    private boolean first;
    private double lastDist;
    private int offGround;
    private int motionDelay;


    @Handler
    public final void tick(final EventTick event) {
        setTag(mode.getCurrent());
        switch (this.mode.getCurrent()) {
            case "NCP":
                if (MoveUtil.isMoving()) {
                    if (MC.thePlayer.onGround) {
                        MC.thePlayer.jump();
                        MC.thePlayer.motionX *= 1.01;
                        MC.thePlayer.motionZ *= 1.01;
                        MC.thePlayer.setSpeedInAir(0.022F);
                    }

                    MC.thePlayer.motionY -= 9.9999E-4;
                    MoveUtil.strafe();
                } else {
                    MC.thePlayer.motionX = 0.0;
                    MC.thePlayer.motionZ = 0.0;
                }

                MC.getTimer().timerSpeed = 1.0865F;
            default:
        }
    }
    @Handler
    public final void packet(final EventPacket packetEvent) {
        if (MC.theWorld != null) {
            if (packetEvent.getType() == EventPacket.Type.RECEIVE) {
                if (packetEvent.getPacket() instanceof S08PacketPlayerPosLook) {
                    S08PacketPlayerPosLook pac = (S08PacketPlayerPosLook)packetEvent.getPacket();
                    if (this.lastCheck.hasTimePassed(300L)) {
                        pac.setYaw(MC.thePlayer.rotationYaw);
                        pac.setPitch(MC.thePlayer.rotationPitch);
                    }

                    this.stage = -4;
                    this.lastCheck.reset();
                }
            }
        }
    }


    @Handler
    public final void onPostTick(final EventPostMotion eventTick) {
        if (!MC.thePlayer.onGround) {
            ++this.offGround;
        } else {
            this.offGround = 0;
        }

        switch (this.mode.getCurrent()) {
            case "CheatmineSafe":
                if (MC.thePlayer.onGround) {
                    MC.thePlayer.motionY = 0.38;
                    MoveUtil.strafe();
                }
                break;
            case "CheatmineRage":
                if (MC.thePlayer.onGround) {
                    MC.thePlayer.jump();
                    MC.thePlayer.motionY = 0.35;
                }
        }
    }

    @Handler
    public final void preTick(final EventPreTick event) {
        this.speedYaw = MC.thePlayer.rotationYaw;
        switch (this.mode.getCurrent()) {
            case "GroundStrafe":
                this.groundStrafe();
                break;
            case "Strafe":
                this.strafe();
                break;
            case "FixedStrafe":
                this.fixedstrafe();
                break;
            case "NCPLow":
                this.ncplow();
                break;
            case "NCPWtf":
                this.ncpwtf();
                break;
            case "NCPRace":
                this.ncprace();
                break;
            case "GamerCheatBHop":
                this.gamercheatbhop();
                break;
            case "GamerCheatOG":
                this.gamercheatog();
                break;
            case "NoCheatMinus":
                this.nocheatminus();
                break;
            case "Verus":
                this.verus();
                break;
            case "Vanilla":
                this.vonground();
                break;
            case "VanillaHop":
                this.vbhop();
                break;
            case "LegitAbuse":
                this.legitAbuse();
                break;
            case "TeleportAbuse":
                this.teleportAbuse();
                break;
            case "Matrix":
                this.matrix();
                break;
            case "Test":
                this.test();
        }
    }

    private void groundStrafe() {
        if (MC.thePlayer.onGround) {
            MC.thePlayer.jump();
            MoveUtil.strafe();
        }

    }

    private void strafe() {
        if (MC.thePlayer.onGround) {
            MC.thePlayer.jump();
        }

        MoveUtil.strafe();
    }

    private void fixedstrafe() {
        if (MC.thePlayer.onGround) {
            MC.thePlayer.jump();
        }

        MoveUtil.setSpeed((float)SigmaMoveUtils.defaultSpeed());
    }

    private void ncpwtf() {
        boolean hasSpeed = MC.thePlayer.isPotionActive(Potion.moveSpeed);
        double speedAmplifier = 0.0;
        if (hasSpeed) {
            speedAmplifier = (double)MC.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
        }

        MoveUtil.strafe();
        EntityPlayerSP var10000 = MC.thePlayer;
        var10000.motionX *= 1.0045 + speedAmplifier * 0.019999999552965164;
        var10000 = MC.thePlayer;
        var10000.motionZ *= 1.0045 + speedAmplifier * 0.019999999552965164;
        boolean pushDown = true;
        if (MC.thePlayer.onGround && this.isMoving() && !MC.gameSettings.keyBindJump.pressed) {
            float boost = (float)(speedAmplifier * 0.06499999761581421);
            MC.thePlayer.jump();
            if (!pushDown) {
                return;
            }

            MC.timer.timerSpeed = 1.405F;
            MoveUtil.strafe((double)((MC.thePlayer.ticksExisted % 10 > 7 ? 0.4F : 0.325F) + boost));
            var10000 = MC.thePlayer;
            var10000.motionX *= 1.01 + speedAmplifier * 0.17499999701976776;
            var10000 = MC.thePlayer;
            var10000.motionZ *= 1.01 + speedAmplifier * 0.17499999701976776;
            MC.thePlayer.cameraPitch = 0.0F;
            MC.thePlayer.cameraYaw = 0.0F;
        } else if (!MC.thePlayer.onGround && MC.thePlayer.motionY > 0.3 && !MC.gameSettings.keyBindJump.pressed) {
            MC.timer.timerSpeed = 0.85F;
            MC.thePlayer.motionY = -0.42;
            var10000 = MC.thePlayer;
            var10000.posY -= 0.45;
            MC.thePlayer.cameraPitch = 0.0F;
        }

        MC.thePlayer.stepHeight = 0.5F;
        MoveUtil.strafe();
    }

    private void ncprace() {
        if (MC.thePlayer.onGround) {
            MC.thePlayer.jump();
            MC.timer.timerSpeed = 1.2F;
            MoveUtil.multiplyXZ(1.0708);
            EntityPlayerSP var10000 = MC.thePlayer;
            var10000.moveStrafing *= 2.0F;
        } else {
            MC.timer.timerSpeed = 0.98F;
            MC.thePlayer.jumpMovementFactor = 0.0265F;
        }
    }

    private void gamercheatbhop() {
        MoveUtil.setSpeed(0.6F);
        if (MC.thePlayer.isCollidedVertically) {
            MoveUtil.setSpeed(0.2F);
            MC.thePlayer.motionY = 0.35;
        }

    }

    private void gamercheatog() {
        MoveUtil.setSpeed(0.56F);
        MC.thePlayer.motionY = 0.0;
        if (MC.thePlayer.ticksExisted % 3 == 0) {
            double d = MC.thePlayer.posY - 1.0E-10;
            MC.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(MC.thePlayer.posX, d, MC.thePlayer.posZ, true));
        }

        double y1 = MC.thePlayer.posY + 1.0E-10;
        MC.thePlayer.setPosition(MC.thePlayer.posX, y1, MC.thePlayer.posZ);
    }

    private void nocheatminus() {
        if (MC.thePlayer.moveForward == 0.0F && MC.thePlayer.moveStrafing == 0.0F) {
            this.speed = SigmaMoveUtils.defaultSpeed();
        }

        if (this.stage != 1 || !MC.thePlayer.isCollidedVertically || MC.thePlayer.moveForward == 0.0F && MC.thePlayer.moveStrafing == 0.0F) {
            if (!MC.thePlayer.isInWater() && !MC.thePlayer.isInLava() && this.stage == 2 && MC.thePlayer.isCollidedVertically && SigmaMoveUtils.isOnGround(0.001) && (MC.thePlayer.moveForward != 0.0F || MC.thePlayer.moveStrafing != 0.0F)) {
                MC.thePlayer.motionY = 0.4;
                MC.thePlayer.jump();
                this.speed *= 2.149;
            } else if (this.stage == 3) {
                double difference = 0.66 * (this.lastDist - SigmaMoveUtils.defaultSpeed());
                this.speed = this.lastDist - difference;
            } else {
                List<AxisAlignedBB> collidingList = MC.theWorld.getCollidingBoundingBoxes(MC.thePlayer, MC.thePlayer.boundingBox.offset(0.0, MC.thePlayer.motionY, 0.0));
                if ((collidingList.size() > 0 || MC.thePlayer.isCollidedVertically) && this.stage > 0) {
                    if (1.35 * SigmaMoveUtils.defaultSpeed() - 0.01 > this.speed) {
                        this.stage = 0;
                    } else {
                        this.stage = MC.thePlayer.moveForward == 0.0F && MC.thePlayer.moveStrafing == 0.0F ? 0 : 1;
                    }
                }

                this.speed = this.lastDist - this.lastDist / 159.0;
            }
        } else {
            this.speed = 0.25 + SigmaMoveUtils.defaultSpeed() - 0.01;
        }

        this.speed = Math.max(this.speed, SigmaMoveUtils.defaultSpeed());
        if (this.stage > 0) {
            if (!MC.thePlayer.isInWater() && !MC.thePlayer.isInLava()) {
                this.speed = 0.1;
            }

            MoveUtil.setSpeed((float)this.speed);
        }

        if (MC.thePlayer.moveForward != 0.0F || MC.thePlayer.moveStrafing != 0.0F) {
            ++this.stage;
        }

    }

    private void verus() {
        MoveUtil.setSpeed2(0.2919999957084656);
        if (this.damageBoost.get() && MC.thePlayer.hurtTime != 0 && MC.thePlayer.fallDistance < 3.0F) {
            MoveUtil.setSpeed2((double)((float)this.dmgSpeed.getAsDouble()));
        } else {
            MoveUtil.setSpeed2(0.2919999957084656);
        }

        if (this.canJump()) {
            MC.thePlayer.jump();
        } else {
            MC.thePlayer.jumpMovementFactor = 0.1F;
        }

    }

    private void vonground() {
        if (this.isMoving()) {
            MoveUtil.setSpeed((float)(0.1 * this.vanillaSpeed.getAsFloat()), this.strafe.get());
        }

    }

    private void vbhop() {
        if (this.canJump()) {
            MC.thePlayer.motionY = this.vanillaHeight.getAsFloat();
            if (this.strafe.get()) {
                MoveUtil.strafe();
            }
        } else if (this.isMoving()) {
            MoveUtil.setSpeed((float)(0.1 * this.vanillaSpeed.getAsFloat()), this.strafe.get());
        }

    }

    private void legitAbuse() {
        if (this.isMoving()) {
            if (MC.thePlayer.onGround) {
                MC.thePlayer.jump();
            }

            KeyBinding[] gameSettings = new KeyBinding[]{MC.gameSettings.keyBindForward, MC.gameSettings.keyBindRight, MC.gameSettings.keyBindBack, MC.gameSettings.keyBindLeft};
            int[] down = new int[]{0};
            Arrays.stream(gameSettings).forEach((keyBinding) -> {
                down[0] += keyBinding.isKeyDown() ? 1 : 0;
            });
            boolean active = down[0] == 1;
            if (!active) {
                return;
            }

            double increase = MC.thePlayer.onGround ? 0.0026000750109401644 : 5.199896488849598E-4;
            double yaw = MoveUtil.direction();
            EntityPlayerSP var10000 = MC.thePlayer;
            var10000.motionX += (double)(-MathHelper.sin((float)yaw)) * increase;
            var10000 = MC.thePlayer;
            var10000.motionZ += (double)MathHelper.cos((float)yaw) * increase;
        }

    }

    private void teleportAbuse() {
        if (MC.thePlayer.onGround) {
            MC.thePlayer.jump();
            if (!this.first) {
                MC.timer.timerSpeed = 40.0F;
            } else {
                MC.timer.timerSpeed = 5.0F;
            }

            this.first = false;
        } else {
            MC.timer.timerSpeed = 0.3F;
        }
    }

    private void matrix() {
        ++this.tickCounter;
        if (MC.thePlayer.motionX != 0.0 || MC.thePlayer.motionZ != 0.0) {
            if (!MoveUtil.isMoving()) {
                EntityPlayerSP var10000 = MC.thePlayer;
                var10000.motionY -= 0.009998999536037445;
                if (MC.thePlayer.onGround) {
                    this.tickCounter = 0;
                    MoveUtil.strafeMatrix();
                } else if (MC.thePlayer.movementInput.moveForward > 0.0F && MC.thePlayer.movementInput.moveStrafe != 0.0F) {
                    MC.thePlayer.setSpeedInAir(0.02F);
                } else {
                    MC.thePlayer.setSpeedInAir(0.0208F);
                }

            }
        }
    }

    private void test() {
        if (MC.thePlayer.onGround) {
            this.tickCounter = 0;
        }

        label41: {
            if (MoveUtil.isMoving()) {
                if (AuraUtil.getTarget() == null) {
                    if (MC.thePlayer.isUsingItem()) {
                        if (MC.thePlayer.isBlocking()) {
                            MC.getTimer().timerSpeed = 1.23F;
                        } else if (MC.getTimer().timerSpeed > 1.0F) {
                            MC.getTimer().timerSpeed = 1.0F;
                        }
                    } else {
                        MC.getTimer().timerSpeed = 1.23F;
                    }
                    break label41;
                }
            }

            if (MC.getTimer().timerSpeed > 1.0F) {
                MC.getTimer().timerSpeed = 1.0F;
            }
        }

        if (MC.thePlayer.hurtTime > 8 && !MC.thePlayer.isBurning() && MC.thePlayer.fallDistance < 2.0F && !MC.thePlayer.isPotionActive(Potion.wither) && !MC.thePlayer.isPotionActive(Potion.poison)) {
            MoveUtil.addSpeed(0.4000000059604645, false);
        }

        ++this.tickCounter;
    }

    private void ncplow() {
        if (this.isMoving()) {
            if (MC.gameSettings.keyBindJump.pressed) {
                return;
            }

            EntityPlayerSP var10000;
            if (MC.thePlayer.onGround) {
                ++this.motionDelay;
                this.motionDelay %= 3;
                if (this.motionDelay == 0) {
                    var10000 = MC.thePlayer;
                    var10000.motionY += 0.18000000715255737;
                    var10000 = MC.thePlayer;
                    var10000.motionX *= 1.2000000476837158;
                    var10000 = MC.thePlayer;
                    var10000.motionZ *= 1.2000000476837158;
                }
            }

            if (!MC.thePlayer.onGround) {
                var10000 = MC.thePlayer;
                var10000.motionX *= 1.0499999523162842;
                var10000 = MC.thePlayer;
                var10000.motionZ *= 1.0499999523162842;
            }

            MC.thePlayer.setSpeedInAir(0.022F);
        }

    }
    @Handler
    public void onEventSilentMove(EventSilentMove eventSilentMove) {
        switch (this.mode.getCurrent()) {
            case "Matrix2":
            case "LegitHop":
            case "Matrix":
                if (this.isMoving()) {
                    MC.thePlayer.movementInput.jump = true;
                }
            default:
        }
    }

    @Handler
    public final void moveFlying(final EventMoveFlying event) {

        if (MC.thePlayer.fallDistance > 4.0F && !this.mode.is("LegitHop")) {
            event.setCancelled(true);
        }

        if (!Scaffold.getInstance().isToggled()) {
            float var2;

                label34: {
                    if (KillAura.getInstance().isToggled()) {
                        
                        if (AuraUtil.getTarget() != null && KillAura.getInstance().moveFix.get()) {
                            var2 = MC.thePlayer.rotationYaw;
                            break label34;
                        }
                    }

                    var2 = MC.thePlayer.rotationYaw;
                }


            event.setYaw(var2);
        }
    }

    private boolean isMoving() {
        return MC.thePlayer.moveForward != 0.0F || MC.thePlayer.moveStrafing != 0.0F && !MC.thePlayer.isCollidedHorizontally;
    }

    private boolean canJump() {
        return this.isMoving() && MC.thePlayer.onGround;
    }
    public static Speed getInstance() {
        return Mega.INSTANCE.getModuleManager().getModule(Speed.class);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.lastDist = 0.0;
        this.first = true;
        boolean player = MC.thePlayer == null;
        this.collided = !player && MC.thePlayer.isCollidedHorizontally;
        this.lessSlow = false;
        if (MC.thePlayer != null) {
            this.speed = SigmaMoveUtils.defaultSpeed();
        }

        this.less = 0.0;
        this.stage = 2;
        MC.getTimer().timerSpeed = 1.0F;
        if (MC.thePlayer != null) {
            MC.thePlayer.jumpMovementFactor = 0.02F;
            MC.thePlayer.setSpeedInAir(0.02F);
            MC.thePlayer.setSpeedOnGround(0.1F);
        }

        this.tickCounter2 = -2;
        this.tickCounter = 0;
        this.ticks = 0;
    }
    @Override
    public void onDisable() {
        super.onDisable();
        MC.getTimer().timerSpeed = 1.0F;
            MC.thePlayer.jumpMovementFactor = 0.02F;
            MC.thePlayer.setSpeedInAir(0.02F);
            MC.thePlayer.setSpeedOnGround(0.1F);


        this.tickCounter2 = -2;
        this.tickCounter = 0;
    }
}
