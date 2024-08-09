package club.mega.module.impl.hud;

import club.mega.Mega;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ColorSetting;
import club.mega.module.setting.impl.ListSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.util.ColorUtil;

import club.mega.util.MathUtil;
import club.mega.util.RenderUtil;
import club.mega.util.newshader.ShaderUtil;
import club.mega.util.shader.Animation;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.awt.*;
import java.nio.FloatBuffer;

import static net.minecraft.client.renderer.OpenGlHelper.glUniform1;

@Module.ModuleInfo(name = "Shadow", description = "Renders a Shadow", category = Category.HUD)
public class Shadow extends Module {
    public final NumberSetting shadowRadius = new NumberSetting("Shadow Radius", this, 2, 30, 4, 2);
    public final NumberSetting exposure = new NumberSetting("Exposure", this, .5, 3.5, 2.2, 0.1);
    public final ListSetting shadowColorMode = new ListSetting("Shadow Mode", this, new String[]{"Normal", "Fade"});
    public final NumberSetting shadowColorSpeed = new NumberSetting("Fade Speed", this, 1, 50, 15, 1, () -> shadowColorMode.is("Fade"));
    public final ColorSetting shadowColorNormal = new ColorSetting("Shadow Color", this, new Color(0,0,0), () -> shadowColorMode.is("Normal"));
    public final BooleanSetting shadowThirdColor = new BooleanSetting("Third Color", this, true, () -> shadowColorMode.is("Fade"));
    public final ColorSetting shadowColor1 = new ColorSetting("shadow Start", this, new Color(0,0,0), () -> shadowColorMode.is("Fade"));
    public final ColorSetting shadowColor2 = new ColorSetting("Shadow Mid", this, new Color(0,0,0), () -> shadowColorMode.is("Fade") && shadowThirdColor.get());
    public final ColorSetting shadowColor3 = new ColorSetting("Shadow End", this, new Color(0,0,0), () -> shadowColorMode.is("Fade"));

    public final ShaderUtil outlineShader = new ShaderUtil("Tenacity/Shaders/outline.frag");
    public final ShaderUtil glowShader = new ShaderUtil("Tenacity/Shaders/glow.frag");



    public void render2d(Framebuffer framebuffer, Framebuffer outlineFrameBuffer, Framebuffer glowFrameBuffer, Animation fadeIn) {
        if (Shadow.getInstance().isToggled()) {
            ScaledResolution sr = new ScaledResolution(MC);
            if (framebuffer != null && outlineFrameBuffer != null) {
                GlStateManager.enableAlpha();
                GlStateManager.alphaFunc(516, 0.0f);
                GlStateManager.enableBlend();
                OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

                outlineFrameBuffer.framebufferClear();
                outlineFrameBuffer.bindFramebuffer(true);
                this.outlineShader.init();
                this.setupOutlineUniforms(0, 1);
                RenderUtil.bindTexture(framebuffer.framebufferTexture);
                ShaderUtil.drawQuads();
                this.outlineShader.init();
                this.setupOutlineUniforms(1, 0);
                RenderUtil.bindTexture(framebuffer.framebufferTexture);
                ShaderUtil.drawQuads();
                this.outlineShader.unload();
                outlineFrameBuffer.unbindFramebuffer();

                GlStateManager.color(1, 1, 1, 1);
                glowFrameBuffer.framebufferClear();
                glowFrameBuffer.bindFramebuffer(true);
                this.glowShader.init();
                this.setupGlowUniforms(1, 0, fadeIn);
                RenderUtil.bindTexture(outlineFrameBuffer.framebufferTexture);
                ShaderUtil.drawQuads();
                this.glowShader.unload();
                glowFrameBuffer.unbindFramebuffer();

                MC.getFramebuffer().bindFramebuffer(true);
                glowShader.init();
                setupGlowUniforms(0, 1, fadeIn);
                if (true) {
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
    public void setupGlowUniforms(float dir1, float dir2, Animation fadeIn) {
        ColorUtil colorUtil = new ColorUtil();

        Color color = colorUtil.interpolateColorsBackAndForth(Shadow.getInstance().shadowColorNormal, Shadow.getInstance().shadowColor1, Shadow.getInstance().shadowColor2, Shadow.getInstance().shadowColor3, Shadow.getInstance().shadowThirdColor, Shadow.getInstance().shadowColorMode, Shadow.getInstance().shadowColorSpeed);

        glowShader.setUniformi("texture", 0);
        if (true) {
            glowShader.setUniformi("textureToCheck", 16);
        }
        glowShader.setUniformf("radius", Shadow.getInstance().shadowRadius.getAsFloat());
        glowShader.setUniformf("texelSize", 1.0f / MC.displayWidth, 1.0f / MC.displayHeight);
        glowShader.setUniformf("direction", dir1, dir2);
        glowShader.setUniformf("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
        glowShader.setUniformf("exposure", (float) (Shadow.getInstance().exposure.getAsFloat() * fadeIn.getOutput()));
        glowShader.setUniformi("avoidTexture", true ? 1 : 0);

        final FloatBuffer buffer = BufferUtils.createFloatBuffer(256);
        for (int i = 1; i <= Shadow.getInstance().shadowRadius.getAsFloat(); i++) {
            buffer.put(MathUtil.calculateGaussianValue(i, Shadow.getInstance().shadowRadius.getAsFloat() / 2));
        }
        buffer.rewind();

        glUniform1(glowShader.getUniform("weights"), buffer);
    }


    public void setupOutlineUniforms(float dir1, float dir2) {
        ColorUtil colorUtil = new ColorUtil();
        Color color = colorUtil.interpolateColorsBackAndForth(Shadow.getInstance().shadowColorNormal, Shadow.getInstance().shadowColor1, Shadow.getInstance().shadowColor2, Shadow.getInstance().shadowColor3, Shadow.getInstance().shadowThirdColor, Shadow.getInstance().shadowColorMode, Shadow.getInstance().shadowColorSpeed);
        outlineShader.setUniformi("texture", 0);
        outlineShader.setUniformf("radius", Shadow.getInstance().shadowRadius.getAsFloat() / 1.5f);
        outlineShader.setUniformf("texelSize", 1.0f / MC.displayWidth, 1.0f / MC.displayHeight);
        outlineShader.setUniformf("direction", dir1, dir2);
        outlineShader.setUniformf("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
    }
    public static Shadow getInstance() {
        return Mega.INSTANCE.getModuleManager().getModule(Shadow.class);
    }
}
