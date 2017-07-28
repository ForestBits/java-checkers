package checkers.packet;

import checkers.Game.Move;
import checkers.packet.client.CaptureMovePacket;
import checkers.packet.client.GameOptionPacket;
import checkers.packet.client.NormalMovePacket;
import checkers.packet.server.CreateBoardPacket;
import checkers.packet.server.GameOverPacket;
import checkers.packet.server.GameStatePacket;
import checkers.packet.server.PlayerColorPacket;
import checkers.packet.server.PlayerTurnPacket;
import checkers.packet.server.ServerDisconnectPacket;
import checkers.packet.server.SetBoardEmptyPacket;
import checkers.packet.server.SetBoardPiecePacket;
import checkers.packet.shared.ChatPacket;
import checkers.packet.shared.HeartbeatPacket;
import checkers.packet.shared.LoginPacket;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PacketUtil 
{
    private final static List<Class<?>> knownTypes = new ArrayList<>();
    
    private final static Map<Byte, Class<? extends Packet>> packetIDs = new HashMap<>();
    
    static
    {
        registerPacketType(ServerDisconnectPacket.class);
        registerPacketType(LoginPacket.class);
        registerPacketType(GameStatePacket.class);
        registerPacketType(CreateBoardPacket.class);
        registerPacketType(SetBoardEmptyPacket.class);
        registerPacketType(SetBoardPiecePacket.class);
        registerPacketType(PlayerColorPacket.class);
        registerPacketType(PlayerTurnPacket.class);
        registerPacketType(NormalMovePacket.class);
        registerPacketType(CaptureMovePacket.class);
        registerPacketType(GameOverPacket.class);
        registerPacketType(GameOptionPacket.class);
        registerPacketType(ChatPacket.class);
        registerPacketType(HeartbeatPacket.class);
        
        
        knownTypes.add(boolean.class);
        knownTypes.add(byte.class);
        knownTypes.add(char.class);
        knownTypes.add(short.class);
        knownTypes.add(int.class);
        knownTypes.add(long.class);
        knownTypes.add(float.class);
        knownTypes.add(double.class);
        knownTypes.add(String.class);
        knownTypes.add(Enum.class);
    }
    
    public static boolean sendPacket(Socket socket, Packet packet)
    {
        if (!Packet.class.isAssignableFrom(packet.getClass()))
            throw new RuntimeException("Tried sending a type that isn't a packet by name " + packet.getClass().getCanonicalName());
        
        try
        {
            writeToStream(packet, new DataOutputStream(socket.getOutputStream()));
        }
        
        catch (IOException exception)
        {
            return false;
        }
        
        catch (IllegalAccessException exception)
        {
            throw new RuntimeException(exception);
        }
        
        return true;
    }
    
    public static void registerPacketType(Class<? extends Packet> cls) 
    {
        if (packetIDs.containsKey(cls))
            throw new RuntimeException(cls.getSimpleName() + " was already registered");
        
        packetIDs.put((byte) packetIDs.size(), cls);
    }
    
    private static Object createObject(Class<?> cls)
    {
        Constructor[] ctors = cls.getDeclaredConstructors();

        Constructor ctor = ctors[0];
        
        ctor.setAccessible(true);

        int count = ctor.getParameterCount();

        Class[] types = ctor.getParameterTypes();

        Object[] parameters = new Object[count];

        for (int i = 0; i < types.length; ++i)
            if (types[i].isPrimitive())
            {
                if (types[i].equals(boolean.class))
                    parameters[i] = false;
                else if (types[i].equals(char.class))
                    parameters[i] = ' ';
                else if (types[i].equals(byte.class))
                    parameters[i] = (byte) 0;
                else if (types[i].equals(short.class))
                    parameters[i] = (short) 0;
                else if (types[i].equals(int.class))
                    parameters[i] = (int) 0;
                else if (types[i].equals(long.class))
                    parameters[i] = (long) 0;
            }
            else
                parameters[i] = null;

        Object object = null;
        
        try
        {
           object = ctor.newInstance(parameters);
            if (object == null)
                System.out.println("Ctor.newinstance was null");
           ctor.setAccessible(false);
           
           return object;
        }

        catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException ex)
        {
            System.out.println(object == null);//throw new RuntimeException("Failed to force create object of type " + cls.getSimpleName(), ex);
        }
        
        return null;
    }
    
    private static void recursiveWriteData(DataOutputStream dos, Class<?> dataType, Object value)
    {
        if (knownTypes.contains(dataType))
            writeData(dos, dataType, value);
        else
        {
            Class<?> superClass = dataType.getSuperclass();
            
            if (superClass == Object.class || superClass == Enum.class)
            {
                if (superClass == Enum.class)
                {
                    writeData(dos, dataType, value);
                    
                    return;
                }
                
                Field[] fields = dataType.getDeclaredFields();
                
                for (Field field : fields)
                {
                    if ((field.getModifiers() & Modifier.STATIC) != 0)
                        continue;

                    field.setAccessible(true);
                    
                    try
                    {

                        if (knownTypes.contains(field.getType()))
                        {
                            writeData(dos, field.getType(), field.get(value));
                        }
                        else
                        {
                            recursiveWriteData(dos, field.getType(), field.get(value));
                        }
                    }
                    
                    catch (IllegalAccessException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                    
                    field.setAccessible(false);
                }
            }
            else
            {
                throw new RuntimeException("Passed in a " + dataType.getSimpleName() + " which has a unknown supertype");
            }
        }
    }
    
    private static void writeData(DataOutputStream dos, Class<?> dataType, Object value)
    {
        try
        {
            if (dataType.equals(boolean.class))
            {
                boolean b = (boolean) value;

                dos.writeBoolean(b);
            }
            else if (dataType.equals(byte.class))
            {
                byte b = (byte) value;

                dos.writeByte(b);
            }
            else if (dataType.equals(char.class))
            {
                char c = (char) value;

                dos.writeChar(c);
            }
            else if (dataType.equals(short.class))
            {
                short s = (short) value;

                dos.writeShort(s);
            }
            else if (dataType.equals(int.class))
            {
                int i = (int) value;

                dos.writeInt(i);
            }
            else if (dataType.equals(long.class))
            {
                long l = (long) value;
                
                dos.writeLong(l);
            }
            else if (dataType.equals(Float.class))
            {
                float f = (float) value;

                dos.writeFloat(f);
            }
            else if (dataType.equals(Double.class))
            {
                double d = (double) value;

                dos.writeDouble(d);
            }
            else if (dataType.equals(String.class))
            {
                String s = (String) value;

                dos.writeUTF(s);
            }
            else if (dataType.getSuperclass().equals(Enum.class))
            {
                Method method = null;
                
                try
                {
                    method = dataType.getMethod("ordinal");
                }
                
                catch (NoSuchMethodException exception)
                {
                    throw new RuntimeException(exception);
                }
                
                try
                {
                    dos.writeInt((int) method.invoke(value));
                }
                
                catch (IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exception)
                {
                    throw new RuntimeException(exception);
                }
            }
            else
                throw new RuntimeException("Unwriteable type in packet " + dataType.getSimpleName() + ": " + value.getClass().getSimpleName());
        }

        catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }
    }
    
    public static void writeToStream(Packet packet, DataOutputStream out) throws IOException, IllegalAccessException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        DataOutputStream dos = new DataOutputStream(baos);
        
        if (!packetIDs.containsValue(packet.getClass()))
        {
            throw new RuntimeException(packet.getClass().getSimpleName() + " is not a registered packet");
        }
        
        for (Map.Entry<Byte, Class<? extends Packet>> entry : packetIDs.entrySet())
            if (entry.getValue().equals(packet.getClass()))
            {
                try
                {
                    dos.writeByte(entry.getKey());
                }
                
                catch (IOException exception)
                {
                    throw new RuntimeException(exception);
                }
                
                break;
            }
        
        Class<? extends Packet> packetClass = packet.getClass();
        
        Field[] fields = packetClass.getFields();
        
        for (Field field : fields)
        {            
            Class<?> type = field.getType();
            
            try
            {
                if (type.isArray())
                {
                    Class<?> elementType = type.getComponentType();

                    Object arrayObject = field.get(packet);
                    
                    short arrayLength = (short) Array.getLength(arrayObject);
                    
                    dos.writeShort(arrayLength);
                    
                    for (int i = 0; i < arrayLength; ++i)
                        recursiveWriteData(dos, elementType, Array.get(arrayObject, i));
                }
                else
                {if (field.get(packet) == null)
                {
                    throw new RuntimeException("Tried to write reference field, it was null - " + packet.getClass().getCanonicalName() + " field " + field.getName());
                }
                    recursiveWriteData(dos, type, field.get(packet));
                }
            }
            
            catch (IllegalAccessException | IOException exception)
            {
                throw exception;
            }
        }
        
        byte[] bytes = baos.toByteArray();
        
        try
        {
            out.writeInt(bytes.length);
            
            for (byte b : bytes)
                out.writeByte(b);
        }
        
        catch (IOException exception)
        {
            throw exception;
        }
    }
    
    private static Object recursiveReadData(DataInputStream dis, Class<?> packetClass, Class<?> dataType)
    {
        if (knownTypes.contains(dataType))
            return readData(dis, packetClass, dataType);
        else
        {
            Class<?> superClass = dataType.getSuperclass();
            
            if (superClass == Object.class || superClass == Enum.class)
            {
                if (superClass == Enum.class)
                    return readData(dis, packetClass, dataType);
                
                Field[] fields = dataType.getDeclaredFields();
                
                Object object = createObject(dataType);
                
                for (Field field : fields)
                {
                    if ((field.getModifiers() & Modifier.STATIC) != 0)
                        continue;
                    
                    field.setAccessible(true);
                    
                    try
                    {
                        if (knownTypes.contains(field.getType()))
                        {if (object == null)
                            System.out.println("object be null");
                            field.set(object, readData(dis, packetClass, field.getType()));
                        }
                        else
                            field.set(object, recursiveReadData(dis, packetClass, field.getType()));
                    }
                    
                    catch (IllegalAccessException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                    
                    field.setAccessible(false);
                }
                
                return object;
            }
            else
            {
                throw new RuntimeException("Passed in a " + dataType.getSimpleName() + " which has a unknown supertype");
            }
        }
    }
    
    private static Object readData(DataInputStream dis, Class<?> packetClass, Class<?> dataType)
    {
        try
        {
            if (dataType.equals(boolean.class))
                return dis.readBoolean();
            else if (dataType.equals(byte.class))
                return dis.readByte();
            else if (dataType.equals(char.class))
                return dis.readChar();
            else if (dataType.equals(short.class))
                return dis.readShort();
            else if (dataType.equals(int.class))
                return dis.readInt();
            else if (dataType.equals(long.class))
                return dis.readLong();
            else if (dataType.equals(float.class))
                return dis.readFloat();
            else if (dataType.equals(double.class))
                return dis.readDouble();
            else if (dataType.equals(String.class))
                return dis.readUTF();
            else if (dataType.getSuperclass().equals(Enum.class))
            {
                int enumNumber = dis.readInt();
                
                Object value = null;
                
                for (Object enumValue : dataType.getEnumConstants())
                    if (((Enum) enumValue).ordinal() == enumNumber)
                        return enumValue;
                
                throw new RuntimeException("Unable to read enum value.");
            }
            else
                throw new RuntimeException("Unreadable type in packet " + packetClass.getSimpleName() + ": " + dataType.getSimpleName());
        }
        
        catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }
    }
    
    public static Packet readFromStream(DataInputStream in)
    {
        try
        {
            byte id = in.readByte();
            
            if (id >= packetIDs.size())
                throw new RuntimeException("Unknown packet id: " + id);
            
            Class<? extends Packet> cls = packetIDs.get(id);
            
            Packet packet = null;
            
            packet = (Packet) createObject(cls);
            
            Field[] fields = cls.getFields();
            
            for (Field field : fields)
            {
                Class<?> fieldType = field.getType();
                
                try
                {
                    if (fieldType.isArray())
                    {
                        Class<?> elementType = fieldType.getComponentType();

                        short arrayLength = in.readShort();

                        Object arrayObject = Array.newInstance(elementType, arrayLength);

                        for (int i = 0; i < arrayLength; ++i)
                            Array.set(arrayObject, i, recursiveReadData(in, cls, elementType));

                        field.set(packet, arrayObject);
                    }
                    else
                    {
                        field.setAccessible(true);
                        
                        field.set(packet, recursiveReadData(in, cls, fieldType));
                        
                        field.setAccessible(false);
                    }
                }
                
                catch (IllegalAccessException | IllegalArgumentException exception)
                {
                    throw new RuntimeException(exception);
                }
            }
            
            return packet;
        }
        
        catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }
    }
}
