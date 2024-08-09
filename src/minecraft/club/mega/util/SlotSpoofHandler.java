package club.mega.util;

import club.mega.interfaces.MinecraftInterface;
import net.minecraft.item.ItemStack;

public class SlotSpoofHandler implements MinecraftInterface {
    private int spoofedSlot;
    private boolean spoofing;

    public void startSpoofing(int slot) {
        this.spoofing = true;
        this.spoofedSlot = slot;
    }

    public void stopSpoofing() {
        this.spoofing = false;
    }

    public int getSpoofedSlot() {
        return this.spoofing ? this.spoofedSlot : MC.thePlayer.inventory.currentItem;
    }

    public ItemStack getSpoofedStack() {
        return this.spoofing ? MC.thePlayer.inventory.getStackInSlot(this.spoofedSlot) : MC.thePlayer.inventory.getCurrentItem();
    }

    public boolean isSpoofing() {
        return this.spoofing;
    }
}
