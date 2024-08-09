package club.mega.module.impl.player;

import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.NumberSetting;
import net.minecraft.network.play.client.C03PacketPlayer;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "Regen", description = "Regens health quicker", category = Category.PLAYER)
public class Regen extends Module {
    private final NumberSetting packets = new NumberSetting("Packets", this, 1, 1000, 200, 1);

    @Handler
    public void ontick(EventTick event) {
            if (this.MC.thePlayer.getHealth() < 20.0F && this.MC.thePlayer.getFoodStats().getFoodLevel() > 19) {
                for (int i = 0; i < packets.getAsInt(); i++) {
                    this.MC.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
                }
                
            }

    }
}
