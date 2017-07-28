package checkers.packet;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;


public class PacketHandler 
{
    private final Map<Class<? extends Packet>, BiConsumer<Socket, Packet>> map = new HashMap<>();
    private final Map<Socket, PacketListenThread> threads = new HashMap<>();
    
    synchronized void newPacket(Socket socket, byte[] packetData)
    {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(packetData));
        
        Packet packet = PacketUtil.readFromStream(in);
        
        for (Map.Entry<Class<? extends Packet>, BiConsumer<Socket, Packet>> entry : map.entrySet())
            if (entry.getKey().equals(packet.getClass()))
            {
                entry.getValue().accept(socket, packet);
                
                return;
            }
        
        throw new RuntimeException("No handler for packet type " + packet.getClass().getSimpleName());
    }

    public synchronized void addSocket(Socket socket)
    {
        PacketListenThread listen = new PacketListenThread(this, socket);
        
        new Thread(listen).start();
        
        threads.put(socket, listen);
    }
    
    public synchronized void removeSocket(Socket socket)
    {
        threads.get(socket).end();
        
        threads.remove(socket);
    }
    
    public synchronized void registerHandler(Class<? extends Packet> packetClass, BiConsumer<Socket, Packet> handler)
    {
        map.put(packetClass, handler);
    }
}
