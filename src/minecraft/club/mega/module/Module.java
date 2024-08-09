package club.mega.module;

import club.mega.Mega;
import club.mega.event.impl.EventEarlyTick;
import club.mega.event.impl.EventPreTick;
import club.mega.interfaces.MinecraftInterface;
import club.mega.module.impl.hud.ModuleList;
import club.mega.module.impl.hud.Sounds;
import club.mega.module.setting.Setting;
import club.mega.util.ChatUtil;
import club.mega.util.RenderUtil;
import club.mega.util.SoundUtil;
import rip.hippo.lwjeb.annotation.Handler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

public class Module implements MinecraftInterface {

    private final ModuleInfo moduleInfo = getClass().getAnnotation(ModuleInfo.class);
    private final String name = moduleInfo.name(), description = moduleInfo.description();
    private final Category category = moduleInfo.category();
    private boolean toggled;
    private int key;
    private String tag;
    private double currentWidth, currentHeight, targetHeight, targetWidth;

    private final ArrayList<Setting> settings = new ArrayList<>();

    private boolean expand;

    public void onEnable() {
        Mega.INSTANCE.getPubSub().subscribe(this);
        if (MC.theWorld != null) {
            if(!this.name.equalsIgnoreCase("Clickgui")) {
                if (Mega.INSTANCE.getModuleManager().isToggled(Sounds.class)) {
                    String module = Mega.INSTANCE.getModuleManager().getModule(Sounds.class).mode.getCurrent();
                    switch (module) {
                        case "Sigma":
                            SoundUtil.playSound("toggleSound.wav");
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }


    public void onDisable() {
        Mega.INSTANCE.getPubSub().unsubscribe(this);
        if(!this.name.equalsIgnoreCase("Clickgui")) {
            if (Mega.INSTANCE.getModuleManager().isToggled(Sounds.class)) {
                String module = Mega.INSTANCE.getModuleManager().getModule(Sounds.class).mode.getCurrent();
                switch (module) {
                    case "Sigma":
                        SoundUtil.playSound("toggleSound2.wav");
                        break;
                    default:
                        break;

                }
            }
        }
    }

    public final void toggle() {
        toggled = !toggled;
        if (toggled)
            onEnable();
        else
            onDisable();
    }

    public final boolean isToggled() {
        return toggled;
    }

    public final void setToggled(final boolean toggled) {
        if (this.toggled != toggled)
            toggle();
    }


    public final String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public final String getDescription() {
        return description;
    }

    public final Category getCategory() {
        return category;
    }

    public final ArrayList<Setting> getSettings() {
        return settings;
    }

    public final Setting getSetting(final String name) {
        return settings.stream().filter(setting -> setting.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public final int getKey() {
        return key;
    }

    public final void setKey(final int key) {
        this.key = key;
    }

    public final double getCurrentWidth() {
        return currentWidth;
    }

    public final double getCurrentHeight() {
        return currentHeight;
    }

    public final void setCurrentWidth(final double currentWidth) {
        this.currentWidth = currentWidth;
    }

    public final void setCurrentHeight(final double current) {
        this.currentHeight = current;
    }

    public final double getTargetHeight() {
        return targetHeight;
    }

    public final double getTargetWidth() {
        return targetWidth;
    }

    public final void setTargetHeight(final double targetHeight) {
        this.targetHeight = targetHeight;
    }

    public final void setTargetWidth(final double target) {
        this.targetWidth = target;
    }

    public boolean isExpanded() {
        return expand;
    }

    public void setExpanded(boolean expand) {
        this.expand = expand;
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ModuleInfo {

        String name();
        String description();
        Category category();

    }

}
