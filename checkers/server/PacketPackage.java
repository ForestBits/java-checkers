package checkers.server;

import checkers.packet.Packet;
import java.net.Socket;


public class PacketPackage 
{
    public final Socket socket;
    
    public final Packet packet;
    
    public PacketPackage(Socket socket, Packet packet)
    {
        this.socket = socket;
        this.packet = packet;
    }
}
