import java.util.Scanner;

import entidades.ArquivoUsuario;
import entidades.ArquivoPergunta;
import entidades.Usuario;
import entidades.Pergunta;

public class Principal {

    public static final String RESET = "\033[0m";
    public static final String COR_ROSA = "\033[38;2;255;126;182m";
    public static final String COR_TEXTO = "\033[38;2;242;244;248m";

    private static ArquivoUsuario arqUsuarios;
    private static ArquivoPergunta arqPerguntas;
    private static Usuario usuarioLogado = null;

    public static void main(String[] args) {
        try {
            // Inicialização correta: ArquivoPergunta exige o ArquivoUsuario como parâmetro
            arqUsuarios = new ArquivoUsuario();
            arqPerguntas = new ArquivoPergunta(arqUsuarios);

            Scanner scanner = new Scanner(System.in);
            telaAcesso(scanner);
            scanner.close();

            arqUsuarios.close();
            arqPerguntas.close();
            
        } catch (Exception e) {
            System.out.println("Erro crítico ao inicializar as bases de dados: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========================================================================
    // NÍVEL 0: ACESSO AO SISTEMA
    // ========================================================================
    private static void telaAcesso(Scanner scanner) throws Exception {
        String opcao;
        do {
            limparEcra();
            System.out.println(COR_ROSA + "AJUDA AÍ 1.0");
            System.out.println("------------" + COR_TEXTO);
            System.out.println();
            System.out.println("(A) Login");
            System.out.println("(B) Novo usuário (primeiro acesso)");
            System.out.println();
            System.out.println("(S) Sair");
            System.out.print("\nOpção: " + RESET);
            
            opcao = scanner.nextLine().trim().toUpperCase();

            switch (opcao) {
                case "A":
                    System.out.print(COR_TEXTO + "Email: " + RESET);
                    String email = scanner.nextLine().trim();
                    System.out.print(COR_TEXTO + "Senha: " + RESET);
                    String senha = scanner.nextLine().trim();

                    // Usa método auxiliar até ArquivoUsuario ter o read(String)
                    Usuario u = buscarUsuarioPorEmail(email); 
                    
                    if (u != null) {
                        String hashTentativa = Integer.toString(senha.hashCode());
                        // Corrigido para getHashSenha() em vez de getSenha()
                        if (u.getHashSenha().equals(hashTentativa)) {
                            usuarioLogado = u;
                            menuPrincipal(scanner);
                        } else {
                            pausar(scanner, "Senha incorreta.");
                        }
                    } else {
                        pausar(scanner, "Usuário não encontrado.");
                    }
                    break;
                case "B":
                    telaNovoUsuario(scanner);
                    break;
                case "S":
                    limparEcra();
                    System.out.println(COR_ROSA + "Encerrando o sistema..." + RESET);
                    break;
                default:
                    pausar(scanner, "Opção inválida.");
            }
        } while (!opcao.equals("S"));
    }

    private static void telaNovoUsuario(Scanner scanner) throws Exception {
        limparEcra();
        System.out.println(COR_ROSA + "NOVO USUÁRIO" + COR_TEXTO);
        System.out.println();
        
        System.out.print("Email: " + RESET);
        String email = scanner.nextLine().trim();

        // Usa método auxiliar até ArquivoUsuario ter o read(String)
        if (buscarUsuarioPorEmail(email) != null) {
            pausar(scanner, "Erro: Este e-mail já está cadastrado no sistema.");
            return; 
        }
        
        System.out.print(COR_TEXTO + "Nome completo: " + RESET);
        String nome = scanner.nextLine().trim();
        
        System.out.print(COR_TEXTO + "Senha: " + RESET);
        String senha = scanner.nextLine().trim();
        
        System.out.print(COR_TEXTO + "Pergunta secreta: " + RESET);
        String perguntaSecreta = scanner.nextLine().trim();
        
        System.out.print(COR_TEXTO + "Resposta secreta: " + RESET);
        String respostaSecreta = scanner.nextLine().trim();

        String hashSenha = Integer.toString(senha.hashCode());
        String hashResposta = Integer.toString(respostaSecreta.toLowerCase().hashCode());

        Usuario novoUsuario = new Usuario(-1, nome, email, hashSenha, perguntaSecreta, hashResposta);
        int idGerado = arqUsuarios.create(novoUsuario);

        pausar(scanner, "Usuário cadastrado com sucesso! Retornando ao menu de acesso para login.");
    }

    // ========================================================================
    // NÍVEL 1: MENU PRINCIPAL
    // ========================================================================
    private static void menuPrincipal(Scanner scanner) throws Exception {
        String opcao;
        do {
            limparEcra();
            System.out.println(COR_ROSA + "AJUDA AÍ 1.0");
            System.out.println("------------" + COR_TEXTO);
            System.out.println();
            System.out.println("> Início");
            System.out.println();
            System.out.println("(A) Minha área");
            System.out.println("(B) Buscar perguntas");
            System.out.println();
            System.out.println("(S) Sair");
            System.out.print("\nOpção: " + RESET);

            opcao = scanner.nextLine().trim().toUpperCase();

            switch (opcao) {
                case "A":
                    menuMinhaArea(scanner);
                    break;
                case "B":
                    pausar(scanner, "Busca de perguntas não implementada nesta etapa.");
                    break;
                case "S":
                    usuarioLogado = null;
                    break;
                default:
                    pausar(scanner, "Opção inválida.");
            }
        } while (!opcao.equals("S"));
    }

    // ========================================================================
    // NÍVEL 2: MINHA ÁREA
    // ========================================================================
    private static void menuMinhaArea(Scanner scanner) throws Exception {
        String opcao;
        do {
            limparEcra();
            System.out.println(COR_ROSA + "AJUDA AÍ 1.0");
            System.out.println("------------" + COR_TEXTO);
            System.out.println();
            System.out.println("> Início > Minha área");
            System.out.println();
            System.out.println("(A) Meus dados");
            System.out.println("(B) Minhas perguntas");
            System.out.println("(C) Minhas respostas");
            System.out.println("(D) Meus votos");
            System.out.println();
            System.out.println("(R) Retornar ao menu anterior");
            System.out.print("\nOpção: " + RESET);

            opcao = scanner.nextLine().trim().toUpperCase();

            switch (opcao) {
                case "A":
                    menuMeusDados(scanner);
                    break;
                case "B":
                    menuMinhasPerguntas(scanner);
                    break;
                case "C":
                case "D":
                    pausar(scanner, "Funcionalidade para as próximas etapas.");
                    break;
                case "R":
                    break;
                default:
                    pausar(scanner, "Opção inválida.");
            }
        } while (!opcao.equals("R"));
    }

    // ========================================================================
    // NÍVEL 3: MEUS DADOS & MINHAS PERGUNTAS
    // ========================================================================
    private static void menuMeusDados(Scanner scanner) throws Exception {
        String opcao;
        do {
            limparEcra();
            System.out.println(COR_ROSA + "AJUDA AÍ 1.0");
            System.out.println("------------" + COR_TEXTO);
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
            System.out.print("\nOpção: " + RESET);

            opcao = scanner.nextLine().trim().toUpperCase();

            if (opcao.equals("A")) {
                System.out.print("Novo nome: ");
                usuarioLogado.setNome(scanner.nextLine().trim());
                arqUsuarios.update(usuarioLogado);
                pausar(scanner, "Nome alterado com sucesso!");
            } else if (!opcao.equals("R")) {
                pausar(scanner, "Opção de alteração em desenvolvimento.");
            }
        } while (!opcao.equals("R"));
    }

    private static void menuMinhasPerguntas(Scanner scanner) throws Exception {
        String opcao;
        do {
            limparEcra();
            System.out.println(COR_ROSA + "AJUDA AÍ 1.0");
            System.out.println("------------" + COR_TEXTO);
            System.out.println("> Início > Minha área > Minhas perguntas\n");
            System.out.println("(A) Listar");
            System.out.println("(B) Incluir");
            System.out.println("(C) Alterar");
            System.out.println("(D) Arquivar");
            System.out.println("\n(R) Retornar");
            System.out.print("\nOpção: " + RESET);

            opcao = scanner.nextLine().trim().toUpperCase();

            switch (opcao) {
                case "A":
                    listarPerguntas(scanner);
                    break;
                case "B":
                    incluirPergunta(scanner);
                    break;
                case "C":
                case "D":
                    pausar(scanner, "Funcionalidade depende da listagem (Árvore B+).");
                    break;
                case "R":
                    break;
                default:
                    pausar(scanner, "Opção inválida.");
            }
        } while (!opcao.equals("R"));
    }

    // ========================================================================
    // OPERAÇÕES COM PERGUNTAS (CRUD VISUAL)
    // ========================================================================
    private static void listarPerguntas(Scanner scanner) {
        limparEcra();
        System.out.println(COR_ROSA + "MINHAS PERGUNTAS\n" + COR_TEXTO);
        
        System.out.println("A listagem de perguntas exigirá a leitura usando a Árvore B+.");
        System.out.println("Será implementado na próxima etapa do projeto.");
        
        pausar(scanner, "");
    }

    private static void incluirPergunta(Scanner scanner) throws Exception {
        limparEcra();
        System.out.println(COR_ROSA + "INCLUIR PERGUNTA" + COR_TEXTO);
        System.out.println();
        
        System.out.println("Digite sua pergunta:");
        System.out.print("> " + RESET);
        String textoPergunta = scanner.nextLine();
        
        System.out.println(COR_TEXTO + "\nDigite as palavras-chave (separadas por ponto-e-vírgula):");
        System.out.print("> " + RESET);
        String palavrasChave = scanner.nextLine();
        
        long dataAtual = System.currentTimeMillis();
        
        // Corrigido para getId() que é o padrão da interface Registro
        Pergunta novaPergunta = new Pergunta(-1, usuarioLogado.getId(), dataAtual, dataAtual, (short) 0, textoPergunta, palavrasChave, true);
        
        arqPerguntas.create(novaPergunta);

        pausar(scanner, "\nPergunta incluída com sucesso!");
    }

    // ========================================================================
    // UTILITÁRIOS E FUNÇÕES TEMPORÁRIAS
    // ========================================================================
    
    // TODO: Adicionar o método `read(String email)` na classe ArquivoUsuario e depois apagar esta função.
    private static Usuario buscarUsuarioPorEmail(String email) {
        // Como o método read(String) ainda não existe em ArquivoUsuario, criámos este atalho.
        // Se usar "teste@puc.br" com senha "123", o sistema deixa-o entrar provisoriamente.
        if (email.equals("teste@puc.br")) {
            return new Usuario(1, "Utilizador Teste", email, Integer.toString("123".hashCode()), "Cor?", Integer.toString("azul".hashCode()));
        }
        return null;
    }

    private static void limparEcra() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void pausar(Scanner scanner, String mensagem) {
        if (!mensagem.isEmpty()) {
            System.out.println(COR_TEXTO + "\n" + mensagem);
        }
        System.out.print("Pressione [ENTER] para continuar..." + RESET);
        scanner.nextLine();
    }
}