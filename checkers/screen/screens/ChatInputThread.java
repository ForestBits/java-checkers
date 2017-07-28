package checkers.screen.screens;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;


public class ChatInputThread implements Runnable
{
    private GameScreen screen;
    
    private BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
    
    public ChatInputThread(GameScreen screen)
    {
        this.screen = screen;
    }
    
    public void quit()
    {
        buf = null;
        
        screen = null;
    }
    
    @Override
    public void run()
    {
        try
        {   
            while (true)
            {
                screen.addChatInput(buf.readLine());
            }
        }
        
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        
        catch (NullPointerException ex)
        {
            
        }
    }
}
