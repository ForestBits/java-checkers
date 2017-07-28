package checkers.server;

import java.net.Socket;


public class PlayerInfo 
{
    public Socket socket;
    
    public String name;
    
    public GameOption choice = null;
    
    public long lastHeartbeat = System.currentTimeMillis();
}
