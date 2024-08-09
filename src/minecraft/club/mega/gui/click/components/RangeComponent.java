package club.mega.gui.click.components;

import club.mega.Mega;
import club.mega.gui.click.Component;
import club.mega.module.impl.hud.ClickGui;
import club.mega.module.setting.impl.RangeSetting;
import club.mega.util.*;
import club.mega.util.animation.AnimationUtil;
import net.minecraft.util.MathHelper;

import java.awt.*;

public class RangeComponent extends Component {

    private final RangeSetting setting;
    private boolean draggingMin = false;
    private boolean draggingMax = false;
    private double currentMin;
    private double currentMax;

    public RangeComponent(RangeSetting setting, double width, double height) {
        super(setting, width, 30);  // Erhöhe die Höhe um 10
        this.setting = setting;
    }

    @Override
    public void drawComponent(final int mouseX, final int mouseY) {
        // Draw the background for the extended area
        RenderUtil.drawRect(x - 10, y, width + 20, height, new Color(19, 19, 19, 255));

        int l = (int) width;
        double diffMin = Math.min(l, Math.max(0, mouseX - (x + 1)));
        double diffMax = Math.min(l, Math.max(0, mouseX - (x + 1)));

        if (draggingMin) {
            if (diffMin == 0) {
                setting.setCurrentMin(setting.getMin());
            } else {
                double newValue = MathUtil.round(((diffMin / l) * (setting.getMax() - setting.getMin()) + setting.getMin()), 2, setting.getIncrement());
                setting.setCurrentMin(MathHelper.clamp_double(newValue, setting.getMin(), setting.getMax()));
            }
        }

        if (draggingMax) {
            if (diffMax == 0) {
                setting.setCurrentMax(setting.getMin());
            } else {
                double newValue = MathUtil.round(((diffMax / l) * (setting.getMax() - setting.getMin()) + setting.getMin()), 2, setting.getIncrement());
                setting.setCurrentMax(MathHelper.clamp_double(newValue, setting.getMin(), setting.getMax()));
            }
        }

        currentMin = AnimationUtil.animate(currentMin, ((setting.getCurrentMin() - setting.getMin()) / (setting.getMax() - setting.getMin())) * l, 1);
        currentMax = AnimationUtil.animate(currentMax, ((setting.getCurrentMax() - setting.getMin()) / (setting.getMax() - setting.getMin())) * l, 1);

        // Draw the name of the setting
        Mega.INSTANCE.getFontManager().getFont("Arial 18").drawString(setting.getName() + ":", x + 5, y + 4, -1);

        // Draw the current values slightly lower
        Mega.INSTANCE.getFontManager().getFont("Arial 16").drawString(String.valueOf(setting.getCurrentMin()), (float) (x + currentMin - Mega.INSTANCE.getFontManager().getFont("Arial 16").getWidth(String.valueOf(setting.getCurrentMin())) / 2), y + 22, isInside(mouseX, mouseY) ? Color.white.darker().getRGB() : Color.white.getRGB());
        Mega.INSTANCE.getFontManager().getFont("Arial 16").drawString(String.valueOf(setting.getCurrentMax()), (float) (x + currentMax - Mega.INSTANCE.getFontManager().getFont("Arial 15").getWidth(String.valueOf(setting.getCurrentMax())) / 2), y + 22, isInside(mouseX, mouseY) ? Color.white.darker().getRGB() : Color.white.getRGB());

        // Move the slider slightly lower
        RenderUtil.drawRect(x + 1, y + 17, width - 2, 1, new Color(50, 50, 50));
        RenderUtil.drawRect(x + currentMin, y + 17, currentMax - currentMin, 1, ClickGui.getInstance().color.getColor());
        RenderUtil.drawFullCircle(x + currentMin, y + 18 - 0.5, 3, isInside(mouseX, mouseY) ? ClickGui.getInstance().color.getColor().darker().getRGB() : ClickGui.getInstance().color.getColor().getRGB());
        RenderUtil.drawFullCircle(x + currentMax, y + 18 - 0.5, 3, isInside(mouseX, mouseY) ? ClickGui.getInstance().color.getColor().darker().getRGB() : ClickGui.getInstance().color.getColor().getRGB());
    }

    @Override
    public void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (isInside(mouseX, mouseY) && mouseButton == 0) {
            double diffMin = Math.abs(mouseX - (x + currentMin));
            double diffMax = Math.abs(mouseX - (x + currentMax));
            if (diffMin < diffMax) {
                draggingMin = true;
            } else {
                draggingMax = true;
            }
        }
    }

    @Override
    public void mouseReleased(final int mouseX, final int mouseY, final int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            draggingMin = false;
            draggingMax = false;

            // Überprüfe und tausche min und max, falls notwendig
            if (setting.getCurrentMin() > setting.getCurrentMax()) {
                double temp = setting.getCurrentMin();
                setting.setCurrentMin(setting.getCurrentMax());
                setting.setCurrentMax(temp);
            }
        }
    }
}
