package club.mega.event.impl;

import club.mega.event.Event;

public class EventClickMouse extends Event {
    private boolean shouldRightClick;
    private int slot;

    public EventClickMouse(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return this.slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public boolean isShouldRightClick() {
        return this.shouldRightClick;
    }

    public void setShouldRightClick(boolean shouldRightClick) {
        this.shouldRightClick = shouldRightClick;
    }
}
