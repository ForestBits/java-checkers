package checkers.input.inputs;

import checkers.input.ActivatableElement;
import checkers.input.Input;
import checkers.utility.EventQueue;
import org.jsfml.graphics.Color;
import org.jsfml.graphics.ConstFont;
import org.jsfml.graphics.ConstTexture;
import org.jsfml.graphics.RenderStates;
import org.jsfml.graphics.RenderTarget;
import org.jsfml.graphics.Sprite;
import org.jsfml.graphics.Text;
import org.jsfml.system.Vector2f;
import org.jsfml.window.Keyboard.Key;
import org.jsfml.window.Mouse;
import org.jsfml.window.event.Event;


public class Button implements Input
{
    private final Sprite sprite = new Sprite();
    
    private final Text text = new Text();
    
    private final Runnable onClick;
    
    private boolean hover = false;
    
    public Button(ConstTexture buttonTexture, ConstFont font, String string, float x, float y, boolean centered, Runnable onClick)
    {
        this(buttonTexture, font, string, new Vector2f(x, y), centered, onClick);
    }
    
    public Button(ConstTexture buttonTexture, ConstFont font, String string, Vector2f position, boolean centered, Runnable onClick)
    {
        this.onClick = onClick;
        
        sprite.setTexture(buttonTexture);
        
        if (centered)
            sprite.setOrigin(sprite.getLocalBounds().width/2, sprite.getGlobalBounds().height/2);
        
        sprite.setPosition(position);
        
        text.setFont(font);
        text.setColor(Color.BLACK);
        text.setString(string);
        text.setOrigin(text.getLocalBounds().width/2, text.getLocalBounds().height/2);
        text.setPosition(position);
    }
    
    @Override
    public void update(EventQueue events)
    {
        for (Event event : events)
        {
            if (event.type == Event.Type.MOUSE_BUTTON_PRESSED && event.asMouseButtonEvent().button == Mouse.Button.LEFT && sprite.getGlobalBounds().contains(new Vector2f(event.asMouseEvent().position)))
                onClick.run();
            
            if (event.type == Event.Type.MOUSE_MOVED && sprite.getGlobalBounds().contains(new Vector2f(event.asMouseEvent().position)))
                hover = true;
            else
                hover = false;
        }
    }
    
    @Override
    public void draw(RenderTarget target, RenderStates states)
    {
        if (hover)
            sprite.setColor(Color.WHITE);
        else
            sprite.setColor(new Color(125, 125, 125, 255));
        
        target.draw(sprite);
        target.draw(text);
    }
}
