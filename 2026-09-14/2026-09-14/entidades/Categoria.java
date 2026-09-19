package entidades;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Classe que representa uma categoria de livros com seus atributos e métodos de serialização.
 * Realiza validações nos dados de entrada.
 */
public class Categoria implements aed3.InterfaceRegistro {

    /** Identificador único da categoria */
    private int id;
    
    /** Nome da categoria */
    private String nome;

    /**
     * Construtor padrão que inicializa uma categoria com valores padrão.
     */
    public Categoria() {
        this(-1, "");
    }

    /**
     * Construtor que inicializa uma categoria com o nome especificado.
     * @param nome Nome da categoria
     */
    public Categoria(String nome) {
        this(-1, nome);
    }

    /**
     * Construtor que inicializa uma categoria com todos os seus atributos.
     * Valida o nome da categoria.
     * @param id Identificador único da categoria
     * @param nome Nome da categoria
     */
    public Categoria(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }
    
    /**
     * Obtém o identificador único da categoria.
     * 
     * @return O ID da categoria
     */
    public int getId() {
        return id;
    }

    /**
     * Define o identificador único da categoria.
     * 
     * @param id O ID a ser atribuído
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtém o nome da categoria.
     * 
     * @return O nome da categoria
     */
    public String getNome() {
        return nome;
    }

    /**
     * Define o nome da categoria.
     * 
     * @param nome O nome a ser atribuído
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Retorna uma representação em texto formatado dos dados do livro.
     * Exibe todos os atributos de forma legível com a data no formato dd/MM/yyyy.
     * 
     * @return String contendo os dados do livro formatados
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
     * Serializa os dados da categoria em um array de bytes.
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
     * Desserializa os dados da categoria a partir de um array de bytes.
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