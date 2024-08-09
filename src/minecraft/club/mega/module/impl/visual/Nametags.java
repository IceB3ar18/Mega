package club.mega.module.impl.visual;

import club.mega.Mega;
import club.mega.event.impl.EventRender3D;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import rip.hippo.lwjeb.annotation.Handler;

import javax.vecmath.Vector4f;
import java.awt.*;

@Module.ModuleInfo(name = "Nametags", description = "Raplaces Nametags with better nametags", category = Category.VISUAL)
public class Nametags extends Module {

    private final BooleanSetting ownTag = new BooleanSetting("Own Nametag", this, true);



    @Handler
    public final void onRender(final EventRender3D event) {
        for (Entity e : MC.theWorld.loadedEntityList) {
            if (e instanceof EntityPlayer) {
                if (MC.thePlayer.getDistanceToEntity(e) <= 1320) {
                        if (e != MC.thePlayer || (ownTag.get() && MC.gameSettings.thirdPersonView > 0) ) {
                            GlStateManager.pushMatrix();
                            GlStateManager.disableLighting();
                            GlStateManager.disableDepth();
                            final float DISTANCE = MC.thePlayer.getDistanceToEntity(e);
                            final String name = e.getDisplayName().getFormattedText();
                            float partialTicks = MC.timer.renderPartialTicks;
                            double x = e.lastTickPosX + (e.posX - e.lastTickPosX) * (double) partialTicks - RenderManager.renderPosX;
                            double y = e.lastTickPosY + (e.posY - e.lastTickPosY) * (double) partialTicks - RenderManager.renderPosY;
                            double z = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * (double) partialTicks - RenderManager.renderPosZ;
                            y += MC.thePlayer.getDistanceToEntity(e) * 0.02F;

                            float SCALE = Math.min(Math.max(1.2F * DISTANCE * 0.15F, 1.25F), 6.0F) * 0.02F;

                            GlStateManager.translate((float) x, (float) y + e.height + 0.5F - (0.0F), (float) z);

                            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
                            GlStateManager.rotate(-MC.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
                            GlStateManager.rotate(MC.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
                            GL11.glScalef(-SCALE, -SCALE, SCALE);

                            int WeiteDesRectanglesXD = (int) (Mega.INSTANCE.getFontManager().getFont("Arial 20").getWidth(name + ((EntityLivingBase) e).getHealth() )/ 2.0F + 2);

                            RenderUtil.rectangleBordered(-WeiteDesRectanglesXD - 1.0f, -13.0f, WeiteDesRectanglesXD + 2.0f, -2.0f, 0.5f, Integer.MIN_VALUE, new Color(20, 20, 20).getRGB());

                            float health = (float) (Math.round(((EntityLivingBase) e).getHealth() * 10.0) / 10.0);

                            Mega.INSTANCE.getFontManager().getFont("Arial 20").drawStringWithShadow(name + " " + health, -Mega.INSTANCE.getFontManager().getFont("Arial 20").getWidth(name) / 2.0f - 10, -Mega.INSTANCE.getFontManager().getFont("Arial 20").getHeight(name) / 32.0f - 13f, -1);



                            GlStateManager.resetColor();
                            GlStateManager.enableDepth();
                            GlStateManager.popMatrix();
                        }


                }
            }
        }

    }



}

