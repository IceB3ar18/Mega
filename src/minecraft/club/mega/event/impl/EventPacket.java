package club.mega.event.impl;

import club.mega.event.Event;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;

public class EventPacket extends Event {

    private Packet packet;
    private INetHandler iNetHandler;
    private final Type type;

    public enum Type {
        SEND, RECEIVE;
    }

    public EventPacket(Type type, Packet<?> packet, INetHandler iNetHandler) {
        this.type = type;
        this.packet = packet;
        this.iNetHandler = iNetHandler;
    }

    public Type getType() {
        return type;
    }

    public final Packet getPacket() {
        return packet;
    }
    public INetHandler getINetHandler() {
        return iNetHandler;
    }

    public void setINetHandler(INetHandler iNetHandler) {
        this.iNetHandler = iNetHandler;
    }
    public final void setPacket(final Packet packet) {
        this.packet = packet;
    }

}
