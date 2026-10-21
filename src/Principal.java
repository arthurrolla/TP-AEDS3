import java.util.Scanner;
import entidades.ArquivoUsuario;
import entidades.ArquivoPergunta;
import entidades.ArquivoResposta;
import entidades.ArquivoAvaliacao;

public class Principal {

    public static void main(String[] args) {
        ArquivoUsuario arqUsuarios = null;
        ArquivoPergunta arqPerguntas = null;
        ArquivoResposta arqRespostas = null;
        ArquivoAvaliacao arqAvaliacoes = null;
        
        try {
            // Inicializa as bases de dados
            arqUsuarios = new ArquivoUsuario();
            arqPerguntas = new ArquivoPergunta(arqUsuarios);
            arqRespostas = new ArquivoResposta(arqUsuarios, arqPerguntas);
            arqPerguntas.setArquivoResposta(arqRespostas);
            arqUsuarios.setArquivoResposta(arqRespostas);
            arqAvaliacoes = new ArquivoAvaliacao(arqRespostas, arqUsuarios);
            arqUsuarios.setArquivoAvaliacao(arqAvaliacoes);

            // Linha para amarrar a dependência do Delete!
            arqUsuarios.setArquivoPergunta(arqPerguntas);

            // Inicializa os controladores com acesso aos arquivos correspondentes
            ControleResposta controleResposta = new ControleResposta(arqRespostas, arqPerguntas, arqUsuarios, arqAvaliacoes);
            ControlePergunta controlePergunta = new ControlePergunta(arqPerguntas, arqUsuarios, arqRespostas, controleResposta);
            ControleUsuario controleUsuario = new ControleUsuario(arqUsuarios, controlePergunta, controleResposta);

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
                if (arqRespostas != null) arqRespostas.close();
                if (arqAvaliacoes != null) arqAvaliacoes.close();
            } catch (Exception e) {
                System.out.println("Erro ao fechar ficheiros: " + e.getMessage());
            }
        }
    }
}