package entidades;

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
public class Livro implements aed3.InterfaceRegistro {

    /** Identificador único do livro */
    private int id;
    
    /** Código ISBN-13 do livro */
    private String isbn;
    
    /** Título do livro */
    private String titulo;
        
    /** Categoria ou gênero do livro */
    private int idCategoria;
    
    /** Editora responsável pela publicação */
    private String editora;
    
    /** Número da edição (0 a 255) */
    private short edicao;
    
    /** Data de lançamento do livro */
    private LocalDate dataLancamento;
    
    /** Número total de páginas do livro (0 a 65535) */
    private int numeroPaginas;
    
    /** Preço do livro em centavos */
    private int precoEmCentavos;
    
    /** Indica se o livro é disponível em formato e-book */
    private boolean ebook;

    /**
     * Construtor padrão que inicializa um livro com valores padrão.
     */
    public Livro() {
        this(-1, "", "", -1, "", 0, LocalDate.now(), 0, 0, false);
    }

    public Livro(String isbn, String titulo, int idCategoria, String editora, int edicao, LocalDate dataLancamento, int numeroPaginas, int precoEmCentavos, boolean ebook) {
        this(-1, isbn, titulo, idCategoria, editora, edicao, dataLancamento, numeroPaginas, precoEmCentavos, ebook);
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
    public Livro(int id,
                 String isbn,
                 String titulo,
                 int idCategoria,
                 String editora,
                 int edicao,
                 LocalDate dataLancamento,
                 int numeroPaginas,
                 int precoEmCentavos,
                 boolean ebook) {

        this.id = id;

        // O ISBN vazio é permitido apenas para possibilitar
        // a construção de um objeto ainda não preenchido.
        if (isbn == null || isbn.isEmpty())
            this.isbn = "";
        else 
            setIsbn(isbn);
        
        this.titulo = titulo;
        this.idCategoria = idCategoria;
        this.editora = editora;
        setEdicao(edicao);
        this.dataLancamento = dataLancamento;
        setNumeroPaginas(numeroPaginas);
        setPrecoEmCentavos(precoEmCentavos);
        this.ebook = ebook;
    }

    /**
     * Obtém o identificador único do livro.
     * 
     * @return O ID do livro
     */
    public int getId() {
        return id;
    }

    /**
     * Define o identificador único do livro.
     * 
     * @param id O ID a ser atribuído
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtém o código ISBN do livro.
     * 
     * @return O ISBN do livro
     */
    public String getIsbn() {
        return isbn;
    }

    /**
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
     * Obtém o título do livro.
     * 
     * @return O título do livro
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * Define o título do livro.
     * 
     * @param titulo O título a ser atribuído
     */
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /**
     * Obtém a categoria ou gênero do livro.
     * 
     * @return A categoria do livro
     */
    public int getIdCategoria() {
        return idCategoria;
    }

    /**
     * Define a categoria ou gênero do livro.
     * 
     * @param idCategoria O identificador da categoria a ser atribuída
     */
    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    /**
     * Obtém a editora do livro.
     * 
     * @return A editora do livro
     */
    public String getEditora() {
        return editora;
    }

    /**
     * Define a editora do livro.
     * 
     * @param editora A editora a ser atribuída
     */
    public void setEditora(String editora) {
        this.editora = editora;
    }

    /**
     * Obtém o número da edição do livro.
     * 
     * @return O número da edição
     */
    public short getEdicao() {
        return edicao;
    }

    /**
     * Define o número da edição do livro.
     * O valor deve estar entre 0 e 255 (unsigned byte).
     * 
     * @param edicao O número da edição
     * @throws IllegalArgumentException Se a edição estiver fora do intervalo 0-255
     */
    public void setEdicao(int edicao) {
        if (edicao < 0 || edicao > 255)
            throw new IllegalArgumentException(
                "A edição deve estar entre 0 e 255."
            );
        this.edicao = (short) edicao;
    }

    /**
     * Obtém a data de lançamento do livro.
     * 
     * @return A data de lançamento
     */
    public LocalDate getDataLancamento() {
        return dataLancamento;
    }

    /**
     * Define a data de lançamento do livro.
     * 
     * @param dataLancamento A data a ser atribuída
     */
    public void setDataLancamento(LocalDate dataLancamento) {
        this.dataLancamento = dataLancamento;
    }

    /**
     * Obtém o número de páginas do livro.
     * 
     * @return O número de páginas
     */
    public int getNumeroPaginas() {
        return numeroPaginas;
    }

    /**
     * Define o número de páginas do livro.
     * O valor deve estar entre 0 e 65535 (unsigned short).
     * 
     * @param numeroPaginas O número de páginas
     * @throws IllegalArgumentException Se o número de páginas estiver fora do intervalo 0-65535
     */
    public void setNumeroPaginas(int numeroPaginas) {
        if (numeroPaginas < 0 || numeroPaginas > 65535)
            throw new IllegalArgumentException(
                "O número de páginas deve estar entre 0 e 65535."
            );
        this.numeroPaginas = numeroPaginas;
    }

    /**
     * Obtém o preço do livro em centavos.
     * 
     * @return O preço em centavos
     */
    public int getPrecoEmCentavos() {
        return precoEmCentavos;
    }

    /**
     * Define o preço do livro em centavos.
     * O preço não pode ser negativo.
     * 
     * @param precoEmCentavos O preço em centavos
     * @throws IllegalArgumentException Se o preço for negativo
     */
    public void setPrecoEmCentavos(int precoEmCentavos) {
        if (precoEmCentavos < 0)
            throw new IllegalArgumentException(
                "O preço não pode ser negativo."
            );
        this.precoEmCentavos = precoEmCentavos;
    }

    /**
     * Verifica se o livro está disponível em formato e-book.
     * 
     * @return true se é e-book, false caso contrário
     */
    public boolean isEbook() {
        return ebook;
    }

    /**
     * Define se o livro está disponível em formato e-book.
     * 
     * @param ebook true para e-book, false caso contrário
     */
    public void setEbook(boolean ebook) {
        this.ebook = ebook;
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
     * Retorna uma representação em texto formatado dos dados do livro.
     * Exibe todos os atributos de forma legível com a data no formato dd/MM/yyyy.
     * 
     * @return String contendo os dados do livro formatados
     */
    @Override
    public String toString() {

        DateTimeFormatter formatoData =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return String.format(
            "ID................: %d%n" +
            "ISBN..............: %s%n" +
            "Título............: %s%n" +
            "Categoria.........: %d%n" +
            "Editora...........: %s%n" +
            "Edição............: %d%n" +
            "Data de lançamento: %s%n" +
            "Número de páginas.: %d%n" +
            "Preço.............: R$ %.2f%n" +
            "E-book............: %s%n",
            id,
            isbn,
            titulo,
            idCategoria,
            editora,
            edicao,
            dataLancamento.format(formatoData),
            numeroPaginas,
            precoEmCentavos / 100.0,
            ebook ? "Sim" : "Não"
        );
    }

    /**
     * Serializa os dados do livro em um array de bytes.
     * Converte todos os atributos para formato binário, adequado para armazenamento em arquivo.
     * 
     * @return Array de bytes contendo os dados serializados
     * @throws Exception Se houver erro durante a serialização
     */
    public byte[] serialize() throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id);
        dos.write(isbn.getBytes());
        dos.writeUTF(titulo);
        dos.writeInt(idCategoria);
        dos.writeUTF(editora);
        dos.writeByte(edicao); // unsigned byte: 0 a 255
        dos.writeInt((int) dataLancamento.toEpochDay());
        dos.writeShort(numeroPaginas); // unsigned short: 0 a 65535
        dos.writeInt(precoEmCentavos);
        dos.writeBoolean(ebook);

        return baos.toByteArray();
    }

    /**
     * Desserializa os dados do livro a partir de um array de bytes.
     * Reconstrói todos os atributos a partir dos dados binários armazenados em arquivo.
     * 
     * @param data Array de bytes contendo os dados a desserializar
     * @throws Exception Se houver erro durante a desserialização
     */
    public void deserialize(byte[] data) throws Exception {
        
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);

        id = dis.readInt();
        byte[] isbnBytes = new byte[13];
        dis.readFully(isbnBytes);
        isbn = new String(isbnBytes);
        titulo = dis.readUTF();
        idCategoria = dis.readInt();
        editora = dis.readUTF();
        edicao = (short) dis.readUnsignedByte(); // unsigned byte: 0 a 255
        dataLancamento = LocalDate.ofEpochDay(dis.readInt());
        numeroPaginas = dis.readUnsignedShort(); // unsigned short: 0 a 65535
        precoEmCentavos = dis.readInt();
        ebook = dis.readBoolean();
    }
}