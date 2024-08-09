package club.mega.module.impl.visual;

import club.mega.Mega;
import club.mega.event.impl.EventRender2D;
import club.mega.event.impl.EventRender3D;
import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ColorSetting;
import club.mega.module.setting.impl.ListSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.util.ColorUtil;
import club.mega.util.MathUtil;
import club.mega.util.RenderUtil;
import club.mega.util.animation.impl.DecelerateAnimation;
import club.mega.util.newshader.ShaderUtil;
import club.mega.util.shader.Animation;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import rip.hippo.lwjeb.annotation.Handler;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL20.glUniform1;

@Module.ModuleInfo(name = "ChestESP", description = "See players through walls", category = Category.VISUAL)
public class ChestESP extends Module {
    private final BooleanSetting shader = new BooleanSetting("Shader", this, false);

    public final NumberSetting radius = new NumberSetting("Glow Radius", this, 2, 30, 4, 2, shader::get);
    public final NumberSetting exposure = new NumberSetting("Exposure", this, .5, 3.5, 2.2, 0.1, shader::get);
    public final BooleanSetting seperate = new BooleanSetting("Seperate Texture", this, false, shader::get);
    public final ListSetting shaderColorMode = new ListSetting("Glow Mode", this, new String[]{"Normal", "Fade"}, shader::get);
    public final NumberSetting shaderColorSpeed = new NumberSetting("Fade Speed", this, 1, 50, 15, 1, () -> shaderColorMode.is("Fade") && shader.get());
    public final ColorSetting shaderColorNormal = new ColorSetting("Glow Color", this, Color.white, () -> shaderColorMode.is("Normal") && shader.get());
    public final ColorSetting shaderColor1 = new ColorSetting("Glow Start", this, Color.white, () -> shaderColorMode.is("Fade") && shader.get());
    public final ColorSetting shaderColor2 = new ColorSetting("Glow Mid", this, Color.white, () -> shaderColorMode.is("Fade") && shader.get());
    public final BooleanSetting boxy = new BooleanSetting("Boxy", this, false);
    public final ListSetting mode = new ListSetting("Color Mode", this, new String[]{"Normal", "Fade"});

    public final ColorSetting colornormal = new ColorSetting("Color", this, new Color(255, 255, 255, 87), () -> {
        return this.mode.is("Normal") && boxy.get();
    });
    public final ColorSetting color1 = new ColorSetting("Start Color", this, new Color(255, 255, 255, 87), () -> {
        return this.mode.is("Fade") && boxy.get();
    });
    public final ColorSetting color2 = new ColorSetting("End Color", this, new Color(255, 255, 255, 87), () -> {
        return this.mode.is("Fade") && boxy.get();
    });
    public final NumberSetting lineWidth = new NumberSetting("Outline", this, 0, 15, 6, 1, boxy::get);


    @Handler
    public final void onTick(final EventTick event) {
        if(boxy.get()) {
            this.setTag("Boxy");
        }
    }
    private final ShaderUtil outlineShader = new ShaderUtil("Tenacity/Shaders/outline.frag");
    private final ShaderUtil glowShader = new ShaderUtil("Tenacity/Shaders/glow.frag");

    public Framebuffer framebuffer;
    public Framebuffer outlineFrameBuffer;
    public Framebuffer glowFrameBuffer;
    private final Frustum frustum = new Frustum();

    private final List<Entity> entities = new ArrayList<>();

    public static Animation fadeIn;
    private int chests = 0;



    private int index = 0;
    @Handler
    public void onRender(EventRender3D eventRender3D) {
        if(shader.get()) {
            createFrameBuffers();
            framebuffer.framebufferClear();
            framebuffer.bindFramebuffer(true);
            chests = 0;
            for (Object v : MC.theWorld.loadedTileEntityList) {
                if (v instanceof TileEntityChest && !(v instanceof TileEntityEnderChest)) {
                    chests += 1;
                    TileEntityChest chest = (TileEntityChest) v;
                    GlStateManager.pushMatrix();
                    MC.entityRenderer.disableLightmap();
                    TileEntityRendererDispatcher.instance.renderTileEntityAt(chest, (double)chest.getPos().getX() - RenderManager.getRenderPosX(), (double)chest.getPos().getY() - RenderManager.renderPosY, (double)chest.getPos().getZ() - RenderManager.getRenderPosZ(), eventRender3D.getPartialTicks());
                    RendererLivingEntity.renderNametags = true;
                    GlStateManager.resetColor();
                    GlStateManager.popMatrix();

                }
            }
            framebuffer.unbindFramebuffer();
            MC.getFramebuffer().bindFramebuffer(true);
            GlStateManager.disableLighting();

        }

        if(boxy.get()) {
            renderBox();
        }
    }
    @Handler
    public void render2D(EventRender2D event) {
        if(shader.get()) {
            ScaledResolution sr = new ScaledResolution(MC);
            if (framebuffer != null && outlineFrameBuffer != null && chests > 0) {
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


    public void setupOutlineUniforms(float dir1, float dir2) {
        ColorUtil colorUtil = new ColorUtil();
        Color color = colorUtil.interpolateColorsBackAndForth(shaderColorNormal, shaderColor1, shaderColor2, shaderColorMode, shaderColorSpeed);
        outlineShader.setUniformi("texture", 0);
        outlineShader.setUniformf("radius", radius.getAsFloat() / 1.5f);
        outlineShader.setUniformf("texelSize", 1.0f / MC.displayWidth, 1.0f / MC.displayHeight);
        outlineShader.setUniformf("direction", dir1, dir2);
        outlineShader.setUniformf("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
    }

    private void renderBox() {
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDisable(3553);
        GlStateManager.disableCull();
        GL11.glDepthMask(false);
        Color color = mode.is("Normal") ? colornormal.getColor() : ColorUtil.getGradientOffset(color1.getColor(), color2.getColor(), index * 2);
        for (Object v : MC.theWorld.loadedTileEntityList) {
            if (v instanceof TileEntityChest && !(v instanceof TileEntityEnderChest)) {

                float lineWidth = (float) (this.lineWidth.getAsDouble() / 2.0);
                if (MC.thePlayer.getDistance(((TileEntityChest) v).getPos().getX(), ((TileEntityChest) v).getPos().getY(), ((TileEntityChest) v).getPos().getZ()) > 1.0) {
                    double d0 = 1.0 - MC.thePlayer.getDistance(((TileEntityChest) v).getPos().getX(), ((TileEntityChest) v).getPos().getY(), ((TileEntityChest) v).getPos().getZ()) / 20.0;
                    if (d0 < 0.3) {
                        d0 = 0.3;
                    }

                    lineWidth = (float) ((double) lineWidth * d0);
                }
                RenderUtil.drawBlockESP(((TileEntityChest) v).getPos(), color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F, 1.0F, lineWidth);
            }
        }
        index++;
        GL11.glDepthMask(true);
        GlStateManager.enableCull();
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDisable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(2848);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
    public void createFrameBuffers() {
        framebuffer = RenderUtil.createFrameBuffer(framebuffer);
        outlineFrameBuffer = RenderUtil.createFrameBuffer(outlineFrameBuffer);
        glowFrameBuffer = RenderUtil.createFrameBuffer(glowFrameBuffer);
    }

    public static ChestESP getInstance() {
        return Mega.INSTANCE.getModuleManager().getModule(ChestESP.class);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        fadeIn = new DecelerateAnimation(250, 1);
    }
}
