package club.mega.module.impl.player;

import club.mega.event.impl.EventClickMouse;
import club.mega.event.impl.EventMouseClicked;
import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.util.RandomUtil;
import club.mega.util.TimeUtil;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemFishingRod;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "FastPlace", description = "FastPlace", category = Category.PLAYER)
public class FastPlace  extends Module {
    private static final TimeUtil placeTimer = new TimeUtil();
    private static int  finalCps = 0;
    public final NumberSetting maxAPS = new NumberSetting("Max APS", this, 5, 40, 14, 1);
    public final NumberSetting minAPS = new NumberSetting("Min APS", this, 5, 40, 12, 1);
    @Handler
    public final void ontick(final EventTick event) {
        if(MC.gameSettings.keyBindUseItem.pressed && !(MC.thePlayer.getHeldItem().getItem() instanceof ItemEnderPearl) && !(MC.thePlayer.getHeldItem().getItem() instanceof ItemFishingRod) && !(MC.thePlayer.getHeldItem().getItem() instanceof ItemBucket)) {
            int aps = (int) Math.round(RandomUtil.getRandomNumber(minAPS.getAsInt(), maxAPS.getAsInt()));

            if (finalCps < aps)
                finalCps += Math.round(RandomUtil.getRandomNumber(0.8D, 2D));
            if (finalCps <= aps)
                finalCps--;

            if (!placeTimer.hasTimePassed(1000 / aps))
                return;
            MC.rightClickMouse();
            placeTimer.reset();
        }
    }
}
