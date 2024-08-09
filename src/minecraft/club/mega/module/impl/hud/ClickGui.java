package club.mega.module.impl.hud;

import club.mega.Mega;
import club.mega.gui.click.ConfigGui;
import club.mega.gui.click.Panel;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ColorSetting;
import club.mega.module.setting.impl.ListSetting;
import club.mega.module.setting.impl.NumberSetting;
import org.lwjgl.input.Keyboard;

import java.awt.*;

@Module.ModuleInfo(name = "ClickGui", description = "ClickGui", category = Category.HUD)
public class ClickGui extends Module {
    public final ListSetting mode = new ListSetting("Mode", this, new String[]{"Panel", "Augustus"});
    public final NumberSetting animationSpeed = new NumberSetting("Animation speed", this, 0.1, 10, 5, 0.1);
    public final ColorSetting color = new ColorSetting("Color", this, new Color(255, 0, 59));

    public final BooleanSetting blur = new BooleanSetting("Blur", this, false, mode.is("Panel"));
    public final BooleanSetting debug = new BooleanSetting("Debug", this, false, mode.is("Panel"));

    public static ClickGui getInstance() {
        return Mega.INSTANCE.getModuleManager().getModule(ClickGui.class);
    }
    public ClickGui() {
        setKey(Keyboard.KEY_RSHIFT);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        switch (mode.getCurrent()) {
            case "Panel":
                MC.displayGuiScreen(Mega.INSTANCE.getClickGUI());
                break;
            case "Augustus":
                MC.displayGuiScreen(Mega.INSTANCE.getClickGui());
                break;
        }

    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
