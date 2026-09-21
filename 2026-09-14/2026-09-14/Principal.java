import java.util.Scanner;
import entidades.ArquivoUsuario;
import entidades.ArquivoPergunta;

public class Principal {

    public static void main(String[] args) {
        ArquivoUsuario arqUsuarios = null;
        ArquivoPergunta arqPerguntas = null;
        
        try {
            // Inicializa as bases de dados
            arqUsuarios = new ArquivoUsuario();
            arqPerguntas = new ArquivoPergunta(arqUsuarios);

            // Linha para amarrar a dependência do Delete!
            arqUsuarios.setArquivoPergunta(arqPerguntas);

            // Inicializa os controladores com acesso aos arquivos correspondentes
            ControlePergunta controlePergunta = new ControlePergunta(arqPerguntas);
            ControleUsuario controleUsuario = new ControleUsuario(arqUsuarios, controlePergunta);

            // Inicia o programa
            Scanner scanner = new Scanner(System.in);
            controleUsuario.telaAcesso(scanner);
            scanner.close();

        } catch (Exception e) {
            System.out.println("Erro crítico ao inicializar as bases de dados: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (arqUsuarios != null) arqUsuarios.close();
                if (arqPerguntas != null) arqPerguntas.close();
            } catch (Exception e) {
                System.out.println("Erro ao fechar ficheiros: " + e.getMessage());
            }
        }
    }
}