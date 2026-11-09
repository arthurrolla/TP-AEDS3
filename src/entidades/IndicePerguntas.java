package entidades;

import aed3.ArvoreBMais;
import aed3.ElementoLista;
import aed3.ListaInvertida;
import aed3.ParIdId;
import aed3.TratamentoTermo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Índice invertido das palavras-chave das perguntas usando TF-IDF.
 */
public class IndicePerguntas {

    public static class Resultado {
        public final int idPergunta;
        public final double pontuacao;

        Resultado(int idPergunta, double pontuacao) {
            this.idPergunta = idPergunta;
            this.pontuacao = pontuacao;
        }
    }

    private final ListaInvertida lista;
    private final ArvoreBMais<ParIdId> indexadas;

    public IndicePerguntas() throws Exception {
        lista = new ListaInvertida(
            10,
            "./dados/perguntas/listaInvertida.dicionario.db",
            "./dados/perguntas/listaInvertida.blocos.db"
        );
        indexadas = new ArvoreBMais<>(
            ParIdId.class.getConstructor(),
            5,
            "./dados/perguntas/perguntasIndexadas.db"
        );
        acertarContagem();
    }

    private boolean estaIndexada(int idPergunta) throws Exception {
        return !indexadas.read(new ParIdId(idPergunta, -1)).isEmpty();
    }

    private void adicionarTermos(int idPergunta, String texto) throws Exception {
        ArrayList<String> termos = TratamentoTermo.extrair(texto);
        if (termos.isEmpty()) {
            return;
        }

        Map<String, Integer> frequencias = TratamentoTermo.frequencias(texto);
        for (Map.Entry<String, Integer> entrada : frequencias.entrySet()) {
            float tf = TratamentoTermo.tf(entrada.getValue(), termos.size());
            ElementoLista elemento = new ElementoLista(idPergunta, tf);
            if (lista.read(entrada.getKey(), idPergunta) == null) {
                lista.create(entrada.getKey(), elemento);
            } else {
                lista.update(entrada.getKey(), elemento);
            }
        }
    }

    private void removerTermos(int idPergunta, String texto) throws Exception {
        for (String termo : new LinkedHashSet<>(TratamentoTermo.extrair(texto))) {
            lista.delete(termo, idPergunta);
        }
    }

    public void incluir(Pergunta pergunta) throws Exception {
        if (pergunta == null || estaIndexada(pergunta.getId())) {
            return;
        }
        adicionarTermos(pergunta.getId(), pergunta.getPalavrasChave());
        indexadas.create(new ParIdId(pergunta.getId(), pergunta.getId()));
        lista.incrementaEntidades();
    }

    public void remover(Pergunta pergunta) throws Exception {
        if (pergunta == null || !estaIndexada(pergunta.getId())) {
            return;
        }
        removerTermos(pergunta.getId(), pergunta.getPalavrasChave());
        indexadas.delete(new ParIdId(pergunta.getId(), pergunta.getId()));
        lista.decrementaEntidades();
    }

    public void atualizar(int idPergunta, String palavrasAntigas,
                          Pergunta perguntaNova) throws Exception {
        if (!estaIndexada(idPergunta)) {
            incluir(perguntaNova);
            return;
        }
        removerTermos(idPergunta, palavrasAntigas);
        adicionarTermos(idPergunta, perguntaNova.getPalavrasChave());
    }

    public ArrayList<Resultado> buscar(String consulta) throws Exception {
        int totalPerguntas = lista.numeroEntidades();
        Map<Integer, Double> pontuacoes = new HashMap<>();
        if (totalPerguntas == 0) {
            return new ArrayList<>();
        }

        for (String termo : new LinkedHashSet<>(TratamentoTermo.extrair(consulta))) {
            ElementoLista[] ocorrencias = lista.read(termo);
            if (ocorrencias.length == 0) {
                continue;
            }

            double idf = Math.log10((double) totalPerguntas / ocorrencias.length);
            for (ElementoLista ocorrencia : ocorrencias) {
                pontuacoes.merge(
                    ocorrencia.getId(),
                    ocorrencia.getFrequencia() * idf,
                    Double::sum
                );
            }
        }

        ArrayList<Resultado> resultados = new ArrayList<>();
        for (Map.Entry<Integer, Double> entrada : pontuacoes.entrySet()) {
            resultados.add(new Resultado(entrada.getKey(), entrada.getValue()));
        }
        resultados.sort(
            Comparator.comparingDouble((Resultado r) -> r.pontuacao)
                .reversed()
                .thenComparingInt(r -> r.idPergunta)
        );
        return resultados;
    }

    private void acertarContagem() throws Exception {
        int quantidadeReal = indexadas.read(null).size();
        while (lista.numeroEntidades() < quantidadeReal) {
            lista.incrementaEntidades();
        }
        while (lista.numeroEntidades() > quantidadeReal) {
            lista.decrementaEntidades();
        }
    }

    public void close() throws Exception {
        lista.close();
    }
}
