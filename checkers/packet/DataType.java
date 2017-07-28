package checkers.packet;


public enum DataType 
{
    BOOLEAN(boolean.class),
    CHARACTER(char.class),
    INT(int.class),
    FLOAT(float.class),
    DOUBLE(double.class),
    STRING(String.class);
    
    private final Class cls;
    
    DataType(Class<?> cls)
    {
        this.cls = cls;
    }
        
    public Class<?> getTypeClass() {return cls;}
}
