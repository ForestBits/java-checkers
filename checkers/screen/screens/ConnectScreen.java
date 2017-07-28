package checkers.screen.screens;

import checkers.CheckerBoardDrawer;
import checkers.input.inputs.Button;
import checkers.input.inputs.TextBox;
import checkers.screen.Screen;
import checkers.screen.ScreenConstructionParameters;
import checkers.screen.ScreenUpdateParameters;
import checkers.screen.TransitionInfo;
import checkers.utility.EventQueue;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.jsfml.graphics.Color;
import org.jsfml.graphics.FloatRect;
import org.jsfml.graphics.RenderTarget;
import org.jsfml.system.Vector2f;


public class ConnectScreen extends Screen
{
    private final TextBox addressTextBox;
    private final TextBox nameTextBox;
    
    private final Button backButton;
    private final Button connectButton;
    
    public ConnectScreen(ScreenConstructionParameters params)
    {
        addressTextBox = new TextBox("IP", new Vector2f(100, 100), 30, params.font, Color.BLACK, 
                null);
        
        nameTextBox = new TextBox("Name", new Vector2f(100, 200), 10, params.font, Color.BLACK,
                null);
        
        backButton = new Button(params.textures.get("button"), params.font, "Back", new Vector2f(params.window.getSize().x/2, params.window.getSize().y/2), true, 
        () -> params.screenChanger.setScreen(new MainMenuScreen(params)));
        
        connectButton = new Button(params.textures.get("button"), params.font, "Connect", new Vector2f(params.window.getSize().x/2, params.window.getSize().y*(3/5)), true, 
        () -> 
        {
            try
            {
                params.screenChanger.setScreen(new GameScreen(params, InetAddress.getByName(addressTextBox.getContents()), nameTextBox.getContents()));
            }
            
            catch (UnknownHostException ex)
            {
                throw new RuntimeException(ex);
            }
        });
    }
    
    @Override
    public void update(ScreenUpdateParameters params)
    {
        EventQueue events = new EventQueue(params.window);
        
        addressTextBox.update(events);
        nameTextBox.update(events);
        backButton.update(events);
        connectButton.update(events);
    }
    
    @Override
    public void draw(RenderTarget target, TransitionInfo transition)
    {
        CheckerBoardDrawer.draw(target, new FloatRect(0, 0, target.getSize().x, target.getSize().y), Color.GREEN, Color.WHITE, 0.2);
        
        target.draw(addressTextBox);
        target.draw(nameTextBox);
        target.draw(backButton);
        target.draw(connectButton);
    }
}
