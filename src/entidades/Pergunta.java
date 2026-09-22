package entidades;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Pergunta implements aed3.InterfaceRegistro {

    private int id;
    private int idUsuario;
    private long criacao;
    private long alteracao;
    private short nota;
    private String pergunta;
    private String palavrasChave;
    private boolean ativa;

    // Construtor padrão, usado pelo CRUD genérico ao ler registros do arquivo
    public Pergunta() {
        this(-1, -1, 0, 0, (short) 0, "", "", true);
    }

    // Cria uma nova pergunta; id, datas e nota são preenchidos automaticamente
    public Pergunta(int idUsuario, String pergunta, String palavrasChave) {
        this(-1, idUsuario, System.currentTimeMillis(), System.currentTimeMillis(),
             (short) 0, pergunta, palavrasChave, true);
    }

    // Constrói a pergunta com todos os campos já definidos
    public Pergunta(int id, int idUsuario, long criacao, long alteracao,
                     short nota, String pergunta, String palavrasChave, boolean ativa) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.criacao = criacao;
        this.alteracao = alteracao;
        this.nota = nota;
        this.pergunta = pergunta == null ? "" : pergunta;
        this.palavrasChave = palavrasChave == null ? "" : palavrasChave;
        this.ativa = ativa;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public long getCriacao() {
        return criacao;
    }

    public long getAlteracao() {
        return alteracao;
    }

    // Atualiza a data de alteração; chamado pelo ArquivoPergunta a cada edição/arquivamento
    public void setAlteracao(long alteracao) {
        this.alteracao = alteracao;
    }

    public short getNota() {
        return nota;
    }

    public void setNota(short nota) {
        this.nota = nota;
    }

    public String getPergunta() {
        return pergunta;
    }

    public void setPergunta(String pergunta) {
        this.pergunta = pergunta == null ? "" : pergunta;
    }

    public String getPalavrasChave() {
        return palavrasChave;
    }

    public void setPalavrasChave(String palavrasChave) {
        this.palavrasChave = palavrasChave == null ? "" : palavrasChave;
    }

    public boolean isAtiva() {
        return ativa;
    }

    // Marca a pergunta como arquivada (não há como desarquivar)
    public void arquivar() {
        this.ativa = false;
    }

    // Monta o texto exibido na listagem: data, pergunta e palavras-chave
    @Override
    public String toString() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String dataFormatada = Instant.ofEpochMilli(criacao)
                .atZone(ZoneId.systemDefault())
                .format(formato);
        return dataFormatada + System.lineSeparator()
             + pergunta + System.lineSeparator()
             + "Palavras chave: " + palavrasChave;
    }

    // Converte a pergunta em bytes para gravação no arquivo de dados
    @Override
    public byte[] serialize() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream saida = new DataOutputStream(buffer);

        saida.writeInt(id);
        saida.writeInt(idUsuario);
        saida.writeLong(criacao);
        saida.writeLong(alteracao);
        saida.writeShort(nota);
        saida.writeUTF(pergunta);
        saida.writeUTF(palavrasChave);
        saida.writeBoolean(ativa);

        return buffer.toByteArray();
    }

    // Reconstrói a pergunta a partir dos bytes lidos do arquivo de dados
    @Override
    public void deserialize(byte[] dados) throws Exception {
        ByteArrayInputStream buffer = new ByteArrayInputStream(dados);
        DataInputStream entrada = new DataInputStream(buffer);

        id = entrada.readInt();
        idUsuario = entrada.readInt();
        criacao = entrada.readLong();
        alteracao = entrada.readLong();
        nota = entrada.readShort();
        pergunta = entrada.readUTF();
        palavrasChave = entrada.readUTF();
        ativa = entrada.readBoolean();
    }
}
