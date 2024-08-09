package club.mega.module.impl.visual;

import club.mega.event.impl.EventRender2D;
import club.mega.event.impl.EventRender3D;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ColorSetting;
import club.mega.module.setting.impl.ListSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.util.*;
import club.mega.util.animation.impl.DecelerateAnimation;
import club.mega.util.newshader.ShaderUtil;
import club.mega.util.shader.Animation;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import rip.hippo.lwjeb.annotation.Handler;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.lwjgl.opengl.GL20.glUniform1;

@Module.ModuleInfo(name = "ItemESP", description = "Makes Items visable through walls", category = Category.VISUAL)
public class ItemESP extends Module {
    private final BooleanSetting shader = new BooleanSetting("Shader", this, false);

    public final NumberSetting radius = new NumberSetting("Shader Radius", this, 2, 30, 4, 2, shader::get);
    public final NumberSetting exposure = new NumberSetting("Exposure", this, .5, 3.5, 2.2, 0.1, shader::get);
    public final BooleanSetting seperate = new BooleanSetting("Seperate Texture", this, false, shader::get);
    public final ListSetting shaderColorMode = new ListSetting("Shader Mode", this, new String[]{"Normal", "Fade"}, shader::get);
    public final NumberSetting shaderColorSpeed = new NumberSetting("Fade Speed", this, 1, 50, 15, 1, () -> shaderColorMode.is("Fade") && shader.get());
    public final ColorSetting shaderColorNormal = new ColorSetting("Shader Color", this, Color.white, () -> shaderColorMode.is("Normal") && shader.get());
    public final ColorSetting shaderColor1 = new ColorSetting("Shader Start", this, Color.white, () -> shaderColorMode.is("Fade") && shader.get());
    public final ColorSetting shaderColor2 = new ColorSetting("Shader End", this, Color.white, () -> shaderColorMode.is("Fade") && shader.get());

    private final BooleanSetting boxy = new BooleanSetting("Boxy", this, false);
    public final ColorSetting boxyColor = new ColorSetting("Boxy Color", this, new Color(255, 255, 255, 153), boxy::get);
    private final BooleanSetting boxyOutline = new BooleanSetting("Boxy Outline", this, false, boxy::get);
    public static Animation fadeIn;

    private final ShaderUtil outlineShader = new ShaderUtil("Tenacity/Shaders/outline.frag");
    private final ShaderUtil glowShader = new ShaderUtil("Tenacity/Shaders/glow.frag");
    public static boolean renderNameTags = true;

    public Framebuffer framebuffer;
    public Framebuffer outlineFrameBuffer;
    public Framebuffer glowFrameBuffer;
    private final List<Entity> entities = new ArrayList<>();

    @Handler
    public void onRender(EventRender3D eventRender3D) {
        if (boxy.get()) {
            RenderUtil renderUtil = new RenderUtil();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            Iterator var9 = MC.theWorld.loadedEntityList.iterator();

            while (var9.hasNext()) {
                Entity e = (Entity) var9.next();
                if (e instanceof EntityItem) {
                    EntityItem item = (EntityItem) e;
                    float pTicks = eventRender3D.getPartialTicks();
                    double RPX = RenderManager.renderPosX;
                    double RPY = RenderManager.renderPosY;
                    double RPZ = RenderManager.renderPosZ;
                    double x = item.lastTickPosX + (item.posX - item.lastTickPosX) * (double) pTicks - RPX;
                    double y = item.lastTickPosY + (item.posY - item.lastTickPosY) * (double) pTicks - RPY;
                    double z = item.lastTickPosZ + (item.posZ - item.lastTickPosZ) * (double) pTicks - RPZ;
                    Color c = boxyColor.getColor();

                    if (boxyOutline.get()) {
                        renderUtil.renderBoxWithOutline(x, y - 0.699999988079071, z, 0.5F, 0.5F, c);
                    } else {
                        renderUtil.renderBox(x, y - 0.699999988079071, z, 0.5F, 0.5F, c);
                    }

                }
            }

            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
        }
        if(shader.get()) {
            createFrameBuffers();
            collectEntities();
            framebuffer.framebufferClear();
            framebuffer.bindFramebuffer(true);
            renderEntities(eventRender3D.getPartialTicks());
            framebuffer.unbindFramebuffer();
            MC.getFramebuffer().bindFramebuffer(true);
            GlStateManager.disableLighting();

        }
    }



    @Handler
    public void render2D(EventRender2D event) {
        if(shader.get()) {
            ScaledResolution sr = new ScaledResolution(MC);
            if (framebuffer != null && outlineFrameBuffer != null && entities.size() > 0) {
                GlStateManager.enableAlpha();
                GlStateManager.alphaFunc(516, 0.0f);
                GlStateManager.enableBlend();
                OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

                outlineFrameBuffer.framebufferClear();
                outlineFrameBuffer.bindFramebuffer(true);
                outlineShader.init();
                setupOutlineUniforms(0, 1);
                RenderUtil.bindTexture(framebuffer.framebufferTexture);
                ShaderUtil.drawQuads();
                outlineShader.init();
                setupOutlineUniforms(1, 0);
                RenderUtil.bindTexture(framebuffer.framebufferTexture);
                ShaderUtil.drawQuads();
                outlineShader.unload();
                outlineFrameBuffer.unbindFramebuffer();

                GlStateManager.color(1, 1, 1, 1);
                glowFrameBuffer.framebufferClear();
                glowFrameBuffer.bindFramebuffer(true);
                glowShader.init();
                setupGlowUniforms(1, 0);
                RenderUtil.bindTexture(outlineFrameBuffer.framebufferTexture);
                ShaderUtil.drawQuads();
                glowShader.unload();
                glowFrameBuffer.unbindFramebuffer();

                MC.getFramebuffer().bindFramebuffer(true);
                glowShader.init();
                setupGlowUniforms(0, 1);
                if (seperate.get()) {
                    GL13.glActiveTexture(GL13.GL_TEXTURE16);
                    RenderUtil.bindTexture(framebuffer.framebufferTexture);
                }
                GL13.glActiveTexture(GL13.GL_TEXTURE0);
                RenderUtil.bindTexture(glowFrameBuffer.framebufferTexture);
                ShaderUtil.drawQuads();
                glowShader.unload();

            }

        }

    }

    @Override
    public void onEnable() {
        super.onEnable();
        fadeIn = new DecelerateAnimation(250, 1);
    }

    public void createFrameBuffers() {
        framebuffer = RenderUtil.createFrameBuffer(framebuffer);
        outlineFrameBuffer = RenderUtil.createFrameBuffer(outlineFrameBuffer);
        glowFrameBuffer = RenderUtil.createFrameBuffer(glowFrameBuffer);
    }

    public void setupGlowUniforms(float dir1, float dir2) {
        ColorUtil colorUtil = new ColorUtil();
        Color color = colorUtil.interpolateColorsBackAndForth(shaderColorNormal, shaderColor1, shaderColor2, shaderColorMode, shaderColorSpeed);

        glowShader.setUniformi("texture", 0);
        if (seperate.get()) {
            glowShader.setUniformi("textureToCheck", 16);
        }
        glowShader.setUniformf("radius", radius.getAsFloat());
        glowShader.setUniformf("texelSize", 1.0f / MC.displayWidth, 1.0f / MC.displayHeight);
        glowShader.setUniformf("direction", dir1, dir2);
        glowShader.setUniformf("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
        glowShader.setUniformf("exposure", (float) (exposure.getAsFloat() * fadeIn.getOutput()));
        glowShader.setUniformi("avoidTexture", seperate.get() ? 1 : 0);

        final FloatBuffer buffer = BufferUtils.createFloatBuffer(256);
        for (int i = 1; i <= radius.getAsFloat(); i++) {
            buffer.put(MathUtil.calculateGaussianValue(i, radius.getAsFloat() / 2));
        }
        buffer.rewind();

        glUniform1(glowShader.getUniform("weights"), buffer);
    }


    public void collectEntities() {
        entities.clear();
        for (Entity entity : MC.theWorld.getLoadedEntityList()) {
            if (!isInView(entity)) continue;
            if ((entity != MC.thePlayer || MC.gameSettings.thirdPersonView > 0) && !entity.isDead) {
                if (entity instanceof EntityItem && RenderUtil.isInViewFrustrum(entity)) {
                    entities.add(entity);
                }
            }
        }
    }
    public void renderEntities(float ticks) {
        RendererLivingEntity.renderNametags = false;
        MC.gameSettings.entityShadows = false;
        entities.forEach(entity -> {
            renderNameTags = false;
            MC.getRenderManager().renderEntityStatic(entity, ticks, true);
            renderNameTags = true;
        });
        RendererLivingEntity.renderNametags = true;
        MC.gameSettings.entityShadows = true;
    }
    public void setupOutlineUniforms(float dir1, float dir2) {
        ColorUtil colorUtil = new ColorUtil();
        Color color = colorUtil.interpolateColorsBackAndForth(shaderColorNormal, shaderColor1, shaderColor2, shaderColorMode, shaderColorSpeed);
        outlineShader.setUniformi("texture", 0);
        outlineShader.setUniformf("radius", radius.getAsFloat() / 1.5f);
        outlineShader.setUniformf("texelSize", 1.0f / MC.displayWidth, 1.0f / MC.displayHeight);
        outlineShader.setUniformf("direction", dir1, dir2);
        outlineShader.setUniformf("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
    }
    public static boolean isInView(Entity ent) {
        Frustum frustum = new Frustum();
        frustum.setPosition(MC.getRenderViewEntity().posX, MC.getRenderViewEntity().posY, MC.getRenderViewEntity().posZ);
        return frustum.isBoundingBoxInFrustum(ent.getEntityBoundingBox()) || ent.ignoreFrustumCheck;
    }
}
