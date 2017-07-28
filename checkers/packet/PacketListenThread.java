package checkers.packet;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;


public class PacketListenThread implements Runnable
{
    private final Socket socket;
    
    private final PacketHandler handler;
    
    private volatile boolean stop = false;
    
    void end() {stop = true;}
    
    public PacketListenThread(PacketHandler handler, Socket socket)
    {
        this.socket = socket;
        this.handler = handler;
    }
    
    @Override
    public void run()
    {
        InputStream in = null;

        DataInputStream dis = null;
        
        try
        {
            in = socket.getInputStream();
            
            dis = new DataInputStream(in);
        }
        
        catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }
        
        int packetSize = 0;
        
        boolean readPacketSize = false;
        
        while (!stop)
        {
            try
            {
                if (in.available() >= 4 && !readPacketSize)
                {
                    readPacketSize = true;
                    
                    packetSize = dis.readInt();
                }
                
                if (in.available() >= packetSize && readPacketSize)
                {
                    byte[] buffer = new byte[1024];

                    int read = in.read(buffer, 0, packetSize);

                    byte[] data = new byte[read];

                    System.arraycopy(buffer, 0, data, 0, read);

                    handler.newPacket(socket, data);
                    
                    readPacketSize = false;
                    
                    packetSize = -1;
                }

            }
            
            catch (IOException exception)
            {
                throw new RuntimeException(exception);
            }
            
            try
            {
                Thread.sleep(33);
            }
            
            catch (InterruptedException exception)
            {
                throw new RuntimeException(exception);
            }
        }
    }
}
