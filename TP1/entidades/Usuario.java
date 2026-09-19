package TP1.entidades;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Classe que representa um usuario com seus atributos e métodos de serialização.
 * Realiza validações nos dados de entrada.
 */
public class Usuario implements aed3.InterfaceRegistro {

    /** Identificador único do usuario */
    private int idUsuario;
    
    /** Nome do usuario */
    private String nome;

    /** Email do usuario */
    private String email;

    /** Senha do usuario */
    private String hashSenha;

    /** Pergunta secreta para recupera senha do usuario */
    private String perguntaSecreta;

    /** Resposta de pergunda secreta do usuario */
    private String hashPerguntaSecreta;



    /**
     * Construtor padrão que inicializa um usuario com valores padrão.
     */
    public Usuario() {
        this(-1, "", "", "", "", "");
    }

    public Usuario(String nome, String email, String hashSenha, String perguntaSecreta, String hashPerguntaSecreta) {
        this(-1, nome, email, hashSenha, perguntaSecreta, hashPerguntaSecreta);
    }


    /**
     * Construtor que inicializa um autor com todos os seus atributos.
     * Valida o nome do autor.
     * @param id Identificador único do autor
     * @param nome Nome do autor    
     */
    public Usuario(int id, String nome, String email, String hashSenha, String perguntaSecreta, String hashPerguntaSecreta) {
        this.idUsuario = id;
        this.nome = nome;
        this.email = email;
        this.hashSenha = hashSenha;
        this.perguntaSecreta = perguntaSecreta;
        this.hashPerguntaSecreta = hashPerguntaSecreta;
    }
    
    // GETERS + SETERS: ---------------------------------------------------------------------

    /**
     * Obtém o identificador único do autor.
     * 
     * @return O ID do autor
     */
    public int getId() {
        return idUsuario;
    }

    /**
     * Define o identificador único do autor.
     * 
     * @param id O ID a ser atribuído
     */
    public void setId(int id) {
        this.idUsuario = id;
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
     * Obtém o ____ do usuario.
     * 
     * @return O ____ do usuario
     */
    public String getEmail() {
        return email;
    }

    /**
     * Define o ____ do usuario.
     * 
     * @param email O ____ a ser atribuído
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtém o ____ do usuario.
     * 
     * @return O ____ do usuario
     */
    public String getHashSenha() {
        return hashSenha;
    }

    /**
     * Define o ____ do usuario.
     * 
     * @param hashSenha O ____ a ser atribuído
     */
    public void setHashSenha(String hashSenha) {
        this.hashSenha = hashSenha;
    }

    /**
     * Obtém o ____ do usuario.
     * 
     * @return O ____ do usuario
     */
    public String getPerguntaSecraeta() {
        return perguntaSecreta;
    }

    /**
     * Define o ____ do usuario.
     * 
     * @param perguntaSecreta O ____ a ser atribuído
     */
    public void setPerguntaSecreta(String perguntaSecreta) {
        this.perguntaSecreta = perguntaSecreta;
    }

    /**
     * Obtém o ____ do usuario.
     * 
     * @return O ____ do usuario
     */
    public String getHashPerguntaSecreta() {
        return hashPerguntaSecreta;
    }

    /**
     * Define o ____ do usuario.
     * 
     * @param ____ O ____ a ser atribuído
     */
    public void setHashPerguntaSecreta(String hashPerguntaSecreta) {
        this.hashPerguntaSecreta = hashPerguntaSecreta;
    }

    //---------------------------------------------------------------------

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
            idUsuario,
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

        dos.writeInt(idUsuario);
        dos.writeUTF(nome);
        dos.writeUTF(email);
        dos.writeUTF(hashSenha);
        dos.writeUTF(perguntaSecreta);
        dos.writeUTF(hashPerguntaSecreta);

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

        idUsuario = dis.readInt();
        nome = dis.readUTF();
        email = dis.readUTF();
        hashSenha = dis.readUTF();
        perguntaSecreta = dis.readUTF();
        hashPerguntaSecreta = dis.readUTF();
    }
}