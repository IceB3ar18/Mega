package club.mega.module.impl.visual;

import club.mega.event.impl.EventRender3D;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.ColorSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.util.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.*;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;
import rip.hippo.lwjeb.annotation.Handler;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
@Module.ModuleInfo(name = "Trajectories", description = "Trajectories", category = Category.VISUAL)

public class Trajectories extends Module {
    private final ArrayList<Vec3> positions = new ArrayList<>();

    public final ColorSetting color_line = new ColorSetting("Line Color", this, new Color(255, 255, 255, 195));
    public final ColorSetting color_final = new ColorSetting("Final Color", this, new Color(0, 89, 255, 136));
    public final NumberSetting lineWidth = new NumberSetting("lineWidth", this, 0.1, 10.0, 5, 0.1);
    
    @Handler
    public final void onRender(final EventRender3D event3D) {
        this.positions.clear();
        ItemStack itemStack = MC.thePlayer.getCurrentEquippedItem();
        MovingObjectPosition m = null;
        if (itemStack != null && (itemStack.getItem() instanceof ItemSnowball || itemStack.getItem() instanceof ItemEgg || itemStack.getItem() instanceof ItemBow || itemStack.getItem() instanceof ItemEnderPearl)) {
            EntityLivingBase thrower = MC.thePlayer;
            float rotationYaw = MC.thePlayer.prevRotationYaw + (MC.thePlayer.rotationYaw -MC.thePlayer.prevRotationYaw) * MC.timer.renderPartialTicks;
            float rotationPitch = MC.thePlayer.prevRotationPitch + (MC.thePlayer.rotationPitch -MC.thePlayer.prevRotationPitch) * MC.timer.renderPartialTicks;
            double posX = thrower.lastTickPosX + (thrower.posX - thrower.lastTickPosX) * MC.timer.renderPartialTicks;
            double posY = thrower.lastTickPosY + thrower.getEyeHeight() + (thrower.posY - thrower.lastTickPosY) * MC.timer.renderPartialTicks;
            double posZ = thrower.lastTickPosZ + (thrower.posZ - thrower.lastTickPosZ) * MC.timer.renderPartialTicks;
            posX -= (MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
            posY -= 0.10000000149011612D;
            posZ -= (MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
            float multipicator = 0.4F;
            if (itemStack.getItem() instanceof ItemBow) {
                multipicator = 1;
            }
            double motionX = (-MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI) * multipicator);
            double motionZ = (MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI) * multipicator);
            double motionY = (-MathHelper.sin((rotationPitch) / 180.0F * (float) Math.PI) * multipicator);

            double x = motionX;
            double y = motionY;
            double z = motionZ;
            float inaccuracy = 0;
            float velocity = 1.5F;
            if (itemStack.getItem() instanceof ItemBow) {
                int i = MC.thePlayer.getCurrentEquippedItem().getMaxItemUseDuration() - MC.thePlayer.getItemInUseCount();
                float f = (float) i / 20.0F;
                f = (f * f + f * 2.0F) / 3.0F;

                if (f > 1.0F) {
                    f = 1.0F;
                }
                velocity = f * 2.0F * 1.5F;
            }

            Random rand = new Random();
            float ff = MathHelper.sqrt_double(x * x + y * y + z * z);
            x = x / (double) ff;
            y = y / (double) ff;
            z = z / (double) ff;
            x = x + rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
            y = y + rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
            z = z + rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
            x = x * (double) velocity;
            y = y * (double) velocity;
            z = z * (double) velocity;
            motionX = x;
            motionY = y;
            motionZ = z;
            float prevRotationYaw = (float) (MathHelper.atan2(x, z) * 180.0D / Math.PI);
            float prevRotationPitch = (float) (MathHelper.atan2(y, MathHelper.sqrt_double(x * x + z * z)) * 180.0D / Math.PI);

            boolean b = true;
            int ticksInAir = 0;
            while (b) {
                if (ticksInAir > 300) {
                    b = false;
                }
                ticksInAir++;
                Vec3 vec3 = new Vec3(posX, posY, posZ);
                Vec3 vec31 = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
                MovingObjectPosition movingobjectposition = MC.theWorld.rayTraceBlocks(vec3, vec31);
                vec3 = new Vec3(posX, posY, posZ);
                vec31 = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
                if (movingobjectposition != null) {
                    vec31 = new Vec3(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
                }
                for (Entity entity : MC.theWorld.loadedEntityList) {
                    if (entity != MC.thePlayer && entity instanceof EntityLivingBase) {
                        float f = 0.3F;
                        AxisAlignedBB localAxisAlignedBB = entity.getEntityBoundingBox().expand(f, f, f);
                        MovingObjectPosition localMovingObjectPosition = localAxisAlignedBB.calculateIntercept(vec3, vec31);
                        if (localMovingObjectPosition != null) {
                            movingobjectposition = localMovingObjectPosition;
                            break;
                        }
                    }
                }
                if (movingobjectposition != null) {
                    b = false;
                }
                m = movingobjectposition;

                posX += motionX;
                posY += motionY;
                posZ += motionZ;

                float f1 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
                rotationYaw = (float) (MathHelper.atan2(motionX, motionZ) * 180.0D / Math.PI);

                for (rotationPitch = (float) (MathHelper.atan2(motionY, f1) * 180.0D / Math.PI); rotationPitch - prevRotationPitch < -180.0F; prevRotationPitch -= 360.0F)

                    while (rotationPitch - prevRotationPitch >= 180.0F) {
                        prevRotationPitch += 360.0F;
                    }

                while (rotationYaw - prevRotationYaw < -180.0F) {
                    prevRotationYaw -= 360.0F;
                }

                while (rotationYaw - prevRotationYaw >= 180.0F) {
                    prevRotationYaw += 360.0F;
                }
                float f2 = 0.99F;
                float f3 = 0.03F;
                if (itemStack.getItem() instanceof ItemBow) {
                    f3 = 0.05F;
                }
                motionX *= f2;
                motionY *= f2;
                motionZ *= f2;
                motionY -= f3;
                this.positions.add(new Vec3(posX, posY, posZ));
            }
            if (this.positions.size() > 1) {

                if (m != null) {
                    BlockPos blockPos = m.getBlockPos();
                    IBlockState blockState = MC.theWorld.getBlockState(blockPos);
                    Block block = blockState.getBlock();
                    Material material = block.getMaterial();

                    if (material != Material.air && material != Material.water) {
                        GL11.glEnable(3042);
                        GL11.glBlendFunc(770, 771);
                        GL11.glEnable(2848);
                        GL11.glDisable(2929);
                        GL11.glDisable(3553);
                        GlStateManager.disableCull();
                        GL11.glDepthMask(false);
                        float lineWidth = 0;

                        RenderUtil.drawBlockESP(blockPos, color_final.getColor().getRed() / 255.0F, color_final.getColor().getGreen() / 255.0F, color_final.getColor().getBlue() / 255.0F, color_final.getColor().getAlpha() / 255.0F, color_final.getColor().getAlpha() / 255.0F, lineWidth);

                        GL11.glDepthMask(true);
                        GlStateManager.enableCull();
                        GL11.glEnable(3553);
                        GL11.glEnable(2929);
                        GL11.glDisable(3042);
                        GL11.glBlendFunc(770, 771);
                        GL11.glDisable(2848);
                    }
                }
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GlStateManager.disableCull();
                GL11.glDepthMask(false);
                Color color = new Color(this.color_line.getColor().getRGB());
                GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 0.7f);
                GL11.glLineWidth((float) (this.lineWidth.getAsFloat() / 2f));
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                worldrenderer.begin(3, DefaultVertexFormats.POSITION);
                for (Vec3 vec3 : this.positions) {
                    worldrenderer.pos((float) vec3.xCoord - MC.getRenderManager().renderPosX, (float) vec3.yCoord - MC.getRenderManager().renderPosY, (float) vec3.zCoord - MC.getRenderManager().renderPosZ).endVertex();
                }
                tessellator.draw();

            }
        }
    }
}
