package club.mega.gui.click.components;

import club.mega.Mega;
import club.mega.gui.click.Component;
import club.mega.module.impl.hud.ClickGui;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.util.*;
import club.mega.util.animation.AnimationUtil;
import net.minecraft.util.MathHelper;

import java.awt.*;

public class NumberComponent extends Component {

    private final NumberSetting setting;
    private boolean dragging = false;
    private double current;

    public NumberComponent(NumberSetting setting, double width, double height) {
        super(setting, width, height);
        this.setting = setting;
    }

    @Override
    public void drawComponent(final int mouseX, final int mouseY) {
        super.drawComponent(mouseX, mouseY);
        int l = (int) width;
        double diff = Math.min(l, Math.max(0, mouseX - (x + 1)));
        if (dragging) {
            if (diff == 0) {
                setting.setCurrent(setting.getMin());
            } else {
                double newValue = MathUtil.round(((diff / l) * (setting.getMax() - setting.getMin()) + setting.getMin()), 2, setting.getIncrement());
                setting.setCurrent(MathHelper.clamp_double(newValue, setting.getMin(), setting.getMax()));
            }
        }
        current = AnimationUtil.animate(current, ((setting.getAsDouble() - setting.getMin()) / (setting.getMax() - setting.getMin())) * l, 1);
        Mega.INSTANCE.getFontManager().getFont("Arial 18").drawString(setting.getName() + ":", x + 5, y + 4, -1);
        Mega.INSTANCE.getFontManager().getFont("Arial 18").drawString(String.valueOf(setting.getAsDouble()), x + width - Mega.INSTANCE.getFontManager().getFont("Arial 18").getWidth(String.valueOf(setting.getAsDouble())) - 5, y + 4, isInside(mouseX, mouseY) ? Color.white.darker().getRGB() : Color.white.getRGB());
        RenderUtil.drawRect(x + 1, y + 15, width - 2, 1, new Color(50,50,50));
        RenderUtil.drawRect(x + 1, y + 15, current - 2, 1, ClickGui.getInstance().color.getColor());
        RenderUtil.drawFullCircle(x + current, y + 16 - 0.5, 3, isInside(mouseX, mouseY) ? ClickGui.getInstance().color.getColor().darker().getRGB() : ClickGui.getInstance().color.getColor().getRGB());
    }

    @Override
    public void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (isInside(mouseX, mouseY) && mouseButton == 0)
            dragging = true;
    }

    @Override
    public void mouseReleased(final int mouseX, final int mouseY, final int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        if (mouseButton == 0)
            dragging = false;
    }

}
