package club.mega.util;

import club.mega.Mega;
import club.mega.util.animation.AnimationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class RoundedButton extends GuiButton {

    private final Color color;
    private final Color hoverColor;
    private final int cr;
    private Color currentColor;
    private double currentWidth;
    private double currentHeight;
    private static final double HOVER_CHANGE = 1.0;

    public RoundedButton(final int buttonId, final int x, final int y, final int cr, final String buttonText) {
        super(buttonId, x, y, buttonText);
        this.color = new Color(15, 15, 15, 180);
        this.hoverColor = new Color(25, 25, 25, 180);
        this.cr = cr;
        this.currentColor = this.color;
        this.currentWidth = this.width;
        this.currentHeight = this.height;
    }

    public RoundedButton(final int buttonId, final int x, final int y, final int widthIn, final int heightIn, final int cr, final String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.color = new Color(15, 15, 15, 180);
        this.hoverColor = new Color(25, 25, 25, 180);
        this.cr = cr;
        this.currentColor = this.color;
        this.currentWidth = this.width;
        this.currentHeight = this.height;
    }

    public RoundedButton(final int buttonId, final int x, final int y, final Color color, final Color hoverColor, final int cr, final String buttonText) {
        super(buttonId, x, y, buttonText);
        this.color = color;
        this.hoverColor = hoverColor;
        this.cr = cr;
        this.currentColor = this.color;
        this.currentWidth = this.width;
        this.currentHeight = this.height;
    }

    public RoundedButton(final int buttonId, final int x, final int y, final int widthIn, final int heightIn, final Color color, final Color hoverColor, final int cr, final String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.color = color;
        this.hoverColor = hoverColor;
        this.cr = cr;
        this.currentColor = this.color;
        this.currentWidth = this.width;
        this.currentHeight = this.height;
    }

    @Override
    public void drawButton(final Minecraft mc, final int mouseX, final int mouseY) {
        if (this.visible) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            this.mouseDragged(mc, mouseX, mouseY);

            Color targetColor = this.color;
            double targetWidth = this.width;
            double targetHeight = this.height;

            if (!this.enabled) {
                targetColor = new Color(10, 10, 10);
            } else if (this.hovered) {
                targetColor = hoverColor;
                targetWidth = this.width - HOVER_CHANGE;
                targetHeight = this.height - HOVER_CHANGE;
            }

            // Animate the color and size changes
            this.currentColor = new Color(
                    (int) AnimationUtil.animate(this.currentColor.getRed(), targetColor.getRed(), 1),
                    (int) AnimationUtil.animate(this.currentColor.getGreen(), targetColor.getGreen(), 1),
                    (int) AnimationUtil.animate(this.currentColor.getBlue(), targetColor.getBlue(), 1),
                    (int) AnimationUtil.animate(this.currentColor.getAlpha(), targetColor.getAlpha(), 1)
            );
            this.currentWidth = AnimationUtil.animate(this.currentWidth, targetWidth, 5.0);
            this.currentHeight = AnimationUtil.animate(this.currentHeight, targetHeight, 5.0);

            double drawX = this.xPosition + (this.width - this.currentWidth) / 2;
            double drawY = this.yPosition + (this.height - this.currentHeight) / 2;

            RenderUtil.drawRoundedRect(drawX, drawY, this.currentWidth, this.currentHeight, cr, currentColor);
            Mega.INSTANCE.getFontManager().getFont("Roboto bold 20").drawCenteredString(displayString, (float)(drawX + this.currentWidth / 2D), (float)(drawY + (this.currentHeight - 8) / 2D), -1);
        }
    }
}
