package club.mega.gui.click.components;

import club.mega.Mega;
import club.mega.gui.click.Component;
import club.mega.module.Module;
import club.mega.module.impl.hud.ClickGui;
import club.mega.module.impl.hud.ModuleList;
import club.mega.module.setting.impl.ListSetting;
import club.mega.util.ChatUtil;
import club.mega.util.RenderUtil;

import java.awt.*;

public class ListComponent extends Component {

    private final ListSetting setting;

    public ListComponent(ListSetting setting, double width, double height) {
        super(setting, width, height);
        this.setting = setting;
    }

    @Override
    public void drawComponent(final int mouseX, final int mouseY) {
        super.drawComponent(mouseX, mouseY);
        double x1 = x + width - Mega.INSTANCE.getFontManager().getFont("Arial 19").getWidth(setting.getCurrent()) - 5 - 2;
        double y1 = y + height / 4 - 1;
        double width1 = Mega.INSTANCE.getFontManager().getFont("Arial 19").getWidth(setting.getCurrent()) + 2;
        double height1 = height / 2 + 2;
        RenderUtil.drawRoundedRect(x1 , y1, width1, height1, 2, new Color(54, 54, 54));

        Mega.INSTANCE.getFontManager().getFont("Arial 19").drawString(setting.getName() + ":", x + 5, y + 5, -1);
        Mega.INSTANCE.getFontManager().getFont("Arial 19").drawString(setting.getCurrent(), x + width - Mega.INSTANCE.getFontManager().getFont("Arial 19").getWidth(setting.getCurrent()) - 5, y + 5, isInside(mouseX, mouseY) ? Color.white.darker() : Color.white);
    }

    @Override
    public void mouseReleased(final int mouseX, final int mouseY, final int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        if (isInside(mouseX, mouseY)) {

            if (mouseButton == 0)
                setting.loopNext();
            if (mouseButton == 1)
                setting.loopPrev();

        }
    }

}
