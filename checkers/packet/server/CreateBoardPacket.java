package checkers.packet.server;

import checkers.packet.Packet;
import org.jsfml.system.Vector2i;


public class CreateBoardPacket implements Packet
{
    public Vector2i size;
    
    public byte whiteRows;
    public byte blackRows;
    
    public CreateBoardPacket(Vector2i size, int whiteRows, int blackRows)
    {
        this.size = size;
        this.whiteRows = (byte) whiteRows;
        this.blackRows = (byte) blackRows;
    }
}
