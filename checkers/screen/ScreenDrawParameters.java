package checkers.screen;

import org.jsfml.graphics.RenderWindow;


public class ScreenDrawParameters 
{
    public final RenderWindow window;
    
    public TransitionInfo transition;
    
    public ScreenDrawParameters(RenderWindow window)
    {
        this.window = window;
    }
}
