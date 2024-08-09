package club.mega.module.impl.movement;

import club.mega.Mega;
import club.mega.event.impl.*;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ListSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.util.ChatUtil;
import club.mega.util.TimeUtil;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "NoSlow", description = "Removes block and eat slowdown", category = Category.MOVEMENT)
public class NoSlow extends Module {
    public BooleanSetting startSlow = new BooleanSetting("StartSlow", this, false);
    public BooleanSetting slowdown = new BooleanSetting("Slowdown", this, false);
    public BooleanSetting sprint = new BooleanSetting("Sprint", this, false);
    public BooleanSetting switchh = new BooleanSetting("Switch", this, false);
    public BooleanSetting toggle = new BooleanSetting("Toggle", this, false);
    public BooleanSetting bug = new BooleanSetting("Bug", this, false);
    public BooleanSetting timer = new BooleanSetting("Timer", this, false);


    public final NumberSetting forward = new NumberSetting("Forward", this, 0.0, 2, 0.2, 0.1);
    public final NumberSetting strafe = new NumberSetting("Strafe", this, 0.0, 2, 0.2, 0.1);
    public final NumberSetting timerSpeed = new NumberSetting("TimerSpeed", this, 0.0, 2, 0.2, 0.1);

    private int counter = 0;
    private ItemStack lastItemStack = null;
    private final TimeUtil timeHelper = new TimeUtil();

    @Handler
    public void onEventNoSlow(EventNoSlow eventNoSlow) {
        ItemStack currentItem = MC.thePlayer.getCurrentEquippedItem();
        if (currentItem == null || !MC.thePlayer.isUsingItem() || MC.thePlayer.moveForward == 0.0F && MC.thePlayer.moveStrafing == 0.0F) {
            this.timeHelper.reset();
        } else if (this.timeHelper.hasTimePassed(400L) || !this.startSlow.get()) {

                eventNoSlow.setSprint(this.sprint.get());
                if (this.slowdown.get()) {
                    eventNoSlow.setMoveForward((float)this.forward.getAsFloat());
                    eventNoSlow.setMoveStrafe((float)this.strafe.getAsFloat());
                }

        }

    }

    @Handler
    public void onEventPreMotion(EventPreTick eventPreMotion) {
        ItemStack currentItem = MC.thePlayer.getCurrentEquippedItem();
        if (currentItem != null && MC.thePlayer.isUsingItem() && (MC.thePlayer.moveForward != 0.0F || MC.thePlayer.moveStrafing != 0.0F)) {
            if (currentItem.getItem() instanceof ItemSword) {
                if (this.toggle.get()) {
                    MC.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                }

                if (this.switchh.get()) {
                    int slotIDtoSwitch = MC.thePlayer.inventory.currentItem >= 7 ? MC.thePlayer.inventory.currentItem - 2 : MC.thePlayer.inventory.currentItem + 2;
                    MC.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(slotIDtoSwitch));
                    MC.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(MC.thePlayer.inventory.currentItem));
                }
            } else if (currentItem.getItem() instanceof ItemBow) {
                if (this.toggle.get()) {
                    MC.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                }
            } else if (this.toggle.get()) {
                MC.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            }
        }

    }

    @Handler
    public void onEventPostMotion(EventPostMotion eventPostMotion) {
        ItemStack currentItem = MC.thePlayer.getCurrentEquippedItem();
        if (currentItem != null && MC.thePlayer.isUsingItem() && (MC.thePlayer.moveForward != 0.0F || MC.thePlayer.moveStrafing != 0.0F)) {
            if (currentItem.getItem() instanceof ItemSword) {
                if (this.toggle.get()) {
                    MC.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(MC.thePlayer.inventory.getCurrentItem()));
                }
            } else if (currentItem.getItem() instanceof ItemBow) {
                if (this.toggle.get()) {
                    MC.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(MC.thePlayer.inventory.getCurrentItem()));
                }
            } else if (this.toggle.get()) {
                MC.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(MC.thePlayer.inventory.getCurrentItem()));
            }
        }

    }

    @Handler
    public void onEventUpdate(EventTick eventUpdate) {
        ItemStack currentItem = MC.thePlayer.getCurrentEquippedItem();
        if (currentItem != null && MC.thePlayer.isUsingItem() && (MC.thePlayer.moveForward != 0.0F || MC.thePlayer.moveStrafing != 0.0F)) {
            if (currentItem.getItem() instanceof ItemSword) {
                if (this.timer.get()) {
                    MC.getTimer().timerSpeed = (float)this.timerSpeed.getAsDouble();
                } else {
                    MC.getTimer().timerSpeed = 1.0F;
                }
            } else {
                int slotIDtoSwitch;
                if (currentItem.getItem() instanceof ItemBow) {
                    if (this.timer.get()) {
                        MC.getTimer().timerSpeed = (float)this.timerSpeed.getAsDouble();
                    } else {
                        MC.getTimer().timerSpeed = 1.0F;
                    }

                    if (this.switchh.get()) {
                        slotIDtoSwitch = MC.thePlayer.inventory.currentItem >= 7 ? MC.thePlayer.inventory.currentItem - 2 : MC.thePlayer.inventory.currentItem + 2;
                        MC.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(slotIDtoSwitch));
                        MC.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(MC.thePlayer.inventory.currentItem));
                    }
                } else {
                    if (this.timer.get()) {
                        MC.getTimer().timerSpeed = (float)this.timerSpeed.getAsDouble();
                    } else {
                        MC.getTimer().timerSpeed = 1.0F;
                    }

                    if (this.switchh.get()) {
                        slotIDtoSwitch = MC.thePlayer.inventory.currentItem >= 7 ? MC.thePlayer.inventory.currentItem - 2 : MC.thePlayer.inventory.currentItem + 2;
                        ChatUtil.sendMessage(slotIDtoSwitch);
                        MC.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(slotIDtoSwitch));
                        MC.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(MC.thePlayer.inventory.currentItem));
                    }
                }
            }
        }

    }

    @Handler
    public void onEventClick(EventClickMouse eventClick) {
        ItemStack currentItem = MC.thePlayer.getCurrentEquippedItem();
        if (currentItem == null || !MC.thePlayer.isUsingItem() || MC.thePlayer.moveForward == 0.0F && MC.thePlayer.moveStrafing == 0.0F) {
            this.counter = 0;
        } else {
            if (this.lastItemStack != null && !this.lastItemStack.equals(currentItem)) {
                this.counter = 0;
            }

            if (currentItem.getItem() instanceof ItemSword) {
                if (this.bug.get()) {
                    eventClick.setShouldRightClick(false);
                    if (this.counter != 1) {
                        MC.thePlayer.sendQueue.addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                        MC.thePlayer.stopUsingItem();
                        MC.thePlayer.closeScreen();
                        eventClick.setCancelled(true);
                        this.counter = 1;
                    }
                }
            } else if (!(currentItem.getItem() instanceof ItemBow) && this.bug.get()) {
                eventClick.setShouldRightClick(false);
                if (this.counter != 3) {
                    MC.thePlayer.sendQueue.addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                    MC.thePlayer.stopUsingItem();
                    MC.thePlayer.closeScreen();
                    eventClick.setCancelled(true);
                    this.counter = 3;
                }
            }

            if (eventClick.isCancelled()) {
                MC.sendClickBlockToController(MC.currentScreen == null && MC.gameSettings.keyBindAttack.isKeyDown() && MC.inGameHasFocus);
            }

            this.lastItemStack = currentItem;
        }

    }

    @Override
    public void onDisable() {
        super.onDisable();
        MC.getTimer().timerSpeed = 1.0F;
    }
    public static NoSlow getInstance() {
        return Mega.INSTANCE.getModuleManager().getModule(NoSlow.class);
    }
}
