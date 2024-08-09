package club.mega.event.impl;

import club.mega.event.Event;

public class EventNametags extends Event {

    String name;

    public String getName() {
        return name;
    }

    public EventNametags(String name) {
        this.name = name;
    }
}
