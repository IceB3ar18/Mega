package club.mega.event.impl;

import club.mega.event.Event;

public class EventRenerChat extends Event {
    public int updateCounter;

    public EventRenerChat(int updateCounter) {
        this.updateCounter = updateCounter;
    }

    public int getUpdateCounter() {
        return updateCounter;
    }
}
