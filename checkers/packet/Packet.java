package checkers.packet;

import java.util.ArrayList;
import java.util.List;


public interface Packet 
{
    /*private static class PacketInfo
    {
        byte id;
        
        String name;
        
        DataType[] types;
    }
    
    private static final List<PacketInfo> packetTypes = new ArrayList<>();
    
    public static void addPacketType(String name, DataType... types)
    {
        PacketInfo info = new PacketInfo();
        
        info.id = (byte) packetTypes.size();
        info.name = name;
        info.types = types;
        
        packetTypes.add(info);
    }
    
    public static String getPacketString(String packetType, Object... args)
    {
        StringBuilder builder = new StringBuilder();
        
        for (PacketInfo type : packetTypes)
            if (type.name.equals(packetType))
            {   
                builder.append(type.id);
                
                if (!(args.length == type.types.length))
                    throw new IllegalArgumentException("Invalid number of parameters passed for packet type " + packetType);
                
                for (int i = 0; i < type.types.length; ++i)
                    if (type.types[i].getTypeClass().equals(args[i].getClass()))
                        builder.append(args[i]);
                    else
                        throw new IllegalArgumentException("Parameter type mismatch in packet type " + packetType + ": Expected " + type.types[i].getTypeClass().getName() + ", but got " + args[i].getClass().getName());

                return builder.toString();
            }
        
        throw new IllegalArgumentException(packetType + " is not a known packet name");
    }*/
}
