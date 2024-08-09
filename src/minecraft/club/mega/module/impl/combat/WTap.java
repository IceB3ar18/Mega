package club.mega.module.impl.combat;

import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.util.AuraUtil;
import club.mega.util.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.input.Keyboard;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "WTap", description = "Places blocks below you", category = Category.COMBAT)
public class WTap extends Module {

    private boolean taped;
    private boolean stoppedLastTick;
    @Handler
    public final void ontick(final EventTick event) {
            if (this.stoppedLastTick) {
                MC.gameSettings.keyBindForward.pressed = Keyboard.isKeyDown(MC.gameSettings.keyBindForward.getKeyCode());
                this.stoppedLastTick = false;
                return;
            }

            if (AuraUtil.getTarget() != null) {
                if (AuraUtil.getTarget().hurtTime >= 2 && MC.thePlayer.onGround && MC.thePlayer.isSprinting()) {
                    if (!this.taped) {
                        MC.gameSettings.keyBindSprint.pressed = false;
                        MC.gameSettings.keyBindForward.pressed = false;
                        this.stoppedLastTick = true;
                        this.taped = true;
                    }
                } else {
                    this.taped = false;
                }
            } else {
                this.taped = false;
            }
        
    }

    @Override
    public void onDisable() {
        super.onDisable();

    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.taped = false;
        this.stoppedLastTick = false;

    }
}
