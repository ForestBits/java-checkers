package checkers.packet.client;

import checkers.packet.Packet;
import checkers.server.GameOption;


public class GameOptionPacket implements Packet
{
    public GameOption option;
    
    public GameOptionPacket(GameOption option)
    {
        this.option = option;
    }
}
