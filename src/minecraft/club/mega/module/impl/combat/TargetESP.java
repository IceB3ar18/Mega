package club.mega.module.impl.combat;

import club.mega.event.impl.EventRender3D;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ColorSetting;
import club.mega.module.setting.impl.ListSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.util.AuraUtil;
import club.mega.util.ColorUtil;
import club.mega.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import rip.hippo.lwjeb.annotation.Handler;

import java.awt.*;

@Module.ModuleInfo(name = "TargetESP", description = "ESP For aura targets", category = Category.VISUAL)
public class TargetESP extends Module {

    private final BooleanSetting spiral = new BooleanSetting("Spiral", this, false);
    public final ListSetting mode = new ListSetting("Color Mode", this, new String[]{"Normal", "Fade"}, spiral::get);
    public final ColorSetting colornormal = new ColorSetting("Spiral Color", this, new Color(0, 187, 255, 203), () -> {
        return this.mode.is("Normal") && spiral.get();
    });
    public final ColorSetting color1 = new ColorSetting("Spiral Color 1", this, new Color(0, 255, 111, 203), () -> {
        return this.mode.is("Fade") && spiral.get();
    });
    public final ColorSetting color2 = new ColorSetting("Spiral Color 2", this, new Color(0, 234, 255, 197), () -> {
        return this.mode.is("Fade") && spiral.get();
    });
    public final NumberSetting coils = new NumberSetting("Coils", this, 1, 10, 3, 0.1, spiral::get);
    public final NumberSetting speed = new NumberSetting("Speed", this, 0, 10, 4, 0.1, spiral::get);
    public final NumberSetting height = new NumberSetting("Height", this, 0.1, 5, 1.8, 0.1, spiral::get);
    public final NumberSetting width = new NumberSetting("Width", this, 0.1, 5, 1.6, 0.1, spiral::get);
    private final BooleanSetting sigma = new BooleanSetting("Sigma", this, false);
    public final ListSetting sigmaMode = new ListSetting("Sigma Mode", this, new String[]{"Normal", "Gradient", "Rainbow"}, sigma::get);
    public final ColorSetting sigmaColor = new ColorSetting("Sigma Color", this, new Color(0, 187, 255, 203), () -> {
        return this.sigmaMode.is("Normal") && sigma.get();
    });
    public final ColorSetting sigamColor1 = new ColorSetting("Sigma Color 1", this, new Color(0, 255, 111, 203), () -> {
        return this.sigmaMode.is("Gradient") && sigma.get();
    });
    public final ColorSetting sigamColor2 = new ColorSetting("Sigma Color 2", this, new Color(0, 234, 255, 197), () -> {
        return this.sigmaMode.is("Gradient") && sigma.get();
    });
    private final BooleanSetting line = new BooleanSetting("Line", this, false, sigma::get);
    private final BooleanSetting hurttime = new BooleanSetting("Sigma Hurttime", this, true, sigma::get);

    private double phaseShift = 0.0;
    private int index = 0;

    @Handler
    public final void onRender(final EventRender3D event) {
        if (KillAura.getInstance().isToggled()) {
            if (AuraUtil.getTarget() != null) {
                if (sigma.get()) {
                    if (AuraUtil.getTarget() instanceof EntityPlayer) {
                        drawTargetESP((EntityPlayer) AuraUtil.getTarget(), MC.timer.renderPartialTicks);
                    }
                }

                if (spiral.get()) {
                    EntityLivingBase entity = AuraUtil.getTarget();


                    RenderUtil renderUtil = new RenderUtil();
                    Color color = mode.is("Normal") ? colornormal.getColor() : ColorUtil.getGradientOffset(color1.getColor(), color2.getColor(), index);
                    renderUtil.drawRainbowSpiral(width.getAsDouble() / 2.0, entity.posX, entity.lastTickPosX, entity.posY + 1.0 - height.getAsDouble() / 2.0, entity.lastTickPosY + 1.0 - height.getAsDouble() / 2.0, entity.posZ, entity.lastTickPosZ, height.getAsDouble(), coils.getAsDouble(), this.phaseShift, 0, color);
                    if (this.phaseShift < 6.283185307179586) {
                        this.phaseShift += speed.getAsDouble() * 6.283185307179586 / 400.0;
                    } else {
                        this.phaseShift = 0.0;
                    }
                    index -= speed.getAsInt() / 100;
                }
            }
        }
    }

    public void drawTargetESP(EntityPlayer target, float pt) {
        final double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * pt
                - MC.getRenderManager().viewerPosX;
        final double y = target.lastTickPosY + (target.posY - target.lastTickPosY) * pt
                - MC.getRenderManager().viewerPosY;
        final double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * pt
                - MC.getRenderManager().viewerPosZ;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GL11.glLineWidth(3F);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_CULL_FACE);
        final double size = target.width * 1.2;
        float factor = (float) Math.sin(System.nanoTime() / 300000000f);
        GL11.glTranslatef(0, factor, 0);

        if (sigmaMode.is("Gradient")) {
            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            {
                for (int j = 0; j < 361; j++) {
                    double percentage = (double) j / 360; // Berechne den Prozentsatz des Fortschritts
                    double halfCirclePercentage = Math.abs(percentage - 0.5) * 2; // Skaliere den Prozentsatz auf den Bereich [0, 1]

                    int r = (int) (sigamColor1.getColor().getRed() * (1 - halfCirclePercentage) + sigamColor2.getColor().getRed() * halfCirclePercentage);
                    int g = (int) (sigamColor1.getColor().getGreen() * (1 - halfCirclePercentage) + sigamColor2.getColor().getGreen() * halfCirclePercentage);
                    int b = (int) (sigamColor1.getColor().getBlue() * (1 - halfCirclePercentage) + sigamColor2.getColor().getBlue() * halfCirclePercentage);

                    if(hurttime.get() && target.hurtTime != 0) {
                        r = 230;
                        g = 0;
                        b = 0;
                    }

                    RenderUtil.color(r, g, b, 200);
                    double x1 = x + Math.cos(Math.toRadians(j)) * size;
                    double z1 = z - Math.sin(Math.toRadians(j)) * size;
                    GL11.glVertex3d(x1, y + 1, z1);

                    RenderUtil.color(r, g, b, 0);
                    GL11.glVertex3d(x1, y + 1 + factor * 0.4f, z1);
                }
            }
        } else if (sigmaMode.is("Rainbow")) {
            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            {
                for (int j = 0; j < 361; j++) {
                    // Berechne die Regenbogenfarbe basierend auf dem aktuellen Winkel j
                    Color rainbowColor = getRainbowColor(j);
                    int r = rainbowColor.getRed();
                    int g = rainbowColor.getGreen();
                    int b = rainbowColor.getBlue();

                    if(hurttime.get() && target.hurtTime != 0) {
                        r = 230;
                        g = 0;
                        b = 0;
                    }

                    RenderUtil.color(r, g, b, 200);

                    double x1 = x + Math.cos(Math.toRadians(j)) * size;
                    double z1 = z - Math.sin(Math.toRadians(j)) * size;
                    GL11.glVertex3d(x1, y + 1, z1);

                    RenderUtil.color(r, g, b, 0);
                    GL11.glVertex3d(x1, y + 1 + factor * 0.4f, z1);
                }
            }
        } else if (sigmaMode.is("Normal")) {
            int[] rgb = ColorUtil.getRGB(sigmaColor.getColor().getRGB());

            if(hurttime.get() && target.hurtTime != 0) {
                rgb = ColorUtil.getRGB(new Color(230,0,0).getRGB());
            }

            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            {
                for (int j = 0; j < 361; j++) {
                    RenderUtil.color(ColorUtil.getColor(rgb[0], rgb[1], rgb[2], 200));
                    double x1 = x + Math.cos(Math.toRadians(j)) * size;
                    double z1 = z - Math.sin(Math.toRadians(j)) * size;
                    GL11.glVertex3d(x1, y + 1, z1);
                    RenderUtil.color(ColorUtil.getColor(rgb[0], rgb[1], rgb[2], 0));
                    GL11.glVertex3d(x1, y + 1 + factor * 0.4f, z1);
                }
            }
        }

        GL11.glEnd();

        if (line.get()) {

            GL11.glBegin(GL11.GL_LINE_STRIP);
            {
                for (int j = 0; j < 361; j++) {
                    RenderUtil.color(ColorUtil.getColor(255, 255, 255, 255)); // Weiße Farbe
                    GL11.glVertex3d(x + Math.cos(Math.toRadians(j)) * size, y + 1, z - Math.sin(Math.toRadians(j)) * size);
                }
            }
            GL11.glEnd();
        }
        GL11.glBegin(GL11.GL_LINE_LOOP);
        {
            
            for (int j = 0; j < 361; j++) {
                int[] rgb = ColorUtil.getRGB(sigmaColor.getColor().getRGB());
                RenderUtil.color(ColorUtil.getColor(rgb[0], rgb[1], rgb[2], 1));
                GL11.glVertex3d(x + Math.cos(Math.toRadians(j)) * size, y + 1, z - Math.sin(Math.toRadians(j)) * size);
            }
        }
        GL11.glEnd();
        GlStateManager.enableAlpha();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GlStateManager.enableBlend();

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.resetColor();
        GlStateManager.popMatrix();
    }

    private Color getRainbowColor(int angle) {
        // Berechne die RGB-Werte basierend auf dem Winkel im Regenbogen
        float hue = (angle % 360) / 360.0f;
        return Color.getHSBColor(hue, 1.0f, 1.0f);
    }

}
