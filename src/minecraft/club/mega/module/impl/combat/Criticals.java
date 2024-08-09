package club.mega.module.impl.combat;

import club.mega.event.impl.EventPacket;
import club.mega.event.impl.EventPreTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.ListSetting;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "Criticals", description = "Always land Criticals", category = Category.COMBAT)
public class Criticals extends Module {

    public final ListSetting mode = new ListSetting("Mode", this, new String[]{"Packet", "No Ground", "Motion"});

    @Handler
    public final void packet(final EventPacket event) {

    }

    @Handler
    public final void preTick(final EventPreTick event) {
        if (mode.is("No Ground"))
        event.setOnGround(false);
    }

}
