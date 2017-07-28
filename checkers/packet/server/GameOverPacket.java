package checkers.packet.server;

import checkers.Game.PieceColor;
import checkers.packet.Packet;


public class GameOverPacket implements Packet
{
    public PieceColor color;
    
    public GameOverPacket(PieceColor color)
    {
        this.color = color;
    }
}
