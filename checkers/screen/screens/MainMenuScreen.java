package checkers.screen.screens;

import checkers.CheckerBoardDrawer;
import checkers.input.inputs.Button;
import checkers.screen.QuitException;
import checkers.screen.Screen;
import checkers.screen.ScreenConstructionParameters;
import checkers.screen.ScreenUpdateParameters;
import checkers.screen.TransitionInfo;
import checkers.utility.EventQueue;
import org.jsfml.graphics.Color;
import org.jsfml.graphics.FloatRect;
import org.jsfml.graphics.RenderTarget;


public class MainMenuScreen extends Screen
{   
    private final Button playButton;
    private final Button quitButton;
    
    public MainMenuScreen(ScreenConstructionParameters params)
    {
        playButton = new Button(params.textures.get("button"), params.font, "Play", params.window.getSize().x/2, params.window.getSize().y/5, true, 
        () -> params.screenChanger.setScreen(new ConnectScreen(params)));
        
        quitButton = new Button(params.textures.get("button"), params.font, "Quit", params.window.getSize().x/2, params.window.getSize().y/5 + 100, true, 
        () -> {throw new QuitException();});
    }
    
    @Override
    public void update(ScreenUpdateParameters params)
    {
        EventQueue events = new EventQueue(params.window);
        
        playButton.update(events);
        quitButton.update(events);
    }
    
    @Override
    public void draw(RenderTarget target, TransitionInfo transition)
    {
        CheckerBoardDrawer.draw(target, new FloatRect(0, 0, target.getSize().x, target.getSize().y), Color.BLUE, Color.WHITE, 0.2);
        
        target.draw(playButton);
        target.draw(quitButton);
    }
}
