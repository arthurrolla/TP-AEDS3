package aed3;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Pré-processamento dos textos usados no índice invertido de perguntas.
 *
 * A classe mantém as repetições dos termos: elas são necessárias para
 * calcular a frequência do termo (TF) de cada pergunta.
 */
public final class TratamentoTermo {

    private static final Set<String> STOP_WORDS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
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
        )));

    private TratamentoTermo() {
    }

    /**
     * Divide o título em palavras, normaliza cada palavra e remove stop-words.
     * As repetições são preservadas para o cálculo de TF.
     */
    public static ArrayList<String> extrair(String titulo) {
        ArrayList<String> termos = new ArrayList<>();
        if (titulo == null || titulo.isEmpty()) {
            return termos;
        }

        String normalizado = Normalizer.normalize(titulo, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT);

        for (String palavra : normalizado.split("[^\\p{L}\\p{N}]+")) {
            if (!palavra.isEmpty() && !STOP_WORDS.contains(palavra)) {
                termos.add(palavra);
            }
        }
        return termos;
    }

    /**
     * Conta quantas vezes cada termo aparece, mantendo a ordem da primeira
     * ocorrência para tornar a gravação do índice determinística.
     */
    public static Map<String, Integer> frequencias(String titulo) {
        Map<String, Integer> frequencias = new LinkedHashMap<>();
        for (String termo : extrair(titulo)) {
            frequencias.merge(termo, 1, Integer::sum);
        }
        return frequencias;
    }

    /**
     * Calcula TF = ocorrências do termo / quantidade total de termos válidos.
     */
    public static float tf(int ocorrencias, String titulo) {
        int total = extrair(titulo).size();
        return total == 0 ? 0f : (float) ocorrencias / total;
    }
}
