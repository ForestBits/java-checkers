package checkers.input.inputs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import checkers.input.Input;
import checkers.utility.EventQueue;
import org.jsfml.graphics.Color;
import org.jsfml.graphics.ConstFont;
import org.jsfml.graphics.RectangleShape;
import org.jsfml.graphics.RenderStates;
import org.jsfml.graphics.RenderTarget;
import org.jsfml.graphics.Text;
import org.jsfml.system.Vector2f;
import org.jsfml.window.Keyboard.Key;
import org.jsfml.window.Mouse;
import org.jsfml.window.event.Event;


public class TextBox implements Input
{
    private boolean active;
    
    private final StringBuilder builder = new StringBuilder();
    
    private final RectangleShape box = new RectangleShape();
    
    private final Text contents = new Text();
    private final Text labelText = new Text();
    
    Consumer<String> onContentsChange;
    
    private final int maxLength;
    
    private static final List<Key> goodKeys;
    
    static
    {
        List<Key> keys = new ArrayList<>();
        
        keys.add(Key.A);
        keys.add(Key.B);
        keys.add(Key.C);
        keys.add(Key.D);
        keys.add(Key.E);
        keys.add(Key.F);
        keys.add(Key.G);
        keys.add(Key.H);
        keys.add(Key.I);
        keys.add(Key.J);
        keys.add(Key.K);
        keys.add(Key.L);
        keys.add(Key.M);
        keys.add(Key.N);
        keys.add(Key.O);
        keys.add(Key.P);
        keys.add(Key.Q);
        keys.add(Key.R);
        keys.add(Key.S);
        keys.add(Key.T);
        keys.add(Key.U);
        keys.add(Key.V);
        keys.add(Key.W);
        keys.add(Key.X);
        keys.add(Key.Y);
        keys.add(Key.Z);
        
        keys.add(Key.NUM0);
        keys.add(Key.NUM1);
        keys.add(Key.NUM2);
        keys.add(Key.NUM3);
        keys.add(Key.NUM4);
        keys.add(Key.NUM5);
        keys.add(Key.NUM6);
        keys.add(Key.NUM7);
        keys.add(Key.NUM8);
        keys.add(Key.NUM9);
        
        keys.add(Key.PERIOD);

        goodKeys = Collections.unmodifiableList(keys);  
    }
    
    public TextBox(String label, Vector2f position, int maxLength, ConstFont font, Color color, Consumer<String> onContentsChange)
    {
        this.maxLength = maxLength;
        
        box.setPosition(position);
        contents.setPosition(position.x + 1, position.y - 4);
        
        contents.setFont(font);
        labelText.setFont(font);
        
        labelText.setString(label);
        
        labelText.setColor(color);
        labelText.setCharacterSize(20);
        contents.setColor(color);
        contents.setCharacterSize(20);
        
        box.setFillColor(Color.TRANSPARENT);
        box.setOutlineThickness(1);
        box.setOutlineColor(color);
        
        box.setSize(new Vector2f(maxLength*(font.getGlyph('K', contents.getCharacterSize(), false).textureRect.width) + 2, 20));
        
        labelText.setPosition(position.x - (labelText.getGlobalBounds().width + 2), position.y - 5);
        
        this.onContentsChange = onContentsChange;
    }
    
    @Override
    public void update(EventQueue events)
    {
        for (Event event : events)
        {
            if (event.type == Event.Type.MOUSE_BUTTON_PRESSED
                    && event.asMouseButtonEvent().button == Mouse.Button.LEFT)
            {
                active = box.getGlobalBounds().contains(new Vector2f(event.asMouseButtonEvent().position));
                
                labelText.setStyle(active ? Text.UNDERLINED : Text.REGULAR);
            }
            
            if (event.type == Event.Type.KEY_RELEASED && active)
            {
                Key pressed = event.asKeyEvent().key;
                
                if (pressed == Key.BACKSPACE)
                {
                    if (builder.length() > 0)
                        builder.deleteCharAt(builder.length() - 1);
                    
                    contents.setString(builder.toString());
                }
                else if (builder.length() < maxLength)
                {
                    for (Key key : goodKeys)
                    {
                        if (pressed == key)
                        {
                            String addition;
                            
                            switch (key)
                            {
                                case NUM0:
                                case NUM1:
                                case NUM2:
                                case NUM3:
                                case NUM4:
                                case NUM5:
                                case NUM6:
                                case NUM7:
                                case NUM8:
                                case NUM9:
                                    addition = key.toString().substring(key.toString().length() - 1);
                                    
                                    break;
                                
                                case PERIOD:
                                    addition = ".";
                                    
                                    break;
                                    
                                default:
                                
                                    addition = event.asKeyEvent().shift ? key.toString() : key.toString().toLowerCase();

                                    break;
                            }
                            
                            builder.append(addition);

                            contents.setString(builder.toString());
                            
                            break;
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public void draw(RenderTarget target, RenderStates states)
    {   
        target.draw(box);
        target.draw(labelText);
        target.draw(contents);
        
    }
    
    public String getContents()
    {
        return builder.toString();
    }
}
