package net.minecraft.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.ServersideAttributeMap;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.network.play.server.S0APacketUseBed;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.network.play.server.S0EPacketSpawnObject;
import net.minecraft.network.play.server.S0FPacketSpawnMob;
import net.minecraft.network.play.server.S10PacketSpawnPainting;
import net.minecraft.network.play.server.S11PacketSpawnExperienceOrb;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.network.play.server.S19PacketEntityHeadLook;
import net.minecraft.network.play.server.S1BPacketEntityAttach;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S20PacketEntityProperties;
import net.minecraft.network.play.server.S49PacketUpdateEntityNBT;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.storage.MapData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static club.mega.interfaces.MinecraftInterface.MC;

public class EntityTrackerEntry
{
    private static final Logger logger = LogManager.getLogger();
    public Entity trackedEntity;
    public int trackingDistanceThreshold;
    public int updateFrequency;
    private Set<EntityTrackerEntry> trackedEntities = Sets.newHashSet();
    public int encodedPosX;
    public int encodedPosY;
    public int encodedPosZ;
    public int encodedRotationYaw;
    public int encodedRotationPitch;
    public int lastHeadMotion;
    public double lastTrackedEntityMotionX;
    public double lastTrackedEntityMotionY;
    public double motionZ;
    public int updateCounter;
    private double lastTrackedEntityPosX;
    private double lastTrackedEntityPosY;
    private double lastTrackedEntityPosZ;
    private boolean firstUpdateDone;
    private boolean sendVelocityUpdates;
    private int ticksSinceLastForcedTeleport;
    private Entity field_85178_v;
    private boolean ridingEntity;
    private boolean onGround;
    public boolean playerEntitiesUpdated;
    public Set<EntityPlayerMP> trackingPlayers = Sets.<EntityPlayerMP>newHashSet();

    public EntityTrackerEntry(Entity trackedEntityIn, int trackingDistanceThresholdIn, int updateFrequencyIn, boolean sendVelocityUpdatesIn)
    {
        this.trackedEntity = trackedEntityIn;
        this.trackingDistanceThreshold = trackingDistanceThresholdIn;
        this.updateFrequency = updateFrequencyIn;
        this.sendVelocityUpdates = sendVelocityUpdatesIn;
        this.encodedPosX = MathHelper.floor_double(trackedEntityIn.posX * 32.0D);
        this.encodedPosY = MathHelper.floor_double(trackedEntityIn.posY * 32.0D);
        this.encodedPosZ = MathHelper.floor_double(trackedEntityIn.posZ * 32.0D);
        this.encodedRotationYaw = MathHelper.floor_float(trackedEntityIn.rotationYaw * 256.0F / 360.0F);
        this.encodedRotationPitch = MathHelper.floor_float(trackedEntityIn.rotationPitch * 256.0F / 360.0F);
        this.lastHeadMotion = MathHelper.floor_float(trackedEntityIn.getRotationYawHead() * 256.0F / 360.0F);
        this.onGround = trackedEntityIn.onGround;
    }

    public boolean equals(Object p_equals_1_)
    {
        return p_equals_1_ instanceof EntityTrackerEntry ? ((EntityTrackerEntry)p_equals_1_).trackedEntity.getEntityId() == this.trackedEntity.getEntityId() : false;
    }

    public int hashCode()
    {
        return this.trackedEntity.getEntityId();
    }

    public void updatePlayerList(List<EntityPlayer> players)
    {
        List<EntityPlayerMP> list = Lists.newArrayList();
        Iterator var2 = this.trackedEntities.iterator();

        while(var2.hasNext()) {
            EntityTrackerEntry entitytrackerentry = (EntityTrackerEntry)var2.next();
            entitytrackerentry.updatePlayerList(MC.theWorld.playerEntities);
            if (entitytrackerentry.playerEntitiesUpdated && entitytrackerentry.trackedEntity instanceof EntityPlayerMP) {
                list.add((EntityPlayerMP)entitytrackerentry.trackedEntity);
            }
        }

        for(int i = 0; i < list.size(); ++i) {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)list.get(i);
            Iterator var4 = this.trackedEntities.iterator();

            while(var4.hasNext()) {
                EntityTrackerEntry entitytrackerentry1 = (EntityTrackerEntry)var4.next();
                if (entitytrackerentry1.trackedEntity != entityplayermp) {
                    entitytrackerentry1.updatePlayerEntity(entityplayermp);
                }
            }
        }

    }

    private void sendMetadataToAllAssociatedPlayers()
    {
        DataWatcher datawatcher = this.trackedEntity.getDataWatcher();

        if (datawatcher.hasObjectChanged())
        {
            this.func_151261_b(new S1CPacketEntityMetadata(this.trackedEntity.getEntityId(), datawatcher, false));
        }

        if (this.trackedEntity instanceof EntityLivingBase)
        {
            ServersideAttributeMap serversideattributemap = (ServersideAttributeMap)((EntityLivingBase)this.trackedEntity).getAttributeMap();
            Set<IAttributeInstance> set = serversideattributemap.getAttributeInstanceSet();

            if (!set.isEmpty())
            {
                this.func_151261_b(new S20PacketEntityProperties(this.trackedEntity.getEntityId(), set));
            }

            set.clear();
        }
    }

    public void sendPacketToTrackedPlayers(Packet packetIn)
    {
        for (EntityPlayerMP entityplayermp : this.trackingPlayers)
        {
            entityplayermp.playerNetServerHandler.sendPacket(packetIn);
        }
    }

    public void func_151261_b(Packet packetIn)
    {
        this.sendPacketToTrackedPlayers(packetIn);

        if (this.trackedEntity instanceof EntityPlayerMP)
        {
            ((EntityPlayerMP)this.trackedEntity).playerNetServerHandler.sendPacket(packetIn);
        }
    }

    public void sendDestroyEntityPacketToTrackedPlayers()
    {
        for (EntityPlayerMP entityplayermp : this.trackingPlayers)
        {
            entityplayermp.removeEntity(this.trackedEntity);
        }
    }

    public void removeFromTrackedPlayers(EntityPlayerMP playerMP)
    {
        if (this.trackingPlayers.contains(playerMP))
        {
            playerMP.removeEntity(this.trackedEntity);
            this.trackingPlayers.remove(playerMP);
        }
    }

    public void updatePlayerEntity(EntityPlayerMP playerMP)
    {
        if (playerMP != this.trackedEntity)
        {
            if (this.func_180233_c(playerMP))
            {
                if (!this.trackingPlayers.contains(playerMP) && (this.isPlayerWatchingThisChunk(playerMP) || this.trackedEntity.forceSpawn))
                {
                    this.trackingPlayers.add(playerMP);
                    Packet packet = this.createSpawnPacket();
                    playerMP.playerNetServerHandler.sendPacket(packet);

                    if (!this.trackedEntity.getDataWatcher().getIsBlank())
                    {
                        playerMP.playerNetServerHandler.sendPacket(new S1CPacketEntityMetadata(this.trackedEntity.getEntityId(), this.trackedEntity.getDataWatcher(), true));
                    }

                    NBTTagCompound nbttagcompound = this.trackedEntity.getNBTTagCompound();

                    if (nbttagcompound != null)
                    {
                        playerMP.playerNetServerHandler.sendPacket(new S49PacketUpdateEntityNBT(this.trackedEntity.getEntityId(), nbttagcompound));
                    }

                    if (this.trackedEntity instanceof EntityLivingBase)
                    {
                        ServersideAttributeMap serversideattributemap = (ServersideAttributeMap)((EntityLivingBase)this.trackedEntity).getAttributeMap();
                        Collection<IAttributeInstance> collection = serversideattributemap.getWatchedAttributes();

                        if (!collection.isEmpty())
                        {
                            playerMP.playerNetServerHandler.sendPacket(new S20PacketEntityProperties(this.trackedEntity.getEntityId(), collection));
                        }
                    }

                    this.lastTrackedEntityMotionX = this.trackedEntity.motionX;
                    this.lastTrackedEntityMotionY = this.trackedEntity.motionY;
                    this.motionZ = this.trackedEntity.motionZ;

                    if (this.sendVelocityUpdates && !(packet instanceof S0FPacketSpawnMob))
                    {
                        playerMP.playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(this.trackedEntity.getEntityId(), this.trackedEntity.motionX, this.trackedEntity.motionY, this.trackedEntity.motionZ));
                    }

                    if (this.trackedEntity.ridingEntity != null)
                    {
                        playerMP.playerNetServerHandler.sendPacket(new S1BPacketEntityAttach(0, this.trackedEntity, this.trackedEntity.ridingEntity));
                    }

                    if (this.trackedEntity instanceof EntityLiving && ((EntityLiving)this.trackedEntity).getLeashedToEntity() != null)
                    {
                        playerMP.playerNetServerHandler.sendPacket(new S1BPacketEntityAttach(1, this.trackedEntity, ((EntityLiving)this.trackedEntity).getLeashedToEntity()));
                    }

                    if (this.trackedEntity instanceof EntityLivingBase)
                    {
                        for (int i = 0; i < 5; ++i)
                        {
                            ItemStack itemstack = ((EntityLivingBase)this.trackedEntity).getEquipmentInSlot(i);

                            if (itemstack != null)
                            {
                                playerMP.playerNetServerHandler.sendPacket(new S04PacketEntityEquipment(this.trackedEntity.getEntityId(), i, itemstack));
                            }
                        }
                    }

                    if (this.trackedEntity instanceof EntityPlayer)
                    {
                        EntityPlayer entityplayer = (EntityPlayer)this.trackedEntity;

                        if (entityplayer.isPlayerSleeping())
                        {
                            playerMP.playerNetServerHandler.sendPacket(new S0APacketUseBed(entityplayer, new BlockPos(this.trackedEntity)));
                        }
                    }

                    if (this.trackedEntity instanceof EntityLivingBase)
                    {
                        EntityLivingBase entitylivingbase = (EntityLivingBase)this.trackedEntity;

                        for (PotionEffect potioneffect : entitylivingbase.getActivePotionEffects())
                        {
                            playerMP.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(this.trackedEntity.getEntityId(), potioneffect));
                        }
                    }
                }
            }
            else if (this.trackingPlayers.contains(playerMP))
            {
                this.trackingPlayers.remove(playerMP);
                playerMP.removeEntity(this.trackedEntity);
            }
        }
    }

    public boolean func_180233_c(EntityPlayerMP playerMP)
    {
        double d0 = playerMP.posX - (double)(this.encodedPosX / 32);
        double d1 = playerMP.posZ - (double)(this.encodedPosZ / 32);
        return d0 >= (double)(-this.trackingDistanceThreshold) && d0 <= (double)this.trackingDistanceThreshold && d1 >= (double)(-this.trackingDistanceThreshold) && d1 <= (double)this.trackingDistanceThreshold && this.trackedEntity.isSpectatedByPlayer(playerMP);
    }

    private boolean isPlayerWatchingThisChunk(EntityPlayerMP playerMP)
    {
        return playerMP.getServerForPlayer().getPlayerManager().isPlayerWatchingChunk(playerMP, this.trackedEntity.chunkCoordX, this.trackedEntity.chunkCoordZ);
    }

    public void updatePlayerEntities(List<EntityPlayer> players)
    {
        for (int i = 0; i < players.size(); ++i)
        {
            this.updatePlayerEntity((EntityPlayerMP)players.get(i));
        }
    }

    private Packet createSpawnPacket()
    {
        if (this.trackedEntity.isDead)
        {
            logger.warn("Fetching addPacket for removed entity");
        }

        if (this.trackedEntity instanceof EntityItem)
        {
            return new S0EPacketSpawnObject(this.trackedEntity, 2, 1);
        }
        else if (this.trackedEntity instanceof EntityPlayerMP)
        {
            return new S0CPacketSpawnPlayer((EntityPlayer)this.trackedEntity);
        }
        else if (this.trackedEntity instanceof EntityMinecart)
        {
            EntityMinecart entityminecart = (EntityMinecart)this.trackedEntity;
            return new S0EPacketSpawnObject(this.trackedEntity, 10, entityminecart.getMinecartType().getNetworkID());
        }
        else if (this.trackedEntity instanceof EntityBoat)
        {
            return new S0EPacketSpawnObject(this.trackedEntity, 1);
        }
        else if (this.trackedEntity instanceof IAnimals)
        {
            this.lastHeadMotion = MathHelper.floor_float(this.trackedEntity.getRotationYawHead() * 256.0F / 360.0F);
            return new S0FPacketSpawnMob((EntityLivingBase)this.trackedEntity);
        }
        else if (this.trackedEntity instanceof EntityFishHook)
        {
            Entity entity1 = ((EntityFishHook)this.trackedEntity).angler;
            return new S0EPacketSpawnObject(this.trackedEntity, 90, entity1 != null ? entity1.getEntityId() : this.trackedEntity.getEntityId());
        }
        else if (this.trackedEntity instanceof EntityArrow)
        {
            Entity entity = ((EntityArrow)this.trackedEntity).shootingEntity;
            return new S0EPacketSpawnObject(this.trackedEntity, 60, entity != null ? entity.getEntityId() : this.trackedEntity.getEntityId());
        }
        else if (this.trackedEntity instanceof EntitySnowball)
        {
            return new S0EPacketSpawnObject(this.trackedEntity, 61);
        }
        else if (this.trackedEntity instanceof EntityPotion)
        {
            return new S0EPacketSpawnObject(this.trackedEntity, 73, ((EntityPotion)this.trackedEntity).getPotionDamage());
        }
        else if (this.trackedEntity instanceof EntityExpBottle)
        {
            return new S0EPacketSpawnObject(this.trackedEntity, 75);
        }
        else if (this.trackedEntity instanceof EntityEnderPearl)
        {
            return new S0EPacketSpawnObject(this.trackedEntity, 65);
        }
        else if (this.trackedEntity instanceof EntityEnderEye)
        {
            return new S0EPacketSpawnObject(this.trackedEntity, 72);
        }
        else if (this.trackedEntity instanceof EntityFireworkRocket)
        {
            return new S0EPacketSpawnObject(this.trackedEntity, 76);
        }
        else if (this.trackedEntity instanceof EntityFireball)
        {
            EntityFireball entityfireball = (EntityFireball)this.trackedEntity;
            S0EPacketSpawnObject s0epacketspawnobject2 = null;
            int i = 63;

            if (this.trackedEntity instanceof EntitySmallFireball)
            {
                i = 64;
            }
            else if (this.trackedEntity instanceof EntityWitherSkull)
            {
                i = 66;
            }

            if (entityfireball.shootingEntity != null)
            {
                s0epacketspawnobject2 = new S0EPacketSpawnObject(this.trackedEntity, i, ((EntityFireball)this.trackedEntity).shootingEntity.getEntityId());
            }
            else
            {
                s0epacketspawnobject2 = new S0EPacketSpawnObject(this.trackedEntity, i, 0);
            }

            s0epacketspawnobject2.setSpeedX((int)(entityfireball.accelerationX * 8000.0D));
            s0epacketspawnobject2.setSpeedY((int)(entityfireball.accelerationY * 8000.0D));
            s0epacketspawnobject2.setSpeedZ((int)(entityfireball.accelerationZ * 8000.0D));
            return s0epacketspawnobject2;
        }
        else if (this.trackedEntity instanceof EntityEgg)
        {
            return new S0EPacketSpawnObject(this.trackedEntity, 62);
        }
        else if (this.trackedEntity instanceof EntityTNTPrimed)
        {
            return new S0EPacketSpawnObject(this.trackedEntity, 50);
        }
        else if (this.trackedEntity instanceof EntityEnderCrystal)
        {
            return new S0EPacketSpawnObject(this.trackedEntity, 51);
        }
        else if (this.trackedEntity instanceof EntityFallingBlock)
        {
            EntityFallingBlock entityfallingblock = (EntityFallingBlock)this.trackedEntity;
            return new S0EPacketSpawnObject(this.trackedEntity, 70, Block.getStateId(entityfallingblock.getBlock()));
        }
        else if (this.trackedEntity instanceof EntityArmorStand)
        {
            return new S0EPacketSpawnObject(this.trackedEntity, 78);
        }
        else if (this.trackedEntity instanceof EntityPainting)
        {
            return new S10PacketSpawnPainting((EntityPainting)this.trackedEntity);
        }
        else if (this.trackedEntity instanceof EntityItemFrame)
        {
            EntityItemFrame entityitemframe = (EntityItemFrame)this.trackedEntity;
            S0EPacketSpawnObject s0epacketspawnobject1 = new S0EPacketSpawnObject(this.trackedEntity, 71, entityitemframe.facingDirection.getHorizontalIndex());
            BlockPos blockpos1 = entityitemframe.getHangingPosition();
            s0epacketspawnobject1.setX(MathHelper.floor_float((float)(blockpos1.getX() * 32)));
            s0epacketspawnobject1.setY(MathHelper.floor_float((float)(blockpos1.getY() * 32)));
            s0epacketspawnobject1.setZ(MathHelper.floor_float((float)(blockpos1.getZ() * 32)));
            return s0epacketspawnobject1;
        }
        else if (this.trackedEntity instanceof EntityLeashKnot)
        {
            EntityLeashKnot entityleashknot = (EntityLeashKnot)this.trackedEntity;
            S0EPacketSpawnObject s0epacketspawnobject = new S0EPacketSpawnObject(this.trackedEntity, 77);
            BlockPos blockpos = entityleashknot.getHangingPosition();
            s0epacketspawnobject.setX(MathHelper.floor_float((float)(blockpos.getX() * 32)));
            s0epacketspawnobject.setY(MathHelper.floor_float((float)(blockpos.getY() * 32)));
            s0epacketspawnobject.setZ(MathHelper.floor_float((float)(blockpos.getZ() * 32)));
            return s0epacketspawnobject;
        }
        else if (this.trackedEntity instanceof EntityXPOrb)
        {
            return new S11PacketSpawnExperienceOrb((EntityXPOrb)this.trackedEntity);
        }
        else
        {
            throw new IllegalArgumentException("Don\'t know how to add " + this.trackedEntity.getClass() + "!");
        }
    }

    public void removeTrackedPlayerSymmetric(EntityPlayerMP playerMP)
    {
        if (this.trackingPlayers.contains(playerMP))
        {
            this.trackingPlayers.remove(playerMP);
            playerMP.removeEntity(this.trackedEntity);
        }
    }
}
