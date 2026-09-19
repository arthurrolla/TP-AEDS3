package aed3;

public interface InterfaceRegistro {

    public void setId(int id);
    public int getId();

    public byte[] serialize() throws Exception;
    public void deserialize(byte[] data) throws Exception;
    
}
