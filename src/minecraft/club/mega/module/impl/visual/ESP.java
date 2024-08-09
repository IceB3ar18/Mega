package club.mega.module.impl.visual;

import club.mega.Mega;
import club.mega.event.impl.EventRender2D;
import club.mega.event.impl.EventRender3D;
import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.*;
import club.mega.util.*;
import club.mega.util.animation.impl.DecelerateAnimation;
import club.mega.util.newshader.ShaderUtil;
import club.mega.util.shader.Animation;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import rip.hippo.lwjeb.annotation.Handler;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL20.glUniform1;

@Module.ModuleInfo(name = "ESP", description = "See players through walls", category = Category.VISUAL)
public class ESP extends Module {
    /////////////////////GLOW
    private final BooleanSetting shader = new BooleanSetting("Shader", this, false);
    public final NumberSetting radius = new NumberSetting("Shader Radius", this, 2, 30, 4, 2, shader::get);
    public final NumberSetting exposure = new NumberSetting("Exposure", this, .5, 3.5, 2.2, 0.1, shader::get);
    public final BooleanSetting seperate = new BooleanSetting("Seperate Texture", this, false, shader::get);
    public final ListSetting shaderColorMode = new ListSetting("Shader Mode", this, new String[]{"Normal", "Fade"}, shader::get);
    public final NumberSetting shaderColorSpeed = new NumberSetting("Fade Speed", this, 1, 50, 15, 1, () -> shaderColorMode.is("Fade") && shader.get());
    public final ColorSetting shaderColorNormal = new ColorSetting("Shader Color", this, Color.white, () -> shaderColorMode.is("Normal") && shader.get());
    public final ColorSetting firstColor = new ColorSetting("Shader Start", this, Color.white, () -> shaderColorMode.is("Fade") && shader.get());
    public final ColorSetting secondColor = new ColorSetting("Shader End", this, Color.white, () -> shaderColorMode.is("Fade") && shader.get());


    /////////////////////BOXY
    private final BooleanSetting boxy = new BooleanSetting("Boxy", this, false);
    public final ListSetting boxyColorMode = new ListSetting("Boxy Mode", this, new String[]{"Normal", "Fade"}, boxy::get);
    public final NumberSetting boxyColorSpeed = new NumberSetting("Fade Speed", this, 1, 50, 15, 1, () -> boxyColorMode.is("Fade") && boxy.get());
    public final ColorSetting boxyColorNormal = new ColorSetting("Boxy Color", this, new Color(255, 255, 255, 79), () -> boxyColorMode.is("Normal") && boxy.get());
    public final ColorSetting boxyColor1 = new ColorSetting("Boxy Start", this, new Color(255, 255, 255, 73), () -> boxyColorMode.is("Fade") && boxy.get());
    public final ColorSetting boxyColor2 = new ColorSetting("Boxy End", this, new Color(255, 255, 255, 73), () -> boxyColorMode.is("Fade") && boxy.get());
    public final NumberSetting boxyLineWidth = new NumberSetting("LineWidth", this, 0.0, 15.0, 3.0, 1, boxy::get);

    /////////////////////FakeCorner
    private final BooleanSetting fakeCorner = new BooleanSetting("Fake Corner", this, false);
    public final ListSetting cornerColorMode = new ListSetting("Corner Mode", this, new String[]{"Normal", "Fade"}, fakeCorner::get);
    public final NumberSetting cornerColorSpeed = new NumberSetting("Fade Speed", this, 1, 50, 15, 1, () -> cornerColorMode.is("Fade") && fakeCorner.get());
    public final ColorSetting cornerColorNormal = new ColorSetting("Corner Color", this, Color.white, () -> cornerColorMode.is("Normal") && fakeCorner.get());
    public final ColorSetting cornerColor1 = new ColorSetting("Corner Start", this, Color.white, () -> cornerColorMode.is("Fade") && fakeCorner.get());
    public final ColorSetting cornerColor2 = new ColorSetting("Corner End", this, Color.white, () -> cornerColorMode.is("Fade") && fakeCorner.get());



    public final BooleanSetting player = new BooleanSetting("Player", this, true);
    public final BooleanSetting mob = new BooleanSetting("Mob", this, false);
    public final BooleanSetting animal = new BooleanSetting("Animal", this, false);
    public final BooleanSetting villager = new BooleanSetting("Villager", this, false);
    public final BooleanSetting armorStand = new BooleanSetting("ArmorStand", this, false);
    public final BooleanSetting invisible = new BooleanSetting("Invisible", this, false);
    public static boolean renderNameTags = true;
    private final ShaderUtil outlineShader = new ShaderUtil("Tenacity/Shaders/outline.frag");
    private final ShaderUtil glowShader = new ShaderUtil("Tenacity/Shaders/glow.frag");

    public Framebuffer framebuffer;
    public Framebuffer outlineFrameBuffer;
    public Framebuffer glowFrameBuffer;
    private final Frustum frustum = new Frustum();

    private final List<Entity> entities = new ArrayList<>();

    public static Animation fadeIn;

    @Handler
    public final void earlyTick(final EventTick eventTick) {
        if(shader.get()) {
            this.setTag("Shader");
        }
    }

    private int index = 0;


    @Handler
    public final void onRender1(final EventRender3D eventRender3D) {
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDisable(3553);
        GlStateManager.disableCull();
        GL11.glDepthMask(false);
        if(boxy.get()) {
            ColorUtil colorUtil = new ColorUtil();
            Color boxyColor = colorUtil.interpolateColorsBackAndForth(boxyColorNormal, boxyColor1, boxyColor2, boxyColorMode, boxyColorSpeed);



            float lineWidth = this.boxyLineWidth.getAsFloat() / 2.0F;

            float red = (float)boxyColor.getRed() / 225.0F;
            float green = (float)boxyColor.getGreen() / 225.0F;
            float blue = (float)boxyColor.getBlue() / 225.0F;
            float alpha = (float)boxyColor.getAlpha() / 225.0F;
            collectEntities();
            for (final Entity e : MC.theWorld.loadedEntityList) {
                if (entities.contains(e)) {
                    if (MC.thePlayer.getDistanceToEntity(e) > 1.0F) {
                        double d0 = (1.0F - MC.thePlayer.getDistanceToEntity(e) / 20.0F);
                        if (d0 < 0.3) {
                            d0 = 0.3;
                        }
                        lineWidth = (float)((double)lineWidth * d0);
                    }
                    
                    RenderUtil.drawEntityESP(e, red, green, blue, alpha, 1.0F, lineWidth);
                    index++;
                }

            }


        }

        if(fakeCorner.get()) {
            ColorUtil colorUtil = new ColorUtil();
            Color cornerColor = colorUtil.interpolateColorsBackAndForth(cornerColorNormal, cornerColor1, cornerColor2, cornerColorMode, cornerColorSpeed);


            float red = (float)cornerColor.getRed() / 225.0F;
            float green = (float)cornerColor.getGreen() / 225.0F;
            float blue = (float)cornerColor.getBlue() / 225.0F;

            collectEntities();
            for (final Entity e : MC.theWorld.loadedEntityList) {
                if (entities.contains(e)) {

                    RenderUtil.drawCornerESP(e, red, green, blue);
                    index++;
                }

            }



        }
        GL11.glDepthMask(true);
        GlStateManager.enableCull();
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDisable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(2848);
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
    @Handler
    public void onRender(EventRender3D eventRender3D) {
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


    public void setupGlowUniforms(float dir1, float dir2) {
        ColorUtil colorUtil = new ColorUtil();
        Color color = colorUtil.interpolateColorsBackAndForth(shaderColorNormal, firstColor, secondColor, shaderColorMode, shaderColorSpeed);

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


    public void setupOutlineUniforms(float dir1, float dir2) {
        ColorUtil colorUtil = new ColorUtil();
        Color color = colorUtil.interpolateColorsBackAndForth(shaderColorNormal, firstColor, secondColor, shaderColorMode, shaderColorSpeed);
        outlineShader.setUniformi("texture", 0);
        outlineShader.setUniformf("radius", radius.getAsFloat() / 1.5f);
        outlineShader.setUniformf("texelSize", 1.0f / MC.displayWidth, 1.0f / MC.displayHeight);
        outlineShader.setUniformf("direction", dir1, dir2);
        outlineShader.setUniformf("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
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


    public void collectEntities() {
        entities.clear();
        for (Entity entity : MC.theWorld.getLoadedEntityList()) {
            if (!isInView(entity)) continue;
            if ((entity != MC.thePlayer || MC.gameSettings.thirdPersonView > 0) && !entity.isDead) {

                if (invisible.get() && entity.isInvisible() && entity instanceof EntityPlayer) {
                    entities.add(entity);
                }

                if (entity == MC.thePlayer && MC.gameSettings.thirdPersonView == 0) continue;
                if (entity instanceof EntityAnimal && animal.get()) {
                    entities.add(entity);
                }


                if (entity instanceof EntityPlayer && player.get()) {
                    entities.add(entity);
                }

                if (entity instanceof EntityMob && mob.get()) {
                    entities.add(entity);
                }
                if (entity instanceof EntityArmorStand && armorStand.get()) {
                    entities.add(entity);
                }
            }
        }
    }

    public static boolean isInView(Entity ent) {
        Frustum frustum = new Frustum();
        frustum.setPosition(MC.getRenderViewEntity().posX, MC.getRenderViewEntity().posY, MC.getRenderViewEntity().posZ);
        return frustum.isBoundingBoxInFrustum(ent.getEntityBoundingBox()) || ent.ignoreFrustumCheck;
    }

    public static ESP getInstance() {
        return Mega.INSTANCE.getModuleManager().getModule(ESP.class);
    }

}
