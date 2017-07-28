package checkers.screen;

import org.jsfml.system.Clock;


public class ScreenEngine 
{
    private ScreenUpdateParameters updateParameters;
    
    private Screen currentScreen;
    private Screen nextScreen;
    
    private boolean transitioningIn;
    private boolean transitioningOut;
    
    private final Clock clock = new Clock();
    
    private void assertCanRun() 
    {
        assert updateParameters != null : "Must construct engine before use";
        assert currentScreen != null : "Must set screen before use";
    }
    
    private TransitionInfo getTransitionInfo()
    {
        double transitionPercent = 0;
        
        if (transitioningIn)
        {
            if (currentScreen.getTransitionInTime() != 0)
                transitionPercent = clock.getElapsedTime().asSeconds()/currentScreen.getTransitionInTime();
            else
                transitionPercent = 1;
        }
        else if (transitioningOut)
        {
            if (currentScreen.getTransitionOutTime() != 0)
                transitionPercent = clock.getElapsedTime().asSeconds()/currentScreen.getTransitionOutTime();
            else
                transitionPercent = 1;
        }
        
        return new TransitionInfo(transitioningIn, transitioningOut, transitionPercent);
    }
    
    public ScreenEngine() {}
    
    public void construct(ScreenUpdateParameters updateParameters)
    {
        this.updateParameters = updateParameters;
    }

    public void setScreen(Screen screen) 
    {
        if (transitioningIn || transitioningOut)
            return;
        
        if (currentScreen == null)
        {
            currentScreen = screen;
            
            transitioningIn = true;
        }
        else
        {
            if (currentScreen.getTransitionOutTime() > 0 || screen.getTransitionInTime() > 0)
            {
                nextScreen = screen;

                transitioningOut = true;
            }
            else
            {
                currentScreen = screen;
            }
        }
        
        clock.restart();
    } 
    
    public void update() 
    {
        assertCanRun();
        
        TransitionInfo transition = getTransitionInfo();
        
        updateParameters.transition = transition;
        
        if (transitioningIn || transitioningOut)
        {
            if (transition.percent >= 1)
            {
                if (transitioningIn)
                {
                    transitioningIn = false;
                    
                    updateParameters.transition = new TransitionInfo(false, false, 0);
                }
                else
                {
                    currentScreen = nextScreen;
                    
                    nextScreen = null;
                    
                    transitioningOut = false;
                    
                    transitioningIn = true;
                    
                    clock.restart();
                }
            }
        }
        
        currentScreen.update(updateParameters);
    }
    
    public void draw() 
    {
        assertCanRun();
        
        currentScreen.draw(updateParameters.window, getTransitionInfo());
    }
}
