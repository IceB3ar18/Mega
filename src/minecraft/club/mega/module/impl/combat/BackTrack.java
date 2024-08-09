package club.mega.module.impl.combat;

import club.mega.Mega;
import club.mega.event.impl.*;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.impl.player.Scaffold;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ColorSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.util.AuraUtil;
import club.mega.util.RenderUtil;
import club.mega.util.TimeUtil;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.OldServerPinger;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import rip.hippo.lwjeb.annotation.Handler;

import java.awt.*;
import java.util.ArrayList;

@Module.ModuleInfo(name = "BackTrack", description = "Changes attack pos", category = Category.COMBAT)
public class BackTrack extends Module {

    private final ArrayList<Packet<INetHandler>> packets = new ArrayList<>();

    public final NumberSetting maxHitRange = new NumberSetting("MaxHitRange",  this, 3.0, 12.0, 6.0, 0.1);
    public final NumberSetting delay = new NumberSetting("Delay",  this, 0.0, 2000.0, 450.0, 1);

    public final BooleanSetting onlyWhenNeed = new BooleanSetting("OnlyWhenNeed", this, false);
    public final BooleanSetting esp = new BooleanSetting("Esp", this, true);

    public final ColorSetting color = new ColorSetting("Color", this, Color.red, esp::get);

    public final BooleanSetting velocity = new BooleanSetting("CancelVelocity", this, true);
    public final BooleanSetting keepAlive = new BooleanSetting("CancelKeepAlive", this, true);
    public final BooleanSetting timeUpdate = new BooleanSetting("CancelTimeUpdate", this, true);


    private EntityLivingBase entity = null;
    private INetHandler packetListener = null;
    private boolean blockPackets;
    private KillAura killAura;
    private WorldClient lastWorld;
    private final TimeUtil timeHelper = new TimeUtil();



    @Override
    public void onEnable() {
        this.killAura = Mega.INSTANCE.getModuleManager().getModule(KillAura.class);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Handler
    public final void packet(final EventPacket packetEvent) {
        if (packetEvent.getINetHandler() != null && packetEvent.getINetHandler() instanceof OldServerPinger) return;
        if (MC.theWorld != null)
            if (packetEvent.getType() == EventPacket.Type.RECEIVE) {
                this.packetListener = packetEvent.getINetHandler();
                synchronized (BackTrack.class) {
                    final Packet<?> p = packetEvent.getPacket();
                    if (p instanceof S14PacketEntity) {
                        S14PacketEntity packetEntity = (S14PacketEntity) p;
                        final Entity entity = MC.theWorld.getEntityByID(packetEntity.getEntityId());
                        if (entity instanceof EntityLivingBase) {
                            EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
                            entityLivingBase.realPosX += packetEntity.func_149062_c();
                            entityLivingBase.realPosY += packetEntity.func_149061_d();
                            entityLivingBase.realPosZ += packetEntity.func_149064_e();
                        }
                    }
                    if (p instanceof S18PacketEntityTeleport) {
                        S18PacketEntityTeleport teleportPacket = (S18PacketEntityTeleport) p;
                        final Entity entity = MC.theWorld.getEntityByID(teleportPacket.getEntityId());
                        if (entity instanceof EntityLivingBase) {
                            EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
                            entityLivingBase.realPosX = teleportPacket.getX();
                            entityLivingBase.realPosY = teleportPacket.getY();
                            entityLivingBase.realPosZ = teleportPacket.getZ();
                        }
                    }

                    this.entity = null;
                    if (this.killAura.isToggled()) {
                        this.entity = (EntityLivingBase) AuraUtil.getTarget();
                    }
                    if (this.entity == null) {
                        this.resetPackets(packetEvent.getINetHandler());
                        return;
                    }
                    if (MC.theWorld != null && MC.thePlayer != null) {
                        if (this.lastWorld != MC.theWorld) {
                            resetPackets(packetEvent.getINetHandler());
                            this.lastWorld = MC.theWorld;
                            return;
                        }
                        this.addPackets(p, packetEvent);
                    }
                    this.lastWorld = MC.theWorld;
                }
            }
    }

    @Handler
    public final void tick(final TickEvent event) {
        if (entity != null && MC.thePlayer != null && this.packetListener != null && MC.theWorld != null && !Scaffold.getInstance().isToggled()) {
            double d0 = (double) this.entity.realPosX / 32.0D;
            double d1 = (double) this.entity.realPosY / 32.0D;
            double d2 = (double) this.entity.realPosZ / 32.0D;
            double d3 = (double) this.entity.serverPosX / 32.0D;
            double d4 = (double) this.entity.serverPosY / 32.0D;
            double d5 = (double) this.entity.serverPosZ / 32.0D;

            AxisAlignedBB alignedBB = new AxisAlignedBB(d3 - (double) this.entity.width, d4, d5 - (double) this.entity.width, d3 + (double) this.entity.width, d4 + (double) this.entity.height, d5 + (double) this.entity.width);
            Vec3 positionEyes = MC.thePlayer.getPositionEyes(MC.timer.renderPartialTicks);
            double currentX = MathHelper.clamp_double(positionEyes.xCoord, alignedBB.minX, alignedBB.maxX);
            double currentY = MathHelper.clamp_double(positionEyes.yCoord, alignedBB.minY, alignedBB.maxY);
            double currentZ = MathHelper.clamp_double(positionEyes.zCoord, alignedBB.minZ, alignedBB.maxZ);
            AxisAlignedBB alignedBB2 = new AxisAlignedBB(d0 - (double) this.entity.width, d1, d2 - (double) this.entity.width, d0 + (double) this.entity.width, d1 + (double) this.entity.height, d2 + (double) this.entity.width);
            double realX = MathHelper.clamp_double(positionEyes.xCoord, alignedBB2.minX, alignedBB2.maxX);
            double realY = MathHelper.clamp_double(positionEyes.yCoord, alignedBB2.minY, alignedBB2.maxY);
            double realZ = MathHelper.clamp_double(positionEyes.zCoord, alignedBB2.minZ, alignedBB2.maxZ);
            double distance = this.maxHitRange.getAsDouble();
            if (!MC.thePlayer.canEntityBeSeen(this.entity)) {
                distance = distance > 3 ? 3 : distance;
            }
            double bestX = MathHelper.clamp_double(positionEyes.xCoord, this.entity.getEntityBoundingBox().minX, this.entity.getEntityBoundingBox().maxX);
            double bestY = MathHelper.clamp_double(positionEyes.yCoord, this.entity.getEntityBoundingBox().minY, this.entity.getEntityBoundingBox().maxY);
            double bestZ = MathHelper.clamp_double(positionEyes.zCoord, this.entity.getEntityBoundingBox().minZ, this.entity.getEntityBoundingBox().maxZ);
            boolean b = false;
            if (positionEyes.distanceTo(new Vec3(bestX, bestY, bestZ)) > 2.9 || (MC.thePlayer.hurtTime < 8 && MC.thePlayer.hurtTime > 1)) {
                b = true;
            }
            if (!this.onlyWhenNeed.get()) {
                b = true;
            }
            if (!(b && positionEyes.distanceTo(new Vec3(realX, realY, realZ)) > positionEyes.distanceTo(new Vec3(currentX, currentY, currentZ)) + 0.05) || !(MC.thePlayer.getDistance(d0, d1, d2) < distance) || this.timeHelper.hasTimePassed(delay.getAsInt())) {
                blockPackets = false;
                this.resetPackets(this.packetListener);
                this.timeHelper.reset();
            } else {
                blockPackets = true;
            }
        }
    }


    private void resetPackets(INetHandler netHandler) {
        if (this.packets.size() > 0) {
            synchronized (this.packets) {
                while (this.packets.size() != 0) {
                    try {
                        this.packets.get(0).processPacket(netHandler);
                    } catch (Exception ignored) {
                    }
                    this.packets.remove(this.packets.get(0));
                }

            }
        }
    }

    @Handler
    public final void onTick(final EventTick eventTick) {
        this.setTag(delay.getAsDouble() + " ms");
    }

    private void addPackets(Packet packet, EventPacket eventReadPacket) {
        synchronized (this.packets) {
            if (this.blockPacket(packet)) {
                this.packets.add(packet);
                eventReadPacket.setCancelled(true);
            }
        }
    }

    private boolean blockPacket(Packet packet) {
        if (packet instanceof S03PacketTimeUpdate) {
            return this.timeUpdate.get();
        } else if (packet instanceof S00PacketKeepAlive) {
            return this.keepAlive.get();
        } else if (packet instanceof S12PacketEntityVelocity || packet instanceof S27PacketExplosion) {
            return this.velocity.get();
        } else {
            return packet instanceof S32PacketConfirmTransaction || packet instanceof S14PacketEntity || packet instanceof S19PacketEntityStatus || packet instanceof S19PacketEntityHeadLook || packet instanceof S18PacketEntityTeleport || packet instanceof S0FPacketSpawnMob;
        }
    }

    @Handler
    public final void onRender(final EventServerESP event) {

        if(esp.get() && !Scaffold.getInstance().isToggled()) {
            if (this.entity != null && blockPackets) {

                double d0 = (double) entity.serverPosX / 32.0;
                double d1 = (double) entity.serverPosY / 32.0;
                double d2 = (double) entity.serverPosZ / 32.0;
                if (entity instanceof EntityLivingBase) {
                    EntityLivingBase livingBase = (EntityLivingBase) entity;
                    d0 = (double) livingBase.realPosX / 32.0;
                    d1 = (double) livingBase.realPosY / 32.0;
                    d2 = (double) livingBase.realPosZ / 32.0;
                }

                float x = (float) (d0 - RenderManager.getRenderPosX());
                float y = (float) (d1 - RenderManager.getRenderPosY());
                float z = (float) (d2 - RenderManager.getRenderPosZ());
                if (entity.posX != x && entity.posY != y && entity.posZ != z) {
                    MC.gameSettings.entityShadows = false;
                    RendererLivingEntity.renderNametags = false;
                    RenderUtil.drawEntityServerESP(entity, event.getPartialTicks(), color.getColor().getRGB());
                    RendererLivingEntity.renderNametags = true;
                    MC.gameSettings.entityShadows = true;
                }
            }
        }
    }

}
