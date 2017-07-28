package checkers.packet.client;

import checkers.Game.Move;
import checkers.packet.Packet;


public class CaptureMovePacket implements Packet
{
    public Move move;
    
    public boolean multiCapture;
    
    public CaptureMovePacket(Move move, boolean multiCapture)
    {
        this.move = move;
        this.multiCapture = multiCapture;
    }
}
