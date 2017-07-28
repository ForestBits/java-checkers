package checkers.packet;

import java.net.Socket;


public interface PacketHandlerLambda 
{
    public void accept(Socket socket, Packet packet);
}
