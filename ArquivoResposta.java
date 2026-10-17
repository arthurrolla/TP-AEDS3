package entidades;

import aed3.*;
import java.util.ArrayList;

public class ArquivoResposta extends Arquivo<Resposta> {

    ArvoreBMais<ParIdId> relUsuarioResposta;
    ArvoreBMais<ParIdId> relPerguntaResposta;
    
    /**
     * Construtor que inicializa o arquivo de respostas e os índices de relacionamento com usuários e perguntas.
     * Abre o arquivo de respostas e os índices do relacionamento usuário -> respostas e pergunta -> respostas
     * 
     * @throws Exception Se ocorrer algum erro ao abrir os arquivos ou criar os índices
     */
    public ArquivoResposta() throws Exception {
        super("respostas", Resposta.class.getConstructor());
        relUsuarioResposta = new ArvoreBMais<>(ParIdId.class.getConstructor(),
            5,
            "./dados/respostas/relUsuarioResposta.db");
        relPerguntaResposta = new ArvoreBMais<>(ParIdId.class.getConstructor(),
            5,
            "./dados/respostas/relPerguntaResposta.db");
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
    public int create(Resposta novaResposta) throws Exception {
        int idUsuario = novaResposta.getIdUsuario();
        if (idUsuario == -1) {
            throw new Exception(
                "Não é possível criar resposta: usuário inexistente."
            );
        }
        int idPergunta = novaResposta.getIdPergunta();
        if (idPergunta == -1) {
            throw new Exception(
                "Não é possível criar resposta: pergunta inexistente."
            );
        }
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
     * @throws Exception Se o usuário não for o dono da resposta ou se a resposta não existir
     */
    public boolean arquivar(int idUsuario, int idResposta) throws Exception {
        Resposta respostaExistente = super.read(idResposta);
        if (respostaExistente == null) {
            return false;
        }
        if (respostaExistente.getIdUsuario() != idUsuario) {
            throw new Exception(
                "Você só pode arquivar suas próprias respostas."
            );
        }
        if (!respostaExistente.isAtiva()) {
            return false;
        }
        respostaExistente.arquivar();
        super.update(respostaExistente);
        return true;
    }

    /**
     * Armazena a avaliação de uma resposta, atualizando a nota e o número de avaliações.
     * 
     * @param idUsuario O ID do usuário que está avaliando a resposta
     * @param idResposta O ID da resposta a ser avaliada
     * @param avaliacao A avaliação da resposta (true para positiva, false para negativa)
     * @return true se a avaliação foi registrada com sucesso, false caso contrário
     * @throws Exception Se o usuário não for o dono da resposta ou se a resposta não existir
     */
    // TODO: Adicionar ligamento com o arquivo de avaliações. Parte 2
    public boolean avaliar(int idUsuario, int idResposta, boolean avaliacao) throws Exception {
        Resposta respostaExistente = super.read(idResposta);
        if (respostaExistente == null) {
            return false;
        }
        if (respostaExistente.getIdUsuario() == idUsuario) {
            throw new Exception(
                "Você não pode avaliar sua própria resposta."
            );
        }
        short novaNota = (short) (respostaExistente.getNota() + (avaliacao ? 1 : -1));
        short novoNumeroAvaliacoes = (short) (respostaExistente.getAvaliacoes() + 1);
        respostaExistente.setNota(novaNota);
        respostaExistente.setAvaliacoes(novoNumeroAvaliacoes);
        super.update(respostaExistente);
        return true;
    }

    /**
     * Busca todas as respostas de um usuário usando a árvore B+.
     * 
     * @param idUsuario O ID do usuário
     * @return Uma lista com todas as respostas do usuário
     * @throws Exception Se ocorrer algum erro ao buscar as respostas
     */
    // TODO: Adicionar metodo com paginação, ordenada por data, para facilitar a visualização
    public ArrayList<Resposta> readAllByUsuario(int idUsuario) throws Exception {
        ArrayList<Resposta> respostasDoUsuario = new ArrayList<>();
        ArrayList<ParIdId> paresUsuarioResposta = relUsuarioResposta.read(new ParIdId(idUsuario, -1));

        for (ParIdId par : paresUsuarioResposta) {
            Resposta resposta = super.read(par.getId2());
            if (resposta != null) {
                respostasDoUsuario.add(resposta);
            }
        }
        return respostasDoUsuario;
    }

    /**
     * Busca todas as respostas de uma pergunta usando a árvore B+.
     * 
     * @param idPergunta O ID da pergunta
     * @return Uma lista com todas as respostas da pergunta
     * @throws Exception Se ocorrer algum erro ao buscar as respostas
     */
    // TODO: Adicionar metodo com paginação, ordenada por nota, para facilitar a visualização
    public ArrayList<Resposta> readAllByPergunta(int idPergunta) throws Exception {
        ArrayList<Resposta> respostasDaPergunta = new ArrayList<>();
        ArrayList<ParIdId> paresPerguntaResposta = relPerguntaResposta.read(new ParIdId(idPergunta, -1));

        for (ParIdId par : paresPerguntaResposta) {
            Resposta resposta = super.read(par.getId2());
            if (resposta != null) {
                respostasDaPergunta.add(resposta);
            }
        }
        return respostasDaPergunta;
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

    /**
     * Uso interno: Remove uma resposta.
     * Utilizar apenas para remover respostas em casos de infrações graves, como spam ou conteúdo impróprio.
     * 
     * @param id O ID da resposta
     * @return true se a resposta for removida, false caso contrário
     * @throws Exception Se ocorrer algum erro ao remover a resposta
     */
    @Override
    public boolean delete(int id) throws Exception {
        Resposta resposta = super.read(id);
        if(resposta != null) {
            relUsuarioResposta.delete(new ParIdId(resposta.getIdUsuario(), id));
            relPerguntaResposta.delete(new ParIdId(resposta.getIdPergunta(), id));
            return super.delete(id);
        }
        return false;
    }
}
