package entidades;

import aed3.*;
import java.text.Normalizer;
import java.util.*;

/**
 * Índice invertido das palavras-chave das perguntas (TF x IDF).
 *
 * Além da ListaInvertida, mantém uma árvore B+ com os IDs das perguntas que já
 * estão indexadas. Ela garante que indexar duas vezes a mesma pergunta (por
 * exemplo, ao reiniciar o programa) não duplique os termos nem a contagem de
 * entidades usada no cálculo do IDF.
 */
public class IndicePerguntas {

    // Palavras irrelevantes para a busca (já sem acento e em minúsculas)
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "a", "o", "as", "os", "um", "uma", "uns", "umas",
        "de", "do", "da", "dos", "das", "em", "no", "na", "nos", "nas",
        "por", "pelo", "pela", "pelos", "pelas", "para", "pra", "com", "sem",
        "sobre", "sob", "ate", "entre", "ao", "aos", "e", "ou", "mas", "que",
        "se", "como", "qual", "quais", "quando", "onde", "quem", "porque",
        "eu", "me", "meu", "minha", "seu", "sua", "ele", "ela", "isso", "isto",
        "esse", "essa", "este", "esta", "aquele", "aquela", "ser", "estar",
        "ter", "ha", "foi", "sao", "eh", "ja", "mais", "menos", "muito",
        "zero", "dois", "duas", "tres", "quatro", "cinco", "seis", "sete",
        "oito", "nove", "dez", "primeiro", "segundo", "terceiro"
    ));

    // Uma pergunta encontrada e sua pontuação TFxIDF
    public static class Resultado {
        public final int idPergunta;
        public final double pontuacao;

        Resultado(int idPergunta, double pontuacao) {
            this.idPergunta = idPergunta;
            this.pontuacao = pontuacao;
        }
    }

    private final ListaInvertida lista;
    private final ArvoreBMais<ParIdId> indexadas; // pares (idPergunta, idPergunta)

    public IndicePerguntas() throws Exception {
        lista = new ListaInvertida(10,
            "./dados/perguntas/listaInvertida.dicionario.db",
            "./dados/perguntas/listaInvertida.blocos.db");
        indexadas = new ArvoreBMais<>(ParIdId.class.getConstructor(), 5,
            "./dados/perguntas/perguntasIndexadas.db");
        acertarContagem();
    }

    // Minúsculas, sem acentos, sem stop words; mantém repetições para o cálculo do TF
    static ArrayList<String> extrairTermos(String texto) {
        ArrayList<String> termos = new ArrayList<>();
        if (texto == null) return termos;
        String limpo = Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase();
        for (String palavra : limpo.split("[^\\p{L}\\p{N}]+")) {
            if (!palavra.isEmpty() && !STOP_WORDS.contains(palavra)) {
                termos.add(palavra);
            }
        }
        return termos;
    }

    private boolean estaIndexada(int idPergunta) throws Exception {
        return !indexadas.read(new ParIdId(idPergunta, -1)).isEmpty();
    }

    // Grava TF = ocorrências / total de termos válidos. Se o par já existir, substitui
    // (não soma), então repetir a operação nunca dobra a frequência.
    private void adicionarTermos(int idPergunta, String palavrasChave) throws Exception {
        ArrayList<String> termos = extrairTermos(palavrasChave);
        Map<String, Integer> ocorrencias = new LinkedHashMap<>();
        for (String termo : termos) {
            ocorrencias.merge(termo, 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> par : ocorrencias.entrySet()) {
            float tf = (float) par.getValue() / termos.size();
            ElementoLista atual = lista.read(par.getKey(), idPergunta);
            if (atual == null) {
                lista.create(par.getKey(), new ElementoLista(idPergunta, tf));
            } else {
                lista.update(par.getKey(), new ElementoLista(idPergunta, tf));
            }
        }
    }

    private void removerTermos(int idPergunta, String palavrasChave) throws Exception {
        for (String termo : new LinkedHashSet<>(extrairTermos(palavrasChave))) {
            lista.delete(termo, idPergunta);
        }
    }

    // Indexa uma pergunta nova. Se ela já estiver indexada, não faz nada.
    public void incluir(Pergunta pergunta) throws Exception {
        if (estaIndexada(pergunta.getId())) return;
        adicionarTermos(pergunta.getId(), pergunta.getPalavrasChave());
        indexadas.create(new ParIdId(pergunta.getId(), pergunta.getId()));
        lista.incrementaEntidades();
    }

    // Tira a pergunta do índice (arquivamento ou exclusão). Se não estiver, não faz nada.
    // As palavras-chave da pergunta devem ser as mesmas que foram indexadas.
    public void remover(Pergunta pergunta) throws Exception {
        if (!estaIndexada(pergunta.getId())) return;
        removerTermos(pergunta.getId(), pergunta.getPalavrasChave());
        indexadas.delete(new ParIdId(pergunta.getId(), pergunta.getId()));
        lista.decrementaEntidades();
    }

    // Troca os termos antigos pelos novos; a contagem de entidades não muda
    public void atualizar(int idPergunta, String palavrasAntigas, Pergunta perguntaNova) throws Exception {
        if (!estaIndexada(idPergunta)) {
            incluir(perguntaNova);
            return;
        }
        removerTermos(idPergunta, palavrasAntigas);
        adicionarTermos(idPergunta, perguntaNova.getPalavrasChave());
    }

    // Busca por termos: soma TF x IDF de cada termo, com IDF = log10(N / tamanho da lista),
    // e ordena da maior para a menor pontuação.
    public ArrayList<Resultado> buscar(String consulta) throws Exception {
        int total = lista.numeroEntidades();
        Map<Integer, Double> pontos = new HashMap<>();
        for (String termo : new LinkedHashSet<>(extrairTermos(consulta))) {
            ElementoLista[] elementos = lista.read(termo);
            if (elementos.length == 0 || total == 0) continue;
            double idf = Math.log10((double) total / elementos.length);
            for (ElementoLista e : elementos) {
                pontos.merge(e.getId(), e.getFrequencia() * idf, Double::sum);
            }
        }
        ArrayList<Resultado> resultados = new ArrayList<>();
        for (Map.Entry<Integer, Double> p : pontos.entrySet()) {
            resultados.add(new Resultado(p.getKey(), p.getValue()));
        }
        resultados.sort((a, b) -> {
            int c = Double.compare(b.pontuacao, a.pontuacao);
            return c != 0 ? c : Integer.compare(a.idPergunta, b.idPergunta);
        });
        return resultados;
    }

    // Se o programa caiu entre gravar a pergunta e atualizar a contagem,
    // iguala a contagem do IDF ao número de perguntas realmente indexadas
    private void acertarContagem() throws Exception {
        int reais = indexadas.read(null).size();
        while (lista.numeroEntidades() < reais) lista.incrementaEntidades();
        while (lista.numeroEntidades() > reais) lista.decrementaEntidades();
    }

    public void close() throws Exception {
        lista.close();
    }
}