package club.mega.module.impl.player;

import club.mega.Mega;
import club.mega.event.impl.EventClickMouse;
import club.mega.event.impl.EventPostMouseOver;
import club.mega.event.impl.EventPreTick;
import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ListSetting;
import club.mega.util.TimeUtil;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.*;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "AutoTool", description = "Switches to tools automaticly", category = Category.PLAYER)
public class AutoTool extends Module {
    private int oldSlot;
    private boolean wasDigging;
    public final BooleanSetting spoof = new BooleanSetting("Item spoof", this, true);
    
    
    @Handler
    public void onTick(EventTick eventTick) {
        if ( MC.currentScreen == null && (Mouse.isButtonDown(0) || MC.gameSettings.keyBindAttack.isKeyDown()) && MC.objectMouseOver != null && MC.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            Block block = MC.theWorld.getBlockState(MC.objectMouseOver.getBlockPos()).getBlock();
            float strength = 0.0F;
            if (!this.wasDigging) {
                this.oldSlot = MC.thePlayer.inventory.currentItem;
                if (this.spoof.get()) {
                    Mega.INSTANCE.getSlotSpoofHandler().startSpoofing(this.oldSlot);
                }
            }

            for(int i = 0; i <= 8; ++i) {
                ItemStack stack = MC.thePlayer.inventory.getStackInSlot(i);
                if (stack != null) {
                    float slotStrength = stack.getStrVsBlock(block);
                    if (slotStrength > strength) {
                        MC.thePlayer.inventory.currentItem = i;
                        strength = slotStrength;
                    }
                }
            }

            this.wasDigging = true;
        } else if (this.wasDigging) {
            MC.thePlayer.inventory.currentItem = this.oldSlot;
            Mega.INSTANCE.getSlotSpoofHandler().stopSpoofing();
            this.wasDigging = false;
        } else {
            this.oldSlot = MC.thePlayer.inventory.currentItem;
        }

    }
    

    @Override
    public void onEnable()   {
        super.onEnable();
        if (MC.thePlayer != null && MC.theWorld != null) {
            
        }
    }

    @Override
    public void onDisable()   {
        super.onDisable();
        if (MC.thePlayer != null && MC.theWorld != null) {
            if (this.wasDigging) {
                MC.thePlayer.inventory.currentItem = this.oldSlot;
                this.wasDigging = false;
            }

            Mega.INSTANCE.getSlotSpoofHandler().stopSpoofing();
        }
    }
}
