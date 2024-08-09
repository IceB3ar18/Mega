package club.mega.module.setting.impl;

import club.mega.module.Module;
import club.mega.module.setting.Setting;

import java.util.function.Supplier;

public class RangeSetting extends Setting {

    private final double min, max, increment;
    private double currentMin, currentMax;

    public RangeSetting(final String name, final Module parent, final double min, final double max, final double currentMin, final double currentMax, final double increment, final boolean configurable, final Supplier<Boolean> visible) {
        super(name, parent, configurable, visible);
        this.min = min;
        this.max = max;
        this.currentMin = currentMin;
        this.currentMax = currentMax;
        this.increment = increment;
    }

    public RangeSetting(final String name, final Module parent, final double min, final double max, final double currentMin, final double currentMax, final double increment, final Supplier<Boolean> visible) {
        this(name, parent, min, max, currentMin, currentMax, increment, true, visible);
    }

    public RangeSetting(final String name, final Module parent, final double min, final double max, final double currentMin, final double currentMax, final double increment, final boolean configurable) {
        this(name, parent, min, max, currentMin, currentMax, increment, configurable, () -> true);
    }

    public RangeSetting(final String name, final Module parent, final double min, final double max, final double currentMin, final double currentMax, final double increment) {
        this(name, parent, min, max, currentMin, currentMax, increment, true, () -> true);
    }

    public final double getMin() {
        return min;
    }

    public final double getMax() {
        return max;
    }

    public final double getCurrentMin() {
        return currentMin;
    }

    public final double getCurrentMax() {
        return currentMax;
    }

    public final void setCurrentMin(final double currentMin) {
        this.currentMin = currentMin;
    }

    public final void setCurrentMax(final double currentMax) {
        this.currentMax = currentMax;
    }

    public final double getIncrement() {
        return increment;
    }
}
