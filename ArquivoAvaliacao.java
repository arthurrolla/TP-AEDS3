package entidades;

import aed3.*;
import java.util.ArrayList;

public class ArquivoAvaliacao extends Arquivo<Avaliacao> {

    ArvoreBMais<ParIdId> relRespostaAvaliacao;

    /**
     * Construtor que inicializa o arquivo de avaliações e o índice de relacionamento com respostas.
     * Abre o arquivo de avaliações e o índice do relacionamento resposta -> avaliações.
     * 
     * @throws Exception Se ocorrer algum erro ao criar o arquivo ou o índice
     */
    public ArquivoAvaliacao() throws Exception {
        super("avaliacoes", Avaliacao.class.getConstructor());
        relRespostaAvaliacao = new ArvoreBMais<>(ParIdId.class.getConstructor(),
            5,
            "./dados/avaliacoes/relRespostaAvaliacao.db");
    }

    /**
     * Cria a avaliação no arquivo e registra o vínculo com a resposta na árvore B+, garantindo que a resposta exista.
     * 
     * @param avaliacao A avaliação a ser criada
     * @return O ID gerado para a nova avaliação
     * @throws Exception Se a resposta não existir
     */
    @Override
    public int create(Avaliacao avaliacao) throws Exception {
        int idResposta = avaliacao.getIdResposta();
        if (idResposta == -1) {
            throw new Exception(
                "Não é possível criar avaliação: resposta inexistente."
            );
        }
        int idGerado = super.create(avaliacao);
        relRespostaAvaliacao.create(new ParIdId(avaliacao.getIdResposta(), idGerado));
        return idGerado;
    }

    // TODO: Parte 2 -> Implementar método para avaliar uma resposta, garantindo que o usuário não possa avaliar a mesma resposta mais de uma vez.
    public boolean avaliarResposta(int idUsuario, int idResposta, boolean avaliacao) throws Exception { return false;}

    /** 
     * Retorna todas as avaliações de uma resposta específica.
     * 
     * @param idResposta O ID da resposta
     * @return Uma lista de avaliações da resposta
     * @throws Exception Se ocorrer algum erro ao ler as avaliações
     */
    public ArrayList<Avaliacao> getAvaliacoesPorResposta(int idResposta) throws Exception {
        ArrayList<Avaliacao> avaliacoes = new ArrayList<>();
        ArrayList<ParIdId> pares = relRespostaAvaliacao.read(new ParIdId(idResposta, -1));

        for (ParIdId par : pares) {
            Avaliacao avaliacao = super.read(par.getId2());
            if (avaliacao != null) {
                avaliacoes.add(avaliacao);
            }
        }
        return avaliacoes;
    }

    // TODO: Parte 2 -> Implementar método para retornar todas as avaliações feitas por um usuário específico.

    /**
     * Atualiza a avaliação no arquivo.
     * 
     * @param avaliacao A avaliação a ser atualizada
     * @return true se a avaliação foi atualizada com sucesso, false caso contrário
     * @throws Exception Se a avaliação não existir
     */
    @Override
    public boolean update(Avaliacao avaliacao) throws Exception {
        // Verifica se a avaliação existe
        Avaliacao existente = super.read(avaliacao.getId());
        if (existente == null) {
            throw new Exception("Avaliação não encontrada.");
        }

        // Atualiza a avaliação
        return super.update(avaliacao);
    }

    /**
     * Remove a avaliação do arquivo.
     * 
     * @param id O ID da avaliação a ser removida
     * @return true se a avaliação foi removida com sucesso, false caso contrário
     * @throws Exception Se a avaliação não existir
     */
    public boolean delete(int id) throws Exception {
        // Verifica se a avaliação existe
        Avaliacao existente = super.read(id);
        if (existente == null) {
            throw new Exception("Avaliação não encontrada.");
        }

        // Remove a avaliação do arquivo e do índice
        relRespostaAvaliacao.delete(new ParIdId(existente.getIdResposta(), id));
        return super.delete(id);
    }
}

/*
    TODO: 1. Adicionar restrições para impedir que um usuário avalie a mesma resposta mais de uma vez.
          2. Implementar método para retornar todas as avaliações feitas por um usuário específico.

*/