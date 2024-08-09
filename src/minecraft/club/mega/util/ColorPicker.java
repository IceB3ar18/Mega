package club.mega.util;

import java.awt.Color;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;

public class ColorPicker {
    private final double radius;
    private final Consumer<Color> color;
    private double selectedX;
    private double selectedY;
    private float brightness = 1.0F;
    private float alpha = 1.0F;
    private ScaledResolution sr;

    public ColorPicker(double radius, Color selectedColor, Consumer<Color> color) {
        this.radius = radius;
        this.alpha = (float)selectedColor.getAlpha() / 255.0F;
        this.brightness = Color.RGBtoHSB(selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue(), (float[])null)[2];
        this.setColor(selectedColor);
        this.color = color;
    }

    public void click(double mouseX, double mouseY, int button) {
        this.sr = new ScaledResolution(Minecraft.getMinecraft());
        int width = this.sr.getScaledWidth() / 2;
        int height = this.sr.getScaledHeight() / 2;
        double sliderX = (double)(width - 70);
        double sliderWidth = 138.0;

        // Klick auf Helligkeitsslider
        if (this.mouseOver(mouseX, mouseY, sliderX, height + this.radius + 10, sliderWidth, 15)) {
            double var1 = MathHelper.clamp_double((mouseX - sliderX) / sliderWidth, 0.0, 1.0);
            this.setBrightness((float)var1);
        }

        // Klick auf Transparenzslider
        if (this.mouseOver(mouseX, mouseY, sliderX, height + this.radius + 10 + 25, sliderWidth, 15)) {
            double var1 = MathHelper.clamp_double((mouseX - sliderX) / sliderWidth, 0.0, 1.0);
            this.setAlpha((float)var1);
        }

        // Klick auf das Farbrad
        if (button == 0 && this.isPointInCircle((double)width, (double)height, this.radius, mouseX, mouseY)) {
            this.selectedX = mouseX - (double)width;
            this.selectedY = mouseY - (double)height;
        }
    }


    public void draw(int mouseX, int mouseY) {
        this.sr = new ScaledResolution(Minecraft.getMinecraft());
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

        // Hintergrund zeichnen
        int screenWidth = this.sr.getScaledWidth();
        int screenHeight = this.sr.getScaledHeight();
        int width = screenWidth / 2;
        int height = screenHeight / 2;
        int bgWidth = (int) (this.radius * 2 + 20);  // Hier kannst du den Abstand vom Rand anpassen
        int bgHeight = (int) (this.radius * 2 + 120);  // Hier kannst du den Abstand vom Rand anpassen
        RenderUtil.drawRoundedRect(width - bgWidth / 2, height - bgHeight / 2 + 30, bgWidth, bgHeight - 30, 5, new Color(1, 1, 1, 140));
        RenderUtil.drawCircle((double)this.sr.getScaledWidth() / 2.0 + this.selectedX, (double)this.sr.getScaledHeight() / 2.0 + this.selectedY, 2.5, -15592942);

        // Originaler Code für den Color Picker und den Slider
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderUtil.drawColoredCircle((double)screenWidth / 2.0, (double)screenHeight / 2.0, this.radius, this.getBrightness());
        RenderUtil.drawCirclePicker((double)this.sr.getScaledWidth() / 2.0 + this.selectedX, (double)this.sr.getScaledHeight() / 2.0 + this.selectedY, 3.5, new Color(30,30,30,255).getRGB());
        RenderUtil.drawCirclePicker((double)this.sr.getScaledWidth() / 2.0 + this.selectedX, (double)this.sr.getScaledHeight() / 2.0 + this.selectedY, 2.5, new Color(this.getColor().getRed(), this.getColor().getGreen(), this.getColor().getBlue(), 255).getRGB());

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();

        // Slider für Helligkeit
        double sliderWidth = 138.0;
        double sliderX = width - sliderWidth / 2;
        double sliderY = height + this.radius + 10; // Abstand zum Farbrad
        double value1 = sliderX + 2.0 + this.getBrightness() * sliderWidth;
        RenderUtil.drawRoundedRect(sliderX, sliderY, sliderWidth, 15, 3, this.getColor());
        Gui.drawRect((int)(value1 - 2.0), (int)sliderY, (int)value1, (int)(sliderY + 15), Color.black.getRGB());

        // Slider für Transparenz
        double sliderY2 = sliderY + 25; // Abstand zwischen den Slidern
        double value12 = sliderX + 2.0 + this.getAlpha() * sliderWidth;
        RenderUtil.drawRoundedRect(sliderX, sliderY2, sliderWidth, 15, 3, this.getColor());
        Gui.drawRect((int)(value12 - 2.0), (int)sliderY2, (int)value12, (int)(sliderY2 + 15), Color.black.getRGB());

        this.color.accept(this.getColor());
    }



    private float getNormalized() {
        return (float)((-Math.toDegrees(Math.atan2(this.selectedY, this.selectedX)) + 450.0) % 360.0) / 360.0F;
    }

    private Color getColor() {
        Color color1 = Color.getHSBColor(this.getNormalized(), (float)(Math.hypot(this.selectedX, this.selectedY) / this.radius), this.getBrightness());
        return new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), (int)(this.getAlpha() * 255.0F));
    }

    private void setColor(Color selectedColor) {
        float[] hsb = Color.RGBtoHSB(selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue(), (float[])null);
        this.selectedX = (double)hsb[1] * this.radius * (Math.sin(Math.toRadians((double)(hsb[0] * 360.0F))) / Math.sin(Math.toRadians(90.0)));
        this.selectedY = (double)hsb[1] * this.radius * (Math.sin(Math.toRadians((double)(90.0F - hsb[0] * 360.0F))) / Math.sin(Math.toRadians(90.0)));
    }

    private boolean isPointInCircle(double x, double y, double radius, double pX, double pY) {
        return (pX - x) * (pX - x) + (pY - y) * (pY - y) <= radius * radius;
    }

    public boolean mouseOver(double mouseX, double mouseY, double posX, double posY, double width, double height) {
        return mouseX >= posX && mouseX <= posX + width && mouseY >= posY && mouseY <= posY + height;
    }

    public float getBrightness() {
        return this.brightness;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public float getAlpha() {
        return this.alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }
}