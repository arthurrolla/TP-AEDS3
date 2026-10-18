package entidades;

import aed3.*;
import java.util.ArrayList;

public class ArquivoAvaliacao extends Arquivo<Avaliacao> {

    ArvoreBMais<ParIdId> relRespostaAvaliacao;
    ArvoreBMais<ParIdId> relUsuarioAvaliacao;
    private ArquivoResposta arquivoResposta;

    // Abre o arquivo de avaliações e os índices resposta -> avaliações e usuário -> avaliações
    public ArquivoAvaliacao(ArquivoResposta arquivoResposta) throws Exception {
        super("avaliacoes", Avaliacao.class.getConstructor());
        this.arquivoResposta = arquivoResposta;
        relRespostaAvaliacao = new ArvoreBMais<>(ParIdId.class.getConstructor(),
            5,
            "./dados/avaliacoes/relRespostaAvaliacao.db");
        relUsuarioAvaliacao = new ArvoreBMais<>(ParIdId.class.getConstructor(),
            5,
            "./dados/avaliacoes/relUsuarioAvaliacao.db");
        arquivoResposta.setArquivoAvaliacao(this);
    }

    // Cria a avaliação (exige resposta existente) e registra os vínculos nas duas árvores B+
    @Override
    public int create(Avaliacao avaliacao) throws Exception {
        if (arquivoResposta.read(avaliacao.getIdResposta()) == null) {
            throw new Exception(
                "Não é possível criar avaliação: resposta inexistente."
            );
        }
        int idGerado = super.create(avaliacao);
        relRespostaAvaliacao.create(new ParIdId(avaliacao.getIdResposta(), idGerado));
        relUsuarioAvaliacao.create(new ParIdId(avaliacao.getIdUsuario(), idGerado));
        return idGerado;
    }

    // Avalia uma resposta: impede avaliação dupla e atualiza nota e contador da resposta
    public boolean avaliar(int idUsuario, int idResposta, boolean positiva) throws Exception {
        Resposta resposta = arquivoResposta.read(idResposta);
        if (resposta == null) {
            return false;
        }
        if (resposta.getIdUsuario() == idUsuario) {
            throw new Exception("Você não pode avaliar sua própria resposta.");
        }
        if (buscarPorUsuarioResposta(idUsuario, idResposta) != null) {
            throw new Exception("Você já avaliou esta resposta.");
        }
        create(new Avaliacao(-1, idResposta, idUsuario, positiva));
        return arquivoResposta.ajustarNota(idResposta, positiva ? 1 : -1, 1);
    }

    // Cancela a avaliação: remove registro e índices e reverte nota e contador da resposta
    public boolean cancelar(int idAvaliacao) throws Exception {
        Avaliacao avaliacao = super.read(idAvaliacao);
        if (avaliacao == null) {
            return false;
        }
        removerVinculos(avaliacao);
        if (!super.delete(idAvaliacao)) {
            return false;
        }
        arquivoResposta.ajustarNota(avaliacao.getIdResposta(), avaliacao.isAvaliacao() ? -1 : 1, -1);
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
            "Use cancelar(idAvaliacao) e avaliar(idUsuario, idResposta, positiva) para alterar uma avaliação."
        );
    }

    // Bloqueado: a remoção deve passar por cancelar() para manter a nota da resposta consistente
    @Override
    public boolean delete(int id) throws Exception {
        throw new UnsupportedOperationException(
            "Use cancelar(idAvaliacao) para remover uma avaliação."
        );
    }
}