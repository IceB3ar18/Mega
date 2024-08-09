package club.mega.module.impl.combat;

import club.mega.Mega;
import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.util.AuraUtil;
import club.mega.util.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "AutoGApple", description = "AutoGApple", category = Category.COMBAT)
public class AutoGApple extends Module {
    public final NumberSetting health = new NumberSetting("Health", this, 1, 19, 3, 1);
    public final NumberSetting delay = new NumberSetting("Delay", this, 100, 1000, 350, 50);
    public final BooleanSetting spoof = new BooleanSetting("Item spoof", this, true);
    private TimeUtil timer = new TimeUtil();
    private TimeUtil eatTimer = new TimeUtil();
    private int prevSlot = -1;
    private int slotID;

    @Handler
    public final void onTick(final EventTick eventTick) {
        if (this.spoof.get() && prevSlot != -1) {
            Mega.INSTANCE.getSlotSpoofHandler().startSpoofing(prevSlot);
        }
        ItemStack itemStack = this.getItemStack();
        if (this.prevSlot < 0 && MC.thePlayer.getHealth() < (float)this.health.getAsInt() && !MC.thePlayer.isPotionActive(Potion.regeneration.id) && this.timer.delay(this.delay.getAsLong()) && (AuraUtil.getTarget() == null || !KillAura.getInstance().isToggled()) && itemStack != null) {
            this.prevSlot = MC.thePlayer.inventory.currentItem;
            MC.thePlayer.inventory.setCurrentItem(itemStack.getItem(), 0, false, false);
            MC.gameSettings.keyBindUseItem.pressed = true;
            this.eatTimer.reset();
            this.timer.reset();
        } else if (this.prevSlot >= 0 && this.eatTimer.getDifference() > 1750L) {
            MC.playerController.onStoppedUsingItem(MC.thePlayer);
            MC.gameSettings.keyBindUseItem.pressed = false;
            Mega.INSTANCE.getSlotSpoofHandler().stopSpoofing();
            MC.thePlayer.inventory.setCurrentItem(MC.thePlayer.inventory.getStackInSlot(prevSlot).getItem(), 0, false, false);
            this.prevSlot = -1;
            this.timer.reset();
        }
    }

    private ItemStack getItemStack() {
        ItemStack itemStack = null;

        // Search for enchanted golden apples first (ID 322, Data Value 1) in hotbar
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = MC.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemAppleGold && stack.getMetadata() == 1 && stack.stackSize > 0) {
                this.slotID = i;
                itemStack = stack;
                break;
            }
        }

        // If no enchanted golden apples were found, search for normal golden apples (ID 322, Data Value 0) in hotbar
        if (itemStack == null) {
            for (int i = 0; i < 9; ++i) {
                ItemStack stack = MC.thePlayer.inventory.getStackInSlot(i);
                if (stack != null && stack.getItem() instanceof ItemAppleGold && stack.getMetadata() == 0 && stack.stackSize > 0) {
                    this.slotID = i;
                    itemStack = stack;
                    break;
                }
            }
        }

        return itemStack;
    }

    public boolean isEating() {
        return this.prevSlot >= 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (MC.thePlayer != null && MC.theWorld != null) {
            Mega.INSTANCE.getSlotSpoofHandler().stopSpoofing();
        }
    }
}
