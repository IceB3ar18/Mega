package club.mega.module.impl.player;

import club.mega.event.impl.EventPacket;
import club.mega.event.impl.EventRender3D;
import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.util.RandomUtil;
import club.mega.util.customPackets.CustomC00PacketKeepAlive;
import io.netty.buffer.Unpooled;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.status.client.C01PacketPing;
import rip.hippo.lwjeb.annotation.Handler;

import java.util.ArrayList;
import java.util.Iterator;

@Module.ModuleInfo(name = "Disabler", description = "Disables every single anicheat in the fucking world lmao", category = Category.PLAYER)
public class Disabler extends Module {
    private final ArrayList<CustomC00PacketKeepAlive> keepAlivePackets = new ArrayList();
    private final int counter = 0;
    public final BooleanSetting pingSpoof = new BooleanSetting("PingSpoof", this, false);
    public final BooleanSetting royalPixels = new BooleanSetting("RoyalPixels", this, false);
    public final BooleanSetting minemenStrafe = new BooleanSetting("MinemenStrafe", this, false);
    public final BooleanSetting universoCraft = new BooleanSetting("UniversoCraft", this, false);
    public final BooleanSetting spectate = new BooleanSetting("Spectate", this, false);
    public final BooleanSetting keepAlive = new BooleanSetting("C00PacketKeepAlive", this, false);
    public final BooleanSetting ping = new BooleanSetting("C01PacketPing", this, false);
    public final BooleanSetting entityAction = new BooleanSetting("C0BPacketEntityAction", this, false);
    public final BooleanSetting playerAbilities = new BooleanSetting("PingSpooC13PacketPlayerAbilitiesf", this, false);
    public final BooleanSetting confirmTransaction = new BooleanSetting("C0FPacketConfirmTransaction", this, false);
    public final BooleanSetting ncpTimer = new BooleanSetting("NCPTimerSemi", this, false);
    public final BooleanSetting hac = new BooleanSetting("HAC", this, false);
    public final BooleanSetting noSprint = new BooleanSetting("NoSprint", this, false);
    public final BooleanSetting blcSpoof = new BooleanSetting("BLC-Spoof", this, false);
    public final BooleanSetting aac5 = new BooleanSetting("AAC5", this, false);
    public final BooleanSetting antiVanillaEumel = new BooleanSetting("NoFlag", this, false);

    public final BooleanSetting frequencyFull = new BooleanSetting("FrequencyFull", this, false);
    public final BooleanSetting debug = new BooleanSetting("Debug", this, false);
    public final NumberSetting delay = new NumberSetting("Delay", this, 1.0, 4000.0, 1000, 1, pingSpoof::get);
    private boolean disabling;


    @Handler
    public void tick(final EventTick event) {
        if (this.ncpTimer.get()) {
            MC.thePlayer.setPosition(MC.thePlayer.posX, MC.thePlayer.posY - 0.017, MC.thePlayer.posZ);
            MC.thePlayer.motionY = 0.019;
        }

        if (this.hac.get() && MC.thePlayer.ticksExisted % 10 == 0) {
            MC.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(MC.thePlayer.posX, MC.thePlayer.posY - 11.0, MC.thePlayer.posZ, MC.thePlayer.cameraYaw, MC.thePlayer.cameraPitch, true));
        }

        if (this.aac5.get() && !MC.isIntegratedServerRunning()) {
            MC.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(MC.thePlayer.posX, MC.thePlayer.posY - 1.0E159, MC.thePlayer.posZ + 10.0, 0.0F, 0.0F, true));
            MC.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(MC.thePlayer.posX, MC.thePlayer.posY, MC.thePlayer.posZ, 0.0F, 0.0F, true));
        }

        if (this.royalPixels.get()) {
            C13PacketPlayerAbilities capabilities = new C13PacketPlayerAbilities();
            capabilities.setAllowFlying(true);
            capabilities.setFlying(true);
            MC.thePlayer.sendQueue.addToSendQueue(capabilities);
        }

    }

    @Handler
    public void onEventRender3D(final EventRender3D event) {
        if (this.pingSpoof.get() && MC.thePlayer != null && !this.keepAlivePackets.isEmpty()) {
            ArrayList<CustomC00PacketKeepAlive> toRemove = new ArrayList();
            Iterator var3 = this.keepAlivePackets.iterator();

            while(var3.hasNext()) {
                CustomC00PacketKeepAlive packet = (CustomC00PacketKeepAlive)var3.next();
                if (packet.getTime() < System.currentTimeMillis()) {
                    MC.thePlayer.sendQueue.addToSendQueue(new C00PacketKeepAlive(packet.getKey()));
                    toRemove.add(packet);
                }
            }

            this.keepAlivePackets.removeIf(toRemove::contains);
        }

    }

    @Handler
    public void packet(final EventPacket event) {
        if(event.getType() == EventPacket.Type.RECEIVE) {
            if (this.frequencyFull.get() && event.getPacket() instanceof S02PacketChat && ((S02PacketChat)event.getPacket()).getChatComponent().getUnformattedText().contains("Frequency")) {
                event.setCancelled(true);
            }

            if (this.antiVanillaEumel.get() && event.getPacket() instanceof S08PacketPlayerPosLook && MC.thePlayer.ticksExisted > 20) {
                double x = ((S08PacketPlayerPosLook)event.getPacket()).getX() - MC.thePlayer.posX;
                double y = ((S08PacketPlayerPosLook)event.getPacket()).getY() - MC.thePlayer.posY;
                double z = ((S08PacketPlayerPosLook)event.getPacket()).getZ() - MC.thePlayer.posZ;
                double diff = Math.sqrt(x * x + y * y + z * z);
                if (diff <= 8.0) {

                    event.setCancelled(true);
                    MC.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(((S08PacketPlayerPosLook)event.getPacket()).getX(), ((S08PacketPlayerPosLook)event.getPacket()).getY(), ((S08PacketPlayerPosLook)event.getPacket()).getZ(), ((S08PacketPlayerPosLook)event.getPacket()).getYaw(), ((S08PacketPlayerPosLook)event.getPacket()).getPitch(), true));
                }
            }

        } else if(event.getType() == EventPacket.Type.SEND) {
            {
                Packet packet = event.getPacket();
                if (this.universoCraft.get()) {
                    if (packet instanceof S07PacketRespawn) {
                        this.disabling = true;
                    } else if (packet instanceof C02PacketUseEntity) {
                        this.disabling = false;
                    } else if (packet instanceof C03PacketPlayer && MC.thePlayer.ticksExisted <= 10) {
                        this.disabling = true;
                    } else if (packet instanceof C0FPacketConfirmTransaction && this.disabling && MC.thePlayer.ticksExisted < 350) {
                        ((C0FPacketConfirmTransaction)packet).setUid((short)(MC.thePlayer.ticksExisted % 2 == 0 ? Short.MIN_VALUE : 32767));
                    }
                }

                if (this.noSprint.get() && packet instanceof C0BPacketEntityAction && ((C0BPacketEntityAction)packet).getAction() == C0BPacketEntityAction.Action.START_SPRINTING) {
                    event.setCancelled(true);
                }

                if (this.pingSpoof.get() && packet instanceof C00PacketKeepAlive) {
                    C00PacketKeepAlive c00PacketKeepAlive = (C00PacketKeepAlive)packet;
                    this.keepAlivePackets.add(new CustomC00PacketKeepAlive(c00PacketKeepAlive.getKey(), (long)((double)System.currentTimeMillis() + this.delay.getAsDouble() + (double)RandomUtil.nextLong(0L, 200L))));
                    event.setCancelled(true);
                }

                if (this.minemenStrafe.get() && packet instanceof C0FPacketConfirmTransaction && MC.thePlayer.ticksExisted % 3 == 0) {
                    event.setCancelled(true);
                }

                if (packet instanceof C00PacketKeepAlive && this.keepAlive.get()) {
                    event.setCancelled(true);
                }

                if (packet instanceof C01PacketPing && this.ping.get()) {
                    event.setCancelled(true);
                }

                if (packet instanceof C0BPacketEntityAction && this.entityAction.get()) {
                    event.setCancelled(true);
                }

                if (packet instanceof C13PacketPlayerAbilities && this.playerAbilities.get()) {
                    event.setCancelled(true);
                }

                if (packet instanceof C0FPacketConfirmTransaction && this.confirmTransaction.get()) {
                    event.setCancelled(true);
                }

                if (packet instanceof C03PacketPlayer && this.spectate.get()) {
                    MC.thePlayer.sendQueue.addToSendQueue(new C18PacketSpectate(MC.thePlayer.getUniqueID()));
                }

                if (packet instanceof C17PacketCustomPayload && this.blcSpoof.get()) {
                    C17PacketCustomPayload c17 = (C17PacketCustomPayload)event.getPacket();
                    if (c17.getChannelName().equals("MC|Brand")) {
                        event.setPacket(new C17PacketCustomPayload(c17.getChannelName(), (new PacketBuffer(Unpooled.buffer())).writeString("blc")));
                    }
                }

            }

        }

    }
    @Override
    public void onEnable() {
        super.onEnable();
        this.keepAlivePackets.clear();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (!this.keepAlivePackets.isEmpty()) {
            this.keepAlivePackets.clear();
        }
    }

}
