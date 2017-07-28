package checkers.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class IncomingConnectionThread implements Runnable
{
    private final ServerMain server;
    
    private ServerSocket listener;
    
    public IncomingConnectionThread(ServerMain server)
    {
        this.server = server;
    }
    
    public void stop()
    {
        try
        {
            listener.close();
        }
        
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public void run()
    {
        try
        {
            ServerSocket listener = new ServerSocket(11111);
            
            while (true)
            {
                Socket socket = listener.accept();
                
                server.addConnection(socket);
            }
        }
        
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
