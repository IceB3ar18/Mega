package club.mega.event.impl;

import club.mega.event.Event;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class EventPostRender extends Event {

    private final ScaledResolution sr;

    private final float partialTicks;

    public EventPostRender(float partialTicks) {
        this.partialTicks = partialTicks;
        sr = new ScaledResolution(Minecraft.getMinecraft());
    }

    public ScaledResolution getSr() {
        return sr;
    }

    public float getPartialTicks() {
        return partialTicks;
    }


}
