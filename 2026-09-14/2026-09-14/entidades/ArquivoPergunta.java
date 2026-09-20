package entidades;

import aed3.*;
import java.util.ArrayList;

public class ArquivoPergunta extends Arquivo<Pergunta> {

    ArvoreBMais<ParIdId> relUsuarioPergunta;

    // Abre o arquivo de perguntas e o índice do relacionamento usuário -> perguntas
    public ArquivoPergunta() throws Exception {
        super("perguntas", Pergunta.class.getConstructor());
        relUsuarioPergunta = new ArvoreBMais<>(ParIdId.class.getConstructor(),
            5,
            "./dados/perguntas/relUsuarioPergunta.db");
    }

    // Cria a pergunta no arquivo e registra o vínculo com o usuário na árvore B+
    @Override
    public int create(Pergunta novaPergunta) throws Exception {
        int idGerado = super.create(novaPergunta);
        relUsuarioPergunta.create(new ParIdId(novaPergunta.getIdUsuario(), idGerado));
        return idGerado;
    }

    // Atualiza texto e palavras-chave de uma pergunta existente
    public boolean atualizarConteudo(int idPergunta, String novoTexto, String novasPalavras) throws Exception {
        Pergunta perguntaExistente = super.read(idPergunta);
        if (perguntaExistente == null) {
            return false;
        }
        perguntaExistente.setPergunta(novoTexto);
        perguntaExistente.setPalavrasChave(novasPalavras);
        perguntaExistente.setAlteracao(System.currentTimeMillis());
        return super.update(perguntaExistente);
    }

    // Marca a pergunta como arquivada
    public boolean arquivar(int idPergunta) throws Exception {
        Pergunta perguntaExistente = super.read(idPergunta);
        if (perguntaExistente == null || !perguntaExistente.isAtiva()) {
            return false;
        }
        perguntaExistente.arquivar();
        perguntaExistente.setAlteracao(System.currentTimeMillis());
        return super.update(perguntaExistente);
    }

    // Busca todas as perguntas de um usuário usando a árvore B+
    public ArrayList<Pergunta> readAllByUsuario(int idUsuario) throws Exception {
        ArrayList<Pergunta> perguntasDoUsuario = new ArrayList<>();
        ArrayList<ParIdId> paresUsuarioPergunta = relUsuarioPergunta.read(new ParIdId(idUsuario, -1));

        for (ParIdId par : paresUsuarioPergunta) {
            Pergunta perguntaEncontrada = super.read(par.getId2());
            if (perguntaEncontrada != null) {
                perguntasDoUsuario.add(perguntaEncontrada);
            }
        }
        return perguntasDoUsuario;
    }

    // Bloqueado: alteração deve passar por atualizarConteudo() ou arquivar()
    @Override
    public boolean update(Pergunta perguntaAtualizada) throws Exception {
        throw new UnsupportedOperationException(
            "Use atualizarConteudo(idPergunta, texto, palavrasChave) para alterar uma pergunta, " +
            "ou arquivar(idPergunta) para arquivá-la."
        );
    }

    // Bloqueado: perguntas não podem ser excluídas, só arquivadas
    @Override
    public boolean delete(int id) throws Exception {
        throw new UnsupportedOperationException(
            "Perguntas não podem ser excluídas. Use arquivar(idPergunta)."
        );
    }
}
