package club.mega.event.impl;

import club.mega.event.Event;

public class EventServerESP extends Event{

    public float partialTicks;

    public EventServerESP(final float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }
}
