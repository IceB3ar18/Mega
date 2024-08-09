package club.mega.gui.widgets;

import java.awt.Color;

import club.mega.Mega;
import club.mega.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

public class ConfigButton extends GuiButton {
    private final Color color;
    private String date;
    private String time;
    private boolean selected;

    public ConfigButton(int id, int x, int y, int width, int height, String name, String date, String time, Color color) {
        super(id, x, y, width, height, name);
        this.color = color;
        this.date = date;
        this.time = time;
    }


    public void draw(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            FontRenderer fontrenderer = mc.fontRendererObj;
            mc.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            //RenderUtil.drawRect(this.xPosition, this.yPosition, this.width, this.height, new Color(20,20,20));
            Mega.INSTANCE.getFontManager().getFont("Arial 18").drawCenteredString(this.displayString, (float)(this.xPosition + 45), ((float)this.yPosition - (float)fontrenderer.FONT_HEIGHT / 2.0F + (float)this.height / 2.0F), selected ? new Color(255, 255, 255) : new Color(220, 220, 220));
        }
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
