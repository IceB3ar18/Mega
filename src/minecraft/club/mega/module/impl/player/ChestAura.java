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

@Module.ModuleInfo(name = "ChestAura", description = "Opens Chests automaticly", category = Category.PLAYER)
public class ChestAura extends Module {
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
    public final BooleanSetting moveFix = new BooleanSetting("MoveFix", this, true);
    public final NumberSetting yawSpeed = new NumberSetting("YawSpeed", this, 0, 180, 180, 1);
    public final NumberSetting pitchSpeed = new NumberSetting("PitchSpeed", this, 0, 180, 180, 1);
    public final NumberSetting delay = new NumberSetting("Delay", this, 0, 1000.0, 500.0, 1);
    public final NumberSetting range = new NumberSetting("Range", this, 0, 6.0, 4.5, 0.1);

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }


    @Handler
    public void onEventEarlyTick(EventEarlyTick eventEarlyTick) {

        float yawSpeed = (float) (this.yawSpeed.getAsFloat() / 200 + (double) RandomUtil.nextFloat(0.0F, 15.0F));
        float pitchSpeed = (float) (this.pitchSpeed.getAsFloat() + (double) RandomUtil.nextFloat(0.0F, 15.0F));
        Block block = MC.theWorld.getBlockState(this.b).getBlock();
        float[] floats = this.rotationUtil.rotateToBlockPos((double) this.b.getX() + block.getBlockBoundsMaxX() / 2.0, (double) this.b.getY() + block.getBlockBoundsMaxY() / 2.0, (double) this.b.getZ() + block.getBlockBoundsMaxZ() / 2.0, this.rots[0], this.rots[1], yawSpeed, pitchSpeed, false);

    }

    @Handler
    public void onEventClick(EventClickMouse eventClick) {


    }

    @Handler
    public final void moveFlying(final EventMoveFlying event) {

    }


    @Handler
    public final void look(final EventLook event) {

    }

    @Handler
    public final void renderPitch(final EventRenderPitch event) {

    }


}
