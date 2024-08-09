package club.mega.module.impl.movement;

import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.ListSetting;
import net.minecraft.block.BlockAir;
import net.minecraft.util.BlockPos;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "AntiVoid", description = "Saves you from falling into the void", category = Category.PLAYER)
public class AntiVoid extends Module {
    private final ListSetting mode = new ListSetting("Mode", this, new String[] {"Watchdog", "Intave"});

    @Handler
    public void onTick(EventTick event) {
        switch (mode.getCurrent()) {
            case "Intave":
                if (!isBlockUnder()) {
                    if (MC.thePlayer.fallDistance > 25F) {
                        MC.thePlayer.isDead = true;
                    }else {
                        MC.thePlayer.isDead = false;
                    }
                }
                break;


            case "Watchdog":
                if (!isBlockUnder()) {
                    if (MC.thePlayer.fallDistance > 0.2F) {
                        MC.thePlayer.motionY = -1F;
                    }

                }
                break;

        }
    }


    public boolean isBlockUnder() {
        for (int i = (int) MC.thePlayer.posY; i >= 0; --i) {
            BlockPos position = new BlockPos(MC.thePlayer.posX, i, MC.thePlayer.posZ);

            if (!(MC.theWorld.getBlockState(position).getBlock() instanceof BlockAir)) {
                return true;
            }
        }

        return false;
    }
    
    @Override
    public void onEnable() {
        if(MC.theWorld == null)
            return;
        MC.thePlayer.respawnPlayer();
        MC.thePlayer.isDead = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        MC.thePlayer.respawnPlayer();
        MC.thePlayer.isDead = false;
        super.onDisable();
    }
}
