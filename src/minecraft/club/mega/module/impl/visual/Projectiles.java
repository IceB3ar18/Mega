package club.mega.module.impl.visual;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import club.mega.event.impl.EventRender3D;
import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ColorSetting;
import club.mega.module.setting.impl.NumberSetting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "Projectiles", description = "Predicts a projectiles line", category = Category.VISUAL)
public class Projectiles extends Module {
    private final ColorSetting color = new ColorSetting("Color", this, new Color(21, 121, 230), () -> !this.calculatedColor.get());
    private final BooleanSetting calculatedColor = new BooleanSetting("CalculateColor", this, true);
    private final NumberSetting lineWidth = new NumberSetting("LineWidth", this, 1, 12, 6, 1);
    private final ArrayList<ArrayList<Vec3>> points = new ArrayList();
    private final HashMap<ArrayList<Vec3>, MovingObjectPosition> hashMap = new HashMap();
    

    public void onEnable() {
        super.onEnable();
    }

    public void onDisable() {
        super.onDisable();
        this.points.clear();
    }

    @Handler
    public void onEventTickEventTick(EventTick eventTick) {
        this.points.clear();
        this.hashMap.clear();
        Iterator var2 = MC.theWorld.loadedEntityList.iterator();

        while(true) {
            Entity entity;
            do {
                do {
                    do {
                        do {
                            if (!var2.hasNext()) {
                                return;
                            }

                            entity = (Entity)var2.next();
                        } while(entity.ticksExisted < 0);
                    } while(entity.onGround);
                } while(entity.isInWater());
            } while(!(entity instanceof EntityArrow) && !(entity instanceof EntitySnowball) && !(entity instanceof EntityEgg) && !(entity instanceof EntityEnderPearl) && !(entity instanceof EntityFireball));

            boolean b = true;
            int ticksInAir = 0;
            double posX = entity.posX;
            double posY = entity.posY;
            double posZ = entity.posZ;
            double motionX = entity.motionX;
            double motionY = entity.motionY;
            double motionZ = entity.motionZ;
            float rotationYaw = entity.rotationYaw;
            float rotationPitch = entity.rotationPitch;
            float prevRotationPitch = entity.prevRotationPitch;
            float prevRotationYaw = entity.prevRotationYaw;
            ArrayList<Vec3> vec3s = new ArrayList();
            MovingObjectPosition objectPosition = null;

            while(b) {
                if (ticksInAir > 300) {
                    b = false;
                }

                ++ticksInAir;
                Vec3 vec3 = new Vec3(posX, posY, posZ);
                Vec3 vec31 = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
                MovingObjectPosition movingobjectposition = MC.theWorld.rayTraceBlocks(vec3, vec31);
                if (movingobjectposition != null) {
                    b = false;
                }

                posX += motionX;
                posY += motionY;
                posZ += motionZ;
                MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
                rotationYaw = (float)(MathHelper.atan2(motionX, motionZ) * 180.0 / Math.PI);
                if (entity instanceof EntityFireball) {
                    rotationYaw = (float)(MathHelper.atan2(motionX, motionZ) * 180.0 / Math.PI) + 90.0F;
                }

                while(rotationPitch - prevRotationPitch >= 180.0F) {
                    prevRotationPitch += 360.0F;
                }

                while(rotationYaw - prevRotationYaw < -180.0F) {
                    prevRotationYaw -= 360.0F;
                }

                while(rotationYaw - prevRotationYaw >= 180.0F) {
                    prevRotationYaw += 360.0F;
                }

                float f2 = 0.99F;
                if (entity instanceof EntityFireball) {
                    f2 = 0.95F;
                }

                float f3 = 0.03F;
                if (entity instanceof EntityArrow) {
                    f3 = 0.05F;
                } else if (entity instanceof EntityFireball) {
                    f3 = 0.0F;
                }

                if (entity instanceof EntityFireball) {
                    EntityFireball entityFireball = (EntityFireball)entity;
                    motionX += entityFireball.accelerationX;
                    motionY += entityFireball.accelerationY;
                    motionZ += entityFireball.accelerationZ;
                }

                motionX *= (double)f2;
                motionY *= (double)f2;
                motionZ *= (double)f2;
                motionY -= (double)f3;
                vec3s.add(new Vec3(posX, posY, posZ));
            }

            this.points.add(vec3s);
            this.hashMap.put(vec3s, objectPosition);
        }
    }

    @Handler
    public void onEventRender3D(EventRender3D eventRender3D) {
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glDisable(3553);
        GlStateManager.disableCull();
        GL11.glDepthMask(false);
        GL11.glLineWidth(this.lineWidth.getAsFloat() / 2.0F);
        Iterator var2 = this.points.iterator();

        while(true) {
            ArrayList vec3s;
            do {
                if (!var2.hasNext()) {
                    GL11.glLineWidth(1.0F);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    GL11.glDepthMask(true);
                    GlStateManager.enableCull();
                    GL11.glEnable(3553);
                    GL11.glEnable(2929);
                    GL11.glDisable(3042);
                    GL11.glBlendFunc(770, 771);
                    GL11.glDisable(2848);
                    return;
                }

                vec3s = (ArrayList)var2.next();
            } while(vec3s.size() <= 1);

            if (this.calculatedColor.get()) {
                double dist = Math.min(Math.max(MC.thePlayer.getDistance(((Vec3)vec3s.get(1)).xCoord, ((Vec3)vec3s.get(1)).yCoord, ((Vec3)vec3s.get(1)).zCoord), 6.0), 36.0) - 6.0;
                Color color = new Color(this.color.getColor().getRed(), this.color.getColor().getGreen(), this.color.getColor().getBlue(), this.color.getColor().getAlpha());
                float[] hsbColor = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), (float[])null);
                int colorRGB = Color.HSBtoRGB((float)(0.01141552533954382 * dist), hsbColor[1], hsbColor[2]);
                float f = (float)(colorRGB >> 24 & 255) / 255.0F;
                float red = (float)(colorRGB >> 16 & 255) / 255.0F;
                float green = (float)(colorRGB >> 8 & 255) / 255.0F;
                float blue = (float)(colorRGB & 255) / 255.0F;
                GL11.glColor4f(red, green, blue, 0.85F);
            } else {
                GL11.glColor4f((float)this.color.getColor().getRed() / 255.0F, (float)this.color.getColor().getGreen() / 255.0F, (float)this.color.getColor().getBlue() / 255.0F, 0.85F);
            }

            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            worldrenderer.begin(3, DefaultVertexFormats.POSITION);
            Iterator var14 = vec3s.iterator();

            while(var14.hasNext()) {
                Vec3 vec3 = (Vec3)var14.next();
                worldrenderer.pos((double)((float)vec3.xCoord) - MC.getRenderManager().getRenderPosX(), (double)((float)vec3.yCoord) - MC.getRenderManager().getRenderPosY(), (double)((float)vec3.zCoord) - MC.getRenderManager().getRenderPosZ()).endVertex();
            }

            tessellator.draw();
        }
    }
}
