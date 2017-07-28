package checkers.Game;

import org.jsfml.system.Vector2i;


public class CaptureMove extends Move
{
    public final Vector2i capturedPos;
    
    CaptureMove(Vector2i from, Vector2i to)
    {
        super(from, to);
        
        capturedPos = new Vector2i((from.x + to.x)/2, (from.y + to.y)/2);
    }
    
    @Override
    public boolean isCaptureMove()
    {
        return true;
    }
    
    @Override
    public CaptureMove asCaptureMove() 
    {
        return this;
    }
}
