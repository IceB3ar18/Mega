package club.mega.module.impl.player;

import club.mega.Mega;
import club.mega.event.impl.*;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.impl.combat.Criticals;
import club.mega.module.impl.combat.KillAura;
import club.mega.module.impl.combat.RotationHandler;
import club.mega.module.impl.movement.Flight;
import club.mega.module.setting.impl.ListSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.module.setting.impl.RangeSetting;
import club.mega.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import rip.hippo.lwjeb.annotation.Handler;

import java.security.SecureRandom;

@Module.ModuleInfo(name = "NoFall", description = "Remove fall damage", category = Category.PLAYER)
public class NoFall extends Module {
    public final ListSetting mode = new ListSetting("Mode", this, new String[]{"Legit", "SpoofGround", "OnGround", "NoGround", "Vulcan", "ACR"});
    public final NumberSetting fallDistance = new NumberSetting("MinFallDist", this, 1.0, 24.0, 2.0, 1, () -> (!mode.is("OnGround")));
    public final NumberSetting lookRange = new NumberSetting("AimRange", this, 1.0, 24.0, 2.0, 1, () -> mode.is("Legit"));
    private final RangeSetting yawSpeed = new RangeSetting("YawSpeed", this, 1.0, 180.0, 30.0, 50.0, 1.0, () -> mode.is("Legit"));
    private final RangeSetting pitchSpeed = new RangeSetting("PitchSpeed", this, 1.0, 180.0, 30.0, 50.0, 1.0, () -> mode.is("Legit"));
    private final RangeSetting delay = new RangeSetting("Delay", this, 0.00, 2000.0, 300, 500, 1.0, () -> mode.is("Legit"));

    private boolean clickTimer;
    private boolean should;
    private BlockPos b;
    private BlockPos waterPos;
    private boolean rotated;
    private int slotID = 0;
    private final TimeUtil timeHelper = new TimeUtil();
    private final TimeUtil timeHelper2 = new TimeUtil();
    private RotationUtil rotationUtil = new RotationUtil();
    @Handler
    public final void packet(final EventPacket packetEvent) {
        if (MC.theWorld != null) {
            if (packetEvent.getType() == EventPacket.Type.SEND) {
                Packet packet = packetEvent.getPacket();

                switch (this.mode.getCurrent()) {
                    case "Vulcan":

                        if (packet instanceof C03PacketPlayer && !Mega.INSTANCE.getModuleManager().getModule(Flight.class).isToggled()) {
                            C03PacketPlayer event = (C03PacketPlayer) packet;
                            if (MC.thePlayer.fallDistance >= fallDistance.getAsDouble()) {

                                MC.thePlayer.fallDistance = 0;
                                MC.thePlayer.motionY = -10;
                                MC.thePlayer.motionX = 0;
                                MC.thePlayer.motionZ = 0;
                                event.onGround = true;

                            }
                        }
                        break;
                    case "NoGround":
                        if(packet instanceof C03PacketPlayer) {
                            C03PacketPlayer c03PacketPlayer = (C03PacketPlayer) packet;
                            c03PacketPlayer.setOnGround(false);
                        }
                }
            }
        }
    }

    @Handler
    public final void preTick(final EventPreTick event) {
        if (mode.is("SpoofGround")) {
            if (MC.thePlayer.fallDistance >= fallDistance.getAsDouble() && !Mega.INSTANCE.getModuleManager().isToggled(Criticals.class)) {
                event.setOnGround(true);
            }
        }
        if (this.mode.is("Legit") && MC.thePlayer != null) {
            int bestItem = this.getItem();
            if (bestItem != -1) {
                if (MC.thePlayer.inventoryContainer.getSlot(bestItem + 36).getStack().getItem().equals(Item.getItemById(325)) && MC.thePlayer.isInWater()) {
                    if (this.waterPos == null) {
                        this.waterPos = this.getWaterPos();
                    }
                    this.b = this.waterPos;
                } else {
                    if ((double)MC.thePlayer.fallDistance < this.fallDistance.getAsDouble()) {
                        this.rotated = false;
                        return;
                    }

                    this.b = this.getBlockPos();
                }

                if (this.b != null) {
                    SecureRandom secureRandom = new SecureRandom();
                    float deltaYaw = (RandomUtil.nextFloat(this.yawSpeed.getCurrentMin() - 0.0010000000474974513, this.yawSpeed.getCurrentMax()) / 2.0F + secureRandom.nextFloat() + RandomUtil.nextFloat(this.yawSpeed.getCurrentMin() - 0.0010000000474974513, this.yawSpeed.getCurrentMax()) / 2.0F) * 0.5F;
                    float deltaPitch = (RandomUtil.nextFloat(this.pitchSpeed.getCurrentMin() - 0.0010000000474974513, this.pitchSpeed.getCurrentMax()) / 2.0F + secureRandom.nextFloat() + RandomUtil.nextFloat(this.pitchSpeed.getCurrentMin() - 0.0010000000474974513, this.pitchSpeed.getCurrentMax()) / 2.0F) * 0.5F;

                    RotationUtil.setRotations(this.rotationUtil.rotateToBlockPos((double)this.b.getX() + 0.5, (double)(this.b.getY() + 1), (double)this.b.getZ() + 0.5, RotationUtil.getPrevRotations()[0], RotationUtil.getPrevRotations()[1], deltaYaw, deltaPitch, true));
                    if (MC.thePlayer.inventoryContainer.getSlot(bestItem + 36).getStack().getItem().equals(Item.getItemById(325)) && MC.thePlayer.isInWater()) {
                        event.setYaw(RotationUtil.getRotations()[0]);
                        MC.thePlayer.rotationYawHead = event.getRotations()[0];
                        MC.thePlayer.renderYawOffset = event.getRotations()[0];
                    }
                    Mega.INSTANCE.getModuleManager().getModule(RotationHandler.class).setBackRotated(false);
                    rotated = true;
                    event.setPitch(RotationUtil.getRotations()[1]);
                    RotationUtil.setPrevRotations(RotationUtil.getRotations());
                }
            }
        }
    }


    @Handler
    public final void moveFlying(final EventMoveFlying event) {
        if (RotationUtil.getRotations() == null  || Scaffold.getInstance().isToggled() || (KillAura.getInstance().isToggled() && AuraUtil.getTarget() != null))
            return;
        if (this.mode.is("Legit") && MC.thePlayer != null) {
            int bestItem = this.getItem();
            if (bestItem != -1) {
                if (MC.thePlayer.inventoryContainer.getSlot(bestItem + 36).getStack().getItem().equals(Item.getItemById(325)) && MC.thePlayer.isInWater()) {
                    if (this.waterPos == null) {
                        this.waterPos = this.getWaterPos();
                    }
                    this.b = this.waterPos;
                } else {
                    if ((double)MC.thePlayer.fallDistance < this.fallDistance.getAsDouble()) {
                        this.rotated = false;
                        return;
                    }

                    this.b = this.getBlockPos();
                }

                if (this.b != null) {
                    SecureRandom secureRandom = new SecureRandom();
                    float deltaYaw = (RandomUtil.nextFloat(this.yawSpeed.getCurrentMin() - 0.0010000000474974513, this.yawSpeed.getCurrentMax()) / 2.0F + secureRandom.nextFloat() + RandomUtil.nextFloat(this.yawSpeed.getCurrentMin() - 0.0010000000474974513, this.yawSpeed.getCurrentMax()) / 2.0F) * 0.5F;
                    float deltaPitch = (RandomUtil.nextFloat(this.pitchSpeed.getCurrentMin() - 0.0010000000474974513, this.pitchSpeed.getCurrentMax()) / 2.0F + secureRandom.nextFloat() + RandomUtil.nextFloat(this.pitchSpeed.getCurrentMin() - 0.0010000000474974513, this.pitchSpeed.getCurrentMax()) / 2.0F) * 0.5F;

                    RotationUtil.setRotations(this.rotationUtil.rotateToBlockPos((double)this.b.getX() + 0.5, (double)(this.b.getY() + 1), (double)this.b.getZ() + 0.5, RotationUtil.getPrevRotations()[0], RotationUtil.getPrevRotations()[1], deltaYaw, deltaPitch, true));
                    if (MC.thePlayer.inventoryContainer.getSlot(bestItem + 36).getStack().getItem().equals(Item.getItemById(325)) && MC.thePlayer.isInWater()) {
                        event.setYaw(RotationUtil.getRotations()[0]);
                        Mega.INSTANCE.getModuleManager().getModule(RotationHandler.class).setBackRotated(false);
                    }

                }
            }
        }
    }

    @Handler
    public final void tick(final EventTick event) {
        if (MC.thePlayer != null) {
            switch (this.mode.getCurrent()) {
                case "OnGround":
                    if ((double)MC.thePlayer.fallDistance > this.fallDistance.getAsDouble()) {
                        MC.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
                    }
                    break;
                case "ACR":
                    if (this.should && SigmaMoveUtils.isOnGround(fallDistance.getAsFloat()) && MC.thePlayer.fallDistance > fallDistance.getAsFloat()) {
                        this.should = false;
                        MC.thePlayer.setPosition(MC.thePlayer.posX + 1.0, MC.thePlayer.posY, MC.thePlayer.posZ + 1.0);
                    } else if (MC.thePlayer.onGround && SigmaMoveUtils.isOnGround(0.01)) {
                        this.should = true;
                    }
            }
        }

    }

    @Handler
    public void onEventClick(EventClickMouse eventClick) {
        long rdmDelay = RandomUtil.nextLong((long) delay.getCurrentMin(), (long)delay.getCurrentMax());
        if (this.mode.is("Legit") && this.b != null && this.rotated) {
            int currentItem = MC.thePlayer.inventory.currentItem;
            int bestItem = this.getItem();
            if (bestItem != this.slotID) {
                this.slotID = bestItem;
            }

            if (this.slotID != -1) {
                if (this.clickTimer) {
                    if (MC.thePlayer.inventoryContainer.getSlot(this.slotID + 36).getStack().getItem().equals(Item.getItemById(325)) && MC.thePlayer.isInWater()) {
                        if (this.timeHelper.hasTimePassed(rdmDelay + RandomUtil.nextLong(-20L, 20L)) && this.timeHelper2.hasTimePassed(360L + RandomUtil.nextLong(0L, 50L))) {
                            MC.thePlayer.inventory.currentItem = this.slotID;
                            MC.rightClickMouse();
                            MC.thePlayer.inventory.currentItem = currentItem;
                            this.clickTimer = false;
                            this.timeHelper.reset();
                            this.timeHelper2.reset();
                        }
                    } else if (!((double)MC.thePlayer.fallDistance < this.fallDistance.getAsDouble()) && MC.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && MC.objectMouseOver.getBlockPos().equals(this.b) && !MC.thePlayer.inventoryContainer.getSlot(this.slotID + 36).getStack().getItem().equals(Item.getItemById(325)) && this.timeHelper.hasTimePassed(rdmDelay)) {
                        if (MC.thePlayer.inventoryContainer.getSlot(this.slotID + 36).getStack().getItem().equals(Item.getItemById(326))) {
                            this.waterPos = new BlockPos(this.b.getX(), this.b.getY() + 1, this.b.getZ());
                        }

                        MC.thePlayer.inventory.currentItem = this.slotID;
                        MC.rightClickMouse();
                        MC.thePlayer.inventory.currentItem = currentItem;
                        this.clickTimer = false;
                        this.timeHelper.reset();
                        this.timeHelper2.reset();
                    }
                } else {
                    this.clickTimer = true;
                }
            }
        }

    }
    private BlockPos getWaterPos() {
        BlockPos b = new BlockPos(MC.thePlayer.posX, MC.thePlayer.posY, MC.thePlayer.posZ);

        for(int y = b.getY() + 1; y > b.getY() - 5; --y) {
            if (this.isValidBlock(new BlockPos(b.getX(), y, b.getZ()))) {
                return new BlockPos(b.getX(), y, b.getZ());
            }
        }

        return null;
    }
    private boolean isValidBlock(BlockPos blockPos) {
        Block block = MC.theWorld.getBlockState(blockPos).getBlock();
        return !(block instanceof BlockLiquid) && !(block instanceof BlockAir);
    }
    private BlockPos getBlockPos() {
        BlockPos b = new BlockPos(MC.thePlayer.posX + MC.thePlayer.motionX * 2.0, MC.thePlayer.posY, MC.thePlayer.posZ + MC.thePlayer.motionZ * 2.0);

        for(int y = b.getY() - 1; (double)y > (double)b.getY() - this.lookRange.getAsDouble() + 1.0; --y) {
            if (this.isValidBlock(new BlockPos(b.getX(), y, b.getZ()))) {
                return new BlockPos(b.getX(), y, b.getZ());
            }
        }

        return null;
    }
    private int getItem() {
        for(int i = 36; i < MC.thePlayer.inventoryContainer.inventorySlots.size(); ++i) {
            ItemStack itemStack = MC.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (itemStack != null && ((itemStack.getItem() == Item.getItemById(30) || itemStack.getItem() == Item.getItemById(326)) && !MC.thePlayer.isInWater() || itemStack.getItem() == Item.getItemById(325) && MC.thePlayer.fallDistance < 1.0F && MC.thePlayer.isInWater())) {
                return i - 36;
            }
        }

        return -1;
    }
    @Handler
    public void look(EventLook event) {
        if (this.mode.is("Legit") && MC.thePlayer != null) {
            int bestItem = this.getItem();
            if (bestItem != -1) {
                if (MC.thePlayer.inventoryContainer.getSlot(bestItem + 36).getStack().getItem().equals(Item.getItemById(325)) && MC.thePlayer.isInWater()) {
                    if (this.waterPos == null) {
                        this.waterPos = this.getWaterPos();
                    }
                    this.b = this.waterPos;
                } else {
                    if ((double)MC.thePlayer.fallDistance < this.fallDistance.getAsDouble()) {
                        this.rotated = false;
                        return;
                    }

                    this.b = this.getBlockPos();
                }

                if (this.b != null) {
                    if (MC.thePlayer.inventoryContainer.getSlot(bestItem + 36).getStack().getItem().equals(Item.getItemById(325)) && MC.thePlayer.isInWater()) {
                        event.setYaw(RotationUtil.getRotations()[0]);
                        MC.thePlayer.rotationYawHead = event.getRotations()[0];
                        MC.thePlayer.renderYawOffset = event.getRotations()[0];
                    }
                    rotated = true;
                    event.setPitch(RotationUtil.getRotations()[1]);
                    RotationUtil.setPrevRotations(RotationUtil.getRotations());
                }
            }
        }
    }

    @Handler
    public final void renderPitch(final EventRenderPitch event) {

        if (this.mode.is("Legit") && MC.thePlayer != null) {
            int bestItem = this.getItem();
            if (bestItem != -1) {
                if (MC.thePlayer.inventoryContainer.getSlot(bestItem + 36).getStack().getItem().equals(Item.getItemById(325)) && MC.thePlayer.isInWater()) {
                    if (this.waterPos == null) {
                        this.waterPos = this.getWaterPos();
                    }
                    this.b = this.waterPos;
                } else {
                    if ((double)MC.thePlayer.fallDistance < this.fallDistance.getAsDouble()) {
                        this.rotated = false;
                        return;
                    }

                    this.b = this.getBlockPos();
                }

                if (this.b != null) {

                    rotated = true;
                    event.setPitch(RotationUtil.getRotations()[1]);
                    RotationUtil.setPrevRotations(RotationUtil.getRotations());
                }
            }
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (MC.thePlayer != null) {
            this.slotID = MC.thePlayer.inventory.currentItem;
        }
    }
}
