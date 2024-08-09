package club.mega.gui.click.components;

import java.awt.Color;
import java.io.IOException;

import club.mega.Mega;
import club.mega.module.setting.Setting;
import club.mega.module.setting.impl.ColorSetting;
import club.mega.util.ColorPicker;
import club.mega.util.RenderUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

public class ColorPickerGUI extends GuiScreen {
    private final Setting setting;
    private final ColorPicker colorPicker;

    public ColorPickerGUI(ColorSetting setting) {
        this.setting = setting;
        Color var10004 = setting.getColor();
        setting.getClass();
        this.colorPicker = new ColorPicker(70.0, var10004, setting::setColor);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        ScaledResolution sr = new ScaledResolution(this.mc);
        this.colorPicker.draw(mouseX, mouseY);
        GL11.glPushMatrix();
        GL11.glScaled(1.5, 1.5, 1.0);


        Mega.INSTANCE.getFontManager().getFont("Arial 28").drawCenteredString("Select color:", (int)((double)sr.getScaledWidth() / 1.5 / 2.0), (int)((double)sr.getScaledHeight() / 1.5 / 2.0 - 58.666666666666664), (new Color(255, 255, 255)).getRGB());

        GL11.glScaled(1.0, 1.0, 1.0);
        GL11.glPopMatrix();

    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.colorPicker.click((double)mouseX, (double)mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        this.colorPicker.click((double)mouseX, (double)mouseY, clickedMouseButton);
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            this.mc.displayGuiScreen(Mega.INSTANCE.getClickGUI());
        }

    }

    public void onGuiClosed() {
    }
}