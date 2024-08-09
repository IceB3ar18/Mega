package club.mega.module.impl.movement;

import club.mega.Mega;
import club.mega.event.impl.EventPostTick;
import club.mega.event.impl.EventPreTick;
import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.impl.player.Scaffold;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ListSetting;
import club.mega.util.ChatUtil;
import club.mega.util.MovementUtil;
import net.minecraft.network.play.client.C03PacketPlayer;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "Strafe", description = "Strafe", category = Category.MOVEMENT)
public class Strafe extends Module {


    public final ListSetting mode = new ListSetting("Mode", this, new String[]{"Normal", "Matrix"});

    public final BooleanSetting strafeWhileKB = new BooleanSetting("WhileKB", this, false);
    public final BooleanSetting onlyOnGround = new BooleanSetting("OnlyOnGround", this, false);

    @Handler
    public final void tick(final EventPostTick event) {
        if ((this.strafeWhileKB.get() || MC.thePlayer.hurtTime == 0) && (!this.onlyOnGround.get() || MC.thePlayer.onGround) && !Scaffold.getInstance().isToggled()) {
            switch (this.mode.getCurrent()) {
                case "Normal":
                    MovementUtil.strafe();
                    break;
                case "Matrix":
                    MovementUtil.strafeMatrix();
                    break;
                default:
                    break;
            }
        }

    }



    public static Speed getInstance() {
        return Mega.INSTANCE.getModuleManager().getModule(Speed.class);
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
