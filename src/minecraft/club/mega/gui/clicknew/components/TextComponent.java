package club.mega.gui.clicknew.components;

import club.mega.Mega;
import club.mega.gui.clicknew.SettingComponent;
import club.mega.module.setting.impl.TextSetting;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class TextComponent extends SettingComponent {

    private final TextSetting setting;
    private boolean editingText = false;

    public TextComponent(TextSetting setting, double x, double y, double width, double height) {
        super(x, y, width, height);
        this.setting = setting;
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        String displayText = setting.getText();
        if (editingText) {
            displayText = setting.getRawText();
        }

        // Draw the background of the text box
        Gui.drawRect(x, y, x + width, y + height, new Color(0, 0, 0, 150).getRGB());

        // Draw the border of the text box
        Gui.drawRect(x - 1, y - 1, x + width + 1, y + height + 1, new Color(255, 255, 255, 255).getRGB());

        // Draw the text inside the box
        Mega.INSTANCE.getFontManager().getFont("Arial 18").drawStringWithShadow(displayText, (int) (x + 5), (int) (y + (height / 2) - 4), new Color(255, 255, 255, 255));
    }

    @Override
    public void handleMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && isHovered(mouseX, mouseY)) {
            editingText = !editingText;
        }
    }

    @Override
    public void handleMouseRelease(int mouseX, int mouseY, int mouseButton) {
        // No action needed on mouse release
    }

    @Override
    public void handleMouseDrag(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        // No action needed on mouse drag
    }

    @Override
    public void handleKeyPress(char typedChar, int keyCode) {
        if (editingText) {
            if (keyCode == Keyboard.KEY_RETURN) {
                editingText = false;
            } else if (keyCode == Keyboard.KEY_BACK) {
                setting.remove();
            } else {
                setting.addChar(typedChar);
            }
        }
    }

    private boolean isHovered(int mouseX, int mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }
}
