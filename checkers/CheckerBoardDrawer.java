package checkers;

import static javax.swing.Spring.height;
import static javax.swing.Spring.width;
import org.jsfml.graphics.Color;
import org.jsfml.graphics.FloatRect;
import org.jsfml.graphics.RectangleShape;
import org.jsfml.graphics.RenderTarget;
import org.jsfml.system.Vector2f;


public class CheckerBoardDrawer 
{
    public static void draw(RenderTarget target, FloatRect area, Color first, Color second)
    {
        draw(target, 8, 8, area, first, second, 1);
    }
    
    public static void draw(RenderTarget target, int width, int height, FloatRect area, Color first, Color second)
    {
        draw(target, width, height, area, first, second, 1);
    }
    
    public static void draw(RenderTarget target, FloatRect area, Color first, Color second, double alpha)
    {
        draw(target, 8, 8, area, first, second, alpha);
    }
    
    public static void draw(RenderTarget target, int width, int height, FloatRect area, Color first, Color second, double alpha)
    {
        RectangleShape rectangle = new RectangleShape();
        
        rectangle.setSize(new Vector2f(area.width/width, area.height/height));
        
        for (int x = 0; x < width; ++x)
            for (int y = 0; y < height; ++y)
            {
                rectangle.setFillColor((x + y) % 2 == 0 ? first : second);
                rectangle.setPosition(area.left + x*rectangle.getSize().x, area.top + y*rectangle.getSize().y);
                
                target.draw(rectangle);
            }
        
        RectangleShape overlay = new RectangleShape(new Vector2f(area.width, area.height));
                
        overlay.setPosition(area.left, area.top);
        overlay.setFillColor(new Color(Color.WHITE, (int) ((1 - alpha)*255)));
        
        target.draw(overlay);
    }
}
