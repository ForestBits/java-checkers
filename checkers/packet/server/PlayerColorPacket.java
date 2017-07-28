package checkers.packet.server;

import checkers.Game.PieceColor;
import checkers.packet.Packet;


public class PlayerColorPacket implements Packet
{
    public PieceColor color;
    
    public PlayerColorPacket(PieceColor color)
    {
        this.color = color;
    }
}
