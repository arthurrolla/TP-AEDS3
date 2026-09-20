package entidades;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Classe que representa uma autoria de livros com seus atributos e métodos de serialização.
 * Realiza validações nos dados de entrada.
 */
public class Autoria implements aed3.InterfaceRegistro {

    private int id;  // Identificador único da autoria
    private int idLivro;  // Identificador do livro
    private int idAutor;  // Identificador do autor

    /**
     * Construtor padrão que inicializa uma autoria com valores padrão.
     */
    public Autoria() {
        this(-1, -1, -1);
    }
    public Autoria(int idLivro, int idAutor) {
        this(-1, idLivro, idAutor);
    }

    /**
     * Construtor que inicializa uma autoria com todos os seus atributos.
     * @param id Identificador único da autoria
     * @param idLivro Identificador do livro
     * @param idAutor Identificador do autor
     */
    public Autoria(int id, int idLivro, int idAutor) {
        this.id = id;
        this.idLivro = idLivro;
        this.idAutor = idAutor;
    }
    
    /* */
    public int getId() {
        return id;
    }

    /**
     * Define o identificador único da autoria.
     * 
     * @param id O ID a ser atribuído
     */
    public void setId(int id) {
        this.id = id;
    }

    /* Retorna o id do livro */
    public int getIdLivro() {
        return idLivro;
    }

    /* Retorna o id do autor */
    public int getIdAutor() {
        return idAutor;
    }

    /**
     * Retorna uma representação em texto formatado dos dados da autoria.
     * Exibe todos os atributos de forma legível.
     * 
     * @return String contendo os dados da autoria formatados
     */
    @Override
    public String toString() {

        return String.format(
            "ID......: %d%n" +
            "ID Livro: %d%n" +
            "ID Autor: %d%n",
            id,
            idLivro,
            idAutor
        );
    }

    /**
     * Serializa os dados da autoria em um array de bytes.
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
        dos.writeInt(idLivro);
        dos.writeInt(idAutor);

        return baos.toByteArray();
    }

    /**
     * Desserializa os dados da autoria a partir de um array de bytes.
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
        idLivro = dis.readInt();
        idAutor = dis.readInt();
    }
}