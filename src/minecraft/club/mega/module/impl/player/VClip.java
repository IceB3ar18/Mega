package club.mega.module.impl.player;

import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.util.*;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.BlockPos;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "V-Clip", description = "VClip", category = Category.MOVEMENT)
public class VClip extends Module {

    private final TimeUtil timeUtil = new TimeUtil();


    @Override
    public void onEnable() {
        super.onEnable();
        if(MC.theWorld == null)
            return;

        BlockPos blockPos = MC.getObjectMouseOver().getBlockPos();
        MC.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ(), MC.thePlayer.onGround));
        MC.thePlayer.setPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        this.toggle();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
