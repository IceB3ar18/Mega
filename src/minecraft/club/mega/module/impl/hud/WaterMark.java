package club.mega.module.impl.hud;

import club.mega.Mega;
import club.mega.event.impl.EventRender2D;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.TextSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import rip.hippo.lwjeb.annotation.Handler;

import java.awt.*;

@Module.ModuleInfo(name = "WaterMark", description = "WaterMark", category = Category.HUD)
public class WaterMark extends Module {

    private final TextSetting text = new TextSetting("Text", this, Mega.INSTANCE.getName());

    @Handler
    public final void render2D(final EventRender2D event) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        Mega.INSTANCE.getFontManager().getFont("Impact 80").drawStringWithShadow(text.getText(), 11,6, new Color(20,20,20, 120));
        Mega.INSTANCE.getFontManager().getFont("Impact 80").drawStringWithShadow(text.getText(), 10,5, Color.white);
        Mega.INSTANCE.getFontManager().getFont("Roboto-Medium 35").drawStringWithShadow("Dev", 11 + Mega.INSTANCE.getFontManager().getFont("Impact 80").getWidth("MEGA"),6, new Color(2, 0, 0));
        Mega.INSTANCE.getFontManager().getFont("Roboto-Medium 35").drawStringWithShadow("Dev", 10 + Mega.INSTANCE.getFontManager().getFont("Impact 80").getWidth("MEGA"),5, new Color(1, 225, 255));
        }

}
