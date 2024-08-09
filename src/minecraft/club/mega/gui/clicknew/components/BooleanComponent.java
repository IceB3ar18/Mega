package club.mega.gui.clicknew.components;

import club.mega.Mega;
import club.mega.gui.clicknew.SettingComponent;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.awt.*;

public class BooleanComponent extends SettingComponent {

    private final BooleanSetting booleanSetting;
    private final FontRenderer fontRenderer;

    public BooleanComponent(BooleanSetting booleanSetting, double x, double y, double width, double height) {
        super(x, y, width, height);
        this.booleanSetting = booleanSetting;
        this.fontRenderer = Minecraft.getMinecraft().fontRendererObj;
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        Mega.INSTANCE.getFontManager().getFont("Arial 18").drawStringWithShadow(
                booleanSetting.getName() + ": ", (int) x, (int) (y + height / 2 - 5), new Color(199, 199, 199, 255)
        );

        int textColor = booleanSetting.get() ? new Color(0, 255, 0).getRGB() : new Color(255, 0, 0).getRGB();
        Mega.INSTANCE.getFontManager().getFont("Arial 18").drawStringWithShadow(
                booleanSetting.get() ? "true" : "false",
                (int) (x + Mega.INSTANCE.getFontManager().getFont("Arial 18").getWidth(booleanSetting.getName() + ": ")),
                (int) (y + height / 2 - 5), textColor
        );
    }

    @Override
    public void handleMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && isMouseHovering(mouseX, mouseY)) {
            booleanSetting.set(!booleanSetting.get());
        }
    }


    @Override
    public void handleMouseRelease(int mouseX, int mouseY, int state) {
        // This component does not handle mouse release events directly
    }

    @Override
    public void handleKeyPress(char typedChar, int keyCode) {

    }

    private boolean isMouseHovering(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}