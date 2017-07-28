package checkers.screen;

import org.jsfml.graphics.RenderTarget;


public abstract class Screen 
{
    public abstract void update(ScreenUpdateParameters params);
    
    public abstract void draw(RenderTarget target, TransitionInfo transition);
    
    public double getTransitionInTime() {return 0;}
    public double getTransitionOutTime() {return 0;}
}
