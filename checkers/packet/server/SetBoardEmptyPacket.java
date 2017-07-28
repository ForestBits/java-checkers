package checkers.packet.server;

import checkers.packet.Packet;
import org.jsfml.system.Vector2i;


public class SetBoardEmptyPacket implements Packet
{
    public Vector2i pos;
    
    public SetBoardEmptyPacket(Vector2i pos)
    {
        this.pos = pos;
    }
}
