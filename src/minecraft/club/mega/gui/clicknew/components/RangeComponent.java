package club.mega.gui.clicknew.components;

import club.mega.Mega;
import club.mega.gui.clicknew.SettingComponent;
import club.mega.module.impl.hud.ClickGui;
import club.mega.module.setting.impl.RangeSetting;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;

import java.awt.*;

public class RangeComponent extends SettingComponent {
    private final RangeSetting setting;
    private boolean dragging;
    private final double baseX;

    public RangeComponent(RangeSetting setting, double x, double y, double width, double height) {
        super(x, y, width, height);
        this.setting = setting;
        this.dragging = false;
        this.baseX = x;
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        if(isSliderHovered(mouseX, mouseY) && Mouse.isButtonDown(0)) {
            updateSetting(mouseX);
        }
        // Berechnung der Positionen
        double textWidth = Mega.INSTANCE.getFontManager().getFont("Arial 18").getWidth(setting.getName() + ":");
        double sliderStartX = baseX + textWidth + 5;
        double sliderMaxWidth = 100; // Breite des Sliders
        double minSliderPosition = (setting.getCurrentMin() - setting.getMin()) / (setting.getMax() - setting.getMin()) * sliderMaxWidth;
        double maxSliderPosition = (setting.getCurrentMax() - setting.getMin()) / (setting.getMax() - setting.getMin()) * sliderMaxWidth;

        // Text: Setting-Name links
        Mega.INSTANCE.getFontManager().getFont("Arial 18").drawStringWithShadow(setting.getName(), (int) baseX, (int) (y + height / 2 - 5), new Color(199, 199, 199, 255));

        // Slider-Füllung
        Gui.drawRect(sliderStartX + minSliderPosition, y + 5, sliderStartX + maxSliderPosition, y + height - 4, ClickGui.getInstance().color.getColor().getRGB());

        // Rand um den Slider-Bereich
        drawOutlinedRect(sliderStartX, y + 5, sliderStartX + sliderMaxWidth, y + height - 4, new Color(0, 0, 0, 255).getRGB());

        // Werte rechts vom Slider
        Mega.INSTANCE.getFontManager().getFont("Arial 18").drawStringWithShadow(String.format("%.1f - %.1f", setting.getCurrentMin(), setting.getCurrentMax()), (int) (sliderStartX + sliderMaxWidth + 5), (int) (y + height / 2 - 5), new Color(199, 199, 199, 255));

    }

    private void drawOutlinedRect(double left, double top, double right, double bottom, int color) {
        Gui.drawRect(left, top, right, top + 1, color);
        Gui.drawRect(left, bottom - 1, right, bottom, color);
        Gui.drawRect(left, top, left + 1, bottom, color);
        Gui.drawRect(right - 1, top, right, bottom, color);
    }

    @Override
    public void handleMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && isSliderHovered(mouseX, mouseY)) {
            updateSetting(mouseX);
        }
    }

    @Override
    public void handleMouseRelease(int mouseX, int mouseY, int state) {

    }

    @Override
    public void handleKeyPress(char typedChar, int keyCode) {
        // No action needed for key press in this component
    }



    private boolean isSliderHovered(int mouseX, int mouseY) {
        double textWidth = Mega.INSTANCE.getFontManager().getFont("Arial 18").getWidth(setting.getName() + ":");
        double sliderStartX = baseX + textWidth + 5;
        return mouseX >= sliderStartX && mouseX <= sliderStartX + 100 && mouseY >= y && mouseY <= y + height; // Slider-Bereich angepasst
    }

    private void updateSetting(int mouseX) {
        double textWidth = Mega.INSTANCE.getFontManager().getFont("Arial 18").getWidth(setting.getName() + ":");
        double sliderStartX = baseX + textWidth + 5;
        double sliderMaxWidth = 100;

        double newValue = setting.getMin() + (mouseX - sliderStartX) / sliderMaxWidth * (setting.getMax() - setting.getMin());
        newValue = Math.round(newValue / setting.getIncrement()) * setting.getIncrement();
        newValue = Math.max(setting.getMin(), Math.min(setting.getMax(), newValue));

        // Berechne die aktuellen Slider-Positionen
        double minSliderPosition = (setting.getCurrentMin() - setting.getMin()) / (setting.getMax() - setting.getMin()) * sliderMaxWidth;
        double maxSliderPosition = (setting.getCurrentMax() - setting.getMin()) / (setting.getMax() - setting.getMin()) * sliderMaxWidth;

        // Bestimme welcher Wert (min oder max) näher an der Maus ist
        if (Math.abs(mouseX - (sliderStartX + minSliderPosition)) < Math.abs(mouseX - (sliderStartX + maxSliderPosition))) {
            setting.setCurrentMin(newValue);
        } else {
            setting.setCurrentMax(newValue);
        }
    }
}
