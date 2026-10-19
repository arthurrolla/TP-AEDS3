import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

// Classe com métodos úteis para a interface do projeto
public class Utilidades {
    public static final String RESET = "\033[0m";
    public static final String COR_ROSA = "\033[38;2;255;126;182m";
    public static final String COR_TEXTO = "\033[38;2;242;244;248m";

    public static void limparEcra() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void pausar(Scanner scanner, String mensagem) {
        if (!mensagem.isEmpty()) {
            System.out.println(COR_TEXTO + "\n" + mensagem);
        }
        System.out.print("Pressione [ENTER] para continuar..." + RESET);
        scanner.nextLine();
    }

    // Limpa a tela e imprime o cabeçalho padrão seguido do caminho de navegação (breadcrumb)
    public static void exibirCabecalho(String caminho) {
        limparEcra();
        System.out.println(COR_ROSA + "AJUDA AÍ 2.0"); // Atualizando Versão
        System.out.println("------------" + COR_TEXTO);
        System.out.println(caminho + "\n");
    }

    // Converte um texto em número inteiro. Retorna -1 se o texto não for um número válido,
    // evitando que o programa quebre quando o usuário digita letras no lugar de números.
    public static int lerNumero(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // Formata uma data em milissegundos como "dd/MM/yyyy às HH:mm"
    public static String formatarData(long milissegundos) {
        return new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm").format(new Date(milissegundos));
    }

    // Reduz um texto (possivelmente com várias linhas) a uma única linha de até 'maximo' caracteres
    public static String resumir(String texto, int maximo) {
        String linha = texto.replace("\r", " ").replace("\n", " ").trim();
        if (linha.length() > maximo) {
            return linha.substring(0, maximo - 3) + "...";
        }
        return linha;
    }

    // Texto da nota de uma resposta, com singular/plural correto
    public static String textoNota(int nota, int avaliacoes) {
        return "Nota: " + nota + " (" + avaliacoes + (avaliacoes == 1 ? " avaliação)" : " avaliações)");
    }
}
