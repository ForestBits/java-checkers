package checkers.client;

import checkers.screen.ScreenChanger;
import checkers.screen.ScreenConstructionParameters;
import checkers.screen.ScreenEngine;
import checkers.screen.ScreenUpdateParameters;
import checkers.screen.screens.MainMenuScreen;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.jsfml.graphics.ConstTexture;
import org.jsfml.graphics.Font;
import org.jsfml.graphics.RenderWindow;
import org.jsfml.graphics.Texture;
import org.jsfml.window.VideoMode;


public class ClientMain 
{
    private Path getPath(String filename)
    {
        return Paths.get(System.getProperty("user.dir") + File.separator + "res" + File.separator + filename);
    }
    
    private ConstTexture loadTexture(String filename) throws IOException
    {
        Texture texture = new Texture();
        
        texture.loadFromFile(getPath(filename));
        
        return texture;
    }
    
    public void run()
    {
        try
        {
            Map<String, ConstTexture> textures = new HashMap<>();

            textures.put("button", loadTexture("button.png"));
            textures.put("whitePiece", loadTexture("whitePiece.png"));
            textures.put("blackPiece", loadTexture("blackPiece.png"));
            

            Font font = new Font();
            
            font.loadFromFile(getPath("font.ttf"));
            
            RenderWindow window = new RenderWindow(new VideoMode(864, 660), "Checkers");
            
            window.setKeyRepeatEnabled(false);

            ScreenEngine engine = new ScreenEngine();

            ScreenChanger screenChanger = new ScreenChanger(engine);

            ScreenConstructionParameters constructParams = new ScreenConstructionParameters(screenChanger, window, font, textures);

            ScreenUpdateParameters updateParams = new ScreenUpdateParameters(screenChanger, window, constructParams);

            engine.construct(updateParams);
            
            engine.setScreen(new MainMenuScreen(constructParams));//new GameScreen(constructParams, InetAddress.getByName("localhost"), new Random().nextInt(1000) + ""));//new ServerDisconnectScreen(constructParams, "Couldn't connect"));//new MainMenuScreen(constructParams));

            while (true)
            {
                engine.update();
                engine.draw();
                
                window.display();


                Thread.sleep(33);
            }
        }
        
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
