package entidades;

import aed3.*;
import java.util.ArrayList;

public class ArquivoAvaliacao extends Arquivo<Avaliacao> {

    ArvoreBMais<ParIdId> relRespostaAvaliacao;
    ArvoreBMais<ParIdId> relUsuarioAvaliacao;
    private ArquivoResposta arquivoResposta;
    private ArquivoUsuario arquivoUsuario;

    // Abre o arquivo de avaliações e os índices resposta -> avaliações e usuário -> avaliações
    public ArquivoAvaliacao(ArquivoResposta arquivoResposta, ArquivoUsuario arquivoUsuario) throws Exception {
        super("avaliacoes", Avaliacao.class.getConstructor());
        this.arquivoResposta = arquivoResposta;
        this.arquivoUsuario = arquivoUsuario;
        relRespostaAvaliacao = new ArvoreBMais<>(ParIdId.class.getConstructor(),
            5,"./dados/avaliacoes/relRespostaAvaliacao.db");
        relUsuarioAvaliacao = new ArvoreBMais<>(ParIdId.class.getConstructor(),
            5,"./dados/avaliacoes/relUsuarioAvaliacao.db");
        arquivoResposta.setArquivoAvaliacao(this);
    }

    // Cria a avaliação (exige resposta existente) e registra os vínculos nas duas árvores B+
    @Override
    public int create(Avaliacao avaliacao) throws Exception {
        if (avaliacao == null) {
            throw new Exception("Avaliação não informada.");
        }
        Usuario usuario = arquivoUsuario.read(avaliacao.getIdUsuario());
        if (usuario == null) {
            throw new Exception("Usuário inexistente.");
        }
        Resposta resposta = arquivoResposta.read(avaliacao.getIdResposta());
        if (resposta == null) {
            throw new Exception("Resposta inexistente.");
        }
        if (!resposta.isAtiva()) {
            throw new Exception("Não é possível avaliar uma resposta arquivada.");
        }
        if (resposta.getIdUsuario() == avaliacao.getIdUsuario()) {
            throw new Exception("Você não pode avaliar sua própria resposta.");
        }
        if (buscarPorUsuarioResposta(avaliacao.getIdUsuario(),avaliacao.getIdResposta()) != null) {
            throw new Exception("Você já avaliou esta resposta.");
        }
        if (resposta.getAvaliacoes() == Short.MAX_VALUE) {
            throw new Exception("Esta resposta atingiu o limite de avaliações.");
        }
        int idGerado = super.create(avaliacao);
        boolean vinculouResposta = relRespostaAvaliacao.create(new ParIdId(avaliacao.getIdResposta(), idGerado));
        if (!vinculouResposta) {
            throw new Exception("Não foi possível registrar o vínculo com a resposta.");
        }
        boolean vinculouUsuario = relUsuarioAvaliacao.create(new ParIdId(avaliacao.getIdUsuario(), idGerado));
        if (!vinculouUsuario) {
            throw new Exception("Não foi possível registrar o vínculo com o usuário.");
        }
        boolean ajustou = arquivoResposta.ajustarNota(avaliacao.getIdResposta(),avaliacao.isAvaliacao() ? 1 : -1,1);
        if (!ajustou) {
            throw new Exception("A avaliação foi registrada, mas não foi possível ajustar a resposta.");
        }

        return idGerado;
    }

    // Avalia uma resposta: impede avaliação dupla e atualiza nota e contador da resposta
    public boolean avaliar(int idUsuario, int idResposta, boolean positiva)throws Exception {
        create(new Avaliacao(-1, idResposta, idUsuario, positiva));
        return true;
    }

    // Cancela somente uma avaliação pertencente ao usuário informado.
    public boolean cancelar(int idUsuario, int idAvaliacao) throws Exception {
        Usuario usuario = arquivoUsuario.read(idUsuario);
        if (usuario == null) {
            throw new Exception("Usuário inexistente.");
        }
        Avaliacao avaliacao = super.read(idAvaliacao);
        if (avaliacao == null) {
            return false;
        }
        if (avaliacao.getIdUsuario() != idUsuario) {
            throw new Exception("Você só pode cancelar suas próprias avaliações.");
        }
        return cancelarInternamente(idAvaliacao);
    }

    // Cancela a avaliação: remove registro e índices e reverte nota e contador da resposta
    private boolean cancelarInternamente(int idAvaliacao) throws Exception {
        Avaliacao avaliacao = super.read(idAvaliacao);
        if (avaliacao == null) {
            return false;
        }
        removerVinculos(avaliacao);
        if (!super.delete(idAvaliacao)) {
            return false;
        }
        boolean ajustou = arquivoResposta.ajustarNota(avaliacao.getIdResposta(),avaliacao.isAvaliacao() ? -1 : 1,-1);
        if (!ajustou) {
            throw new Exception("A avaliação foi removida, mas não foi possível ajustar a resposta.");
        }
        return true;
    }

    // Busca todas as avaliações de uma resposta usando a árvore B+
    public ArrayList<Avaliacao> getAvaliacoesPorResposta(int idResposta) throws Exception {
        return lerPorPares(relRespostaAvaliacao.read(new ParIdId(idResposta, -1)));
    }

    // Busca todas as avaliações feitas por um usuário usando a árvore B+
    public ArrayList<Avaliacao> readAvaliacoesPorUsuario(int idUsuario) throws Exception {
        return lerPorPares(relUsuarioAvaliacao.read(new ParIdId(idUsuario, -1)));
    }

    // Uso interno: cancela as avaliações quando seu usuário é excluído.
    void excluirPorUsuario(int idUsuario) throws Exception {
        ArrayList<Avaliacao> avaliacoes =
            readAvaliacoesPorUsuario(idUsuario);

        for (Avaliacao avaliacao : avaliacoes) {
            if (!cancelarInternamente(avaliacao.getId())) {
                throw new Exception(
                    "Não foi possível cancelar uma avaliação do usuário."
                );
            }
        }
    }

    // Uso interno: remove as avaliações de uma resposta excluída (a nota some junto com a resposta)
    void excluirPorResposta(int idResposta) throws Exception {
        for (Avaliacao avaliacao : getAvaliacoesPorResposta(idResposta)) {
            removerVinculos(avaliacao);
            if (!super.delete(avaliacao.getId())) {
                throw new Exception("Não foi possível excluir uma avaliação da resposta.");
            }
        }
    }

    private Avaliacao buscarPorUsuarioResposta(int idUsuario, int idResposta) throws Exception {
        for (Avaliacao avaliacao : readAvaliacoesPorUsuario(idUsuario)) {
            if (avaliacao.getIdResposta() == idResposta) {
                return avaliacao;
            }
        }
        return null;
    }

    private ArrayList<Avaliacao> lerPorPares(ArrayList<ParIdId> pares) throws Exception {
        ArrayList<Avaliacao> avaliacoes = new ArrayList<>();
        for (ParIdId par : pares) {
            Avaliacao avaliacao = super.read(par.getId2());
            if (avaliacao != null) {
                avaliacoes.add(avaliacao);
            }
        }
        return avaliacoes;
    }

    private void removerVinculos(Avaliacao avaliacao) throws Exception {
        relRespostaAvaliacao.delete(new ParIdId(avaliacao.getIdResposta(), avaliacao.getId()));
        relUsuarioAvaliacao.delete(new ParIdId(avaliacao.getIdUsuario(), avaliacao.getId()));
    }

    // Bloqueado: trocar o voto deve ser feito com cancelar() e avaliar()
    @Override
    public boolean update(Avaliacao avaliacao) throws Exception {
        throw new UnsupportedOperationException(
            "Use cancelar(idUsuario, idAvaliacao) e avaliar(idUsuario, idResposta, positiva) para alterar uma avaliação."
        );
    }

    // Bloqueado: a remoção deve passar por cancelar() para manter a nota da resposta consistente
    @Override
    public boolean delete(int id) throws Exception {
        throw new UnsupportedOperationException(
            "Use cancelar(idUsuario, idAvaliacao) para remover uma avaliação."
        );
    }
}