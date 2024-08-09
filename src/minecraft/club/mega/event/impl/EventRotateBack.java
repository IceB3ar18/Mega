package club.mega.event.impl;

import club.mega.event.Event;

public class EventRotateBack extends Event {
    public boolean shouldRotateBack = false;

    public boolean getShouldRotateBack() {
        return shouldRotateBack;
    }

    public void setShouldRotateBack(boolean shouldRotateBack) {
        this.shouldRotateBack = shouldRotateBack;
    }

}
