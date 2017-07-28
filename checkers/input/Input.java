package checkers.input;

import checkers.utility.EventQueue;
import org.jsfml.graphics.Drawable;


public interface Input extends Drawable
{
    void update(EventQueue events);
}
