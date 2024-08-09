package club.mega.module.impl.hud;

import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.ListSetting;

@Module.ModuleInfo(name = "Sounds", description = "Toggle sounds", category = Category.HUD)
public class Sounds extends Module {
    public final ListSetting mode = new ListSetting("Mode", this, new String[]{"Sigma"});
}
