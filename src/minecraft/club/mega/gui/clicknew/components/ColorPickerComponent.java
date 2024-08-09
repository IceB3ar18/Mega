package club.mega.gui.clicknew.components;

import club.mega.Mega;
import club.mega.gui.clicknew.SettingComponent;
import club.mega.module.setting.impl.ColorSetting;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static club.mega.util.RenderUtil.drawGradientSideways;
import static club.mega.util.RenderUtil.otherDrawOutlinedBoundingBox;

public class ColorPickerComponent extends SettingComponent {
    private final ColorSetting setting;
    private final double baseX;
    private final double pickerHeight; // Neue Variable für die Höhe des ColorPickers
    private static final double SLIDER_GAP = 5; // Abstand zwischen den Slidern
    private String hexInput = ""; // Variable für die Hex-Code-Eingabe

    public ColorPickerComponent(ColorSetting setting, double x, double y, double width, double height) {
        super(x, y, width, height);
        this.setting = setting;
        this.baseX = x;
        this.pickerHeight = height; // Setze die Höhe des ColorPickers
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        String colorHex = String.format("#%02X%02X%02X%02X", setting.getColor().getRed(), setting.getColor().getGreen(), setting.getColor().getBlue(), setting.getColor().getAlpha());
        double pickerStartY = y + 5 + Mega.INSTANCE.getFontManager().getFont("Arial 18").getHeight(setting.getName() + ": " + colorHex);

        // Draw Color Picker (Hue, Saturation, Brightness)
        drawColorPicker(mouseX, mouseY, pickerStartY);

        Mega.INSTANCE.getFontManager().getFont("Arial 18").drawStringWithShadow(setting.getName() + ": " + colorHex, (int) baseX, (int) (y + 5), new Color(199, 199, 199, 255));

        if(Mouse.isButtonDown(0)) {
            if (isPickerHovered(mouseX, mouseY, "Hue")) {
                updatePickerValue(mouseX, mouseY, "Hue");
            } else if (isPickerHovered(mouseX, mouseY, "SB")) {
                updatePickerValue(mouseX, mouseY, "SB");
            } else if (isPickerHovered(mouseX, mouseY, "Alpha")) {
                updatePickerValue(mouseX, mouseY, "Alpha");
            }
        }
    }

    private void drawColorPicker(int mouseX, int mouseY, double pickerStartY) {
        double pickerSize = width - 125; // Anpassung der Picker-Größe basierend auf der Höhe
        double pickerHeight = this.pickerHeight - 10;
        double hueWidth = 10;
        double alphaWidth = 10;

        double pickerStartX = baseX;

        // Draw Color Gradient (Hue)
        drawColorGradient(pickerStartX + pickerSize + SLIDER_GAP, pickerStartY, hueWidth, pickerHeight);

        // Draw SB Gradient
        drawSBGradient(pickerStartX, pickerStartY, pickerSize, pickerHeight);

        // Draw Alpha Gradient
        drawAlphaGradient(pickerStartX + pickerSize + hueWidth + 2 * SLIDER_GAP, pickerStartY, alphaWidth, pickerHeight);

        // Draw Outlines
        drawOutlinedRect(pickerStartX, pickerStartY, pickerStartX + pickerSize, pickerStartY + pickerHeight, new Color(0, 0, 0, 255).getRGB());
        drawOutlinedRect(pickerStartX + pickerSize + SLIDER_GAP, pickerStartY, pickerStartX + pickerSize + hueWidth + SLIDER_GAP, pickerStartY + pickerHeight, new Color(0, 0, 0, 255).getRGB());
        drawOutlinedRect(pickerStartX + pickerSize + hueWidth + 2 * SLIDER_GAP, pickerStartY, pickerStartX + pickerSize + hueWidth + 2 * SLIDER_GAP + alphaWidth, pickerStartY + pickerHeight, new Color(0, 0, 0, 255).getRGB());

        // Draw Selectors
        float[] hsbValues = Color.RGBtoHSB(setting.getColor().getRed(), setting.getColor().getGreen(), setting.getColor().getBlue(), null);
        double huePosition = hsbValues[0] * pickerHeight;
        double saturationPosition = hsbValues[1] * pickerSize;
        double brightnessPosition = (1 - hsbValues[2]) * pickerHeight;
        double alphaPosition = (double) setting.getColor().getAlpha() / 255 * pickerHeight;
        drawSelector(pickerStartX + saturationPosition, pickerStartY + brightnessPosition, 2, 2);
        drawSelector(pickerStartX + pickerSize + SLIDER_GAP, pickerStartY + huePosition, hueWidth, 1);
        drawSelector(pickerStartX + pickerSize + hueWidth + 2 * SLIDER_GAP, pickerStartY + alphaPosition, hueWidth, 1);
    }

    private void drawColorGradient(double x, double y, double width, double height) {
        for (int i = 0; i < height; i++) {
            float hue = (float) i / (float) height;
            Color color = Color.getHSBColor(hue, 1.0F, 1.0F);
            Color nextColor = Color.getHSBColor((float) (i + 1) / (float) height, 1.0F, 1.0F);
            drawGradientSideways(x, y + i, x + width, y + i + 1, color, nextColor);
        }
    }

    private void drawSBGradient(double x, double y, double width, double height) {
        float[] hsbValues = Color.RGBtoHSB(setting.getColor().getRed(), setting.getColor().getGreen(), setting.getColor().getBlue(), null);
        for (int i = 0; i < width; i++) {
            float saturation = (float) i / (float) width;
            for (int j = 0; j < height; j++) {
                float brightness = 1 - (float) j / (float) height;
                Color color = Color.getHSBColor(hsbValues[0], saturation, brightness);
                Gui.drawRect(x + i, y + j, x + i + 1, y + j + 1, color.getRGB());
            }
        }
    }

    private void drawAlphaGradient(double x, double y, double width, double height) {
        for (int i = 0; i < height; i++) {
            float alpha = (float) i / (float) height;
            Color color = new Color(setting.getColor().getRed(), setting.getColor().getGreen(), setting.getColor().getBlue(), (int) (alpha * 255));
            Color nextColor = new Color(setting.getColor().getRed(), setting.getColor().getGreen(), setting.getColor().getBlue(), (int) (((float) (i + 1) / (float) height) * 255));
            drawGradientSideways(x, y + i, x + width, y + i + 1, color, nextColor);
        }
    }

    private void drawSelector(double x, double y, double width, double height) {
        Gui.drawRect(x - 1, y - 1, x + width + 1, y + height + 1, new Color(0, 0, 0, 255).getRGB());
        Gui.drawRect(x, y, x + width, y + height, new Color(255, 255, 255, 255).getRGB());
    }

    private void drawOutlinedRect(double left, double top, double right, double bottom, int color) {
        Gui.drawRect(left, top, right, top + 1, color);
        Gui.drawRect(left, bottom - 1, right, bottom, color);
        Gui.drawRect(left, top, left + 1, bottom, color);
        Gui.drawRect(right - 1, top, right, bottom, color);
    }

    private void drawTextBox(int x, int y, String text) {
        int width = 100; // Textbox-Breite
        int height = 20; // Textbox-Höhe

        // Draw the textbox background
        Gui.drawRect(x, y, x + width, y + height, new Color(255, 255, 255, 255).getRGB());
        // Draw the textbox border
        Gui.drawRect(x - 1, y - 1, x + width + 1, y + height + 1, new Color(0, 0, 0, 255).getRGB());

        // Draw the current input text
        Mega.INSTANCE.getFontManager().getFont("Arial 18").drawStringWithShadow(hexInput, x + 5, y + 5, new Color(0, 0, 0, 255));
    }

    @Override
    public void handleMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {


            if (isPickerHovered(mouseX, mouseY, "Hue")) {
                updatePickerValue(mouseX, mouseY, "Hue");
            } else if (isPickerHovered(mouseX, mouseY, "SB")) {
                updatePickerValue(mouseX, mouseY, "SB");
            } else if (isPickerHovered(mouseX, mouseY, "Alpha")) {
                updatePickerValue(mouseX, mouseY, "Alpha");
            }
        }
    }

    @Override
    public void handleMouseRelease(int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public void handleMouseDrag(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {

    }

    @Override
    public void handleKeyPress(char typedChar, int keyCode) {

    }


    private boolean isPickerHovered(int mouseX, int mouseY, String type) {
        double pickerStartX = baseX;
        double pickerStartY = y + 5 + Mega.INSTANCE.getFontManager().getFont("Arial 18").getHeight(setting.getName() + ": #FFFF");
        double pickerSize = width - 125; // Anpassung der Picker-Größe basierend auf der Höhe
        double pickerHeight = this.pickerHeight - 10;
        if (type.equals("Hue")) {
            pickerStartX += pickerSize + SLIDER_GAP;
            return mouseX > pickerStartX && mouseX < pickerStartX + 10 && mouseY > pickerStartY && mouseY < pickerStartY + pickerHeight;
        } else if (type.equals("SB")) {
            return mouseX > pickerStartX && mouseX < pickerStartX + pickerSize && mouseY > pickerStartY && mouseY < pickerStartY + pickerHeight;
        } else if (type.equals("Alpha")) {
            pickerStartX += pickerSize + 10 + 2 * SLIDER_GAP;
            return mouseX > pickerStartX && mouseX < pickerStartX + 10 && mouseY > pickerStartY && mouseY < pickerStartY + pickerHeight;
        }
        return false;
    }

    private void updatePickerValue(int mouseX, int mouseY, String type) {
        double pickerStartX = baseX;
        double pickerSize = width - 125;
        double pickerStartY = y + 5 + Mega.INSTANCE.getFontManager().getFont("Arial 18").getHeight(setting.getName() + ": #FFFF");
        double pickerHeight = this.pickerHeight - 10;

        if (type.equals("Hue")) {
            pickerStartX += pickerSize + SLIDER_GAP;
            float hue = (float) (mouseY - pickerStartY) / (float) pickerHeight;
            hue = Math.max(0, Math.min(1, hue));
            setting.setColor(Color.getHSBColor(hue, 1.0F, 1.0F));
        } else if (type.equals("SB")) {
            float[] hsbValues = Color.RGBtoHSB(setting.getColor().getRed(), setting.getColor().getGreen(), setting.getColor().getBlue(), null);
            float saturation = (float) (mouseX - pickerStartX) / (float) pickerSize;
            float brightness = 1 - (float) (mouseY - pickerStartY) / (float) pickerHeight;
            saturation = Math.max(0, Math.min(1, saturation));
            brightness = Math.max(0, Math.min(1, brightness));
            setting.setColor(Color.getHSBColor(hsbValues[0], saturation, brightness));
        } else if (type.equals("Alpha")) {
            pickerStartX += pickerSize + 10 + 2 * SLIDER_GAP;
            float alpha = (float) (mouseY - pickerStartY) / (float) pickerHeight;
            alpha = Math.max(0, Math.min(1, alpha));
            Color currentColor = setting.getColor();
            setting.setColor(new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), (int) (alpha * 255)));
        }
    }

}
