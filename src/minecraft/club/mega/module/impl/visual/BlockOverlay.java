package club.mega.module.impl.visual;

import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.ColorSetting;
import club.mega.module.setting.impl.NumberSetting;

import java.awt.*;

@Module.ModuleInfo(name = "BlockOverlay", description = "Changes facing block apperiance", category = Category.VISUAL)
public class BlockOverlay extends Module {

    public final ColorSetting outlineColor = new ColorSetting("Outline", this, new Color(0, 208, 255, 119));


    public final ColorSetting color = new ColorSetting("Color", this, new Color(0, 111, 255, 73));

    public final NumberSetting outline = new NumberSetting("Outline", this, 0, 8, 4, 1);
}
