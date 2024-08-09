package net.minecraft.util;

import club.mega.Mega;
import club.mega.gui.click.ClickGUI;
import club.mega.gui.click.ConfigGui;
import club.mega.gui.clicknew.ClickGui;
import club.mega.interfaces.MinecraftInterface;
import club.mega.module.impl.combat.KillAura;
import club.mega.module.impl.combat.RotationHandler;
import club.mega.module.impl.movement.InvMove;
import club.mega.module.impl.player.Scaffold;
import club.mega.util.AuraUtil;
import club.mega.util.ChatUtil;
import club.mega.util.RotationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Keyboard;

public class MovementInputFromOptions extends MovementInput implements MinecraftInterface
{
    private final GameSettings gameSettings;

    private float lastForward, lastStrafe;

    public MovementInputFromOptions(GameSettings gameSettingsIn)
    {
        this.gameSettings = gameSettingsIn;
    }

    public void updatePlayerMoveState()
    {
        this.moveStrafe = 0.0F;
        this.moveForward = 0.0F;

        if (Mega.INSTANCE.getModuleManager().getModule(InvMove.class).isToggled() && !(MC.currentScreen instanceof GuiChat) && ((MC.currentScreen instanceof ClickGUI || MC.currentScreen instanceof ConfigGui || MC.currentScreen instanceof ClickGui) && Mega.INSTANCE.getModuleManager().getModule(InvMove.class).clickGuiOnly.get()) ) {
            if (Keyboard.isKeyDown(gameSettings.keyBindForward.getKeyCode())) ++moveForward;
            if (Keyboard.isKeyDown(gameSettings.keyBindBack.getKeyCode())) --moveForward;
            if (Keyboard.isKeyDown(gameSettings.keyBindLeft.getKeyCode())) ++moveStrafe;
            if (Keyboard.isKeyDown(gameSettings.keyBindRight.getKeyCode())) --moveStrafe;
            jump = Keyboard.isKeyDown(gameSettings.keyBindJump.getKeyCode());
        } else {
            if (gameSettings.keyBindForward.isKeyDown()) ++moveForward;
            if (gameSettings.keyBindBack.isKeyDown()) --moveForward;
            if (gameSettings.keyBindLeft.isKeyDown()) ++moveStrafe;
            if (gameSettings.keyBindRight.isKeyDown()) --moveStrafe;
            jump = gameSettings.keyBindJump.isKeyDown();
        }
        this.sneak = this.gameSettings.keyBindSneak.isKeyDown();


        /*if(Minecraft.getMinecraft().thePlayer.isMoving() &&
                ((Scaffold.getInstance().isToggled() && Scaffold.getInstance().silentMoveFix.get() && Scaffold.getInstance().silentMoveFix.isVisible()) || (KillAura.getInstance().isToggled() && KillAura.getInstance().silentMoveFix.get() && KillAura.getInstance().silentMoveFix.isVisible() && AuraUtil.getTarget() != null && !Scaffold.getInstance().isToggled()) || (RotationHandler.getInstance().isToggled() && RotationHandler.getInstance().silentMoveFix.get() && RotationHandler.getInstance().silentMoveFix.isVisible() && AuraUtil.isLeftToBackRotate() && !Scaffold.getInstance().isToggled()))) {
            testFix(this.moveForward, this.moveStrafe, 1);
        }*/
        if (this.sneak)
        {
            this.moveStrafe = (float)((double)this.moveStrafe * 0.3D);
            this.moveForward = (float)((double)this.moveForward * 0.3D);
        }
    }

    private void testFix(float forward, float strafe, int x) {
            if (!Minecraft.getMinecraft().thePlayer.isMoving()) return;

            if (x == 1 && RotationUtil.getRotations() == null) return;
            float slipperiness = 0.91F;
            if (MC.thePlayer.onGround) {
                slipperiness = MC.theWorld.getBlockState(new BlockPos(MathHelper.floor_double(MC.thePlayer.posX), MathHelper.floor_double(MC.thePlayer.getEntityBoundingBox().minY) - 1, MathHelper.floor_double(MC.thePlayer.posZ))).getBlock().slipperiness * 0.91F;
            }

            float moveSpeedOffset = 0.16277136F / (slipperiness * slipperiness * slipperiness);
            float friction;

            if (MC.thePlayer.onGround) {
                friction = MC.thePlayer.getAIMoveSpeed() * moveSpeedOffset;
            } else {
                friction = MC.thePlayer.jumpMovementFactor;
            }

            float f = strafe * strafe + forward * forward;
            f = MathHelper.sqrt_float(f);

            if (f < 1.0F) f = 1.0F;

            f = friction / f;


            float clientStrafe = strafe * f;
            float clientForward = forward * f;
            float clientRotationSin = MathHelper.sin(MC.thePlayer.rotationYaw * (float) Math.PI / 180.0F);
            float clientRotationCos = MathHelper.cos(MC.thePlayer.rotationYaw * (float) Math.PI / 180.0F);
            float clientMotionX = (clientStrafe * clientRotationCos - clientForward * clientRotationSin);
            float clientMotionZ = (clientForward * clientRotationCos + clientStrafe * clientRotationSin);



            float yaw = RotationUtil.getRotations()[0];
            float serverRotationSin = MathHelper.sin(yaw * (float) Math.PI / 180.0F);
            float serverRotationCos = MathHelper.cos(yaw * (float) Math.PI / 180.0F);

            float smalestDistance = Float.NaN;
            float posibleForward = 0;
            float posibleStrafe = 0;

            for (int strafevalue = -1; strafevalue <= 1; strafevalue++) {
                for (int forwardvalue = -1; forwardvalue <= 1; forwardvalue++) {
                    if (!(forwardvalue == 0 && strafevalue == 0)) {
                        float f2 = strafevalue * strafevalue + forwardvalue * forwardvalue;
                        f2 = MathHelper.sqrt_float(f2);
                        float calcStrafe = strafevalue * f;
                        float calcForward = forwardvalue * f;
                        float calcMotionX = (calcStrafe * serverRotationCos - calcForward * serverRotationSin);
                        float calcMotionZ = (calcForward * serverRotationCos + calcStrafe * serverRotationSin);

                        float diffMotionX = calcMotionX - clientMotionX;
                        float diffMotionZ = calcMotionZ - clientMotionZ;
                        float distance = normalize(MathHelper.sqrt_float(diffMotionX * diffMotionX + diffMotionZ * diffMotionZ));

                        if (Float.isNaN(smalestDistance) || distance < smalestDistance) {
                            posibleForward = forwardvalue;
                            posibleStrafe = strafevalue;
                            smalestDistance = (float) distance;
                        }
                    }
                }
            }
            moveForward = posibleForward;
            moveStrafe = posibleStrafe;
            lastForward = posibleForward;
            lastStrafe = posibleStrafe;

    }

    public float normalize(float value) {
        if (value < 0) {
            return value / -1;
        }
        return value;
    }
}
