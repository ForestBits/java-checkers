package checkers.packet.client;

import checkers.Game.CaptureMove;
import checkers.Game.Move;
import checkers.packet.Packet;


public class NormalMovePacket implements Packet
{
    public Move move;
    
    public NormalMovePacket(Move move)
    {
        this.move = move;
        
        if (move instanceof CaptureMove)
            throw new RuntimeException("Must pass capture move in its own packet");
    }
}
