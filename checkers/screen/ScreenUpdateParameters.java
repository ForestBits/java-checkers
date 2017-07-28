package checkers.screen;

import org.jsfml.graphics.RenderWindow;


public class ScreenUpdateParameters 
{
    public final ScreenChanger screenChanger;
    
    public final RenderWindow window;
    
    public TransitionInfo transition;
    
    public final ScreenConstructionParameters screenConstructionParameters;
    
    public ScreenUpdateParameters(ScreenChanger screenChanger, RenderWindow window, ScreenConstructionParameters parameters)
    {
        this.screenChanger = screenChanger;
        this.window = window;
        this.screenConstructionParameters = parameters;
    }
}
