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
import org.jsfml.window.event.Event;


public class IconButton extends ActivatableElement implements Input
{
    private final Sprite buttonSprite = new Sprite();
    private final Sprite iconSprite = new Sprite();
    
    private final Text text = new Text();
    
    private final Runnable onClick;
    
    public IconButton(ConstTexture buttonTexture, ConstTexture iconTexture, ConstFont font, String string, Vector2f position, boolean centered, Runnable onClick)
    {
        this.onClick = onClick;
        
        buttonSprite.setTexture(buttonTexture);
        
        iconSprite.setTexture(iconTexture);
        
        text.setFont(font);
        text.setString(string);
        
        if (centered)
            buttonSprite.setOrigin(buttonSprite.getGlobalBounds().width/2, buttonSprite.getGlobalBounds().height/2);
        
        buttonSprite.setPosition(position);
        
        float buttonWidth = buttonSprite.getGlobalBounds().width;
        
        float contentsWidth = iconSprite.getGlobalBounds().width + 10 + text.getGlobalBounds().width;
        
        iconSprite.setOrigin(0, iconSprite.getGlobalBounds().height/2);
        
        iconSprite.setPosition(buttonSprite.getGlobalBounds().left + (buttonWidth - contentsWidth)/2, buttonSprite.getGlobalBounds().top + buttonSprite.getGlobalBounds().height/2);
        
        text.setOrigin(text.getGlobalBounds().left, text.getGlobalBounds().top + text.getGlobalBounds().height/2);
        
        text.setPosition(iconSprite.getPosition().x + iconSprite.getGlobalBounds().width + 10, iconSprite.getPosition().y);
    }
    
    @Override
    protected void onStateChange()
    {
        if (!isActive())
        {
            Color color = new Color(150, 150, 150, 255);
            
            buttonSprite.setColor(color);
            iconSprite.setColor(color);
        }
        else
        {
            buttonSprite.setColor(Color.WHITE);
            iconSprite.setColor(Color.WHITE);
        }
    }
    
    @Override
    public void update(EventQueue events)
    {
        if (isActive())
            for (Event event : events)
                if (event.type == Event.Type.KEY_PRESSED && event.asKeyEvent().key == Key.RETURN)
                    onClick.run();
    }
    
    @Override
    public void draw(RenderTarget target, RenderStates states)
    {
        target.draw(buttonSprite);
        target.draw(iconSprite);
        target.draw(text);
    }
}
