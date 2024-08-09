package club.mega.gui.click.components;

import club.mega.Mega;
import club.mega.gui.click.Component;
import club.mega.module.setting.impl.ColorSetting;
import club.mega.util.ColorUtil;
import club.mega.util.RenderUtil;
import net.minecraft.client.Minecraft;

import java.awt.*;

public class ColorComponent extends Component {

    private final ColorSetting setting;

    public ColorComponent(ColorSetting setting, double width, double height) {
        super(setting, width, height);
        this.setting = setting;
    }
    @Override
    public void drawComponent(final int mouseX, final int mouseY) {
        super.drawComponent(mouseX, mouseY);
        if (!isInside(mouseX, mouseY)) {
            RenderUtil.drawRoundedRect(x + width / 1.5 - 1, y + height / 4 - 1, width / 3.5 + 2, height / 2 + 2, 2, setting.getColor().darker());
            RenderUtil.drawRoundedRect(x + width / 1.5, y + height / 4, width / 3.5, height / 2, 2, setting.getColor());
        } else {
            RenderUtil.drawRoundedRect(x + width / 1.5 - 1, y + height / 4 - 1, width / 3.5 + 2, height / 2 + 2, 2, setting.getColor().darker().darker());
            RenderUtil.drawRoundedRect(x + width / 1.5, y + height / 4, width / 3.5, height / 2, 2, setting.getColor().darker());
        }
        Mega.INSTANCE.getFontManager().getFont("Arial 19").drawString(setting.getName(), x + 5, y + 5, -1);


    }

    @Override
    public void mouseReleased(final int mouseX, final int mouseY, final int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        if (isInside(mouseX, mouseY)) {
            ColorPickerGUI colorPickerGUI = new ColorPickerGUI(setting);
            Minecraft.getMinecraft().displayGuiScreen(colorPickerGUI);
        }
    }

}