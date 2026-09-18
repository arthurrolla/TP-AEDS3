package entidades;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Classe que representa um autor com seus atributos e métodos de serialização.
 * Realiza validações nos dados de entrada.
 */
public class Autor implements aed3.InterfaceRegistro {

    /** Identificador único do autor */
    private int id;
    
    /** Nome do autor */
    private String nome;

    /**
     * Construtor padrão que inicializa um autor com valores padrão.
     */
    public Autor() {
        this(-1, "");
    }

    public Autor(String nome) {
        this(-1, nome);
    }


    /**
     * Construtor que inicializa um autor com todos os seus atributos.
     * Valida o nome do autor.
     * @param id Identificador único do autor
     * @param nome Nome do autor    
     */
    public Autor(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }
    
    /**
     * Obtém o identificador único do autor.
     * 
     * @return O ID do autor
     */
    public int getId() {
        return id;
    }

    /**
     * Define o identificador único do autor.
     * 
     * @param id O ID a ser atribuído
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtém o nome do autor.
     * 
     * @return O nome do autor
     */
    public String getNome() {
        return nome;
    }

    /**
     * Define o nome do autor.
     * 
     * @param nome O nome a ser atribuído
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Retorna uma representação em texto formatado dos dados do autor.
     * Exibe todos os atributos de forma legível com a data no formato dd/MM/yyyy.
     * 
     * @return String contendo os dados do autor formatados
     */
    @Override
    public String toString() {

        return String.format(
            "ID..: %d%n" +
            "Nome: %s%n",
            id,
            nome
        );
    }

    /**
     * Serializa os dados do autor em um array de bytes.
     * Converte todos os atributos para formato binário, adequado para armazenamento em arquivo.
     * 
     * @return Array de bytes contendo os dados serializados
     * @throws Exception Se houver erro durante a serialização
     */
    @Override
    public byte[] serialize() throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id);
        dos.writeUTF(nome);

        return baos.toByteArray();
    }

    /**
     * Desserializa os dados do autor a partir de um array de bytes.
     * Reconstrói todos os atributos a partir dos dados binários armazenados em arquivo.
     * 
     * @param data Array de bytes contendo os dados a desserializar
     * @throws Exception Se houver erro durante a desserialização
     */
    @Override
    public void deserialize(byte[] data) throws Exception {
        
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);

        id = dis.readInt();
        nome = dis.readUTF();
    }
}