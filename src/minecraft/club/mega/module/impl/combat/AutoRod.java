package club.mega.module.impl.combat;

import java.awt.Color;

import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.module.setting.impl.RangeSetting;
import club.mega.util.AuraUtil;
import club.mega.util.RandomUtil;
import club.mega.util.TimeUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemFishingRod;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "AutoRod", description = "Automatically rods the enemies", category = Category.COMBAT)
public class AutoRod extends Module {
    public TimeUtil timer = new TimeUtil();
    public TimeUtil timer2 = new TimeUtil();
    private final RangeSetting range = new RangeSetting("Range", this, 0, 15.0, 4.0, 5.0, 0.1);
    private final RangeSetting rodDelay = new RangeSetting("Delay", this, 0, 1000.0, 30.0, 50.0, 1.0);
    public final NumberSetting nextRodDelay = new NumberSetting("NextRod", this, 0, 5000.0, 40.0, 1);
    public final BooleanSetting resetSlot = new BooleanSetting("ResetSlot", this, true);


    @Handler
    public void onEventTick(EventTick e) {
        if (this.isToggled() && MC.currentScreen == null) {
            if (AuraUtil.getTarget() != null) {
                EntityLivingBase target = AuraUtil.getTarget();
                int originalSlot = MC.thePlayer.inventory.currentItem;
                boolean foundRod = false;
                int rodSlot = -1;

                for(int i = 0; i < 9; ++i) {
                    if (MC.thePlayer.inventory.getStackInSlot(i).getItem() instanceof ItemFishingRod) {
                        foundRod = true;
                        rodSlot = i;
                    }
                }

                if (foundRod && rodSlot != -1) {
                    if ((double)target.getDistanceToEntity(MC.thePlayer) < this.range.getCurrentMin() || (double)target.getDistanceToEntity(MC.thePlayer) > this.range.getCurrentMax()) {
                        return;
                    }

                    if (this.timer.hasTimePassed((int) RandomUtil.nextFloat(this.rodDelay.getCurrentMin(), this.rodDelay.getCurrentMax()))) {
                        MC.thePlayer.inventory.currentItem = rodSlot;
                        MC.rightClickMouse();
                        MC.thePlayer.inventory.currentItem = originalSlot;
                        if (this.timer2.hasTimePassed(this.nextRodDelay.getAsLong())) {
                            this.timer.reset();
                            timer2.reset();
                        }
                    }
                }
            }
        }

    }
}
