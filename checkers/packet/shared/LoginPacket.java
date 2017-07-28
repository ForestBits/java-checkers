package checkers.packet.shared;

import checkers.packet.Packet;


public class LoginPacket implements Packet
{
    public String playerName;
    
    public LoginPacket(String playerName)
    {
        this.playerName = playerName;
    }
}
