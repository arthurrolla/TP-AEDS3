package aed3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ParDataId implements InterfaceArvoreBMais<ParDataId>{
    private long criacao;
    private int idPergunta;
    private final short TAMANHO = 12;
    public ParDataId(){
        this(0, -1);
    }
    public ParDataId(long criacao, int idPergunta){
        this.criacao = criacao;
        this.idPergunta = idPergunta;
    }
    public long getCriacao(){
        return this.criacao;
    }
    public int getIdPergunta(){
        return this.idPergunta;
    }
    @Override
    public ParDataId clone(){
        return new ParDataId(this.criacao, this.idPergunta);
    }
    @Override
    public short size(){
        return this.TAMANHO;
    }
    @Override
    public int compareTo(ParDataId outro){
        int comparacao = Long.compare(outro.criacao, this.criacao);
        if(comparacao != 0){
            return comparacao;
        }
        return Integer.compare(this.idPergunta, outro.idPergunta);
    }
    @Override
    public String toString(){
        return this.criacao + ";" + this.idPergunta;
    }
    @Override
    public byte[] serialize() throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeLong(this.criacao);
        dos.writeInt(this.idPergunta);

        return baos.toByteArray();
    }
    @Override
    public void deserialize(byte[] ba) throws IOException{
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        this.criacao = dis.readLong();
        this.idPergunta = dis.readInt();
    }
}