package entidades;
import aed3.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ParIsbnID implements InterfaceHashExtensivel {
    
    private String isbn;      // chave
    private int id;           // valor
    private final short TAMANHO = 17;  // tamanho em bytes

    public ParIsbnID() throws Exception {
        this.isbn = "";
        this.id = -1;
    }

    public ParIsbnID(String isbn, int id) throws Exception {
        if(isbn.length()>0 && !Livro.validarIsbn(isbn)) 
            throw new IllegalArgumentException("ISBN inválido.");
        
        this.isbn = isbn;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    // O método hashCode() deve retornar um número
    // inteiro positivo e exclusivo
    @Override
    public int hashCode() {
        return Math.abs(this.isbn.hashCode());
    }

    public short size() {
        return this.TAMANHO;
    }

    public String toString() {
        return "("+this.isbn + ";" + this.id+")";
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        if(isbn.length()==0)
            dos.write("             ".getBytes());
        else
            dos.write(this.isbn.getBytes());
        dos.writeInt(this.id);
        return baos.toByteArray();
    }

    public void deserialize(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        byte[] isbnBytes = new byte[13];
        dis.readFully(isbnBytes);
        this.isbn = new String(isbnBytes).trim();
        this.id = dis.readInt();
    }

    public static int hash(String isbn) {
        return Math.abs(isbn.hashCode());
    }

}
