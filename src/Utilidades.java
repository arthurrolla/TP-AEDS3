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
}