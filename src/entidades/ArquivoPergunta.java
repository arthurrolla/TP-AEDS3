package entidades;

import aed3.*;
import java.util.ArrayList;

public class ArquivoPergunta extends Arquivo<Pergunta> {

    ArvoreBMais<ParIdId> relUsuarioPergunta;
    ArvoreBMais<ParDataId> indiceDataPergunta;
    private ArquivoUsuario arquivoUsuario;
    private ArquivoResposta arquivoResposta;

    // Abre o arquivo de perguntas e o índice do relacionamento usuário -> perguntas
    public ArquivoPergunta(ArquivoUsuario arquivoUsuario) throws Exception {
        super("perguntas", Pergunta.class.getConstructor());
        this.arquivoUsuario = arquivoUsuario;
        relUsuarioPergunta = new ArvoreBMais<>(ParIdId.class.getConstructor(),5,"./dados/perguntas/relUsuarioPergunta.db");
        indiceDataPergunta = new ArvoreBMais<>(ParDataId.class.getConstructor(),5,"./dados/perguntas/indiceDataPergunta.db");
        arquivoUsuario.setArquivoPergunta(this);
        indexarPerguntasExistentes();
    }
    public void setArquivoResposta(ArquivoResposta arquivoResposta) {
        this.arquivoResposta = arquivoResposta;
    }

    // Inclui no índice por data as perguntas ativas já existentes
    private void indexarPerguntasExistentes() throws Exception{
        ArrayList<ParIdId> pares = relUsuarioPergunta.read(null);
        for(ParIdId par : pares){
            Pergunta pergunta = super.read(par.getId2());
            if (pergunta != null && pergunta.isAtiva()){
                ParDataId chave = new ParDataId(pergunta.getCriacao(),pergunta.getId());
                if (indiceDataPergunta.read(chave).isEmpty()){
                    indiceDataPergunta.create(chave);
                }
            }
        }
    }

    // Cria a pergunta no arquivo e registra o vínculo com o usuário na árvore B+
    @Override
    public int create(Pergunta novaPergunta) throws Exception {
        Usuario usuario = arquivoUsuario.read(novaPergunta.getIdUsuario());
        if (usuario == null) {
            throw new Exception(
                "Não é possível criar pergunta: usuário inexistente."
            );
        }
        int idGerado = super.create(novaPergunta);
        relUsuarioPergunta.create(new ParIdId(novaPergunta.getIdUsuario(), idGerado));
        if(novaPergunta.isAtiva()){
            indiceDataPergunta.create(new ParDataId(novaPergunta.getCriacao(), idGerado));
        }
        return idGerado;
    }

    // Atualiza texto e palavras-chave de uma pergunta existente
    public boolean atualizarConteudo(int idUsuario,int idPergunta, String novoTexto, String novasPalavras) throws Exception {
        Pergunta perguntaExistente = super.read(idPergunta);
        if (perguntaExistente == null) {
            return false;
        }
        if (perguntaExistente.getIdUsuario() != idUsuario) {
            throw new Exception(
                "Você só pode alterar suas próprias perguntas."
            );
        }
        perguntaExistente.setPergunta(novoTexto);
        perguntaExistente.setPalavrasChave(novasPalavras);
        perguntaExistente.setAlteracao(System.currentTimeMillis());
        return super.update(perguntaExistente);
    }

    // Marca a pergunta como arquivada
    public boolean arquivar(int idUsuario, int idPergunta) throws Exception {
        Pergunta perguntaExistente = super.read(idPergunta);
        if (perguntaExistente == null) {
            return false;
        }
        if (perguntaExistente.getIdUsuario() != idUsuario) {
            throw new Exception(
                "Você só pode arquivar suas próprias perguntas."
            );
        }
        if (!perguntaExistente.isAtiva()) {
            return false;
        }
        perguntaExistente.arquivar();
        perguntaExistente.setAlteracao(System.currentTimeMillis());
        boolean atualizou = super.update(perguntaExistente);
        if(atualizou){
            indiceDataPergunta.delete(new ParDataId(perguntaExistente.getCriacao(),perguntaExistente.getId()));
        }
        return atualizou;
    }

    // Busca uma página de até cinco perguntas ativas, das mais recentes às antigas.
    public ArrayList<Pergunta> readRecentes(int pagina) throws Exception {
        if (pagina < 0) {
            throw new IllegalArgumentException("O número da página não pode ser negativo.");
        }

        ArrayList<Pergunta> perguntas = new ArrayList<>();
        ArrayList<ParDataId> pares = indiceDataPergunta.read(null);

        long inicio = (long) pagina * 5;
        long quantidadeAtivas = 0;

        for (ParDataId par : pares){
            Pergunta pergunta = super.read(par.getIdPergunta());
            if(pergunta != null && pergunta.isAtiva()){
                if(quantidadeAtivas >= inicio){
                    perguntas.add(pergunta);
                    if(perguntas.size() == 5){
                        break;
                    }
                }
                quantidadeAtivas++;
            }
        }
        return perguntas;
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

    // Uso interno: remove as perguntas quando seu usuário é excluído
    void excluirPorUsuario(int idUsuario) throws Exception {
        ArrayList<Pergunta> perguntasDoUsuario = readAllByUsuario(idUsuario);

        for(Pergunta pergunta : perguntasDoUsuario) {
            if (arquivoResposta == null) {
                throw new Exception("Abra o arquivo de respostas antes de excluir uma pergunta.");
            }

            arquivoResposta.excluirPorPergunta(pergunta.getId());
            boolean excluiu = super.delete(pergunta.getId());
            if(!excluiu) {
                throw new Exception("Não foi possível excluir uma pergunta do usuário.");
            }
            indiceDataPergunta.delete(new ParDataId(pergunta.getCriacao(), pergunta.getId()));
            boolean removeuVinculo = relUsuarioPergunta.delete(new ParIdId(idUsuario, pergunta.getId()));
            if(!removeuVinculo) {
                throw new Exception("Não foi possível remover o vínculo da pergunta.");
            }
        }
    }

    // Bloqueado: alteração deve passar por atualizarConteudo() ou arquivar()
    @Override
    public boolean update(Pergunta perguntaAtualizada) throws Exception {
        throw new UnsupportedOperationException(
            "Use atualizarConteudo(idUsuario, idPergunta, texto, palavrasChave) " +
            "para alterar uma pergunta, ou arquivar(idUsuario, idPergunta) para arquivá-la."
        );
    }

    // Bloqueado: perguntas não podem ser excluídas, só arquivadas
    @Override
    public boolean delete(int id) throws Exception {
        throw new UnsupportedOperationException(
            "Perguntas não podem ser excluídas. Use arquivar(idUsuario, idPergunta)."
        );
    }
}
