package checkers;

import checkers.client.ClientMain;
import checkers.server.ServerMain;
import java.io.File;
import java.io.IOException;


public class Main 
{
    public static void main(String[] args)
    {
        if (args.length != 1)
            return;
        
        switch (args[0].charAt(0))
        {
            case 's':
                new ServerMain().run();
                
                break;
                
            case 'c':
                new ClientMain().run();
                
                break;
        }
    }
}
