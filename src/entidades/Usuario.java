package entidades;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.text.Normalizer;
import java.util.Locale;

/**
 * Classe que representa um livro com seus atributos e métodos de serialização.
 * Realiza validações nos dados de entrada, especialmente no ISBN.
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
        this.idUsuario = -1;
        this.nome = "";
        this.email = "";
        this.hashSenha = "";
        this.perguntaSecreta = "";
        this.hashPerguntaSecreta = "";
    }

    public Usuario(String nome, String email, String hashSenha, String perguntaSecreta, String hashPerguntaSecreta) {
        this(-1, nome, email, hashSenha, perguntaSecreta, hashPerguntaSecreta);
    }



    /**
     * Construtor que inicializa um livro com todos os seus atributos.
     * Valida o ISBN, edição, número de páginas e preço.
     * 
     * @param id Identificador único do livro
     * @param isbn Código ISBN-13 do livro
     * @param titulo Título do livro
     * @param idCategoria Identificador da categoria do livro
     * @param editora Editora do livro
     * @param edicao Número da edição (0-255)
     * @param dataLancamento Data de lançamento do livro
     * @param numeroPaginas Número de páginas (0-65535)
     * @param precoEmCentavos Preço em centavos
     * @param ebook Indica se é disponível em formato e-book
     */
    public Usuario(int id,
                 String nome,
                 String email,
                 String hashSenha,
                 String perguntaSecreta,
                 String hashPerguntaSecreta) {

        this.idUsuario = id;

        // O ISBN vazio é permitido apenas para possibilitar
        // a construção de um objeto ainda não preenchido.
        if (email == null || email.isEmpty())
            this.email = "";
        else 
            setEmail(email);
        
        this.nome = nome;
        setHashSenha(hashSenha);
        setPerguntaSecreta(perguntaSecreta);
        setHashPerguntaSecreta(hashPerguntaSecreta);
    }

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
     * Obtém o email do usuario.
     * 
     * @return O email do usuario
     */
    public String getEmail() {
        return email;
    }

    /**
     * Define o email do usuario.
     * 
     * @param email O email a ser atribuído
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
     * Define a senha do usuario.
     * 
     * @param hashSenha A senha a ser atribuída (armazenada como hash)
     */
    public void setHashSenha(String hashSenha) {
        if(hashSenha == null || hashSenha.trim().isEmpty()) {
            throw new IllegalArgumentException("Senha não pode ficar vazia.");
        }
        String salt = generateSalt();
        this.hashSenha = salt + ":" + hashPassword(hashSenha, salt);
    }

    public boolean verificarSenha(String senha) {
        if(senha == null || this.hashSenha == null) {
            return false;
        }

        String[] partes = this.hashSenha.split(":", 2);
        if(partes.length != 2) {
            return false;
        }

        String hashCalculado = hashPassword(senha, partes[0]);
        return MessageDigest.isEqual(partes[1].getBytes(StandardCharsets.UTF_8),hashCalculado.getBytes(StandardCharsets.UTF_8));
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
        if(hashPerguntaSecreta == null || hashPerguntaSecreta.trim().isEmpty()) {
            throw new IllegalArgumentException("Resposta secreta não pode ficar vazia.");
        }
        String resposta = normalizarResposta(hashPerguntaSecreta);
        String salt = generateSalt();
        this.hashPerguntaSecreta = salt + ":" + hashPassword(resposta, salt);
    }

    private String normalizarResposta(String resposta) {
        return Normalizer.normalize(resposta, Normalizer.Form.NFD).replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }
    
    public boolean verificarRespostaSecreta(String resposta) {
        if(resposta == null || this.hashPerguntaSecreta == null) {
            return false;
        }
        String[] partes = this.hashPerguntaSecreta.split(":", 2);
        if(partes.length != 2) {
            return false;
        }

        String respostaNormalizada = normalizarResposta(resposta);
        String hashCalculado = hashPassword(respostaNormalizada, partes[0]);

        return MessageDigest.isEqual(partes[1].getBytes(StandardCharsets.UTF_8),hashCalculado.getBytes(StandardCharsets.UTF_8));
    }

    // O método hashCode() deve retornar um número
    // inteiro positivo e exclusivo
    @Override
    public int hashCode() {
        return Math.abs(this.hashSenha.hashCode());
    }

    /**
     * Retorna uma representação em texto formatado dos dados do autor.
     * Exibe todos os atributos de forma legível com a data no formato dd/MM/yyyy.
     * 
     * @return String contendo os dados do autor formatados
     */
    @Override
    public String toString() {
        return String.format("Nome: %s%n" +"Email: %s%n",nome,email);
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

    //---------

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    private String hashPassword(String password, String saltStr) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Combine salt and password
            digest.update(Base64.getDecoder().decode(saltStr));
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // Cleaner hex conversion using String.format
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("SHA-256 algorithm not found", ex);
        }
    }
}