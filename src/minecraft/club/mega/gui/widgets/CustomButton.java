package club.mega.gui.widgets;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

public class CustomButton extends GuiButton {
    private final Color color;

    public CustomButton(int id, int x, int y, int width, int height, String message, Color color) {
        super(id, x, y, width, height, message);
        this.color = color;
    }

    private int getHoverColor(Color color, double addBrightness) {
        int colorRGB;
        if (this.hovered) {
            float[] hsbColor = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), (float[])null);
            float f = (float)((double)hsbColor[2] + addBrightness);
            if ((double)hsbColor[2] + addBrightness > 1.0) {
                f = 1.0F;
            } else if ((double)hsbColor[2] + addBrightness < 0.0) {
                f = 0.0F;
            }

            colorRGB = Color.HSBtoRGB(hsbColor[0], hsbColor[1], f);
        } else {
            colorRGB = color.getRGB();
        }

        return colorRGB;
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            FontRenderer fontrenderer = mc.fontRendererObj;
            mc.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            int i = this.getHoverState(this.hovered);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.blendFunc(770, 771);
            drawRect(this.xPosition, this.yPosition, (int)((float)(this.width + this.xPosition)), (int)((float)(this.height + this.yPosition)), this.getHoverColor(this.color, -0.2));
            this.mouseDragged(mc, mouseX, mouseY);
            int j = 14737632;
            if (!this.enabled) {
                j = 10526880;
            } else if (this.hovered) {
                j = 16777120;
            }

            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
        }

    }
}
