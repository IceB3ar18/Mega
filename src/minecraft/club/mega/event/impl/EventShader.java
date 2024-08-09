package club.mega.event.impl;

import club.mega.event.Event;

public class EventShader extends Event {

    public final boolean bloom;
    public EventShader(boolean bloom){
        this.bloom = bloom;
    }
}
