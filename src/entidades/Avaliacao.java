package entidades;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Avaliacao implements aed3.InterfaceRegistro {
    private int idAvaliacao;
    private int idUsuario;
    private int idResposta;
    private boolean avaliacao; // falso para negativo, verdadeiro para positivo

    /**
     * Construtor padrão que inicializa uma avaliação com valores padrão.
     */
    public Avaliacao() {
        this.idAvaliacao = -1;
        this.idUsuario = -1;
        this.idResposta = -1;
        this.avaliacao = false;
    }

    /**
     * Construtor que inicializa uma avaliação com todos os seus atributos.
     * 
     * @param idAvaliacao Identificador único da avaliação
     * @param idResposta Identificador único da resposta
     * @param idUsuario Identificador único do usuário
     * @param avaliacao A avaliação
     */
    public Avaliacao(int idAvaliacao, int idResposta, int idUsuario, boolean avaliacao) {
        this.idAvaliacao = idAvaliacao;
        this.idResposta = idResposta;
        this.idUsuario = idUsuario;
        this.avaliacao = avaliacao;
    }

    /**
     * Obtém o identificador único da avaliação.
     * 
     * @return O ID da avaliação
     */
    @Override
    public int getId() {
        return idAvaliacao;
    }

    /**
     * Define o identificador único da avaliação.
     * 
     * @param idAvaliacao O ID da avaliação
     */
    @Override
    public void setId(int idAvaliacao) {
        this.idAvaliacao = idAvaliacao;
    }

    /**
     * Obtém o identificador único do usuário.
     * 
     * @return O ID do usuário
     */
    public int getIdUsuario() {
        return idUsuario;
    }

    /**
     * Define o identificador único do usuário.
     * 
     * @param idUsuario O ID do usuário
     */
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    /**
     * Obtém o identificador único da resposta.
     * 
     * @return O ID da resposta
     */
    public int getIdResposta() {
        return idResposta;
    }

    /**
     * Define o identificador único da resposta.
     * 
     * @param idResposta O ID da resposta
     */
    public void setIdResposta(int idResposta) {
        this.idResposta = idResposta;
    }

    /**
     * Obtém a avaliação.
     * 
     * @return A avaliação
     */
    public boolean isAvaliacao() {
        return avaliacao;
    }

    /**
     * Define a avaliação.
     * 
     * @param avaliacao A avaliação
     */
    public void setAvaliacao(boolean avaliacao) {
        this.avaliacao = avaliacao;
    }

    @Override 
    public String toString() {
        return String.format(
            "Avaliacao{ID = %d%n" +
            "ID Usuario = %d%n" +
            "ID Resposta = %d%n" +
            "Avaliacao = %b}%n",
            idAvaliacao, idUsuario, idResposta, avaliacao
        );
    }
    
    /**
     * Serializa o objeto Avaliacao em um array de bytes.
     * 
     * @return Um array de bytes representando o objeto Avaliacao
     * @throws Exception Se ocorrer algum erro durante a serialização
     */
    @Override
    public byte[] serialize() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream saida = new DataOutputStream(buffer);

        saida.writeInt(idAvaliacao);
        saida.writeInt(idUsuario);
        saida.writeInt(idResposta);
        saida.writeBoolean(avaliacao);

        return buffer.toByteArray();
    }

    /**
     * Desserializa um array de bytes para reconstruir o objeto Avaliacao.
     * 
     * @param dados Um array de bytes representando o objeto Avaliacao
     * @throws Exception Se ocorrer algum erro durante a desserialização
     */
    @Override
    public void deserialize(byte[] dados) throws Exception {
        ByteArrayInputStream buffer = new ByteArrayInputStream(dados);
        DataInputStream entrada = new DataInputStream(buffer);

        idAvaliacao = entrada.readInt();
        idUsuario = entrada.readInt();
        idResposta = entrada.readInt();
        avaliacao = entrada.readBoolean();
    }
}