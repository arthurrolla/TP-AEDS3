import java.util.Scanner;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;

import entidades.ArquivoPergunta;
import entidades.Pergunta;

public class ControlePergunta {

    private ArquivoPergunta arqPerguntas;
    
    // Vetor de associação (cache) para ligar o número do ecrã (1, 2, 3...) ao ID real no ficheiro
    private ArrayList<Pergunta> cachePerguntas; 

    public ControlePergunta(ArquivoPergunta arqPerguntas) {
        this.arqPerguntas = arqPerguntas;
        this.cachePerguntas = new ArrayList<>();
    }

    public void menuMinhasPerguntas(Scanner scanner, int idUsuarioLogado) throws Exception {
        String opcao;
        do {
            Utilidades.limparEcra();
            System.out.println(Utilidades.COR_ROSA + "AJUDA AÍ 1.0");
            System.out.println("------------" + Utilidades.COR_TEXTO);
            System.out.println("> Início > Minha área > Minhas perguntas\n");
            System.out.println("(A) Listar");
            System.out.println("(B) Incluir");
            System.out.println("(C) Alterar");
            System.out.println("(D) Arquivar");
            System.out.println("\n(R) Retornar");
            System.out.print("\nOpção: " + Utilidades.RESET);

            opcao = scanner.nextLine().trim().toUpperCase();

            switch (opcao) {
                case "A":
                    listarPerguntas(scanner, idUsuarioLogado);
                    Utilidades.pausar(scanner, "");
                    break;
                case "B":
                    incluirPergunta(scanner, idUsuarioLogado);
                    break;
                case "C":
                    alterarPergunta(scanner, idUsuarioLogado);
                    break;
                case "D":
                    arquivarPergunta(scanner, idUsuarioLogado);
                    break;
                case "R":
                    break;
                default:
                    Utilidades.pausar(scanner, "Opção inválida.");
            }
        } while (!opcao.equals("R"));
    }

    // A) LISTAR
    private void listarPerguntas(Scanner scanner, int idUsuarioLogado) throws Exception {
        Utilidades.limparEcra();
        System.out.println(Utilidades.COR_ROSA + "MINHAS PERGUNTAS\n" + Utilidades.COR_TEXTO);
        
        // Busca na Árvore B+ e atualiza a cache de memória
        cachePerguntas = arqPerguntas.readAllByUsuario(idUsuarioLogado);
        
        if (cachePerguntas.isEmpty()) {
            System.out.println("Não tem nenhuma pergunta cadastrada.");
            return;
        } 
        
        SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        // Exibe numerado sequencialmente, escondendo IDs internos
        for (int i = 0; i < cachePerguntas.size(); i++) {
            Pergunta p = cachePerguntas.get(i);
            
            String status = p.isAtiva() ? "" : " ARQUIVADA";
            System.out.println("(" + (i + 1) + ")" + status);
            
            // Formata a data de long (milisegundos) para String legível
            String dataFormatada = formatador.format(new Date(p.getCriacao()));
            System.out.println(dataFormatada);
            
            System.out.println(p.getPergunta());
            System.out.println("Palavras chave: " + p.getPalavrasChave() + "\n");
        }
    }

    // B) INCLUIR
    private void incluirPergunta(Scanner scanner, int idUsuarioLogado) throws Exception {
        Utilidades.limparEcra();
        System.out.println(Utilidades.COR_ROSA + "INCLUIR PERGUNTA" + Utilidades.COR_TEXTO);
        System.out.println();
        
        System.out.println("Digite a sua pergunta:");
        System.out.print("> " + Utilidades.RESET);
        String textoPergunta = scanner.nextLine();
        
        System.out.println(Utilidades.COR_TEXTO + "\nDigite as palavras-chave (separadas por ponto-e-vírgula):");
        System.out.print("> " + Utilidades.RESET);
        String palavrasChave = scanner.nextLine();
        
        long dataAtual = System.currentTimeMillis();
        
        // A entidade é criada e enviada para o seu método create() no back-end
        Pergunta novaPergunta = new Pergunta(-1, idUsuarioLogado, dataAtual, dataAtual, (short) 0, textoPergunta, palavrasChave, true);
        arqPerguntas.create(novaPergunta);

        Utilidades.pausar(scanner, "\nPergunta incluída com sucesso na Árvore B+ e no ficheiro principal!");
    }

    // C) ALTERAR
    private void alterarPergunta(Scanner scanner, int idUsuarioLogado) throws Exception {
        // Aproveita a função listarPerguntas para exibir as opções antes de alterar
        listarPerguntas(scanner, idUsuarioLogado);
        
        if (cachePerguntas.isEmpty()) {
            Utilidades.pausar(scanner, "");
            return;
        }

        System.out.print(Utilidades.COR_TEXTO + "Digite o número da pergunta que deseja ALTERAR (ou 0 para cancelar): " + Utilidades.RESET);
        int numeroEscolhido = Integer.parseInt(scanner.nextLine().trim());

        if (numeroEscolhido == 0) return;
        
        if (numeroEscolhido < 1 || numeroEscolhido > cachePerguntas.size()) {
            Utilidades.pausar(scanner, "Número inválido.");
            return;
        }

        // Obtém o ID real da pergunta através do vetor de associação (índice - 1)
        Pergunta perguntaAlvo = cachePerguntas.get(numeroEscolhido - 1);

        if (!perguntaAlvo.isAtiva()) {
            Utilidades.pausar(scanner, "Não é possível alterar uma pergunta que já foi arquivada.");
            return;
        }

        System.out.println(Utilidades.COR_TEXTO + "\nTexto atual: " + perguntaAlvo.getPergunta());
        System.out.print("Novo texto (deixe em branco para manter o atual): " + Utilidades.RESET);
        String novoTexto = scanner.nextLine().trim();
        if (novoTexto.isEmpty()) novoTexto = perguntaAlvo.getPergunta();

        System.out.println(Utilidades.COR_TEXTO + "Palavras-chave atuais: " + perguntaAlvo.getPalavrasChave());
        System.out.print("Novas palavras-chave (deixe em branco para manter as atuais): " + Utilidades.RESET);
        String novasPalavras = scanner.nextLine().trim();
        if (novasPalavras.isEmpty()) novasPalavras = perguntaAlvo.getPalavrasChave();

        // Chama o método específico de atualização segura do seu back-end
        boolean sucesso = arqPerguntas.atualizarConteudo(idUsuarioLogado, perguntaAlvo.getId(), novoTexto, novasPalavras);
        
        if (sucesso) {
            Utilidades.pausar(scanner, "Pergunta atualizada com sucesso.");
        } else {
            Utilidades.pausar(scanner, "Erro ao atualizar a pergunta.");
        }
    }

    // D) ARQUIVAR
    private void arquivarPergunta(Scanner scanner, int idUsuarioLogado) throws Exception {
        listarPerguntas(scanner, idUsuarioLogado);
        
        if (cachePerguntas.isEmpty()) {
            Utilidades.pausar(scanner, "");
            return;
        }

        System.out.print(Utilidades.COR_TEXTO + "Digite o número da pergunta que deseja ARQUIVAR (ou 0 para cancelar): " + Utilidades.RESET);
        int numeroEscolhido = Integer.parseInt(scanner.nextLine().trim());

        if (numeroEscolhido == 0) return;

        if (numeroEscolhido < 1 || numeroEscolhido > cachePerguntas.size()) {
            Utilidades.pausar(scanner, "Número inválido.");
            return;
        }

        Pergunta perguntaAlvo = cachePerguntas.get(numeroEscolhido - 1);

        System.out.print(Utilidades.COR_TEXTO + "Tem a certeza que deseja arquivar a pergunta nº " + numeroEscolhido + "? (S/N): " + Utilidades.RESET);
        String confirmacao = scanner.nextLine().trim().toUpperCase();

        if (confirmacao.equals("S")) {
            // Chama o método seguro de arquivamento do seu back-end
            boolean sucesso = arqPerguntas.arquivar(idUsuarioLogado, perguntaAlvo.getId());
            if (sucesso) {
                Utilidades.pausar(scanner, "Pergunta arquivada com sucesso! Ela deixará de receber respostas.");
            } else {
                Utilidades.pausar(scanner, "Erro ao arquivar a pergunta ou ela já estava arquivada.");
            }
        }
    }
}