package club.mega.module.impl.visual;

import club.mega.event.impl.EventBlur;
import club.mega.event.impl.EventRender2D;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.impl.player.Scaffold;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ColorSetting;
import club.mega.module.setting.impl.ListSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.util.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import rip.hippo.lwjeb.annotation.Handler;

import java.awt.*;

@Module.ModuleInfo(name = "Hotbar", description = "Hotbar", category = Category.HUD)
public class Hotbar extends Module {

    public final BooleanSetting blur = new BooleanSetting("Blur", this, true);
    public final BooleanSetting rounded = new BooleanSetting("Rounded", this, true);
    public final NumberSetting radius = new NumberSetting("Radius", this, 1, 10, 5, 1, rounded::get);
    public final ListSetting mode = new ListSetting("Color Mode", this, new String[]{"Normal", "Fade"});
    public final NumberSetting speed = new NumberSetting("Fade Speed", this, 1, 50, 15, 0.1, () -> mode.is("Fade"));
    public final ColorSetting colornormal = new ColorSetting("Color", this, new Color(0, 0, 0, 173), () -> mode.is("Normal"));
    public final BooleanSetting thirdColor = new BooleanSetting("Third Color", this, true, () -> mode.is("Fade"));
    public final ColorSetting color1 = new ColorSetting("Start Color", this, new Color(0, 187, 255, 73), () -> mode.is("Fade"));
    public final ColorSetting color2 = new ColorSetting("Mid Color", this, new Color(0, 234, 255, 73), () -> mode.is("Fade") && thirdColor.get());
    public final ColorSetting color3 = new ColorSetting("End Color", this, new Color(0, 255, 196, 73), () -> mode.is("Fade"));
    public final ColorSetting selection = new ColorSetting("SelectionColor", this, new Color(255, 255, 255, 90));

    @Handler
    public final void onBlur(EventBlur event) {
        if (blur.get()) {
            ScaledResolution sr = new ScaledResolution(MC);
            int i = sr.getScaledWidth() / 2;
            double x = i - 91;
            double y = sr.getScaledHeight() - 22;
            double width = 182;
            double height = 22;

            if (!rounded.get()) {
                RenderUtil.drawRect(x, y, width, height, -1);
            } else {
                RenderUtil.drawRoundedRect(x, y, width, height, radius.getAsFloat(), Color.WHITE);
            }
        }
    }

}
