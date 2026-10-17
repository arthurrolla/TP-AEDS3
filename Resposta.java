package entidades;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Resposta implements aed3.InterfaceRegistro {
    private int idResposta;
    private int idPergunta;
    private int idUsuario;
    private long criacao;
    private String resposta;
    private short nota;         // Nota da resposta com base nas avaliações recebidas.
    private short avaliacoes;   // Quantas avaliações a resposta recebeu.
    private boolean ativa;

    // Construtor padrão que inicializa uma resposta com valores padrão.
    public Resposta() {
        this.idResposta = -1;
        this.idPergunta = -1;
        this.idUsuario = -1;
        this.criacao = Instant.now().toEpochMilli();
        this.resposta = "";
        this.nota = 0;
        this.avaliacoes = 0;
        this.ativa = true;
    }

    /**
     * Construtor que inicializa uma resposta com todos os seus atributos.
     * 
     * @param idResposta Identificador único da resposta
     * @param idPergunta Identificador único da pergunta
     * @param idUsuario Identificador único do usuário
     * @param criacao Timestamp da criação da resposta
     * @param resposta Resposta do usuário
     * @param nota Nota da resposta
     * @param avaliacoes Número de avaliações da resposta
     * @param ativa Indica se a resposta está ativa
     */
    public Resposta(int idResposta, int idPergunta, int idUsuario, long criacao, String resposta, short nota, short avaliacoes, boolean ativa) {
        this.idResposta = idResposta;
        this.idPergunta = idPergunta;
        this.idUsuario = idUsuario;
        this.criacao = criacao;
        this.resposta = resposta;
        this.nota = nota;
        this.avaliacoes = avaliacoes;
        this.ativa = ativa;
    }

    /**
     * Obtém o identificador único da resposta.
     * 
     * @return O ID da resposta
     */
    @Override
    public int getId() {
        return idResposta;
    }

    /**
     * Define o identificador único da resposta.
     * 
     * @param idResposta O ID da resposta
     */
    @Override
    public void setId(int idResposta) {
        this.idResposta = idResposta;
    }

    /**
     * Obtém o identificador único da pergunta.
     * 
     * @return O ID da pergunta
     */
    public int getIdPergunta() {
        return idPergunta;
    }

    /** Define o identificador único da pergunta.
     * 
     * @param idPergunta O ID da pergunta
     */
    public void setIdPergunta(int idPergunta) {
        this.idPergunta = idPergunta;
    }

    /**
     * Obtém o identificador único do usuário.
     * 
     * @return O ID do usuário
     */
    public int getIdUsuario() {
        return idUsuario;
    }

    /** Define o identificador único do usuário.
     * 
     * @param idUsuario O ID do usuário
     */
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    /**
     * Obtém o timestamp da criação da resposta.
     * 
     * @return O timestamp da criação
     */
    public long getCriacao() {
        return criacao;
    }

    /** Define o timestamp da criação da resposta.
     * 
     * @param criacao O timestamp da criação
     */
    public void setCriacao(long criacao) {
        this.criacao = criacao;
    }

    /**
     * Obtém a resposta do usuário.
     * 
     * @return A resposta do usuário
     */
    public String getResposta() {
        return resposta;
    }

    /** Define a resposta do usuário.
     * 
     * @param resposta A resposta do usuário
     */
    public void setResposta(String resposta) {
        this.resposta = resposta;
    }

    /**
     * Obtém a nota da resposta.
     * 
     * @return A nota da resposta
     */
    public short getNota() {
        return nota;
    }

    /** Define a nota da resposta.
     * 
     * @param nota A nota da resposta
     */
    public void setNota(short nota) {
        this.nota = nota;
    }

    /**
     * Obtém o número de avaliações da resposta.
     * 
     * @return O número de avaliações
     */
    public short getAvaliacoes() {
        return avaliacoes;
    }

    /** Define o número de avaliações da resposta.
     * 
     * @param avaliacoes O número de avaliações
     */
    public void setAvaliacoes(short avaliacoes) {
        this.avaliacoes = avaliacoes;
    }

    /**
     * Verifica se a resposta está ativa.
     * 
     * @return true se a resposta estiver ativa, false caso contrário
     */
    public boolean isAtiva() {
        return ativa;
    }

    /** Define se a resposta está ativa.
     * 
     * @param ativa true se a resposta estiver ativa, false caso contrário
     */
    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    /**
     * Arquiva a resposta.
     */
    public void arquivar() {
        this.ativa = false;
    }

    /**
     * Retorna uma representação textual da resposta.
     * Exibe todos os atributos da resposta em formato legível.
     * 
     * @return Uma string com os dados da resposta
     */
    @Override 
    public String toString() {
        String data = formatarData(criacao);
        return String.format(
            "Resposta{ID = %d%n" +
            "ID Pergunta = %d%n" +
            "ID Usuario = %d%n" +
            "criacao = %d%n" +
            "resposta = '%s'%n" +
            "nota = %d%n" +
            "avaliacoes = %d%n" +
            "ativa = %b%n",
            idResposta, idPergunta, idUsuario, data, resposta, nota, avaliacoes, ativa
        );
    }

    // Formata o timestamp em uma string legível.
    private String formatarData(long timestamp) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String dataFormatada = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime().format(formato);
        return dataFormatada;
    }

    /**
     * Serializa os dados da resposta em um array de bytes.
     * Converte todos os atributos para formato binário, adequado para armazenamento em arquivo.
     * 
     * @return Array de bytes contendo os dados serializados
     * @throws Exception Se houver erro durante a serialização
     */
    @Override
    public byte[] serialize() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream saida = new DataOutputStream(buffer);

        saida.writeInt(idResposta);
        saida.writeInt(idPergunta);
        saida.writeInt(idUsuario);
        saida.writeLong(criacao);
        saida.writeUTF(resposta);
        saida.writeShort(nota);
        saida.writeShort(avaliacoes);
        saida.writeBoolean(ativa);

        return buffer.toByteArray();
    }

    /**
     * Desserializa os dados da resposta a partir de um array de bytes.
     * Reconstrói todos os atributos a partir dos dados binários armazenados em arquivo.
     * 
     * @param dados Array de bytes contendo os dados a desserializar
     * @throws Exception Se houver erro durante a desserialização
     */
    @Override
    public void deserialize(byte[] dados) throws Exception {
        ByteArrayInputStream buffer = new ByteArrayInputStream(dados);
        DataInputStream entrada = new DataInputStream(buffer);

        idResposta = entrada.readInt();
        idPergunta = entrada.readInt();
        idUsuario = entrada.readInt();
        criacao = entrada.readLong();
        resposta = entrada.readUTF();
        nota = entrada.readShort();
        avaliacoes = entrada.readShort();
        ativa = entrada.readBoolean();
    }
}