package club.mega.event.impl;


import club.mega.event.Event;

public class EventRender extends Event {
    private float partialTicks;

    public float getPartialTicks() {
        return this.partialTicks;
    }

    public EventRender(float partialTicks) {
        this.partialTicks = partialTicks;
    }
}
