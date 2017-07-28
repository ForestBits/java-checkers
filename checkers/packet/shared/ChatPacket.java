package checkers.packet.shared;

import checkers.packet.Packet;


public class ChatPacket implements Packet
{
    public String message;
    
    public ChatPacket(String message)
    {
        this.message = message;
    }
}
