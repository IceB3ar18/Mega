package club.mega.gui.click;

import club.mega.module.setting.Setting;
import club.mega.util.ColorUtil;
import club.mega.util.MouseUtil;
import club.mega.util.RenderUtil;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.io.IOException;

public class Component {

    protected double x, y, width, height;
    private final Setting setting;
    private boolean dragging;
    private double dragX, dragY;

    public Component(Setting setting, double width, double height) {
        this.setting = setting;
        this.width = width;
        this.height = height;
    }

    public void drawComponent(final int mouseX, final int mouseY) {
        RenderUtil.drawRect(x - 10, y, 10, height, new Color(30, 30, 30, 255));
        RenderUtil.drawRect(x + width, y, 10, height, new Color(30, 30, 30, 255));
        RenderUtil.drawRect(x, y, width, height, new Color(19, 19, 19, 255));
    }

    public void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) {
        if (isInside(mouseX, mouseY) && mouseButton == 0) {
            dragging = true;
            dragX = x - mouseX;
            dragY = y - mouseY;
        }
    }

    public void mouseReleased(final int mouseX, final int mouseY, final int mouseButton) {
        if (mouseButton == 0) {
            dragging = false;
        }
    }

    public void mouseDragged(final int mouseX, final int mouseY) {
        if (dragging) {
            setX(mouseX + dragX);
            setY(mouseY + dragY);
        }
    }

    public void keyTyped(final char typedChar, final int keyCode) throws IOException {}

    protected final boolean isInside(int mouseX, int mouseY) {
        return MouseUtil.isInside(mouseX, mouseY, x, y, width, height);
    }

    public final Setting getSetting() {
        return setting;
    }

    public final double getX() {
        return x;
    }

    public final void setX(final double x) {
        this.x = x;
    }

    public final double getY() {
        return y;
    }

    public final void setY(final double y) {
        this.y = y;
    }

    public final double getWidth() {
        return width;
    }

    public final void setWidth(final double width) {
        this.width = width;
    }

    public final double getHeight() {
        return height;
    }

    public final void setHeight(final double height) {
        this.height = height;
    }
}
