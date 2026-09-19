package TP1.entidades;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Classe que representa um livro com seus atributos e métodos de serialização.
 * Realiza validações nos dados de entrada, especialmente no ISBN.
 */
public class Usuario2 implements aed3.InterfaceRegistro {

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
    public Usuario2() {
        this(-1, "", "", "", "", "");
    }

    public Usuario2(String nome, String email, String hashSenha, String perguntaSecreta, String hashPerguntaSecreta) {
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
    public Usuario2(int id,
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
            setIsbn(email);
        
        this.nome = nome;
        this.hashSenha = hashSenha;
        this.perguntaSecreta = perguntaSecreta;
        this.hashPerguntaSecreta = hashPerguntaSecreta;
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

    /** email
     * Define o código ISBN do livro após validação.
     * O ISBN deve ser uma string com 13 dígitos válidos segundo o algoritmo ISBN-13.
     * 
     * @param isbn O ISBN a ser atribuído
     * @throws IllegalArgumentException Se o ISBN for inválido
     */
    public void setIsbn(String isbn) {
        if (!validarIsbn(isbn))
            throw new IllegalArgumentException("ISBN inválido.");
        this.isbn = isbn;
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

    /**
     * Valida um código ISBN-13 usando o algoritmo de verificação oficial.
     * Verifica se a string contém exatamente 13 dígitos e se o dígito verificador está correto.
     * 
     * @param isbn A string de ISBN a ser validada
     * @return true se o ISBN é válido, false caso contrário
     */
    public static boolean validarIsbn(String isbn) {

        // Neste projeto usamos exclusivamente ISBN-13.
        if (isbn == null || !isbn.matches("\\d{13}"))
            return false;

        int soma = 0;
        for (int i = 0; i < 12; i++) {
            int digito = isbn.charAt(i) - '0';
            if (i % 2 == 0)
                soma += digito;
            else
                soma += 3 * digito;
        }
        int digitoVerificador = (10 - soma % 10) % 10;

        return digitoVerificador == isbn.charAt(12) - '0';
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