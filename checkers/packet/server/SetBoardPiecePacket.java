package checkers.packet.server;

import checkers.Game.Piece;
import checkers.Game.PieceColor;
import checkers.packet.Packet;
import org.jsfml.system.Vector2i;


public class SetBoardPiecePacket implements Packet
{
    public Vector2i pos;
    
    public Piece piece;
    
    public SetBoardPiecePacket(Vector2i pos, Piece piece)
    {
        this.pos = pos;
        this.piece = piece;
    }
}
