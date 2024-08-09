package net.minecraft.client.gui;

import club.mega.Mega;
import club.mega.gui.click.ConfigGui;
import club.mega.util.ColorUtil;
import club.mega.util.RenderUtil;
import club.mega.util.animation.AnimationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiButton extends Gui
{
    protected static final ResourceLocation buttonTextures = new ResourceLocation("textures/gui/widgets.png");
    public int width;
    protected int height;
    public int xPosition;
    public int yPosition;
    public String displayString;
    public int id;
    public boolean enabled;
    public boolean visible;
    protected boolean hovered;

    // Animation fields
    private double currentWidth;
    private double currentHeight;
    private static final double HOVER_CHANGE = 1.0;

    public GuiButton(int buttonId, double x, double y, String buttonText)
    {
        this(buttonId, x, y, 200, 20, buttonText);
    }

    public GuiButton(int buttonId, double x, double y, int widthIn, int heightIn, String buttonText)
    {
        this.width = 200;
        this.height = 20;
        this.enabled = true;
        this.visible = true;
        this.id = buttonId;
        this.xPosition = (int)x;
        this.yPosition = (int)y;
        this.width = widthIn;
        this.height = heightIn;
        this.displayString = buttonText;
        this.currentWidth = this.width;
        this.currentHeight = this.height;
    }

    protected int getHoverState(boolean mouseOver)
    {
        int i = 1;

        if (!this.enabled)
        {
            i = 0;
        }
        else if (mouseOver)
        {
            i = 2;
        }

        return i;
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible)
        {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            this.mouseDragged(mc, mouseX, mouseY);
            Color color = ColorUtil.getMainColor();

            if (!this.enabled)
            {
                color = ColorUtil.getMainColor().darker();
            }

            double targetWidth = this.width;
            double targetHeight = this.height;

            if (this.hovered)
            {
                targetWidth = this.width - HOVER_CHANGE;
                targetHeight = this.height - HOVER_CHANGE;
                color = ColorUtil.getMainColor().darker();
            }

            // Animate the size changes
            this.currentWidth = AnimationUtil.animate(this.currentWidth, targetWidth, 2.0);
            this.currentHeight = AnimationUtil.animate(this.currentHeight, targetHeight, 2.0);

            double drawX = this.xPosition + (this.width - this.currentWidth) / 2;
            double drawY = this.yPosition + (this.height - this.currentHeight) / 2;

            if (!(Minecraft.getMinecraft().currentScreen instanceof ConfigGui)) {
                RenderUtil.drawRoundedRect(drawX, drawY + 1, this.currentWidth, this.currentHeight - 1, 3, color);
            }

            if (!(Minecraft.getMinecraft().currentScreen instanceof ConfigGui)) {
                Mega.INSTANCE.getFontManager().getFont("Roboto medium 20").drawCenteredString(displayString, (float) (drawX + this.currentWidth / 2), (float) (drawY + (this.currentHeight - 8) / 2), -1);
            } else {
                Mega.INSTANCE.getFontManager().getFont("Arial 18").drawCenteredString(displayString, (float) (drawX + this.currentWidth / 2), (float) (drawY + (this.currentHeight - 8) / 2), isMouseOver() ? new Color(255, 255, 255) : new Color(220, 220, 220));
            }
        }
    }

    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY)
    {
    }

    public void mouseReleased(int mouseX, int mouseY)
    {
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        return this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
    }

    public boolean isMouseOver()
    {
        return this.hovered;
    }

    public void drawButtonForegroundLayer(int mouseX, int mouseY)
    {
    }

    public void playPressSound(SoundHandler soundHandlerIn)
    {
        soundHandlerIn.playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
    }

    public int getButtonWidth()
    {
        return this.width;
    }

    public int getButtonHeight()
    {
        return this.height;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }
}
