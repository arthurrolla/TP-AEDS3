package entidades;

import aed3.*;
import java.util.ArrayList;

public class ArquivoResposta extends Arquivo<Resposta> {

    ArvoreBMais<ParIdId> relUsuarioResposta;
    ArvoreBMais<ParIdId> relPerguntaResposta;
    private ArquivoAvaliacao arquivoAvaliacao;
    private ArquivoUsuario arquivoUsuario;
    private ArquivoPergunta arquivoPergunta;

    /**
     * Construtor que inicializa o arquivo de respostas e os índices de relacionamento com usuários e perguntas.
     * Abre o arquivo de respostas e os índices do relacionamento usuário -> respostas e pergunta -> respostas
     * 
     * @throws Exception Se ocorrer algum erro ao abrir os arquivos ou criar os índices
     */
    public ArquivoResposta(ArquivoUsuario arquivoUsuario,ArquivoPergunta arquivoPergunta) throws Exception {
        super("respostas", Resposta.class.getConstructor());
        this.arquivoUsuario = arquivoUsuario;
        this.arquivoPergunta = arquivoPergunta;

        relUsuarioResposta = new ArvoreBMais<>(ParIdId.class.getConstructor(),5,"./dados/respostas/relUsuarioResposta.db");
        relPerguntaResposta = new ArvoreBMais<>(ParIdId.class.getConstructor(),5,"./dados/respostas/relPerguntaResposta.db");
    }

    public void setArquivoAvaliacao(ArquivoAvaliacao arquivoAvaliacao) {
        this.arquivoAvaliacao = arquivoAvaliacao;
    }

    /**
     * Cria a resposta no arquivo e registra os vínculos com o usuário e a pergunta nas árvores B+, 
     * garantindo que o usuário e a pergunta existam.
     * 
     * @param novaResposta A resposta a ser criada
     * @return O ID gerado para a nova resposta
     * @throws Exception Se o usuário ou a pergunta não existirem
     */
    @Override
    public int create(Resposta novaResposta) throws Exception{
        if (novaResposta == null){
            throw new Exception("Resposta não informada.");
        }
        Usuario usuario = arquivoUsuario.read(novaResposta.getIdUsuario());
        if (usuario == null){
            throw new Exception("Não é possível criar resposta: usuário inexistente.");
        }

        Pergunta pergunta = arquivoPergunta.read(novaResposta.getIdPergunta());
        if (pergunta == null){
            throw new Exception("Não é possível criar resposta: pergunta inexistente.");
        }

        if (!pergunta.isAtiva()){
            throw new Exception("Não é possível responder a uma pergunta arquivada.");
        }

        String texto = novaResposta.getResposta();
        if (texto == null || texto.trim().isEmpty()){
            throw new Exception("A resposta não pode ficar vazia.");
        }
        // Define os valores iniciais de uma nova resposta.
        novaResposta.setCriacao(System.currentTimeMillis());
        novaResposta.setNota((short) 0);
        novaResposta.setAvaliacoes((short) 0);
        novaResposta.setAtiva(true);

        int idGerado = super.create(novaResposta);
        relUsuarioResposta.create(new ParIdId(novaResposta.getIdUsuario(), idGerado));
        relPerguntaResposta.create(new ParIdId(novaResposta.getIdPergunta(), idGerado));
        return idGerado;
    }

    /**
     * Marca a resposta como arquivada.
     * 
     * @param idUsuario O ID do usuário que está arquivando a resposta
     * @param idResposta O ID da resposta a ser arquivada
     * @return true se a resposta foi arquivada com sucesso, false caso contrário
     * @throws Exception Se o usuário não tiver permissão ou houver vínculo inválido
     */
    public boolean arquivar(int idUsuario, int idResposta) throws Exception{
        Resposta resposta = super.read(idResposta);
        if (resposta == null) {
            return false;
        }
        Usuario usuario = arquivoUsuario.read(idUsuario);
        if (usuario == null) {
            throw new Exception("Usuário inexistente.");
        }
        Pergunta pergunta = arquivoPergunta.read(resposta.getIdPergunta());
        if (pergunta == null) {
            throw new Exception("A pergunta associada à resposta não existe.");
        }
        boolean autorResposta = resposta.getIdUsuario() == idUsuario;
        boolean autorPergunta = pergunta.getIdUsuario() == idUsuario;
        if (!autorResposta && !autorPergunta) {
            throw new Exception("Somente o autor da resposta ou da pergunta pode arquivá-la.");
        }
        if (!resposta.isAtiva()) {
            return false;
        }
        resposta.arquivar();
        return super.update(resposta);
    }

    // Uso interno (ArquivoAvaliacao): soma deltas na nota e no número de avaliações da resposta
    boolean ajustarNota(int idResposta, int deltaNota, int deltaAvaliacoes) throws Exception{
        Resposta respostaExistente = super.read(idResposta);
        if (respostaExistente == null){
            return false;
        }
        respostaExistente.setNota((short) (respostaExistente.getNota() + deltaNota));
        respostaExistente.setAvaliacoes((short) (respostaExistente.getAvaliacoes() + deltaAvaliacoes));
        return super.update(respostaExistente);
    }

    /**
     * Busca todas as respostas de um usuário usando a árvore B+.
     * 
     * @param idUsuario O ID do usuário
     * @return Uma lista com todas as respostas do usuário
     * @throws Exception Se ocorrer algum erro ao buscar as respostas
     */
    // TODO: Adicionar metodo com paginação, ordenada por data, para facilitar a visualização
    private ArrayList<Resposta> lerTodasPorUsuario(int idUsuario) throws Exception {
        ArrayList<Resposta> respostasDoUsuario = new ArrayList<>();
        ArrayList<ParIdId> paresUsuarioResposta = relUsuarioResposta.read(new ParIdId(idUsuario, -1));

        for (ParIdId par : paresUsuarioResposta){
            Resposta resposta = super.read(par.getId2());
            if (resposta != null){
                respostasDoUsuario.add(resposta);
            }
        }
        return respostasDoUsuario;
    }

    // Lista somente as respostas ativas do usuário.
    public ArrayList<Resposta> readAllByUsuario(int idUsuario)throws Exception {
        ArrayList<Resposta> respostas = lerTodasPorUsuario(idUsuario);
        respostas.removeIf(resposta -> !resposta.isAtiva());
        return respostas;
    }

    /**
     * Busca todas as respostas de uma pergunta usando a árvore B+.
     * 
     * @param idPergunta O ID da pergunta
     * @return Uma lista com todas as respostas da pergunta
     * @throws Exception Se ocorrer algum erro ao buscar as respostas
     */
    // TODO: Adicionar metodo com paginação, ordenada por nota, para facilitar a visualização
    private ArrayList<Resposta> lerTodasPorPergunta(int idPergunta) throws Exception {
        ArrayList<Resposta> respostasDaPergunta = new ArrayList<>();
        ArrayList<ParIdId> paresPerguntaResposta = relPerguntaResposta.read(new ParIdId(idPergunta, -1));

        for (ParIdId par : paresPerguntaResposta){
            Resposta resposta = super.read(par.getId2());
            if (resposta != null) {
                respostasDaPergunta.add(resposta);
            }
        }
        return respostasDaPergunta;
    }

    // Lista somente respostas ativas, em ordem decrescente de nota.
    public ArrayList<Resposta> readAllByPergunta(int idPergunta)throws Exception {
        ArrayList<Resposta> respostas = lerTodasPorPergunta(idPergunta);
        respostas.removeIf(resposta -> !resposta.isAtiva());
        respostas.sort((primeira, segunda) -> {
            int comparacao = Short.compare(segunda.getNota(), primeira.getNota());
            if (comparacao != 0) {
                return comparacao;
            }
            return Integer.compare(primeira.getId(), segunda.getId());
        });
        return respostas;
    }

    /**
     * Uso interno: Arquiva as respostas de um usuário. (Ou esconde a autoria do usuário) quando o usuário é excluído.
     * 
     * @param idUsuario O ID do usuário
     * @throws Exception Se ocorrer algum erro ao arquivar as respostas
     */
    // TODO: Adicionar em metodo no ArquivoUsuario, para que ao excluir um usuário, todas as suas respostas sejam arquivadas
    public void arquivarRespostasDoUsuario(int idUsuario) throws Exception {
        ArrayList<Resposta> respostasDoUsuario = readAllByUsuario(idUsuario);
        for (Resposta resposta : respostasDoUsuario) {
            // Lógica para arquivar a resposta
            boolean arquivou = arquivar(resposta.getIdUsuario(), resposta.getId());
            if(!arquivou) {
                throw new Exception("Não foi possível arquivar uma resposta do usuário.");
            }
        }
    }

    /**
     * Uso interno: Arquiva as respostas quando sua pergunta é arquivada.
     * 
     * @param idPergunta O ID da pergunta
     * @throws Exception Se ocorrer algum erro ao arquivar as respostas
     */
    // TODO: Adicionar em metodo no ArquivoPergunta, para que ao arquivar uma pergunta, todas as suas respostas sejam arquivadas
    public void arquivarRespostasDaPergunta(int idPergunta) throws Exception {
        ArrayList<Resposta> respostasDaPergunta = readAllByPergunta(idPergunta);
        for (Resposta resposta : respostasDaPergunta) {
            // Lógica para arquivar a resposta
            boolean arquivou = arquivar(resposta.getIdUsuario(), resposta.getId());
            if(!arquivou) {
                throw new Exception("Não foi possível arquivar uma resposta da pergunta.");
            }
        }
    }

    // Bloqueado: alteração deve passar por arquivar() ou avaliar()
    @Override
    public boolean update(Resposta respostaAtualizada) throws Exception {
        throw new UnsupportedOperationException(
            "Não é possível atualizar uma resposta. Para alterar, arquive a resposta existente e crie uma nova resposta."
        );
    }

    // Uso interno: remove respostas quando seu usuário é excluído.
    void excluirPorUsuario(int idUsuario) throws Exception {
        for (Resposta resposta : lerTodasPorUsuario(idUsuario)) {
            if (!excluirInternamente(resposta.getId())) {
                throw new Exception("Não foi possível excluir uma resposta do usuário.");
            }
        }
    }

    // Uso interno: remove respostas quando sua pergunta é excluída.
    void excluirPorPergunta(int idPergunta) throws Exception {
        for (Resposta resposta : lerTodasPorPergunta(idPergunta)) {
            if (!excluirInternamente(resposta.getId())) {
                throw new Exception("Não foi possível excluir uma resposta da pergunta.");
            }
        }
    }

    /**
     * Uso interno: Remove uma resposta.
     * Remove fisicamente uma resposta e seus vínculos nas rotinas internas.
     * 
     * @param id O ID da resposta
     * @return true se a resposta for removida, false caso contrário
     * @throws Exception Se ocorrer algum erro ao remover a resposta
     */
    private boolean excluirInternamente(int id) throws Exception {
        Resposta resposta = super.read(id);
        if(resposta != null) {
            if (arquivoAvaliacao == null){
                throw new Exception("Abra o arquivo de avaliações antes de excluir uma resposta.");
            }
            arquivoAvaliacao.excluirPorResposta(id);
            relUsuarioResposta.delete(new ParIdId(resposta.getIdUsuario(), id));
            relPerguntaResposta.delete(new ParIdId(resposta.getIdPergunta(), id));
            return super.delete(id);
        }
        return false;
    }

    @Override
    public boolean delete(int id) throws Exception {
        throw new UnsupportedOperationException("Respostas não podem ser excluídas diretamente. " + "Use arquivar(idUsuario, idResposta).");
    }
}