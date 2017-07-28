package checkers.screen.screens;

import checkers.CheckerBoardDrawer;
import checkers.input.inputs.Button;
import checkers.screen.Screen;
import checkers.screen.ScreenConstructionParameters;
import checkers.screen.ScreenUpdateParameters;
import checkers.screen.TransitionInfo;
import checkers.utility.EventQueue;
import org.jsfml.graphics.Color;
import org.jsfml.graphics.FloatRect;
import org.jsfml.graphics.RenderTarget;
import org.jsfml.graphics.Text;
import org.jsfml.system.Vector2f;


public class ServerDisconnectScreen extends Screen
{
    private final Text text = new Text();
    
    private final Button backButton;
    
    public ServerDisconnectScreen(ScreenConstructionParameters params, String reason)
    {
        text.setFont(params.font); 
        text.setColor(Color.BLACK);
        text.setString(reason);
        text.setOrigin(text.getGlobalBounds().width/2, text.getGlobalBounds().height/2);
        text.setPosition(params.window.getSize().x/2, params.window.getSize().y/5);
        
        backButton = new Button(params.textures.get("button"), params.font, "Menu", params.window.getSize().x/2, params.window.getSize().y*(4/5f), true, 
        () -> params.screenChanger.setScreen(new MainMenuScreen(params)));
    }
    
    @Override
    public void update(ScreenUpdateParameters params)
    {
        EventQueue events = new EventQueue(params.window);
        
        backButton.update(events);
    }
    
    @Override
    public void draw(RenderTarget target, TransitionInfo transition)
    {
        CheckerBoardDrawer.draw(target, new FloatRect(0, 0, target.getSize().x, target.getSize().y), Color.RED, Color.WHITE, 0.2);
        
        target.draw(text);
        target.draw(backButton);
    }
}
