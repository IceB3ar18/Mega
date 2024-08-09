package club.mega.module.impl.player;

import club.mega.Mega;
import club.mega.event.impl.*;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.impl.combat.RotationHandler;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ListSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.module.setting.impl.RangeSetting;
import club.mega.util.*;
import club.mega.util.animation.AnimationUtil;
import com.sun.javafx.geom.Vec2d;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0CPacketInput;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import rip.hippo.lwjeb.annotation.Handler;

import java.awt.*;
import java.security.SecureClassLoader;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;


@Module.ModuleInfo(name = "Scaffold", description = "Places blocks below you", category = Category.PLAYER)
public class Scaffold extends Module {
    public final ListSetting rotateMode = new ListSetting("Mode", this, new String[]{"Normal", "GodBridge", "Snap", "MoonWalk"});
    private final RangeSetting yawSpeed = new RangeSetting("YawSpeed", this, 1.0, 180, 30, 50, 1.0);
    private final RangeSetting pitchSpeed = new RangeSetting("PitchSpeed", this, 1.0, 180, 30, 50, 1.0);
    public final NumberSetting backupTicks = new NumberSetting("Backup", this, 0, 3, 1, 1);
    public final BooleanSetting spamClick = new BooleanSetting("SpamClick", this, false);
    public final BooleanSetting intaveHit = new BooleanSetting("IntaveHit", this, false, spamClick::get);
    public final NumberSetting spamClickDelay = new NumberSetting("ClickDelay", this, 0.0, 100.0, 200.0, 1, () -> spamClick.get() && !intaveHit.get());
    public final BooleanSetting moveFix = new BooleanSetting("MoveFix", this, true);
    public final BooleanSetting silentMoveFix = new BooleanSetting("SilentMoveFix", this, true, () -> moveFix.get() && !rotateMode.is("MoonWalk"));
    public final BooleanSetting sameY = new BooleanSetting("SameY", this, false);
    public final BooleanSetting breezly = new BooleanSetting("Breezly", this, false, () -> rotateMode.is("Normal"));
    public final BooleanSetting autoJump = new BooleanSetting("AutoJump", this, true, () -> rotateMode.is("GodBridge"));
    public final NumberSetting autoJumpBlocks = new NumberSetting("Jump Delay", this, 1, 20, 8, 1, () -> autoJump.get() && autoJump.isVisible());
    public final BooleanSetting noSwing = new BooleanSetting("NoSwing", this, true);
    public final BooleanSetting predict = new BooleanSetting("Predict", this, true);
    public final BooleanSetting sprint = new BooleanSetting("Sprint", this, false);
    public final BooleanSetting sneak = new BooleanSetting("LegitSneak", this, true);
    public final ListSetting towerMode = new ListSetting("Tower", this, new String[]{"None", "Vanilla", "LowHop", "FastJump", "NCP", "AAC"}, () -> !sameY.get());
    public final BooleanSetting towerMove = new BooleanSetting("TowerMove", this, false, () -> !towerMode.is("None") && !sameY.get());
    public final ListSetting silentMode = new ListSetting("SilentMode", this, new String[]{"Switch", "Spoof", "None"});
    public final ListSetting blockCount = new ListSetting("Block Count", this, new String[]{"None", "Augustus"});
    private final TimeUtil hitTimeHelper = new TimeUtil();
    private final TimeUtil startTimeHelper = new TimeUtil();
    private final TimeUtil startTimeHelper2 = new TimeUtil();
    private final RotationUtil rotationUtil;
    private final HashMap<float[], MovingObjectPosition> hashMap = new HashMap();
    ArrayList<double[]> hitpoints = new ArrayList();
    private float[] lastRots = new float[]{0.0f, 0.0f};
    private float[] rots = new float[]{0.0f, 0.0f};
    private int slotID;
    public ItemStack block;
    private int sneakCounter = 4;
    private int randomDelay = 0;
    private int lastSlotID;
    private EnumFacing enumFacing;
    private BlockPos blockPos;
    private boolean start = true;
    private double[] xyz = new double[3];
    private int oldSlot;
    private final ArrayList<Vec3> lastPositions = new ArrayList();
    private static float[] useRots = new float[]{0, 0};
    private static float[] prevUseRots = new float[]{0, 0};
    private MovingObjectPosition objectPosition = null;
    private final TimeUtil sneakTimeHelper = new TimeUtil();
    private int isSneakingTicks = 0;
    private int offGroundTicks;
    public double currentWidth;
    private boolean hasSneaked = true;

    public enum Side {
        LEFT,
        RIGHT,
        NONE
    }

    public Side getPlayerSide() {
        if (blockPos != null) {
            EnumFacing dir = MC.thePlayer.getHorizontalFacing().rotateYCCW();

            double posX = MC.thePlayer.posX;
            double posZ = MC.thePlayer.posZ;

            double blockPosX = blockPos.getX() + 0.5;
            double blockPosZ = blockPos.getZ() + 0.5;

            if (dir == EnumFacing.WEST) { // Norden
                if (blockPosX >= posX) {
                    return Side.LEFT;
                } else if (blockPosX < posX) {
                    return Side.RIGHT;
                }
            } else if (dir == EnumFacing.EAST) { // Süden
                if (blockPosX <= posX) {
                    return Side.LEFT;
                } else if (blockPosX > posX) {
                    return Side.RIGHT;
                }
            } else if (dir == EnumFacing.NORTH) { // Osten
                if (blockPosZ <= posZ) {
                    return Side.RIGHT;
                } else if (blockPosZ > posZ) {
                    return Side.LEFT;
                }
            } else if (dir == EnumFacing.SOUTH) { // West
                if (blockPosZ >= posZ) {
                    return Side.RIGHT;
                } else if (blockPosZ < posZ) {
                    return Side.LEFT;
                }
            }
        }
        return Side.NONE;
    }

    private void switchToOriginalSlot() {
        if (!this.silentMode.is("None")) {
            MC.thePlayer.inventory.currentItem = this.oldSlot;
        }

        Mega.INSTANCE.getSlotSpoofHandler().stopSpoofing();
    }

    public Scaffold() {
        this.rotationUtil = new RotationUtil();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        count = 0;
        this.sneakCounter = 4;
        if (MC.thePlayer != null && MC.theWorld != null) {
            this.oldSlot = MC.thePlayer.inventory.currentItem;
            this.restRotation();
            this.slotID = MC.thePlayer.inventory.currentItem;
            this.lastSlotID = MC.thePlayer.inventory.currentItem;
            this.start = true;
            this.startTimeHelper.reset();
        }
        currentWidth = 8;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        count = 0;
        if (MC.thePlayer != null) {
            this.switchToOriginalSlot();
            this.slotID = MC.thePlayer.inventory.currentItem;
            MC.gameSettings.keyBindSneak.pressed = false;
            MC.gameSettings.keyBindLeft.pressed = false;
            MC.gameSettings.keyBindRight.pressed = false;
        }
    }

    @Handler
    public final void tick(final EventTick event) {
        MC.gameSettings.keyBindSprint.pressed = sprint.get();
        MC.thePlayer.setSprinting(sprint.get());
        if (sneak.get()) {
            final double playerYaw = Math.toDegrees(MC.thePlayer.rotationYaw);
            final double random = RandomUtil.getRandomNumber(0.02, 0.03);
            final BlockPos bp = new BlockPos(MC.thePlayer.posX + random * -Math.cos(playerYaw), MC.thePlayer.posY - 0.9D, MC.thePlayer.posZ + random * Math.sin(playerYaw));
            MC.gameSettings.keyBindSneak.pressed = WorldUtil.getBlock(bp).getMaterial() == Material.air;
        }
    }

    @Handler
    public void render2D(EventRender2D event) {


        if (block != null && blockCount.is("Augustus")) {
            currentWidth = AnimationUtil.animate(currentWidth, 17 * 2, 0.02 * 17);

            ScaledResolution sr = new ScaledResolution(MC);
            int y = sr.getScaledHeight() / 2 + 10;


            RenderUtil.drawRoundedRect(sr.getScaledWidth() / 2 - 17, y, currentWidth, 20, 4, new Color(0, 0, 0, 84));
            RenderItem renderItem = MC.getRenderItem();
            ItemStack stack = this.block;


            Mega.INSTANCE.getFontManager().getFont("Arial 22").drawCenteredString(stack.stackSize + "", sr.getScaledWidth() / 2 + 17 / 2, y + 3, new Color(255, 255, 255)); // Adjust X position here

            RenderHelper.enableGUIStandardItemLighting();
            renderItem.renderItemAndEffectIntoGUI(stack, sr.getScaledWidth() / 2 - 17 + 2, y + 2); // Adjust Y position here

        }
    }

    @Handler
    public void onEventEarlyTick(EventEarlyTick eventEarlyTick) {
        if (MC.thePlayer.onGround) {
            this.offGroundTicks = 0;
        } else {
            ++this.offGroundTicks;
        }
        this.objectPosition = null;
        if (MC.thePlayer != null && MC.theWorld != null) {
            if (!this.sameY.get() && this.isTower2()) {
                this.tower();
            }
            this.blockPos = this.getAimBlockPos();
            this.start = count == 0 || !this.startTimeHelper.hasTimePassed(200L);
            if (this.start) {
                this.startTimeHelper2.reset();
            }
            Vec3 playerPosition;
            if (this.blockPos != null) {
                if (this.lastPositions.size() > 20) {
                    this.lastPositions.remove(0);
                }

                playerPosition = new Vec3(MC.thePlayer.posX, MC.thePlayer.posY, MC.thePlayer.posZ);
                this.lastPositions.add(playerPosition);
                float[] floats = this.getPlayerYawRotation();
                this.lastRots = this.rots;
                RotationUtil.setPrevRotations(rots);

                if (floats != null) {
                    this.rots = floats;
                    RotationUtil.setRotations(rots);
                    Mega.INSTANCE.getModuleManager().getModule(RotationHandler.class).setBackRotated(false);
                }
                this.setRotation();
            }
        }
    }


    public static float roundYawTo45Degrees(float yaw) {
        return Math.round(yaw / 45.0f) * 45.0f;
    }


    private boolean shouldBuild() {
        double add1 = 1.282;
        double add2 = 0.282;
        double x = MC.thePlayer.posX;
        double z = MC.thePlayer.posZ;
        x += (MC.thePlayer.posX - this.xyz[0]) * this.backupTicks.getAsInt();
        z += (MC.thePlayer.posZ - this.xyz[2]) * this.backupTicks.getAsInt();
        this.xyz = new double[]{MC.thePlayer.posX, MC.thePlayer.posY, MC.thePlayer.posZ};
        double maX = (double) this.blockPos.getX() + add1;
        double miX = (double) this.blockPos.getX() - add2;
        double maZ = (double) this.blockPos.getZ() + add1;
        double miZ = (double) this.blockPos.getZ() - add2;
        return x > maX || x < miX || z > maZ || z < miZ || this.predict.get() && this.prediction();

    }

    private boolean prediction() {
        Vec3 predictedPosition = this.getPredictedPosition(1);
        BlockPos blockPos = this.getPredictedBlockPos();
        if (blockPos != null && predictedPosition != null) {
            double maX = (double) blockPos.getX() + 1.285;
            double miX = (double) blockPos.getX() - 0.285;
            double maZ = (double) blockPos.getZ() + 1.285;
            double miZ = (double) blockPos.getZ() - 0.285;
            return predictedPosition.xCoord > maX || predictedPosition.xCoord < miX || predictedPosition.zCoord > maZ || predictedPosition.zCoord < miZ;
        } else {
            return false;
        }
    }

    private BlockPos getPredictedBlockPos() {
        ArrayList<Float> pitchs = new ArrayList();

        for (float i = Math.max(this.rots[1] - 30.0F, -90.0F); i < Math.min(this.rots[1] + 20.0F, 90.0F); i += 0.05F) {
            float[] f = RotationUtil.mouseSens(this.rots[0], i, this.lastRots[0], this.lastRots[1]);
            MovingObjectPosition m4 = MC.thePlayer.customRayTrace(4.5, 2.0F, this.rots[0], f[1]);
            if (m4.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && BlockUtil.isValidBock(m4.getBlockPos()) && this.isNearbyBlockPos(m4.getBlockPos()) && m4.sideHit != EnumFacing.DOWN && m4.sideHit != EnumFacing.UP) {
                pitchs.add(f[1]);
            }
        }

        float[] rotations = new float[2];
        if (!pitchs.isEmpty()) {
            pitchs.sort(Comparator.comparingDouble(this::distanceToLastPitch));
            if (!pitchs.isEmpty()) {
                rotations[1] = (Float) pitchs.get(0);
                rotations[0] = this.rots[0];
            }

            MovingObjectPosition movingObjectPosition = MC.thePlayer.customRayTrace(4.5, 2.0F, rotations[0], rotations[1]);
            EnumFacing enumFacing = movingObjectPosition.sideHit;
            BlockPos blockPos = movingObjectPosition.getBlockPos();
            if (enumFacing == EnumFacing.EAST) {
                return blockPos.add(1, 0, 0);
            }

            if (enumFacing == EnumFacing.WEST) {
                return blockPos.add(-1, 0, 0);
            }

            if (enumFacing == EnumFacing.NORTH) {
                return blockPos.add(0, 0, -1);
            }

            if (enumFacing == EnumFacing.SOUTH) {
                return blockPos.add(0, 0, 1);
            }
        }

        return null;
    }

    private Vec3 getPredictedPosition(int predictTicks) {
        Vec3 playerPosition = new Vec3(MC.thePlayer.posX, MC.thePlayer.posY, MC.thePlayer.posZ);
        Vec3 vec3 = null;
        if (!this.lastPositions.isEmpty() && this.lastPositions.size() > 10 && this.lastPositions.size() > this.lastPositions.size() - predictTicks - 1) {
            vec3 = playerPosition.add(playerPosition.subtract((Vec3) this.lastPositions.get(this.lastPositions.size() - predictTicks - 1)));
        }

        return vec3;
    }

    private boolean canPlace(float[] yawPitch) {
        BlockPos b = new BlockPos(MC.thePlayer.posX, MC.thePlayer.posY - 0.5, MC.thePlayer.posZ);
        MovingObjectPosition m4 = MC.thePlayer.customRayTrace(4.5, 1.0f, yawPitch[0], yawPitch[1]);
        if (m4.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && this.isOkBlock(m4.getBlockPos()) && m4.getBlockPos().equalsBlockPos(this.blockPos) && m4.sideHit != EnumFacing.DOWN && m4.sideHit != EnumFacing.UP && m4.getBlockPos().getY() <= b.getY()) {
            this.hashMap.put(yawPitch, m4);
            return true;
        }
        return false;
    }

    private double distanceToLastRots(float[] predictRots) {
        float diff1 = Math.abs(predictRots[0] - this.rots[0]);
        float diff2 = Math.abs(predictRots[1] - this.rots[1]);
        return diff1 * diff1 + diff2 * diff2;
    }

    private double distanceToLastPitch(float pitch) {
        return Math.abs(pitch - this.rots[1]);
    }

    private double[] getAdvancedDiagonalExpandXZ(BlockPos blockPos) {
        double[] xz = new double[2];
        Vec2d difference = new Vec2d((double) blockPos.getX() - MC.thePlayer.posX, (double) blockPos.getZ() - MC.thePlayer.posZ);
        if (difference.x > -1.0 && difference.x < 0.0 && difference.y < -1.0) {
            this.enumFacing = EnumFacing.SOUTH;
            xz[0] = difference.x * -1.0;
            xz[1] = 1.0;
        }
        if (difference.y < 0.0 && difference.y > -1.0 && difference.x < -1.0) {
            this.enumFacing = EnumFacing.EAST;
            xz[0] = 1.0;
            xz[1] = difference.y * -1.0;
        }
        if (difference.x > -1.0 && difference.x < 0.0 && difference.y > 0.0) {
            this.enumFacing = EnumFacing.NORTH;
            xz[0] = difference.x * -1.0;
            xz[1] = 0.0;
        }
        if (difference.y < 0.0 && difference.y > -1.0 && difference.x > 0.0) {
            this.enumFacing = EnumFacing.WEST;
            xz[0] = 0.0;
            xz[1] = difference.y * -1.0;
            this.enumFacing = EnumFacing.WEST;
        }
        if (difference.x >= 0.0 && difference.y < -1.0) {
            xz[1] = 1.0;
        }
        if (difference.y >= 0.0 & difference.x < -1.0) {
            xz[0] = 1.0;
        }
        if (!(difference.x >= 0.0) || difference.y > 0.0) {
            // empty if block
        }
        if (difference.y <= -1.0 && difference.x < -1.0) {
            xz[0] = 1.0;
            xz[1] = 1.0;
        }
        return xz;
    }


    private int count = 0;

    @Handler
    public void onEventClick(EventClickMouse eventClick) {
        eventClick.setCancelled(true);
        if (MC.currentScreen == null && this.blockPos != null) {
            ItemStack itemStack = this.getItemStack();
            if (this.silentMode.is("Spoof")) {
                Mega.INSTANCE.getSlotSpoofHandler().startSpoofing(this.oldSlot);
                MC.thePlayer.inventory.setCurrentItem(itemStack.getItem(), 0, false, false);
            }
            ItemStack lastItem = MC.thePlayer.inventory.getCurrentItem();
            int slot = MC.thePlayer.inventory.currentItem;

            MovingObjectPosition objectPosition = MC.objectMouseOver;
            if (this.objectPosition != null) {
                objectPosition = this.objectPosition;
            }

            if (objectPosition != null) {
                boolean flag = this.hitTimeHelper.hasTimePassed((long) this.randomDelay);
                if (flag) {
                    this.hitTimeHelper.reset();
                }

                switch (MC.objectMouseOver.typeOfHit) {
                    case ENTITY:
                        if (MC.playerController.isPlayerRightClickingOnEntity(MC.thePlayer, objectPosition.entityHit, objectPosition)) {
                            flag = false;
                        } else if (MC.playerController.interactWithEntitySendPacket(MC.thePlayer, objectPosition.entityHit)) {
                            flag = false;
                        }
                        break;
                    case BLOCK:

                        if (objectPosition.getBlockPos().equalsBlockPos(this.blockPos)) {

                            if (objectPosition.sideHit == EnumFacing.UP) {
                                if (!this.sameY.get() || MC.gameSettings.keyBindJump.isKeyDown() && Mouse.isButtonDown(1)) {
                                    if (this.silentMode.is("Switch")) {
                                        MC.thePlayer.inventory.setCurrentItem(itemStack.getItem(), 0, false, false);
                                    }

                                    if (MC.playerController.onPlayerRightClick(MC.thePlayer, MC.theWorld, itemStack, objectPosition.getBlockPos(), objectPosition.sideHit, objectPosition.hitVec)) {
                                        if (!noSwing.get()) {
                                            MC.thePlayer.swingItem();
                                            MC.entityRenderer.itemRenderer.resetEquippedProgress();
                                        } else {
                                            MC.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                                        }
                                        count++;
                                        flag = false;
                                    }
                                }
                            } else {
                                if (this.silentMode.is("Switch")) {
                                    MC.thePlayer.inventory.setCurrentItem(itemStack.getItem(), 0, false, false);
                                }

                                if ((this.shouldBuild()) && MC.playerController.onPlayerRightClick(MC.thePlayer, MC.theWorld, itemStack, objectPosition.getBlockPos(), objectPosition.sideHit, objectPosition.hitVec)) {
                                    if (!noSwing.get()) {
                                        MC.thePlayer.swingItem();
                                        MC.entityRenderer.itemRenderer.resetEquippedProgress();
                                    } else {
                                        MC.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                                    }
                                    count++;
                                    flag = false;
                                }
                            }
                        } else if (this.isNearbyBlockPos(objectPosition.getBlockPos()) && objectPosition.sideHit != EnumFacing.UP) {
                            if (this.silentMode.is("Switch")) {
                                MC.thePlayer.inventory.setCurrentItem(itemStack.getItem(), 0, false, false);
                            }

                            if ((this.shouldBuild()) && MC.playerController.onPlayerRightClick(MC.thePlayer, MC.theWorld, itemStack, objectPosition.getBlockPos(), objectPosition.sideHit, objectPosition.hitVec)) {
                                if (!noSwing.get()) {
                                    MC.thePlayer.swingItem();
                                    MC.entityRenderer.itemRenderer.resetEquippedProgress();
                                } else {
                                    MC.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                                }
                                count++;
                                flag = false;
                            }
                        }
                }

                if (flag && itemStack != null && this.spamClick.get() && MC.playerController.sendUseItem(MC.thePlayer, MC.theWorld, itemStack)) {
                    MC.entityRenderer.itemRenderer.resetEquippedProgress2();
                }
            }


            if (itemStack != null && itemStack.stackSize == 0) {
                MC.thePlayer.inventory.mainInventory[this.slotID] = null;
            }

            if (this.silentMode.is("Switch")) {
                if (lastItem != null) {
                    MC.thePlayer.inventory.setCurrentItem(lastItem.getItem(), 0, false, false);
                } else {
                    MC.thePlayer.inventory.currentItem = slot;
                }
            }
        }

        MC.sendClickBlockToController(false);
        this.setRandomDelayClick();
    }

    @Handler
    public void onEventPostMouseOver(EventPostMouseOver eventPostMouseOver) {
        if (this.objectPosition != null) {
            MC.objectMouseOver = this.objectPosition;
        }

    }

    private ItemStack getItemStack() {
        ItemStack itemStack = MC.thePlayer.getCurrentEquippedItem();
        if (!this.silentMode.is("None")) {
            for (int i = 36; i < MC.thePlayer.inventoryContainer.inventorySlots.size(); ++i) {
                ItemStack stack = MC.thePlayer.inventoryContainer.getSlot(i).getStack();
                BlockUtil blockUtil = new BlockUtil();
                if (stack != null && stack.getItem() instanceof ItemBlock && stack.stackSize > 0 && blockUtil.isValidStack(stack)) {
                    this.slotID = i - 36;
                    break;
                }
            }

            itemStack = MC.thePlayer.inventoryContainer.getSlot(this.slotID + 36).getStack();
        } else {
            this.slotID = MC.thePlayer.inventory.currentItem;
        }

        block = itemStack;
        return itemStack;
    }

    private boolean shouldSneak() {
        double add1 = 1.15;
        double add2 = 0.15;
        double x = MC.thePlayer.posX;
        double z = MC.thePlayer.posZ;
        x += (MC.thePlayer.posX - this.xyz[0]) * this.backupTicks.getAsDouble();
        z += (MC.thePlayer.posZ - this.xyz[2]) * this.backupTicks.getAsDouble();
        this.xyz = new double[]{MC.thePlayer.posX, MC.thePlayer.posY, MC.thePlayer.posZ};
        double maX = (double) this.blockPos.getX() + add1;
        double miX = (double) this.blockPos.getX() - add2;
        double maZ = (double) this.blockPos.getZ() + add1;
        double miZ = (double) this.blockPos.getZ() - add2;
        return x > maX || x < miX || z > maZ || z < miZ || this.predict.get() && this.prediction();

    }

    @Handler
    public void onEventSilentMove(EventSilentMove eventSilentMove) {
        if (this.blockPos != null && breezly.get() && MC.currentScreen == null && !MC.gameSettings.keyBindJump.isKeyDown() && MoveUtil.isMoving() && this.buildForward() && rotateMode.is("Normal")) {
            if (MC.thePlayer.getHorizontalFacing(this.rots[0]) == EnumFacing.EAST) {
                if ((double) this.blockPos.getZ() + 0.5 > MC.thePlayer.posZ) {
                    this.ad1();
                } else {
                    this.ad2();
                }
            } else if (MC.thePlayer.getHorizontalFacing(this.rots[0]) == EnumFacing.WEST) {
                if ((double) this.blockPos.getZ() + 0.5 < MC.thePlayer.posZ) {
                    this.ad1();
                } else {
                    this.ad2();
                }
            } else if (MC.thePlayer.getHorizontalFacing(this.rots[0]) == EnumFacing.SOUTH) {
                if ((double) this.blockPos.getX() + 0.5 < MC.thePlayer.posX) {
                    this.ad1();
                } else {
                    this.ad2();
                }
            } else if ((double) this.blockPos.getX() + 0.5 > MC.thePlayer.posX) {
                this.ad1();
            } else {
                this.ad2();
            }
        }
        if (rotateMode.is("MoonWalk") && this.blockPos != null && this.buildForwardMoonWalk() && MoveUtil.isMoving() && moveFix.get()){

            if (MC.thePlayer.getHorizontalFacing(this.rots[0] - 18.6F) == EnumFacing.EAST) {
                if ((double) this.blockPos.getZ() + 0.5 > MC.thePlayer.posZ) {
                    MC.thePlayer.movementInput.moveStrafe = 1.0F;
                }
            } else if (MC.thePlayer.getHorizontalFacing(this.rots[0] - 18.6F) == EnumFacing.WEST) {
                if ((double) this.blockPos.getZ() + 0.5 < MC.thePlayer.posZ) {
                    MC.thePlayer.movementInput.moveStrafe = 1.0F;
                }
            } else if (MC.thePlayer.getHorizontalFacing(this.rots[0] - 18.6F) == EnumFacing.SOUTH) {
                if ((double) this.blockPos.getX() + 0.5 < MC.thePlayer.posX) {
                    MC.thePlayer.movementInput.moveStrafe = 1.0F;
                }
            } else if ((double) this.blockPos.getX() + 0.5 > MC.thePlayer.posX) {
                MC.thePlayer.movementInput.moveStrafe = 1.0F;
            }
        }

    }

    public void tower() {
        switch (this.towerMode.getCurrent()) {
            case "Vanilla":
                MC.thePlayer.motionY = 0.5;
                break;
            case "LowHop":
                if (MC.thePlayer.onGround) {
                    MC.thePlayer.motionY = 0.3700000047683716;
                }
                break;
            case "FastJump":
                if (MC.thePlayer.motionY < 0.0) {
                    MC.thePlayer.motionY = (double) MC.thePlayer.getJumpUpwardsMotion();
                    if (MC.thePlayer.isPotionActive(Potion.jump)) {
                        EntityPlayerSP var10000 = MC.thePlayer;
                        var10000.motionY += (double) ((float) (MC.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F);
                    }
                }
                break;
            case "NCP":
                if (MC.thePlayer.posY % 1.0 <= 0.00153598) {
                    MC.thePlayer.setPosition(MC.thePlayer.posX, Math.floor(MC.thePlayer.posY), MC.thePlayer.posZ);
                    MC.thePlayer.motionY = 0.41998;
                } else if (MC.thePlayer.posY % 1.0 < 0.1 && this.offGroundTicks != 0) {
                    MC.thePlayer.setPosition(MC.thePlayer.posX, Math.floor(MC.thePlayer.posY), MC.thePlayer.posZ);
                }
                break;
            case "AAC":
                if (MC.thePlayer.posY % 1.0 <= 0.005) {
                    MC.thePlayer.setPosition(MC.thePlayer.posX, Math.floor(MC.thePlayer.posY), MC.thePlayer.posZ);
                    MC.thePlayer.motionY = 0.41998;
                }
        }

    }

    private void ad1() {
        if (MC.thePlayer.movementInput.moveForward != 0.0F) {
            MC.thePlayer.movementInput.moveStrafe = MC.thePlayer.movementInput.moveForward > 0.0F ? 1.0F : -1.0F;
        } else if (MC.thePlayer.movementInput.moveStrafe != 0.0F) {
            MC.thePlayer.movementInput.moveForward = MC.thePlayer.movementInput.moveStrafe > 0.0F ? -1.0F : 1.0F;
        }

    }

    private void ad2() {
        if (MC.thePlayer.movementInput.moveForward != 0.0F) {
            MC.thePlayer.movementInput.moveStrafe = MC.thePlayer.movementInput.moveForward > 0.0F ? -1.0F : 1.0F;
        } else if (MC.thePlayer.movementInput.moveStrafe != 0.0F) {
            MC.thePlayer.movementInput.moveForward = MC.thePlayer.movementInput.moveStrafe > 0.0F ? 1.0F : -1.0F;
        }

    }

    @Handler
    public void onEventMove(EventMoveFlying eventMove) {
        //MC.gameSettings.keyBindSneak.pressed = shouldSneak();
        if (this.moveFix.get()) {
            eventMove.setYaw(rots[0] + (rotateMode.is("MoonWalk") ? -180 : 0));
            Scaffold.Side side = getPlayerSide();

            if (rotateMode.is("GodBridge") && !silentMoveFix.get()) {
                MC.gameSettings.keyBindLeft.pressed = MC.gameSettings.keyBindForward.pressed && this.buildForward() && side == Side.RIGHT;
                MC.gameSettings.keyBindRight.pressed = MC.gameSettings.keyBindForward.pressed && this.buildForward() && side == Side.LEFT;
            }
        }
        if (autoJump.get() && autoJump.isVisible()) {
            if (MC.thePlayer.isMoving()) {
                MC.gameSettings.keyBindSneak.pressed = count % autoJumpBlocks.getAsInt() == 1;
                MC.gameSettings.keyBindJump.pressed = count % autoJumpBlocks.getAsInt() == 1;
            }
        }


    }

    @Handler
    public final void onSilent(final EventSilentMove event) {
        if (RotationUtil.getRotations() == null || !silentMoveFix.get() || !silentMoveFix.isVisible())
            return;
        event.setAdvanced(true);
        event.setSilent(true);
    }

    private void setRandomDelayClick() {
        if (this.intaveHit.get()) {
            this.randomDelay = 50;
        } else if (this.spamClickDelay.getAsFloat() == 0.0) {
            this.randomDelay = 0;
        } else {
            SecureRandom secureRandom = new SecureRandom();
            this.randomDelay = (int) (this.spamClickDelay.getAsInt() + (double) secureRandom.nextInt(60));
        }

    }


    @Handler
    public final void preTick(final EventPreTick event) {
        event.setRotations(useRots);
    }

    @Handler
    public final void look(final EventLook event) {
        event.setRotations(useRots);
    }


    @Handler
    public void packet(final EventPacket event) {

    }

    private void setRotation() {
        if (MC.currentScreen == null) {
            useRots = rots;
            prevUseRots = lastRots;
        }
    }

    private float[] getPlayerYawRotation() {
        float addYaw = 180;
        if (rotateMode.is("GodBridge")) {
            Scaffold.Side side = getPlayerSide();
            if (side != Scaffold.Side.NONE) {
                if (side == Scaffold.Side.RIGHT) {
                    addYaw = 135.0f;
                } else if (side == Scaffold.Side.LEFT) {
                    addYaw = -135.0f;
                }
            }
        }

        boolean godBridge = rotateMode.is("GodBridge");
        boolean snap = rotateMode.is("Snap");
        boolean moonWalk = rotateMode.is("MoonWalk");
        float yaw = this.rots[0];
        SecureRandom secureRandom = new SecureRandom();
        float yawSpeed = (RandomUtil.nextFloat(this.yawSpeed.getCurrentMin() - 0.0010000000474974513, this.yawSpeed.getCurrentMax()) / 2.0F + secureRandom.nextFloat() + RandomUtil.nextFloat(this.yawSpeed.getCurrentMin() - 0.0010000000474974513, this.yawSpeed.getCurrentMax()) / 2.0F) * 0.1F;
        float pitchSpeed = (RandomUtil.nextFloat(this.pitchSpeed.getCurrentMin() - 0.0010000000474974513, this.pitchSpeed.getCurrentMax()) / 2.0F + secureRandom.nextFloat() + RandomUtil.nextFloat(this.pitchSpeed.getCurrentMin() - 0.0010000000474974513, this.pitchSpeed.getCurrentMax()) / 2.0F) * 0.1F;

        float realYaw = MC.thePlayer.rotationYaw;
        updateRealYawBasedOnMovement(realYaw);

        if (this.isTower()) {
            MovingObjectPosition objectPosition = MC.objectMouseOver;
            if (objectPosition != null) {
                float targetYaw = 90.0F;
                if (snap) {
                    targetYaw = MC.thePlayer.rotationYaw;
                } else if (godBridge) {
                    if (buildForward()) {
                        targetYaw = roundYawTo45Degrees(MC.thePlayer.rotationYaw - addYaw);
                    } else {
                        targetYaw = roundYawTo45Degrees(MC.thePlayer.rotationYaw - 180.0F);
                    }
                } else if (moonWalk && buildForwardMoonWalk()) {
                    targetYaw = MC.thePlayer.rotationYaw - 180.0F + 18.5F;
                } else {
                    targetYaw = MC.thePlayer.rotationYaw - 180.0F;
                }
                yaw = updateRotation(useRots[0], targetYaw, yawSpeed);
                float pitch = updateRotation(useRots[1], 90.0F, pitchSpeed);
                return new float[]{yaw, pitch};
            }
        }

        float[] rotations = new float[]{yaw, this.rots[1]};
        if ((MC.thePlayer.motionX != 0.0 || MC.thePlayer.motionZ != 0.0 || !MC.thePlayer.onGround) && this.startTimeHelper.hasTimePassed(200L)) {
            if (MC.thePlayer.motionX == 0.0 && MC.thePlayer.motionZ == 0.0 && MC.thePlayer.onGround) {
                this.startTimeHelper.reset();
            }
        } else {
            float targetYaw = moonWalk ? 80.0F : 80.34F;
            if (snap) {
                targetYaw = MC.thePlayer.rotationYaw;
            } else if (godBridge) {
                if (buildForward()) {
                    targetYaw = roundYawTo45Degrees(MC.thePlayer.rotationYaw - addYaw);
                } else {
                    targetYaw = roundYawTo45Degrees(MC.thePlayer.rotationYaw - 180.0F);
                }
            } else if (moonWalk && buildForwardMoonWalk()) {
                targetYaw = MC.thePlayer.rotationYaw - 180.0F + 18.5F;
            } else {
                targetYaw = MC.thePlayer.rotationYaw - 180.0F;
            }
            yaw = updateRotation(useRots[0], targetYaw, yawSpeed);
            float pitch = updateRotation(useRots[1], moonWalk ? 80.0F : 80.34F, pitchSpeed);
            rotations = new float[]{yaw, pitch};
        }

        realYaw = MC.thePlayer.rotationYaw;
        updateRealYawBasedOnMovement(realYaw);

        if (snap) {
            yaw = updateRotation(useRots[0], MC.thePlayer.rotationYaw, yawSpeed);
        } else if (godBridge) {
            if (buildForward()) {
                yaw = updateRotation(useRots[0], roundYawTo45Degrees(MC.thePlayer.rotationYaw - addYaw), yawSpeed);
            } else {
                yaw = updateRotation(useRots[0], roundYawTo45Degrees(MC.thePlayer.rotationYaw - 180.0F), yawSpeed);
            }
        } else if (moonWalk && buildForwardMoonWalk()) {
            yaw = updateRotation(useRots[0], MC.thePlayer.rotationYaw - 180.0F + 18.5F, yawSpeed);
        } else {
            yaw = updateRotation(useRots[0], realYaw - 180.0F, yawSpeed);
        }

        rotations[0] = yaw;
        if (this.shouldBuild()) {
            MovingObjectPosition m1 = RayCastUtil.rayCast(1.0F, rotations);
            if (isValidBlock(m1)) {
                this.objectPosition = m1;
                return rotations;
            }

            HashMap<Float, MovingObjectPosition> hashMap = new HashMap<>();
            ArrayList<Float> pitches = findValidPitches(yaw, hashMap);

            if (!pitches.isEmpty()) {
                pitches.sort(Comparator.comparingDouble(this::distanceToLastPitch));
                if (!pitches.isEmpty()) {
                    rotations[1] = pitches.get(0);
                    this.objectPosition = hashMap.get(rotations[1]);
                }
            } else {
                rotations = findBestRotations(yaw);
            }
        }

        return rotations;
    }

    private void updateRealYawBasedOnMovement(float realYaw) {
        if (MC.gameSettings.keyBindBack.pressed) {
            realYaw += 180.0F;
            if (MC.gameSettings.keyBindLeft.pressed) {
                realYaw += 45.0F;
            } else if (MC.gameSettings.keyBindRight.pressed) {
                realYaw -= 45.0F;
            }
        } else if (MC.gameSettings.keyBindForward.pressed) {
            if (MC.gameSettings.keyBindLeft.pressed) {
                realYaw -= 45.0F;
            } else if (MC.gameSettings.keyBindRight.pressed) {
                realYaw += 45.0F;
            }
        } else if (MC.gameSettings.keyBindRight.pressed) {
            realYaw += 90.0F;
        } else if (MC.gameSettings.keyBindLeft.pressed) {
            realYaw -= 90.0F;
        }
    }

    private boolean isValidBlock(MovingObjectPosition m1) {
        return m1.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && BlockUtil.isValidBock(m1.getBlockPos())
                && this.isNearbyBlockPos(m1.getBlockPos()) && m1.sideHit != EnumFacing.DOWN && m1.sideHit != EnumFacing.UP;
    }

    private ArrayList<Float> findValidPitches(float yaw, HashMap<Float, MovingObjectPosition> hashMap) {
        ArrayList<Float> pitches = new ArrayList<>();
        for (float i = Math.max(this.rots[1] - 30.0F, -90.0F); i < Math.min(this.rots[1] + 20.0F, 90.0F); i += 0.05F) {
            float[] f = RotationUtil.mouseSens(yaw, i, this.rots[0], this.rots[1]);
            MovingObjectPosition m4 = RayCastUtil.rayCast(1.0F, new float[]{yaw, f[1]});
            if (isValidBlock(m4)) {
                hashMap.put(f[1], m4);
                pitches.add(f[1]);
            }
        }
        return pitches;
    }

    private float[] findBestRotations(float yaw) {
        int add = 1;
        for (int yawLoops = 0; yawLoops < 180; ++yawLoops) {
            float yaw1 = yaw + yawLoops * add;
            float yaw2 = yaw - yawLoops * add;
            for (int pitchLoops = 0; pitchLoops < 25; ++pitchLoops) {
                float pitch1 = MathHelper.clamp_float(this.rots[1] + pitchLoops * add, -90.0F, 90.0F);
                float pitch2 = MathHelper.clamp_float(this.rots[1] - pitchLoops * add, -90.0F, 90.0F);
                float[][] rotations = {
                        RotationUtil.mouseSens(yaw2, pitch2, this.rots[0], this.rots[1]),
                        RotationUtil.mouseSens(yaw2, pitch1, this.rots[0], this.rots[1]),
                        RotationUtil.mouseSens(yaw1, pitch2, this.rots[0], this.rots[1]),
                        RotationUtil.mouseSens(yaw1, pitch1, this.rots[0], this.rots[1])
                };
                for (float[] rotation : rotations) {
                    MovingObjectPosition m = RayCastUtil.rayCast(1.0F, rotation);
                    if (isValidBlock(m)) {
                        this.objectPosition = m;
                        return rotation;
                    }
                }
            }
        }
        return new float[]{yaw, this.rots[1]};
    }

    float updateRotation(float currentRotation, float nextRotation, float rotationSpeed) {
        float f = MathHelper.wrapAngleTo180_float(nextRotation - currentRotation);
        if (f > rotationSpeed) {
            f = rotationSpeed;
        }
        if (f < -rotationSpeed) {
            f = -rotationSpeed;
        }
        return currentRotation + f;
    }


    private boolean isNearbyBlockPos(BlockPos blockPos) {
        if (!MC.thePlayer.onGround) {
            return blockPos.equalsBlockPos(this.blockPos);
        } else {
            for (int x = this.blockPos.getX() - 1; x <= this.blockPos.getX() + 1; ++x) {
                for (int z = this.blockPos.getZ() - 1; z <= this.blockPos.getZ() + 1; ++z) {
                    if (blockPos.equalsBlockPos(new BlockPos(x, this.blockPos.getY(), z))) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    private boolean buildForwardMoonWalk() {
        float realYaw = MathHelper.wrapAngleTo180_float(MC.thePlayer.rotationYaw - 180.0F);
        if ((double) realYaw > 77.5 && (double) realYaw < 102.5) {
            return true;
        } else if (!((double) realYaw > 167.5) && !(realYaw < -167.0F)) {
            if ((double) realYaw < -77.5 && (double) realYaw > -102.5) {
                return true;
            } else {
                return (double) realYaw > -12.5 && (double) realYaw < 12.5;
            }
        } else {
            return true;
        }
    }

    private boolean buildForward() {
        float realYaw = MathHelper.wrapAngleTo180_float(MC.thePlayer.rotationYaw);

        // Überprüfen, ob realYaw im Bereich von -22,5 bis 22,5 liegt (geradeaus)
        if ((realYaw >= -22.5 && realYaw <= 22.5) || (realYaw <= -157.5 || realYaw >= 157.5)) {
            return true;
        } else if ((realYaw > -67.5 && realYaw < -22.5) || (realYaw > 112.5 && realYaw < 157.5)) {
            return false;
        } else if ((realYaw >= -112.5 && realYaw <= -67.5) || (realYaw >= 67.5 && realYaw <= 112.5)) {
            return true;
        } else if ((realYaw > -157.5 && realYaw < -112.5) || (realYaw > 22.5 && realYaw < 67.5)) {
            return false;
        }

        return false;
    }

    private boolean isTower() {
        return MC.gameSettings.keyBindJump.isKeyDown() && MC.thePlayer.motionX == 0.0 && MC.thePlayer.motionZ == 0.0;
    }

    public boolean isTower2() {
        return MC.gameSettings.keyBindJump.pressed && (!MC.gameSettings.keyBindLeft.pressed && !MC.gameSettings.keyBindRight.pressed && !MC.gameSettings.keyBindForward.pressed && !MC.gameSettings.keyBindBack.pressed || this.towerMove.get());
    }

    private BlockPos getAimBlockPos() {
        int y;
        int z;
        BlockPos playerPos = new BlockPos(MC.thePlayer.posX, MC.thePlayer.posY - 1.0, MC.thePlayer.posZ);
        ArrayList<Vec3> positions = new ArrayList();
        HashMap<Vec3, BlockPos> hashMap = new HashMap();

        for (int x = playerPos.getX() - 5; x <= playerPos.getX() + 5; ++x) {
            for (y = playerPos.getY() - 1; y <= playerPos.getY(); ++y) {
                for (z = playerPos.getZ() - 5; z <= playerPos.getZ() + 5; ++z) {
                    if (BlockUtil.isValidBock(new BlockPos(x, y, z))) {
                        BlockPos blockPos = new BlockPos(x, y, z);
                        Block block = MC.theWorld.getBlockState(blockPos).getBlock();
                        double ex = MathHelper.clamp_double(MC.thePlayer.posX, (double) blockPos.getX(), (double) blockPos.getX() + block.getBlockBoundsMaxX());
                        double ey = MathHelper.clamp_double(MC.thePlayer.posY, (double) blockPos.getY(), (double) blockPos.getY() + block.getBlockBoundsMaxY());
                        double ez = MathHelper.clamp_double(MC.thePlayer.posZ, (double) blockPos.getZ(), (double) blockPos.getZ() + block.getBlockBoundsMaxZ());
                        Vec3 vec3 = new Vec3(ex, ey, ez);
                        positions.add(vec3);
                        hashMap.put(vec3, blockPos);
                    }
                }
            }
        }

        if (!positions.isEmpty()) {
            positions.sort(Comparator.comparingDouble(this::getBestBlock));
            return this.isTower() && (double) ((BlockPos) hashMap.get(positions.get(0))).getY() != MC.thePlayer.posY - 1.5 ? new BlockPos(MC.thePlayer.posX, MC.thePlayer.posY - 1.5, MC.thePlayer.posZ) : (BlockPos) hashMap.get(positions.get(0));
        }


        return null;
    }

    private double getBestBlock(Vec3 vec3) {
        return MC.thePlayer.getDistanceSq(vec3.xCoord, vec3.yCoord, vec3.zCoord);
    }


    private boolean isOkBlock(BlockPos blockPos) {
        Block block = MC.theWorld.getBlockState(blockPos).getBlock();
        return !(block instanceof BlockLiquid) && !(block instanceof BlockAir) && !(block instanceof BlockChest) && !(block instanceof BlockFurnace);
    }

    private void restRotation() {
        this.rots[0] = MC.thePlayer.rotationYaw;
        this.rots[1] = MC.thePlayer.rotationPitch;
        this.lastRots[0] = MC.thePlayer.prevRotationYaw;
        this.lastRots[1] = MC.thePlayer.prevRotationPitch;
    }
    @Handler
    public final void onBlur(EventBlur event) {
        if (blockCount.is("Augustus") && block != null) {
            ScaledResolution sr = new ScaledResolution(MC);
            int y = sr.getScaledHeight() / 2 + 10;
            RenderUtil.drawRoundedRect(sr.getScaledWidth() / 2 - 17, y, Scaffold.getInstance().currentWidth, 20, 4, new Color(0, 0, 0, 115));
        }
    }

    public static Scaffold getInstance() {
        return Mega.INSTANCE.getModuleManager().getModule(Scaffold.class);
    }
}
