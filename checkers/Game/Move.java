package checkers.Game;

import org.jsfml.system.Vector2i;


public class Move 
{
    public final Vector2i from;
    public final Vector2i to;
    
    public Move(Vector2i from, Vector2i to)
    {
        this.from = from;
        this.to = to;
    }
    
    public boolean isCaptureMove()
    {
        return false;
    }
    
    public CaptureMove asCaptureMove()
    {
        return null;
    }
}
