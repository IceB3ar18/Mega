package net.minecraft.client.entity;

import club.mega.Mega;
import club.mega.event.impl.*;
import club.mega.module.impl.movement.NoSlow;
import club.mega.module.impl.visual.NoBob;
import club.mega.util.MoveUtil;
import club.mega.util.RotationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSoundMinecartRiding;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiCommandBlock;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.inventory.GuiBeacon;
import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiDispenser;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.gui.inventory.GuiScreenHorseInventory;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0CPacketInput;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C13PacketPlayerAbilities;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.potion.Potion;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.*;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static club.mega.interfaces.MinecraftInterface.MC;
import static net.minecraft.client.gui.GuiPlayerTabOverlay.field_175252_a;

public class EntityPlayerSP extends AbstractClientPlayer
{
    public final NetHandlerPlayClient sendQueue;
    private final StatFileWriter statWriter;
    private Vec3 spawnPos;
    public int ticksSinceLastSwing;
    private double lastReportedPosX;

    private double lastReportedPosY;
    private double lastReportedPosZ;
    private float lastReportedYaw;
    private float lastReportedPitch;
    private boolean serverSneakState;
    private boolean serverSprintState;
    private int positionUpdateTicks;
    private boolean hasValidHealth;
    private String clientBrand;
    public MovementInput movementInput;
    protected Minecraft mc;
    protected int sprintToggleTimer;
    public int rotIncrement;
    public int sprintingTicksLeft;
    public float renderArmYaw;
    public float renderArmPitch;

    public float prevRenderArmYaw;
    public float prevRenderArmPitch;
    private int horseJumpPowerCounter;
    private float horseJumpPower;
    public float timeInPortal;
    public float prevTimeInPortal;
    private Vec3 lastServerPosition = new Vec3(0.0, 0.0, 0.0);
    private Vec3 severPosition = new Vec3(0.0, 0.0, 0.0);
    public int reSprint;

    public EntityPlayerSP(Minecraft mcIn, World worldIn, NetHandlerPlayClient netHandler, StatFileWriter statFile)
    {
        super(worldIn, netHandler.getGameProfile());
        this.sendQueue = netHandler;
        this.statWriter = statFile;
        this.mc = mcIn;
        this.dimension = 0;
    }

    @Override
    public void moveEntity(double x, double y, double z) {
        final EventMovePlayer eventMovePlayer = new EventMovePlayer(x, y, z);
        eventMovePlayer.fire();

        if(!eventMovePlayer.isCancelled())
        super.moveEntity(eventMovePlayer.getX(), eventMovePlayer.getY(), eventMovePlayer.getZ());
    }

    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        return false;
    }

    public void heal(float healAmount)
    {
    }

    public boolean canPosBeSeen(Vec3 pos) {
        Vec3 playerEyePos = new Vec3(MC.thePlayer.posX, MC.thePlayer.posY + (double)MC.thePlayer.getEyeHeight(), MC.thePlayer.posZ);
        return MC.theWorld.rayTraceBlocks(playerEyePos, pos) == null;
    }
    public void mountEntity(Entity entityIn)
    {
        super.mountEntity(entityIn);

        if (entityIn instanceof EntityMinecart)
        {
            this.mc.getSoundHandler().playSound(new MovingSoundMinecartRiding(this, (EntityMinecart)entityIn));
        }
    }

    public ArrayList<NetworkPlayerInfo> searchPlayers(String name) {
        final ArrayList<NetworkPlayerInfo> playerInfos = new ArrayList<>();
        List<NetworkPlayerInfo> list = field_175252_a.<NetworkPlayerInfo>sortedCopy(sendQueue.getPlayerInfoMap());
        for (int i = 0; i < list.size(); ++i)
        {
            NetworkPlayerInfo entityplayer = list.get(i);

            if (name.equals(entityplayer.getGameProfile().getName()))
            {
                playerInfos.add(entityplayer);
            }
        }

        return playerInfos;
    }

    public void onUpdate()
    {
        if (this.worldObj.isBlockLoaded(new BlockPos(this.posX, 0.0D, this.posZ)))
        {
            final EventTick eventTick = new EventTick();
            eventTick.fire();

            super.onUpdate();

            if (this.isRiding())
            {
                this.sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(this.rotationYaw, this.rotationPitch, this.onGround));
                this.sendQueue.addToSendQueue(new C0CPacketInput(this.moveStrafing, this.moveForward, this.movementInput.jump, this.movementInput.sneak));
            }
            else
            {
                this.onUpdateWalkingPlayer();
                EventPostMotion eventPostMotion = new EventPostMotion();
                eventPostMotion.fire();
                final EventPostTick eventPostTick = new EventPostTick();
                eventPostTick.fire();
            }
        }
    }

    public void onUpdateWalkingPlayer()
    {
        final EventPreTick eventPreTick = new EventPreTick(onGround, new float[] {rotationYaw, rotationPitch}, posX, getEntityBoundingBox().minY, posZ);
        eventPreTick.fire();

        final float yaw = eventPreTick.getRotations()[0];
        final float pitch = eventPreTick.getRotations()[1];

        boolean flag = this.isSprinting();

        if (flag != this.serverSprintState)
        {
            if (flag)
            {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.START_SPRINTING));
            }
            else
            {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.STOP_SPRINTING));
            }
            this.reSprint = 1;
            this.serverSprintState = flag;
        }

        boolean flag1 = this.isSneaking();

        if (flag1 != this.serverSneakState)
        {
            if (flag1)
            {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.START_SNEAKING));
            }
            else
            {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.STOP_SNEAKING));
            }

            this.serverSneakState = flag1;
        }

        if (this.isCurrentViewEntity())
        {
            double d0 = eventPreTick.getX() - this.lastReportedPosX;
            double d1 = eventPreTick.getY() - this.lastReportedPosY;
            double d2 = eventPreTick.getZ() - this.lastReportedPosZ;
            double d3 = (double)(yaw - this.lastReportedYaw);
            double d4 = (double)(pitch - this.lastReportedPitch);
            boolean flag2 = d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4D || this.positionUpdateTicks >= 20;
            boolean flag3 = d3 != 0.0D || d4 != 0.0D;

            if (this.ridingEntity == null)
            {
                if (flag2 && flag3)
                {
                    this.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(eventPreTick.getX(), eventPreTick.getY(), eventPreTick.getZ(), yaw, pitch, eventPreTick.isOnGround()));
                }
                else if (flag2)
                {
                    this.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(eventPreTick.getX(), eventPreTick.getY(), eventPreTick.getZ(), eventPreTick.isOnGround()));
                }
                else if (flag3)
                {
                    this.sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(yaw, pitch, eventPreTick.isOnGround()));
                }
                else
                {
                    this.sendQueue.addToSendQueue(new C03PacketPlayer(eventPreTick.isOnGround()));
                }
            }
            else
            {
                this.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(this.motionX, -999.0D, this.motionZ, yaw, pitch, eventPreTick.isOnGround()));
                flag2 = false;
            }

            ++this.positionUpdateTicks;

            if (flag2)
            {
                this.lastReportedPosX = eventPreTick.getX();
                this.lastReportedPosY = eventPreTick.getY();
                this.lastReportedPosZ = eventPreTick.getZ();
                this.positionUpdateTicks = 0;
            }

            if (flag3)
            {
                this.lastReportedYaw = yaw;
                this.lastReportedPitch = pitch;
            }
            --Minecraft.getMinecraft().thePlayer.rotIncrement;
        }
    }

    public EntityItem dropOneItem(boolean dropAll)
    {
        C07PacketPlayerDigging.Action c07packetplayerdigging$action = dropAll ? C07PacketPlayerDigging.Action.DROP_ALL_ITEMS : C07PacketPlayerDigging.Action.DROP_ITEM;
        this.sendQueue.addToSendQueue(new C07PacketPlayerDigging(c07packetplayerdigging$action, BlockPos.ORIGIN, EnumFacing.DOWN));
        return null;
    }

    protected void joinEntityItemWithWorld(EntityItem itemIn)
    {
    }

    public void sendChatMessage(String message)
    {
        final EventChat eventChat = new EventChat(message);
        eventChat.fire();

        if (!eventChat.isCancelled()) {
            this.sendQueue.addToSendQueue(new C01PacketChatMessage(message));
        }
    }

    public void swingItem()
    {
        super.swingItem();
        this.sendQueue.addToSendQueue(new C0APacketAnimation());
    }

    public void respawnPlayer()
    {
        this.sendQueue.addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.PERFORM_RESPAWN));
    }

    protected void damageEntity(DamageSource damageSrc, float damageAmount)
    {
        if (!this.isEntityInvulnerable(damageSrc))
        {
            this.setHealth(this.getHealth() - damageAmount);
        }
    }

    public void closeScreen()
    {
        this.sendQueue.addToSendQueue(new C0DPacketCloseWindow(this.openContainer.windowId));
        this.closeScreenAndDropStack();
    }

    public void closeScreenAndDropStack()
    {
        this.inventory.setItemStack((ItemStack)null);
        super.closeScreen();
        this.mc.displayGuiScreen((GuiScreen)null);
    }

    public void setPlayerSPHealth(float health)
    {
        if (this.hasValidHealth)
        {
            float f = this.getHealth() - health;

            if (f <= 0.0F)
            {
                this.setHealth(health);

                if (f < 0.0F)
                {
                    this.hurtResistantTime = this.maxHurtResistantTime / 2;
                }
            }
            else
            {
                this.lastDamage = f;
                this.setHealth(this.getHealth());
                this.hurtResistantTime = this.maxHurtResistantTime;
                this.damageEntity(DamageSource.generic, f);
                this.hurtTime = this.maxHurtTime = 10;
            }
        }
        else
        {
            this.setHealth(health);
            this.hasValidHealth = true;
        }
    }

    public void addStat(StatBase stat, int amount)
    {
        if (stat != null)
        {
            if (stat.isIndependent)
            {
                super.addStat(stat, amount);
            }
        }
    }

    public void sendPlayerAbilities()
    {
        this.sendQueue.addToSendQueue(new C13PacketPlayerAbilities(this.capabilities));
    }

    public boolean isUser()
    {
        return true;
    }

    protected void sendHorseJump()
    {
        this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.RIDING_JUMP, (int)(this.getHorseJumpPower() * 100.0F)));
    }

    public void sendHorseInventory()
    {
        this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.OPEN_INVENTORY));
    }

    public void setClientBrand(String brand)
    {
        this.clientBrand = brand;
    }

    public String getClientBrand()
    {
        return this.clientBrand;
    }

    public StatFileWriter getStatFileWriter()
    {
        return this.statWriter;
    }

    public void addChatComponentMessage(IChatComponent chatComponent)
    {
        this.mc.ingameGUI.getChatGUI().printChatMessage(chatComponent);
    }

    protected boolean pushOutOfBlocks(double x, double y, double z)
    {
        if (this.noClip)
        {
            return false;
        }
        else
        {
            BlockPos blockpos = new BlockPos(x, y, z);
            double d0 = x - (double)blockpos.getX();
            double d1 = z - (double)blockpos.getZ();

            if (!this.isOpenBlockSpace(blockpos))
            {
                int i = -1;
                double d2 = 9999.0D;

                if (this.isOpenBlockSpace(blockpos.west()) && d0 < d2)
                {
                    d2 = d0;
                    i = 0;
                }

                if (this.isOpenBlockSpace(blockpos.east()) && 1.0D - d0 < d2)
                {
                    d2 = 1.0D - d0;
                    i = 1;
                }

                if (this.isOpenBlockSpace(blockpos.north()) && d1 < d2)
                {
                    d2 = d1;
                    i = 4;
                }

                if (this.isOpenBlockSpace(blockpos.south()) && 1.0D - d1 < d2)
                {
                    d2 = 1.0D - d1;
                    i = 5;
                }

                float f = 0.1F;

                if (i == 0)
                {
                    this.motionX = (double)(-f);
                }

                if (i == 1)
                {
                    this.motionX = (double)f;
                }

                if (i == 4)
                {
                    this.motionZ = (double)(-f);
                }

                if (i == 5)
                {
                    this.motionZ = (double)f;
                }
            }

            return false;
        }
    }

    private boolean isOpenBlockSpace(BlockPos pos)
    {
        return !this.worldObj.getBlockState(pos).getBlock().isNormalCube() && !this.worldObj.getBlockState(pos.up()).getBlock().isNormalCube();
    }

    public void setSprinting(boolean sprinting)
    {
        super.setSprinting(sprinting);
        this.sprintingTicksLeft = sprinting ? 600 : 0;
    }

    public void setXPStats(float currentXP, int maxXP, int level)
    {
        this.experience = currentXP;
        this.experienceTotal = maxXP;
        this.experienceLevel = level;
    }

    public void addChatMessage(IChatComponent component)
    {
        this.mc.ingameGUI.getChatGUI().printChatMessage(component);
    }

    public boolean canCommandSenderUseCommand(int permLevel, String commandName)
    {
        return permLevel <= 0;
    }

    public BlockPos getPosition()
    {
        return new BlockPos(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D);
    }

    public void playSound(String name, float volume, float pitch)
    {
        this.worldObj.playSound(this.posX, this.posY, this.posZ, name, volume, pitch, false);
    }

    public boolean isServerWorld()
    {
        return true;
    }

    public boolean isRidingHorse()
    {
        return this.ridingEntity != null && this.ridingEntity instanceof EntityHorse && ((EntityHorse)this.ridingEntity).isHorseSaddled();
    }

    public float getHorseJumpPower()
    {
        return this.horseJumpPower;
    }

    public void openEditSign(TileEntitySign signTile)
    {
        this.mc.displayGuiScreen(new GuiEditSign(signTile));
    }

    public void openEditCommandBlock(CommandBlockLogic cmdBlockLogic)
    {
        this.mc.displayGuiScreen(new GuiCommandBlock(cmdBlockLogic));
    }

    public void displayGUIBook(ItemStack bookStack)
    {
        Item item = bookStack.getItem();

        if (item == Items.writable_book)
        {
            this.mc.displayGuiScreen(new GuiScreenBook(this, bookStack, true));
        }
    }

    public void displayGUIChest(IInventory chestInventory)
    {
        String s = chestInventory instanceof IInteractionObject ? ((IInteractionObject)chestInventory).getGuiID() : "minecraft:container";

        if ("minecraft:chest".equals(s))
        {
            this.mc.displayGuiScreen(new GuiChest(this.inventory, chestInventory));
        }
        else if ("minecraft:hopper".equals(s))
        {
            this.mc.displayGuiScreen(new GuiHopper(this.inventory, chestInventory));
        }
        else if ("minecraft:furnace".equals(s))
        {
            this.mc.displayGuiScreen(new GuiFurnace(this.inventory, chestInventory));
        }
        else if ("minecraft:brewing_stand".equals(s))
        {
            this.mc.displayGuiScreen(new GuiBrewingStand(this.inventory, chestInventory));
        }
        else if ("minecraft:beacon".equals(s))
        {
            this.mc.displayGuiScreen(new GuiBeacon(this.inventory, chestInventory));
        }
        else if (!"minecraft:dispenser".equals(s) && !"minecraft:dropper".equals(s))
        {
            this.mc.displayGuiScreen(new GuiChest(this.inventory, chestInventory));
        }
        else
        {
            this.mc.displayGuiScreen(new GuiDispenser(this.inventory, chestInventory));
        }
    }

    public void displayGUIHorse(EntityHorse horse, IInventory horseInventory)
    {
        this.mc.displayGuiScreen(new GuiScreenHorseInventory(this.inventory, horseInventory, horse));
    }

    public void displayGui(IInteractionObject guiOwner)
    {
        String s = guiOwner.getGuiID();

        if ("minecraft:crafting_table".equals(s))
        {
            this.mc.displayGuiScreen(new GuiCrafting(this.inventory, this.worldObj));
        }
        else if ("minecraft:enchanting_table".equals(s))
        {
            this.mc.displayGuiScreen(new GuiEnchantment(this.inventory, this.worldObj, guiOwner));
        }
        else if ("minecraft:anvil".equals(s))
        {
            this.mc.displayGuiScreen(new GuiRepair(this.inventory, this.worldObj));
        }
    }

    public void displayVillagerTradeGui(IMerchant villager)
    {
        this.mc.displayGuiScreen(new GuiMerchant(this.inventory, villager, this.worldObj));
    }

    public void onCriticalHit(Entity entityHit)
    {
        this.mc.effectRenderer.emitParticleAtEntity(entityHit, EnumParticleTypes.CRIT);
    }

    public void onEnchantmentCritical(Entity entityHit)
    {
        this.mc.effectRenderer.emitParticleAtEntity(entityHit, EnumParticleTypes.CRIT_MAGIC);
    }

    public boolean isSneaking()
    {
        boolean flag = this.movementInput != null ? this.movementInput.sneak : false;
        return flag && !this.sleeping;
    }

    public void updateEntityActionState()
    {
        super.updateEntityActionState();

        if (this.isCurrentViewEntity())
        {
            this.moveStrafing = this.movementInput.moveStrafe;
            this.moveForward = this.movementInput.moveForward;
            this.isJumping = this.movementInput.jump;
            this.prevRenderArmYaw = this.renderArmYaw;
            this.prevRenderArmPitch = this.renderArmPitch;
            this.renderArmPitch = (float)((double)this.renderArmPitch + (double)(this.rotationPitch - this.renderArmPitch) * 0.5D);
            this.renderArmYaw = (float)((double)this.renderArmYaw + (double)(this.rotationYaw - this.renderArmYaw) * 0.5D);
        }
    }

    protected boolean isCurrentViewEntity()
    {
        return this.mc.getRenderViewEntity() == this;
    }

    public void onLivingUpdate()
    {
        if (this.sprintingTicksLeft > 0)
        {
            --this.sprintingTicksLeft;

            if (this.sprintingTicksLeft == 0)
            {
                this.setSprinting(false);
            }
        }

        if (this.sprintToggleTimer > 0)
        {
            --this.sprintToggleTimer;
        }

        this.prevTimeInPortal = this.timeInPortal;

        if (this.inPortal)
        {
            if (this.mc.currentScreen != null && !this.mc.currentScreen.doesGuiPauseGame())
            {
                this.mc.displayGuiScreen((GuiScreen)null);
            }

            if (this.timeInPortal == 0.0F)
            {
                this.mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("portal.trigger"), this.rand.nextFloat() * 0.4F + 0.8F));
            }

            this.timeInPortal += 0.0125F;

            if (this.timeInPortal >= 1.0F)
            {
                this.timeInPortal = 1.0F;
            }

            this.inPortal = false;
        }
        else if (this.isPotionActive(Potion.confusion) && this.getActivePotionEffect(Potion.confusion).getDuration() > 60)
        {
            this.timeInPortal += 0.006666667F;

            if (this.timeInPortal > 1.0F)
            {
                this.timeInPortal = 1.0F;
            }
        }
        else
        {
            if (this.timeInPortal > 0.0F)
            {
                this.timeInPortal -= 0.05F;
            }

            if (this.timeInPortal < 0.0F)
            {
                this.timeInPortal = 0.0F;
            }
        }

        if (this.timeUntilPortal > 0)
        {
            --this.timeUntilPortal;
        }

        boolean flag = this.movementInput.jump;
        boolean flag1 = this.movementInput.sneak;
        float forward = this.movementInput.moveForward;
        float strafe = this.movementInput.moveStrafe;
        float f = 0.8F;
        boolean flag2 = this.movementInput.moveForward >= f;
        this.movementInput.updatePlayerMoveState();

        EventSilentMove eventSilentMove = new EventSilentMove(MC.thePlayer.rotationYaw);
        eventSilentMove.fire();
        float movef;
        if (eventSilentMove.isSilent()) {
            float[] floats = this.mySilentStrafe(this.movementInput.moveStrafe, this.movementInput.moveForward, eventSilentMove.getYaw(), eventSilentMove.isAdvanced());
            float diffForward = forward - floats[1];
            movef = strafe - floats[0];
            if (this.movementInput.sneak) {
                this.movementInput.moveStrafe = MathHelper.clamp_float(floats[0], -0.3F, 0.3F);
                this.movementInput.moveForward = MathHelper.clamp_float(floats[1], -0.3F, 0.3F);
            } else {
                if (diffForward >= 2.0F) {
                    floats[1] = 0.0F;
                }

                if (diffForward <= -2.0F) {
                    floats[1] = 0.0F;
                }

                if (movef >= 2.0F) {
                    floats[0] = 0.0F;
                }

                if (movef <= -2.0F) {
                    floats[0] = 0.0F;
                }

                this.movementInput.moveStrafe = MathHelper.clamp_float(floats[0], -1.0F, 1.0F);
                this.movementInput.moveForward = MathHelper.clamp_float(floats[1], -1.0F, 1.0F);
            }
        }


        EventNoSlow eventNoSlow = new EventNoSlow(0.2F, 0.2F);
        eventNoSlow.fire();
        if (this.isUsingItem() && !this.isRiding()) {
            MovementInput var10000 = this.movementInput;
            var10000.moveStrafe *= eventNoSlow.getMoveStrafe();
            var10000 = this.movementInput;
            var10000.moveForward *= eventNoSlow.getMoveForward();
            this.sprintToggleTimer = 0;
        }

        this.pushOutOfBlocks(this.posX - (double)this.width * 0.35, this.getEntityBoundingBox().minY + 0.5, this.posZ + (double)this.width * 0.35);
        this.pushOutOfBlocks(this.posX - (double)this.width * 0.35, this.getEntityBoundingBox().minY + 0.5, this.posZ - (double)this.width * 0.35);
        this.pushOutOfBlocks(this.posX + (double)this.width * 0.35, this.getEntityBoundingBox().minY + 0.5, this.posZ - (double)this.width * 0.35);
        this.pushOutOfBlocks(this.posX + (double)this.width * 0.35, this.getEntityBoundingBox().minY + 0.5, this.posZ + (double)this.width * 0.35);
        boolean flag3 = (float)this.getFoodStats().getFoodLevel() > 6.0F || this.capabilities.allowFlying;
        movef = this.movementInput.moveForward;
        if (this.reSprint == 2) {
            this.movementInput.moveForward = 0.0F;
        }

        if (this.onGround && !flag1 && !flag2 && this.movementInput.moveForward >= f && !this.isSprinting() && flag3 && (!this.isUsingItem() || eventNoSlow.isSprint()) && !this.isPotionActive(Potion.blindness)) {
            if (this.sprintToggleTimer <= 0 && !this.mc.gameSettings.keyBindSprint.isKeyDown()) {
                this.sprintToggleTimer = 7;
            } else {
                this.setSprinting(true);
            }
        }

        if (!this.isSprinting() && this.movementInput.moveForward >= f && flag3 && (!this.isUsingItem() || eventNoSlow.isSprint()) && !this.isPotionActive(Potion.blindness) && this.mc.gameSettings.keyBindSprint.isKeyDown()) {
            this.setSprinting(true);
        }

        if (this.isSprinting() && (this.movementInput.moveForward < f || this.isCollidedHorizontally || !flag3)) {
            this.setSprinting(false);
        }

        if (!eventNoSlow.isSprint() && this.isUsingItem() && !this.isRiding()) {
            this.setSprinting(false);
        }
        if (this.reSprint == 2) {
            this.movementInput.moveForward = movef;
            this.reSprint = 1;
        }


        if (this.capabilities.allowFlying)
        {
            if (this.mc.playerController.isSpectatorMode())
            {
                if (!this.capabilities.isFlying)
                {
                    this.capabilities.isFlying = true;
                    this.sendPlayerAbilities();
                }
            }
            else if (!flag && this.movementInput.jump)
            {
                if (this.flyToggleTimer == 0)
                {
                    this.flyToggleTimer = 7;
                }
                else
                {
                    this.capabilities.isFlying = !this.capabilities.isFlying;
                    this.sendPlayerAbilities();
                    this.flyToggleTimer = 0;
                }
            }
        }

        if (this.capabilities.isFlying && this.isCurrentViewEntity())
        {
            if (this.movementInput.sneak)
            {
                this.motionY -= (double)(this.capabilities.getFlySpeed() * 3.0F);
            }

            if (this.movementInput.jump)
            {
                this.motionY += (double)(this.capabilities.getFlySpeed() * 3.0F);
            }
        }

        if (this.isRidingHorse())
        {
            if (this.horseJumpPowerCounter < 0)
            {
                ++this.horseJumpPowerCounter;

                if (this.horseJumpPowerCounter == 0)
                {
                    this.horseJumpPower = 0.0F;
                }
            }

            if (flag && !this.movementInput.jump)
            {
                this.horseJumpPowerCounter = -10;
                this.sendHorseJump();
            }
            else if (!flag && this.movementInput.jump)
            {
                this.horseJumpPowerCounter = 0;
                this.horseJumpPower = 0.0F;
            }
            else if (flag)
            {
                ++this.horseJumpPowerCounter;

                if (this.horseJumpPowerCounter < 10)
                {
                    this.horseJumpPower = (float)this.horseJumpPowerCounter * 0.1F;
                }
                else
                {
                    this.horseJumpPower = 0.8F + 2.0F / (float)(this.horseJumpPowerCounter - 9) * 0.1F;
                }
            }
        }
        else
        {
            this.horseJumpPower = 0.0F;
        }

        super.onLivingUpdate();

        if (this.onGround && this.capabilities.isFlying && !this.mc.playerController.isSpectatorMode())
        {
            this.capabilities.isFlying = false;
            this.sendPlayerAbilities();
        }
    }

    public float[] mySilentStrafe(float strafe, float forward, float yaw, boolean advanced) {
        Minecraft mc = Minecraft.getMinecraft();
        float diff = MathHelper.wrapAngleTo180_float(yaw - RotationUtil.getRotations()[0]);
        float newForward = 0.0F;
        float newStrafe = 0.0F;
        if (advanced) {
            double[] realMotion = MoveUtil.getMotion(0.22, strafe, forward, MC.thePlayer.rotationYaw);
            double[] realPos = new double[]{mc.thePlayer.posX, mc.thePlayer.posZ};
            realPos[0] += realMotion[0];
            realPos[1] += realMotion[1];
            ArrayList<float[]> possibleForwardStrafe = new ArrayList();
            int i = 0;

            for(boolean b = false; !b; ++i) {
                newForward = 0.0F;
                newStrafe = 0.0F;
                if (i == 0) {
                    newStrafe += strafe;
                    newForward += forward;
                    newStrafe -= forward;
                    newForward += strafe;
                    possibleForwardStrafe.add(new float[]{newForward, newStrafe});
                } else if (i == 1) {
                    newStrafe -= forward;
                    newForward += strafe;
                    possibleForwardStrafe.add(new float[]{newForward, newStrafe});
                } else if (i == 2) {
                    newStrafe -= strafe;
                    newForward -= forward;
                    newStrafe -= forward;
                    newForward += strafe;
                    possibleForwardStrafe.add(new float[]{newForward, newStrafe});
                } else if (i == 3) {
                    newStrafe -= strafe;
                    newForward -= forward;
                    possibleForwardStrafe.add(new float[]{newForward, newStrafe});
                } else if (i == 4) {
                    newStrafe -= strafe;
                    newForward -= forward;
                    newStrafe += forward;
                    newForward -= strafe;
                    possibleForwardStrafe.add(new float[]{newForward, newStrafe});
                } else if (i == 5) {
                    newStrafe += forward;
                    newForward -= strafe;
                    possibleForwardStrafe.add(new float[]{newForward, newStrafe});
                } else if (i == 6) {
                    newStrafe += strafe;
                    newForward += forward;
                    newStrafe += forward;
                    newForward -= strafe;
                    possibleForwardStrafe.add(new float[]{newForward, newStrafe});
                } else {
                    newStrafe += strafe;
                    newForward += forward;
                    possibleForwardStrafe.add(new float[]{newForward, newStrafe});
                    b = true;
                }
            }

            double distance = 5000.0;
            float[] floats = new float[2];
            Iterator var17 = possibleForwardStrafe.iterator();

            while(var17.hasNext()) {
                float[] flo = (float[])var17.next();
                if (flo[0] > 1.0F) {
                    flo[0] = 1.0F;
                } else if (flo[0] < -1.0F) {
                    flo[0] = -1.0F;
                }

                if (flo[1] > 1.0F) {
                    flo[1] = 1.0F;
                } else if (flo[1] < -1.0F) {
                    flo[1] = -1.0F;
                }

                double[] motion = MoveUtil.getMotion(0.22, flo[1], flo[0], RotationUtil.getRotations()[0]);
                motion[0] += mc.thePlayer.posX;
                motion[1] += mc.thePlayer.posZ;
                double diffX = Math.abs(realPos[0] - motion[0]);
                double diffZ = Math.abs(realPos[1] - motion[1]);
                double d0 = diffX * diffX + diffZ * diffZ;
                if (d0 < distance) {
                    distance = d0;
                    floats = flo;
                }
            }

            return new float[]{floats[1], floats[0]};
        } else {
            if ((double)diff >= 22.5 && (double)diff < 67.5) {
                newStrafe += strafe;
                newForward += forward;
                newStrafe -= forward;
                newForward += strafe;
            } else if ((double)diff >= 67.5 && (double)diff < 112.5) {
                newStrafe -= forward;
                newForward += strafe;
            } else if ((double)diff >= 112.5 && (double)diff < 157.5) {
                newStrafe -= strafe;
                newForward -= forward;
                newStrafe -= forward;
                newForward += strafe;
            } else if (!((double)diff >= 157.5) && !((double)diff <= -157.5)) {
                if ((double)diff > -157.5 && (double)diff <= -112.5) {
                    newStrafe -= strafe;
                    newForward -= forward;
                    newStrafe += forward;
                    newForward -= strafe;
                } else if ((double)diff > -112.5 && (double)diff <= -67.5) {
                    newStrafe += forward;
                    newForward -= strafe;
                } else if ((double)diff > -67.5 && (double)diff <= -22.5) {
                    newStrafe += strafe;
                    newForward += forward;
                    newStrafe += forward;
                    newForward -= strafe;
                } else {
                    newStrafe += strafe;
                    newForward += forward;
                }
            } else {
                newStrafe -= strafe;
                newForward -= forward;
            }

            return new float[]{newStrafe, newForward};
        }
    }

    public boolean isMoving() {
        return mc.thePlayer != null
                && (mc.thePlayer.movementInput.moveForward != 0F || mc.thePlayer.movementInput.moveStrafe != 0F);
    }

    public Vec3 getLastServerPosition() {
        return this.lastServerPosition;
    }

    public void setLastServerPosition(Vec3 lastServerPosition) {
        this.lastServerPosition = lastServerPosition;
    }

    public Vec3 getSeverPosition() {
        return this.severPosition;
    }

    public void setSeverPosition(Vec3 severPosition) {
        this.severPosition = severPosition;
    }

    public void setServerSprintState(boolean serverSprintState) {
        this.serverSprintState = serverSprintState;
    }
    public Vec3 getSpawnPos() {
        return this.spawnPos;
    }
    public void resetCooldown() {
        this.ticksSinceLastSwing = 0;
    }
}
