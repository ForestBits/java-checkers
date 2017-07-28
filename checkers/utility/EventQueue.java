package checkers.utility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jsfml.window.Window;
import org.jsfml.window.event.Event;


public class EventQueue implements Iterable<Event>
{
    private List<Event> events = new ArrayList<>();
    
    public EventQueue(Window window)
    {
        Event event;
        
        while (true)
        {
            event = window.pollEvent();
            
            if (event == null)
                break;
            
            events.add(event);
        }
    }
    
    public int getSize() {return events.size();}
    
    public Event getEvent(int index)
    {
        assert index >= 0 && index < events.size() : "Tried to access event that does not exist";
        
        return events.get(index);
    }
    
    @Override
    public Iterator<Event> iterator() {return events.iterator();}
}
