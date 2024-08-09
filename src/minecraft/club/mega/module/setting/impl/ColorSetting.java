package club.mega.module.setting.impl;

import club.mega.module.Module;
import club.mega.module.setting.Setting;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.awt.*;
import java.util.function.Supplier;

public class ColorSetting extends Setting {
    @Expose
    @SerializedName("Red")
    private int red;
    @Expose
    @SerializedName("Green")
    private int green;
    @Expose
    @SerializedName("Blue")
    private int blue;
    @Expose
    @SerializedName("Alpha")
    private int alpha;
    private Color color;

    public ColorSetting(final String name, final Module parent, final Color color, final boolean configurable, final Supplier<Boolean> visible) {
        super(name, parent, configurable, visible);
        this.color = color;
        this.red = this.color.getRed();
        this.blue = this.color.getBlue();
        this.green = this.color.getGreen();
        this.alpha = this.color.getAlpha();
    }

    public ColorSetting(final String name, final Module parent, final Color color, final Supplier<Boolean> visible) {
        this(name, parent, color, true, visible);
    }

    public ColorSetting(final String name, final Module parent, final Color color, final boolean configurable) {
        this(name, parent, color, configurable, () -> true);
    }

    public ColorSetting(final String name, final Module parent, final Color color) {
        this(name, parent, color, true);
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
        this.red = this.color.getRed();
        this.blue = this.color.getBlue();
        this.green = this.color.getGreen();
        this.alpha = this.color.getAlpha();
    }
    public void setAlpha(int alpha) {
        this.alpha = Math.max(0, Math.min(255, alpha));
        this.color = new Color(red, green, blue, this.alpha);
    }

}
