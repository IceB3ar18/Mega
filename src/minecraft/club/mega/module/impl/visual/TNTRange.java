package club.mega.module.impl.visual;


import club.mega.event.impl.EventRender3D;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.ColorSetting;
import rip.hippo.lwjeb.annotation.Handler;

import java.awt.*;

@Module.ModuleInfo(name = "TNTRange", description = "See players through walls", category = Category.VISUAL)
public class TNTRange extends Module {


    public final ColorSetting color = new ColorSetting("Color", this, Color.red);

    @Handler
    public final void onRender(final EventRender3D eventRender3D) {
        /*final FrameBufferShader shader = GlowShader.GLOW_SHADER;
        for (Entity all : MC.theWorld.loadedEntityList) {
            if (all instanceof EntityTNTPrimed) {
                EntityTNTPrimed e = (EntityTNTPrimed) all;
                shader.renderShader(eventRender3D.getPartialTicks(), color.getColor().getRGB(), 3, 320, 32);
                double xPos = (e.lastTickPosX + (e.posX - e.lastTickPosX) * MC.timer.renderPartialTicks) - RenderManager.renderPosX;
                double yPos = (e.lastTickPosY + (e.posY - e.lastTickPosY) * MC.timer.renderPartialTicks) - RenderManager.renderPosY;
                double zPos = (e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * MC.timer.renderPartialTicks) - RenderManager.renderPosZ;
                GlStateManager.disableDepth();
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glDepthMask(true);
                GL11.glLineWidth(1.0F);
                GL11.glTranslated(xPos, yPos, zPos);
                GL11.glTranslated(0.0D, 0.0D, 0.075);
                GL11.glColor4f(1.38F, 0.85F, 1.38F, 1.0F);
                GL11.glTranslated(0.0D, 0.0D, -0.125D);
                GL11.glTranslated(-0.09D, 0.0D, 0.0D);
                GL11.glColor4f(1.35F, 0.0F, 0.0F, 1.0F);
                GL11.glTranslated(-0.07D, 0.0D, 0.5899D);
                org.lwjgl.util.glu.Sphere sphere = new org.lwjgl.util.glu.Sphere();
                sphere.setDrawStyle(GLU.GLU_FILL);
                sphere.setNormals(GLU.GLU_SMOOTH);
                org.lwjgl.util.glu.Sphere tip = new Sphere();
                tip.setDrawStyle(GLU.GLU_LINE);
                final int TNT_RADIUS = 8;
                tip.draw(TNT_RADIUS, 32, 32);
                GL11.glColor4f(1F, 0.1F, 0.1F, 0.2F);
                sphere.setOrientation(GLU.GLU_LINE);
                //    sphere.draw(TNT_RADIUS, 32,32);
                GL11.glDepthMask(true);
                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                shader.clearShader();
            }

        }*/
    }

}
