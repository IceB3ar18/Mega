//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package club.mega.util;

import java.util.ArrayList;

import club.mega.module.impl.combat.Velocity;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class PlayerUtil {
    public PlayerUtil() {
    }
    static Minecraft mc = Minecraft.getMinecraft();
    public static double getEffectiveHealth(EntityLivingBase entity) {
        return entity.getHealth() * (entity.getMaxHealth() / entity.getTotalArmorValue());
    }
    public static ArrayList<ItemStack> arrayToArrayList(ItemStack[] itemStackArray) {
        ArrayList<ItemStack> itemStackList = new ArrayList();
        if (itemStackArray != null) {
            ItemStack[] var2 = itemStackArray;
            int var3 = itemStackArray.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                ItemStack itemStack = var2[var4];
                if (itemStack != null) {
                    itemStackList.add(itemStack);
                }
            }
        }

        return itemStackList;
    }


    public static void verusdmg() {
        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.001, mc.thePlayer.posZ, false));
        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
    }

    public static void cubedmg() {
        for(int i = 0; i < 51; ++i) {
            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.06, mc.thePlayer.posZ, false));
            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
        }

        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
        mc.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
    }

    public static double[] predictPosition(EntityPlayer entity, int predictTicks) {
        double diffX = entity.prevPosX - entity.posX;
        double diffZ = entity.prevPosZ - entity.posZ;
        double posX = entity.posX;
        double posZ = entity.posZ;

        for(int i = 0; i <= predictTicks; ++i) {
            posX -= diffX * (double)i;
            posZ -= diffZ * (double)i;
        }

        return new double[]{posX, posZ};
    }

  

    public static Vec3 getPredictedPos(boolean isHitting, Entity targetEntity, float forward, float strafe) {
        strafe *= 0.98F;
        forward *= 0.98F;
        float f4 = 0.91F;
        double motionX = mc.thePlayer.motionX;
        double motionZ = mc.thePlayer.motionZ;
        double motionY = mc.thePlayer.motionY;
        boolean isSprinting = mc.thePlayer.isSprinting();
        float f;
        float friction;
        if (isHitting) {
            f = (float)mc.thePlayer.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
            friction = 0.0F;
            if (targetEntity instanceof EntityLivingBase) {
                friction = EnchantmentHelper.getModifierForCreature(mc.thePlayer.getHeldItem(), ((EntityLivingBase)targetEntity).getCreatureAttribute());
            } else {
                friction = EnchantmentHelper.getModifierForCreature(mc.thePlayer.getHeldItem(), EnumCreatureAttribute.UNDEFINED);
            }

            if (f > 0.0F || friction > 0.0F) {
                int i = EnchantmentHelper.getKnockbackModifier(mc.thePlayer);
                if (mc.thePlayer.isSprinting()) {
                    ++i;
                }

                boolean flag2 = targetEntity.attackEntityFrom(DamageSource.causePlayerDamage(mc.thePlayer), f);
                if (flag2) {
                    if (i > 0) {
                        Minecraft.getMinecraft().clickMouse();

                        motionX *= 0.6;
                        motionZ *= 0.6;
                        isSprinting = false;
                    } else if (Velocity.getInstance().isToggled() && Velocity.getInstance().mode.is("Intave") && Minecraft.getMinecraft().thePlayer.hurtTime != 0) {
                        motionX *= Velocity.getInstance().XZValueIntave.getAsFloat();
                        motionZ *= Velocity.getInstance().XZValueIntave.getAsFloat();
                        isSprinting = false;
                    }
                }
            }
        }

        if (mc.thePlayer.isJumping && mc.thePlayer.onGround && mc.thePlayer.getJumpTicks() == 0) {
            motionY = (double)mc.thePlayer.getJumpUpwardsMotion();
            if (mc.thePlayer.isPotionActive(Potion.jump)) {
                motionY += (double)((float)(mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F);
            }

            if (isSprinting) {
                f = mc.thePlayer.rotationYaw * 0.017453292F;
                motionX -= (double)(MathHelper.sin(f) * 0.2F);
                motionZ += (double)(MathHelper.cos(f) * 0.2F);
            }
        }

        if (mc.thePlayer.onGround) {
            f4 = mc.thePlayer.worldObj.getBlockState(new BlockPos(MathHelper.floor_double(mc.thePlayer.posX), MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minY) - 1, MathHelper.floor_double(mc.thePlayer.posZ))).getBlock().slipperiness * 0.91F;
        }

        f = 0.16277136F / (f4 * f4 * f4);
        if (mc.thePlayer.onGround) {
            friction = mc.thePlayer.getAIMoveSpeed() * f;

        } else {
            friction = mc.thePlayer.jumpMovementFactor;
        }

        f = strafe * strafe + forward * forward;
        if (f >= 1.0E-4F) {
            f = MathHelper.sqrt_float(f);
            if (f < 1.0F) {
                f = 1.0F;
            }

            f = friction / f;
            strafe *= f;
            forward *= f;
            float f1 = MathHelper.sin(mc.thePlayer.rotationYaw * 3.1415927F / 180.0F);
            float f2 = MathHelper.cos(mc.thePlayer.rotationYaw * 3.1415927F / 180.0F);
            motionX += (double)(strafe * f2 - forward * f1);
            motionZ += (double)(forward * f2 + strafe * f1);
        }

        f4 = 0.91F;
        if (mc.thePlayer.onGround) {
            f4 = mc.thePlayer.worldObj.getBlockState(new BlockPos(MathHelper.floor_double(mc.thePlayer.posX), MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minY) - 1, MathHelper.floor_double(mc.thePlayer.posZ))).getBlock().slipperiness * 0.91F;
        }

        motionY *= 0.9800000190734863;
        motionX *= (double)f4;
        motionZ *= (double)f4;
        return new Vec3(motionX, motionY, motionZ);
    }
}
