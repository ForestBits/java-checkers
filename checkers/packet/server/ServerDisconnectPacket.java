package checkers.packet.server;

import checkers.packet.Packet;


public class ServerDisconnectPacket implements Packet
{
    public String reason;
    
    public ServerDisconnectPacket(String reason)
    {
        this.reason = reason;
    }
}
