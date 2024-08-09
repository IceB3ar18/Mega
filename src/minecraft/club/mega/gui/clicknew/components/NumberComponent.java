package club.mega.gui.clicknew.components;

import club.mega.Mega;
import club.mega.gui.clicknew.SettingComponent;
import club.mega.module.impl.hud.ClickGui;
import club.mega.module.setting.impl.NumberSetting;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;

import java.awt.Color;

public class NumberComponent extends SettingComponent {
    private final NumberSetting setting;
    private final double baseX;
    private final double sliderMaxWidth = 100; // Width of the slider
    private double sliderPosition;
    private static final double SLIDER_HEIGHT_OFFSET = 5;
    private static final double SLIDER_HEIGHT_PADDING = 4;
    private static final int TEXT_Y_OFFSET = -5;
    private static final int SLIDER_WIDTH_PADDING = 5;

    public NumberComponent(NumberSetting setting, double x, double y, double width, double height) {
        super(x, y, width, height);
        this.setting = setting;
        this.baseX = x;
        this.sliderPosition = (setting.getAsDouble() - setting.getMin()) / (setting.getMax() - setting.getMin()) * sliderMaxWidth;
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        double textWidth = Mega.INSTANCE.getFontManager().getFont("Arial 18").getWidth(setting.getName() + ":");
        double sliderStartX = baseX + textWidth + SLIDER_WIDTH_PADDING;

        if (isSliderHovered(mouseX, mouseY) && Mouse.isButtonDown(0)) {
            updateSetting(mouseX);
        }

        // Draw setting name text
        Mega.INSTANCE.getFontManager().getFont("Arial 18").drawStringWithShadow(setting.getName(), (int) baseX, (int) (y + height / 2 + TEXT_Y_OFFSET), new Color(199, 199, 199, 255));

        // Draw slider fill
        Gui.drawRect(sliderStartX, y + SLIDER_HEIGHT_OFFSET, sliderStartX + sliderPosition, y + height - SLIDER_HEIGHT_PADDING, ClickGui.getInstance().color.getColor().getRGB());

        // Draw slider border
        drawOutlinedRect(sliderStartX, y + SLIDER_HEIGHT_OFFSET, sliderStartX + sliderMaxWidth, y + height - SLIDER_HEIGHT_PADDING, new Color(0, 0, 0, 255).getRGB());

        // Draw value text
        Mega.INSTANCE.getFontManager().getFont("Arial 18").drawStringWithShadow(String.format("%.1f", setting.getAsDouble()), (int) (sliderStartX + sliderMaxWidth + SLIDER_WIDTH_PADDING), (int) (y + height / 2 + TEXT_Y_OFFSET), new Color(199, 199, 199, 255));
    }

    @Override
    public void handleMouseClick(int mouseX, int mouseY, int mouseButton) {
        // Handle mouse click event if needed
    }

    @Override
    public void handleMouseRelease(int mouseX, int mouseY, int state) {
        // Handle mouse release event if needed
    }

    @Override
    public void handleKeyPress(char typedChar, int keyCode) {
        // Implement this method if keyboard input needs to be handled
    }

    private boolean isSliderHovered(int mouseX, int mouseY) {
        double textWidth = Mega.INSTANCE.getFontManager().getFont("Arial 18").getWidth(setting.getName() + ":");
        double sliderStartX = baseX + textWidth + SLIDER_WIDTH_PADDING;
        return mouseX >= sliderStartX && mouseX <= sliderStartX + sliderMaxWidth && mouseY >= y + SLIDER_HEIGHT_OFFSET && mouseY <= y + height - SLIDER_HEIGHT_PADDING;
    }

    private void drawOutlinedRect(double left, double top, double right, double bottom, int color) {
        Gui.drawRect(left, top, right, top + 1, color);
        Gui.drawRect(left, bottom - 1, right, bottom, color);
        Gui.drawRect(left, top, left + 1, bottom, color);
        Gui.drawRect(right - 1, top, right, bottom, color);
    }

    private void updateSetting(int mouseX) {
        double textWidth = Mega.INSTANCE.getFontManager().getFont("Arial 18").getWidth(setting.getName() + ":");
        double newValue = setting.getMin() + (mouseX - (baseX + textWidth + SLIDER_WIDTH_PADDING)) / sliderMaxWidth * (setting.getMax() - setting.getMin());
        newValue = Math.round(newValue / setting.getIncrement()) * setting.getIncrement();
        newValue = Math.max(setting.getMin(), Math.min(setting.getMax(), newValue));
        setting.setCurrent(newValue);
        sliderPosition = (setting.getAsDouble() - setting.getMin()) / (setting.getMax() - setting.getMin()) * sliderMaxWidth;
    }
}
