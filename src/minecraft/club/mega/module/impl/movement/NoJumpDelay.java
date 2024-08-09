package club.mega.module.impl.movement;

import club.mega.event.impl.EventMovePlayer;
import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.impl.combat.KillAura;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.util.AuraUtil;
import club.mega.util.WorldUtil;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "No Jump Delay", description = "No Jump Delay", category = Category.MOVEMENT)
public class NoJumpDelay extends Module {
    public final BooleanSetting disable = new BooleanSetting("KillAuraDisable", this, false);
    @Handler
    public final void tick(final EventTick event) {
        if(disable.get() && AuraUtil.getTarget() != null) {
            return;
        }
        if (WorldUtil.getBlock(new BlockPos(MC.thePlayer.posX, MC.thePlayer.posY + 2, MC.thePlayer.posZ)).getMaterial() != Material.air && MC.gameSettings.keyBindJump.pressed) {
            MC.thePlayer.movementInput.jump = false;
            if (MC.thePlayer.onGround) {
                MC.thePlayer.jump();
            }

        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

}
