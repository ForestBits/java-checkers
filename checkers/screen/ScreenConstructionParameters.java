package checkers.screen;

import java.util.Map;
import org.jsfml.graphics.ConstFont;
import org.jsfml.graphics.ConstTexture;
import org.jsfml.graphics.RenderWindow;


public class ScreenConstructionParameters 
{
    public final ScreenChanger screenChanger;
    
    public final RenderWindow window;
    
    public final ConstFont font;
    
    public final Map<String, ConstTexture> textures;
    
    
    public ScreenConstructionParameters(ScreenChanger screenChanger, RenderWindow window, ConstFont font, Map<String, ConstTexture> textures)
    {
        this.screenChanger = screenChanger;
        this.window = window;
        this.font = font;
        this.textures = textures;
    }
}
