import java.util.Scanner;
import entidades.ArquivoUsuario;
import entidades.Usuario;

public class ControleUsuario {

    private ArquivoUsuario arqUsuarios;
    private ControlePergunta controlePergunta;
    private Usuario usuarioLogado = null;

    public ControleUsuario(ArquivoUsuario arqUsuarios, ControlePergunta controlePergunta) {
        this.arqUsuarios = arqUsuarios;
        this.controlePergunta = controlePergunta;
    }

    public void telaAcesso(Scanner scanner) throws Exception {
        String opcao;
        do {
            Utilidades.limparEcra();
            System.out.println(Utilidades.COR_ROSA + "AJUDA AÍ 1.0");
            System.out.println("------------" + Utilidades.COR_TEXTO);
            System.out.println();
            System.out.println("(A) Login");
            System.out.println("(B) Novo utilizador (primeiro acesso)");
            System.out.println("(C) Esqueci a minha senha");
            System.out.println();
            System.out.println("(S) Sair");
            System.out.print("\nOpção: " + Utilidades.RESET);
            
            opcao = scanner.nextLine().trim().toUpperCase();

            switch (opcao) {
                case "A":
                    efetuarLogin(scanner);
                    break;
                case "B":
                    telaNovoUsuario(scanner);
                    break;
                case "C":
                    recuperarSenha(scanner);
                    break;
                case "DEBUG": // <--- COMANDO SECRETO
                    listarTodosUsuariosDebug(scanner);
                    break;
                case "S":
                    Utilidades.limparEcra();
                    System.out.println(Utilidades.COR_ROSA + "Encerrando o sistema..." + Utilidades.RESET);
                    break;
                default:
                    Utilidades.pausar(scanner, "Opção inválida.");
            }
        } while (!opcao.equals("S"));
    }

    private void efetuarLogin(Scanner scanner) throws Exception {
        System.out.print(Utilidades.COR_TEXTO + "Email: " + Utilidades.RESET);
        String email = scanner.nextLine().trim();
        System.out.print(Utilidades.COR_TEXTO + "Senha: " + Utilidades.RESET);
        String senha = scanner.nextLine().trim();

        Usuario u = arqUsuarios.readByEmail(email);
        
        if (u != null) {
            if (u.verificarSenha(senha)) {
                usuarioLogado = u;
                menuPrincipal(scanner);
            } else {
                Utilidades.pausar(scanner, "Senha incorreta.");
            }
        } else {
            Utilidades.pausar(scanner, "Utilizador não encontrado.");
        }
    }

    private void telaNovoUsuario(Scanner scanner) throws Exception {
        Utilidades.limparEcra();
        System.out.println(Utilidades.COR_ROSA + "NOVO UTILIZADOR" + Utilidades.COR_TEXTO);
        System.out.println();
        
        System.out.print("Email: " + Utilidades.RESET);
        String email = scanner.nextLine().trim();

        if (arqUsuarios.readByEmail(email) != null) {
            Utilidades.pausar(scanner, "Erro: Este e-mail já está registado no sistema.");
            return; 
        }
        
        System.out.print(Utilidades.COR_TEXTO + "Nome completo: " + Utilidades.RESET);
        String nome = scanner.nextLine().trim();
        System.out.print(Utilidades.COR_TEXTO + "Senha: " + Utilidades.RESET);
        String senha = scanner.nextLine().trim();
        System.out.print(Utilidades.COR_TEXTO + "Pergunta secreta: " + Utilidades.RESET);
        String perguntaSecreta = scanner.nextLine().trim();
        System.out.print(Utilidades.COR_TEXTO + "Resposta secreta: " + Utilidades.RESET);
        String respostaSecreta = scanner.nextLine().trim();

        Usuario novoUsuario = new Usuario(-1, nome, email, senha, perguntaSecreta, respostaSecreta);
        arqUsuarios.create(novoUsuario);

        Utilidades.pausar(scanner, "Utilizador registado com sucesso! Retornando ao menu.");
    }

    private void menuPrincipal(Scanner scanner) throws Exception {
        String opcao;
        do {
            Utilidades.limparEcra();
            System.out.println(Utilidades.COR_ROSA + "AJUDA AÍ 1.0");
            System.out.println("------------" + Utilidades.COR_TEXTO);
            System.out.println();
            System.out.println("> Início");
            System.out.println();
            System.out.println("(A) Minha área");
            System.out.println("(B) Buscar perguntas");
            System.out.println();
            System.out.println("(S) Sair");
            System.out.print("\nOpção: " + Utilidades.RESET);

            opcao = scanner.nextLine().trim().toUpperCase();

            switch (opcao) {
                case "A":
                    menuMinhaArea(scanner);
                    break;
                case "B":
                    Utilidades.pausar(scanner, "Busca de perguntas não implementada nesta etapa.");
                    break;
                case "S":
                    usuarioLogado = null;
                    break;
                default:
                    Utilidades.pausar(scanner, "Opção inválida.");
            }
        } while (!opcao.equals("S"));
    }

    private void menuMinhaArea(Scanner scanner) throws Exception {
        String opcao;
        do {
            Utilidades.limparEcra();
            System.out.println(Utilidades.COR_ROSA + "AJUDA AÍ 1.0");
            System.out.println("------------" + Utilidades.COR_TEXTO);
            System.out.println();
            System.out.println("> Início > Minha área");
            System.out.println();
            System.out.println("(A) Meus dados");
            System.out.println("(B) Minhas perguntas");
            System.out.println("(C) Minhas respostas");
            System.out.println("(D) Meus votos");
            System.out.println();
            System.out.println("(R) Retornar ao menu anterior");
            System.out.print("\nOpção: " + Utilidades.RESET);

            opcao = scanner.nextLine().trim().toUpperCase();

            switch (opcao) {
                case "A":
                    menuMeusDados(scanner);
                    break;
                case "B":
                    controlePergunta.menuMinhasPerguntas(scanner, usuarioLogado.getId());
                    break;
                case "C":
                case "D":
                    Utilidades.pausar(scanner, "Funcionalidade para as próximas etapas.");
                    break;
                case "R":
                    break;
                default:
                    Utilidades.pausar(scanner, "Opção inválida.");
            }
        } while (!opcao.equals("R"));
    }

    private void menuMeusDados(Scanner scanner) throws Exception {
        String opcao;
        do {
            Utilidades.limparEcra();
            System.out.println(Utilidades.COR_ROSA + "AJUDA AÍ 1.0");
            System.out.println("------------" + Utilidades.COR_TEXTO);
            System.out.println("> Início > Minha área > Meus dados");
            System.out.println();
            System.out.println("Nome atual: " + usuarioLogado.getNome());
            System.out.println();
            System.out.println("(A) Alterar nome");
            System.out.println("(B) Alterar email");
            System.out.println("(C) Alterar senha");
            System.out.println("(D) Alterar pergunta e resposta secreta");
            System.out.println();
            System.out.println("(R) Retornar");
            System.out.print("\nOpção: " + Utilidades.RESET);

            opcao = scanner.nextLine().trim().toUpperCase();

            if (opcao.equals("A")) {
                System.out.print("Novo nome: ");
                usuarioLogado.setNome(scanner.nextLine().trim());
                arqUsuarios.update(usuarioLogado);
                Utilidades.pausar(scanner, "Nome alterado com sucesso!");
            } else if (opcao.equals("B")) {
                System.out.print("Novo email: ");
                String novoEmail = scanner.nextLine().trim();
                
                // Verifica se já existe outro utilizador com este email
                if (arqUsuarios.readByEmail(novoEmail) != null) {
                    Utilidades.pausar(scanner, "Erro: Este email já está em uso.");
                } else {
                    usuarioLogado.setEmail(novoEmail);
                    arqUsuarios.update(usuarioLogado);
                    Utilidades.pausar(scanner, "Email alterado com sucesso!");
                } 
            } else if (!opcao.equals("R")) {
                    Utilidades.pausar(scanner, "Opção de alteração em desenvolvimento.");
            }
        } while (!opcao.equals("R"));
    }

    // Método exclusivo para o programador testar o banco de dados
    private void listarTodosUsuariosDebug(Scanner scanner) {
        Utilidades.limparEcra();
        System.out.println(Utilidades.COR_ROSA + "--- MODO DEBUG: USUÁRIOS NO BANCO DE DADOS ---" + Utilidades.COR_TEXTO);
        
        int contagem = 0;
        try {
            for (int i = 1; i <= 100; i++) {
                Usuario u = arqUsuarios.read(i);
                if (u != null) {
                    System.out.println("ID: " + u.getId() + " | Nome: " + u.getNome() + " | Email: " + u.getEmail());
                    contagem++;
                }
            }
            if (contagem == 0) {
                System.out.println("Nenhum utilizador encontrado no ficheiro.");
            }
        } catch (Exception e) {
            System.out.println("Fim da leitura.");
        }
        
        System.out.println(Utilidades.COR_ROSA + "----------------------------------------------" + Utilidades.RESET);
        Utilidades.pausar(scanner, "");
    }

    private void recuperarSenha(Scanner scanner) throws Exception {
        Utilidades.limparEcra();
        System.out.println(Utilidades.COR_ROSA + "RECUPERAÇÃO DE SENHA" + Utilidades.COR_TEXTO);
        System.out.print("\nDigite o seu Email: " + Utilidades.RESET);
        String email = scanner.nextLine().trim();

        Usuario u = arqUsuarios.readByEmail(email);
        if (u == null) {
            Utilidades.pausar(scanner, "Email não encontrado.");
            return;
        }

        System.out.println(Utilidades.COR_TEXTO + "\nPergunta Secreta: " + u.getPerguntaSecraeta());
        System.out.print("Sua Resposta: " + Utilidades.RESET);
        String resposta = scanner.nextLine().trim();

        if (u.verificarRespostaSecreta(resposta)) {
            System.out.print(Utilidades.COR_TEXTO + "\nDigite a NOVA senha: " + Utilidades.RESET);
            String novaSenha = scanner.nextLine().trim();
            u.setHashSenha(novaSenha);
            arqUsuarios.update(u);
            Utilidades.pausar(scanner, "Senha alterada com sucesso! Faça login com a nova senha.");
        } else {
            Utilidades.pausar(scanner, "Resposta incorreta.");
        }
    }
}