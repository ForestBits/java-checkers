package checkers.input;

import java.util.ArrayList;
import java.util.List;
import checkers.utility.EventQueue;
import org.jsfml.window.Keyboard.Key;
import org.jsfml.window.event.Event;


public class ActivatableElementContainer 
{
    private final Key upKey;
    private final Key downKey;
    
    private final List<ActivatableElement> elements = new ArrayList<>();
    
    private int currentElementIndex;
    
    public ActivatableElementContainer(Key upKey, Key downKey)
    {
        this.upKey = upKey;
        this.downKey = downKey;
    }
    
    
    static class ActivationKey
    {
        private ActivationKey() {}
    }
    
    private static final ActivationKey activationKey = new ActivationKey();
    
    
    public void add(ActivatableElement element) 
    {
        elements.add(element);
        
        if (elements.size() == 1)
        {
            element.setActive(activationKey, true);
            element.onStateChange(activationKey);
        }
        else
        {
            element.setActive(activationKey, false);
            element.onStateChange(activationKey);
        }
    }
    
    public void remove(ActivatableElement element)
    {
        element.setActive(activationKey, false);
        element.onStateChange(activationKey);
        
        elements.remove(element);
    }
    
    public int size() {return elements.size();}
    
    public void setActive(int index)
    {
        assert index >= 0 && index < elements.size() : "Specified index out of range";
        
        elements.get(index).setActive(activationKey, true);
        elements.get(index).onStateChange(activationKey);
        
        elements.get(currentElementIndex).setActive(activationKey, false);
        elements.get(currentElementIndex).onStateChange(activationKey);
        
        currentElementIndex = index;
    }
    
    public void update(EventQueue events)
    {
        if (elements.size() <= 1)
            return;
        
        for (Event event : events)
        {
            if (event.type == Event.Type.KEY_RELEASED)
            {
                Key pressed = event.asKeyEvent().key;
                
                if (pressed == upKey)
                {
                    int index = currentElementIndex;
                    
                    --index;
                    
                    if (index == -1)
                        index = elements.size() - 1;
                    
                    setActive(index);
                }
                
                if (pressed == downKey)
                {
                    int index = currentElementIndex;
                    
                    ++index;
                    
                    if (index == elements.size())
                        index = 0;
                    
                    setActive(index);
                }
            }
        }
    }
}
