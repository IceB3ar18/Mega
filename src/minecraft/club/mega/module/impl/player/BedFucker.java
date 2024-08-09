package club.mega.module.impl.player;

import club.mega.event.impl.*;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.impl.combat.KillAura;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ListSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.util.*;
import net.minecraft.block.Block;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.*;
import rip.hippo.lwjeb.annotation.Handler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

@Module.ModuleInfo(name = "Bedfucker", description = "Breaks Beds automaticly", category = Category.PLAYER)
public class BedFucker extends Module {
    private final TimeUtil timeHelper = new TimeUtil();
    public ArrayList<Packet> packets = new ArrayList();
    public BlockPos b;
    private RotationUtil rotationUtil = new RotationUtil();
    private float[] rots = new float[2];
    private float[] lastRots = new float[2];
    private EnumFacing lastEnumFacing;
    private int slotID;
    private boolean breaking = false;
    private INetHandler netHandler = null;

    public final ListSetting block = new ListSetting("Block", this, new String[]{"Bed", "Cake", "Custom"});
    public final ListSetting action = new ListSetting("Action", this, new String[]{"Break", "Click", "Use"});
    public final BooleanSetting troughWall = new BooleanSetting("ThroughWall", this, true);
    public final BooleanSetting instant = new BooleanSetting("Instant", this, true);
    public final BooleanSetting moveFix = new BooleanSetting("MoveFix", this, true);
    public final BooleanSetting myBed = new BooleanSetting("FriendMyBlock", this, true);
    public final NumberSetting customID = new NumberSetting("CustomID", this, 0, 400, 26, 1);
    public final NumberSetting yawSpeed = new NumberSetting("YawSpeed", this, 0, 180, 180, 1);
    public final NumberSetting pitchSpeed = new NumberSetting("PitchSpeed", this, 0, 180, 180, 1);
    public final NumberSetting delay = new NumberSetting("Delay", this, 0, 1000.0, 500.0, 1);
    public final NumberSetting range = new NumberSetting("Range", this, 0, 6.0, 4.5, 0.1);

    @Override
    public void onEnable() {
        super.onEnable();
        this.rotationUtil = new RotationUtil();
        if (MC.thePlayer != null) {
            this.breaking = false;
            this.rots[0] = MC.thePlayer.rotationYaw;
            this.rots[1] = MC.thePlayer.rotationPitch;
            this.lastRots[0] = MC.thePlayer.prevRotationYaw;
            this.lastRots[1] = MC.thePlayer.prevRotationPitch;
            this.slotID = MC.thePlayer.inventory.currentItem + 36;
            this.b = null;
            this.lastEnumFacing = null;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (this.slotID != MC.thePlayer.inventory.currentItem + 36) {
            this.slotID = MC.thePlayer.inventory.currentItem + 36;
        }

        if (this.breaking) {
            KeyBinding.setKeyBindState(MC.gameSettings.keyBindAttack.getKeyCode(), false);
            this.breaking = false;
        }

        this.resetPackets(this.netHandler);
    }


    @Handler
    public void onEventEarlyTick(EventEarlyTick eventEarlyTick) {
        Vec3 bedVec = null;
        if ((!KillAura.getInstance().isToggled() || AuraUtil.getTarget() == null ) && !Scaffold.getInstance().isToggled()) {
            bedVec = this.getBedPos();
            this.b = bedVec == null ? null : new BlockPos(bedVec);
        } else {
            this.b = null;
        }

        if (this.b == null) {
            this.lastRots = this.rots;
        } else {
            float yawSpeed = (float)(this.yawSpeed.getAsFloat() / 200 + (double) RandomUtil.nextFloat(0.0F, 15.0F));
            float pitchSpeed = (float)(this.pitchSpeed.getAsFloat() + (double)RandomUtil.nextFloat(0.0F, 15.0F));
            Block block = MC.theWorld.getBlockState(this.b).getBlock();
            float[] floats = this.rotationUtil.rotateToBlockPos((double)this.b.getX() + block.getBlockBoundsMaxX() / 2.0, (double)this.b.getY() + block.getBlockBoundsMaxY() / 2.0, (double)this.b.getZ() + block.getBlockBoundsMaxZ() / 2.0, this.rots[0], this.rots[1], yawSpeed, pitchSpeed, false);
            MovingObjectPosition objectPosition = RayCastUtil.rayCast(1.0F, floats);
            if (objectPosition != null && objectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && !this.isValidBlock(objectPosition.getBlockPos(), this.getId())) {
                for(float yaw = this.rots[0] - Math.min(yawSpeed, 40.0F); yaw < this.rots[0] + Math.min(yawSpeed, 40.0F); yaw += 2.0F) {
                    for(float pitch = MathHelper.clamp_float(this.rots[1] - Math.min(pitchSpeed, 30.0F), -90.0F, 90.0F); pitch < MathHelper.clamp_float(this.rots[1] + Math.min(pitchSpeed, 30.0F), -90.0F, 90.0F); pitch += 2.0F) {
                        float[] sensedRots = RotationUtil.mouseSens(yaw, pitch, this.rots[0], this.rots[1]);
                        MovingObjectPosition objectPosition1 = RayCastUtil.rayCast(1.0F, sensedRots);
                        if (objectPosition1 != null && objectPosition1.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && this.isValidBlock(objectPosition1.getBlockPos(), this.getId())) {
                            this.lastRots = this.rots;
                            this.rots = sensedRots;
                            return;
                        }
                    }
                }
            }

            this.lastRots = this.rots;
            this.rots = floats;
        }
    }

    @Handler
    public void onEventClick(EventClickMouse eventClick) {
        if ((!KillAura.getInstance().isToggled() || AuraUtil.getTarget() == null) && !Scaffold.getInstance().isToggled()) {
            boolean bb = false;
            if (this.b != null) {
                this.breaking = false;
                if (this.troughWall.get()) {
                    eventClick.setCancelled(true);
                    if (MC.thePlayer.isUsingItem()) {
                        MC.thePlayer.stopUsingItem();
                        MC.sendClickBlockToController(MC.currentScreen == null && MC.gameSettings.keyBindAttack.isKeyDown() && MC.inGameHasFocus);
                        return;
                    }

                    this.slotID = MC.thePlayer.inventory.currentItem + 36;
                    MovingObjectPosition movingObjectPosition = RayCastUtil.getHitVec(this.b, this.rots[0], this.rots[1], this.range.getAsFloat());
                    if (movingObjectPosition != null && movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                        switch (this.action.getCurrent()) {
                            case "Break":
                                if (this.instant.get()) {
                                    this.breakInstant(this.b, movingObjectPosition.sideHit);
                                } else {
                                    MC.sendClickBlockToControllerCustom(MC.currentScreen == null && MC.inGameHasFocus, movingObjectPosition, this.b, this.slotID - 36);
                                    this.lastEnumFacing = movingObjectPosition.sideHit;
                                }
                                break;
                            case "Use":
                                if (this.timeHelper.hasTimePassed((long)this.delay.getAsLong()) && MC.playerController.onPlayerRightClick(MC.thePlayer, MC.theWorld, MC.thePlayer.getHeldItem(), this.b, movingObjectPosition.sideHit, movingObjectPosition.hitVec)) {
                                    MC.thePlayer.swingItem();
                                    this.timeHelper.reset();
                                }
                                break;
                            default:
                                if (this.timeHelper.hasTimePassed((long)this.delay.getAsLong())) {
                                    MC.thePlayer.swingItem();
                                    MC.playerController.clickBlock(this.b, movingObjectPosition.sideHit);
                                    this.timeHelper.reset();
                                }
                        }
                    } else if (this.lastEnumFacing != null) {
                        MC.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.b, this.lastEnumFacing));
                    }
                } else {
                    int bestItemID = this.getBestItem();
                    if (bestItemID != -1) {
                        eventClick.setSlot(bestItemID - 36);
                        this.slotID = bestItemID;
                    } else {
                        this.slotID = MC.thePlayer.inventory.currentItem + 36;
                    }

                    if (MC.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && !MC.objectMouseOver.getBlockPos().equals(this.b)) {
                        KeyBinding.setKeyBindState(MC.gameSettings.keyBindAttack.getKeyCode(), true);
                        this.breaking = true;
                    } else {
                        MovingObjectPosition movingObjectPosition = MC.objectMouseOver;
                        if (movingObjectPosition != null) {
                            switch (this.action.getCurrent()) {
                                case "Break":
                                    if (this.instant.get()) {
                                        this.breakInstant(this.b, movingObjectPosition.sideHit);
                                    } else {
                                        bb = true;
                                        this.lastEnumFacing = movingObjectPosition.sideHit;
                                        KeyBinding.setKeyBindState(MC.gameSettings.keyBindAttack.getKeyCode(), true);
                                        this.breaking = true;
                                    }
                                    break;
                                case "Use":
                                    if (this.timeHelper.hasTimePassed(this.delay.getAsLong()) && MC.playerController.onPlayerRightClick(MC.thePlayer, MC.theWorld, MC.thePlayer.getHeldItem(), this.b, movingObjectPosition.sideHit, movingObjectPosition.hitVec)) {
                                        MC.thePlayer.swingItem();
                                        this.timeHelper.reset();
                                    }
                                    break;
                                default:
                                    if (this.timeHelper.hasTimePassed(this.delay.getAsLong())) {
                                        MC.thePlayer.swingItem();
                                        MC.playerController.clickBlock(this.b, movingObjectPosition.sideHit);
                                        this.timeHelper.reset();
                                    }
                            }
                        }
                    }
                }
            } else {
                if (this.breaking) {
                    KeyBinding.setKeyBindState(MC.gameSettings.keyBindAttack.getKeyCode(), false);
                    this.breaking = false;
                }

                this.slotID = MC.thePlayer.inventory.currentItem + 36;
            }
        } else {
            this.slotID = MC.thePlayer.inventory.currentItem + 36;
        }

    }

    @Handler
    public final void moveFlying(final EventMoveFlying event) {
        if (this.b == null || !moveFix.get())
            return;

        event.setYaw(this.rots[0]);
    }

    private int getBestItem() {
        float maxStrength = 0.0F;
        int bestItem = -1;

        for(int i = 36; i < MC.thePlayer.inventoryContainer.inventorySlots.size(); ++i) {
            ItemStack itemStack = MC.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (itemStack != null && itemStack.getItem() instanceof ItemTool && MC.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                Block block = MC.theWorld.getBlockState(MC.objectMouseOver.getBlockPos()).getBlock();
                if (this.getToolSpeed(itemStack, block) > maxStrength) {
                    maxStrength = this.getToolSpeed(itemStack, block);
                    bestItem = i;
                }
            }
        }

        return bestItem;
    }

    private float getToolSpeed(ItemStack itemStack, Block block) {
        float damage = 0.0F;
        if (itemStack.getItem() instanceof ItemTool) {
            damage += itemStack.getItem().getStrVsBlock(itemStack, block) + (float) EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack);
            damage = (float)((double)damage + (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, itemStack) / 11.0);
            damage = (float)((double)damage + (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.silkTouch.effectId, itemStack) / 11.0);
            damage = (float)((double)damage + (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack) / 33.0);
        }
        return damage;
    }

    @Handler
    public final void look(final EventLook event) {
        if (MC.currentScreen == null && this.b != null) {
            MC.thePlayer.rotationYawHead = this.rots[0];
            MC.thePlayer.renderYawOffset = this.rots[0];
            event.setRotations(new float[]{this.rots[0], this.rots[1]});
        }
    }
    @Handler
    public final void renderPitch(final EventRenderPitch event) {

        if (MC.currentScreen == null && this.b != null) {
            event.setPitch(rots[1]);
            RotationUtil rotationUtil1 = new RotationUtil();
            rotationUtil1.setPitch(this.rots[1]);
        }
    }

    private Vec3 getBedPos() {
        BlockPos b = new BlockPos(MC.thePlayer.posX, MC.thePlayer.posY, MC.thePlayer.posZ);
        ArrayList<Vec3> positions = new ArrayList();
        HashMap<Vec3, BlockPos> map = new HashMap();
        int d = (int)(this.range.getAsFloat() + 1.0);

        for(int x = b.getX() - d; x < b.getX() + d; ++x) {
            for(int y = b.getY() - d; y < b.getY() + d; ++y) {
                for(int z = b.getZ() - d; z < b.getZ() + d; ++z) {
                    if (this.isValidBlock(new BlockPos(x, y, z), this.getId())) {
                        BlockPos blockPos = new BlockPos(x, y, z);
                        Vec3 vec3 = new Vec3((double)x, (double)y, (double)z);
                        if (this.myBed.get() && MC.thePlayer.getSpawnPos() != null) {
                            if (MC.thePlayer.getSpawnPos().distanceTo(vec3) > 20.0) {
                                positions.add(vec3);
                                map.put(vec3, blockPos);
                            }
                        } else {
                            positions.add(vec3);
                            map.put(vec3, blockPos);
                        }
                    }
                }
            }
        }

        positions.sort(Comparator.comparingDouble((vec3x) -> {
            return MC.thePlayer.getDistance(vec3x.xCoord, vec3x.yCoord, vec3x.zCoord);
        }));
        if (!positions.isEmpty()) {
            return (Vec3)positions.get(0);
        } else {
            return null;
        }
    }

    private int getId() {
        int id;
        switch (this.block.getCurrent()) {
            case "Bed":
                id = 26;
                break;
            case "Cake":
                id = 92;
                break;
            default:
                id = (int)this.customID.getAsInt();
        }

        return id;
    }

    private boolean isValidBlock(BlockPos blockPos, int id) {
        if (blockPos != null) {
            Block block = MC.theWorld.getBlockState(blockPos).getBlock();
            return block.equals(Block.getBlockById(id));
        } else {
            return false;
        }
    }

    private void breakInstant(BlockPos blockPos, EnumFacing enumFacing) {
        if (this.timeHelper.hasTimePassed((long)this.delay.getAsLong())) {
            MC.thePlayer.swingItem();
            MC.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos, enumFacing));
            MC.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos, enumFacing));
            this.timeHelper.reset();
        }

    }

    public int getSlotID() {
        return this.slotID - 36;
    }

    public void resetPackets(INetHandler iNetHandler) {
        if (this.packets.size() > 0) {
            for(; this.packets.size() != 0; this.packets.remove(this.packets.get(0))) {
                try {
                    ((Packet)this.packets.get(0)).processPacket(iNetHandler);
                } catch (ThreadQuickExitException var3) {
                }
            }
        }

    }
}
