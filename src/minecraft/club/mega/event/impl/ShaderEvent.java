package club.mega.event.impl;


import club.mega.event.Event;

public class ShaderEvent extends Event {

    public final boolean bloom;
    public ShaderEvent(boolean bloom){
        this.bloom = bloom;
    }
}
