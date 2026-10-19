import java.util.Scanner;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;

import entidades.ArquivoPergunta;
import entidades.Pergunta;
import entidades.ArquivoUsuario;
import entidades.Usuario;
import entidades.ArquivoResposta;
import entidades.Resposta;

public class ControlePergunta {

    private ArquivoPergunta arqPerguntas;
    private ArquivoUsuario arqUsuarios;
    private ArquivoResposta arqRespostas;
    private ControleResposta controleResposta;
    
    // Vetor de associação (cache) para ligar o número do ecrã (1, 2, 3...) ao ID real no ficheiro
    private ArrayList<Pergunta> cachePerguntas; 

    public ControlePergunta(
        ArquivoPergunta arqPerguntas,
        ArquivoUsuario arqUsuarios,
        ArquivoResposta arqRespostas,
        ControleResposta controleResposta) {

        this.arqPerguntas = arqPerguntas;
        this.arqUsuarios = arqUsuarios;
        this.arqRespostas = arqRespostas;
        this.controleResposta = controleResposta;
        this.cachePerguntas = new ArrayList<>();
    }

    public void menuBuscarPerguntas(Scanner scanner, int idUsuarioLogado)throws Exception {
        String opcao;
        do{
            Utilidades.limparEcra();
            System.out.println(Utilidades.COR_ROSA + "AJUDA AÍ 1.0");
            System.out.println("------------" + Utilidades.COR_TEXTO);
            System.out.println("> Início > Buscar perguntas\n");
            System.out.println("(A) Perguntas mais recentes");
            System.out.println("(B) Busca por palavras-chave");
            System.out.println("\n(R) Retornar");
            System.out.print("\nOpção: " + Utilidades.RESET);
            opcao = scanner.nextLine().trim().toUpperCase();

            switch(opcao){
                case "A":
                    listarPerguntasRecentes(scanner, idUsuarioLogado);
                    break;
                case "B":
                    Utilidades.pausar(scanner,"Busca por palavras-chave não implementada nesta etapa.");
                    break;
                case "R":
                    break;
                default:
                    Utilidades.pausar(scanner, "Opção inválida.");
            }
        }while(!opcao.equals("R"));
    }

    private void listarPerguntasRecentes(Scanner scanner, int idUsuarioLogado)throws Exception{
        int pagina = 0;
        SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm");
        while (true){
            ArrayList<Pergunta> perguntas = arqPerguntas.readRecentes(pagina);
            Utilidades.limparEcra();
            System.out.println(Utilidades.COR_ROSA + "AJUDA AÍ 1.0");
            System.out.println("------------" + Utilidades.COR_TEXTO);
            System.out.println("> Início > Buscar perguntas > Perguntas mais recentes\n");
            System.out.println("Página " + (pagina + 1) + "\n");
            if (perguntas.isEmpty()){
                System.out.println("Nenhuma pergunta encontrada.");
            }
            for (int i = 0; i < perguntas.size(); i++) {
                Pergunta pergunta = perguntas.get(i);
                Usuario autor = arqUsuarios.read(pergunta.getIdUsuario());
                String nomeAutor = autor != null ? autor.getNome() : "Usuário indisponível";
                String data = formatador.format(new Date(pergunta.getCriacao()));
                
                System.out.println("(" + (i + 1) + ") " + pergunta.getPergunta());
                System.out.println("    Criada por " + nomeAutor + " em " + data);
                System.out.println();
            }
            System.out.println("(A) Página anterior");
            System.out.println("(B) Próxima página");
            System.out.println("\n(R) Retornar");
            System.out.print("\nOpção: " + Utilidades.RESET);

            String opcao = scanner.nextLine().trim().toUpperCase();
            switch (opcao){
                case "A":
                    if (pagina > 0) {
                        pagina--;
                    } else {
                        Utilidades.pausar(scanner, "Você já está na primeira página.");
                    }
                    break;
                case "B":
                    if (!arqPerguntas.readRecentes(pagina + 1).isEmpty()) {
                        pagina++;
                    } else {
                        Utilidades.pausar(scanner, "Não há próxima página.");
                    }
                    break;
                case "R":
                    return;
                default:
                    int numero;
                    try{
                        numero = Integer.parseInt(opcao);
                    }catch(NumberFormatException e){
                        Utilidades.pausar(scanner, "Opção inválida.");
                        break;
                    }
                    if (numero < 1 || numero > perguntas.size()) {
                        Utilidades.pausar(scanner, "Número inválido.");
                        break;
                    }

                    Pergunta selecionada = perguntas.get(numero - 1);
                    visualizarPergunta(scanner, idUsuarioLogado, selecionada.getId());
            }
        }
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
                    consultarMinhasPerguntas(scanner, idUsuarioLogado);
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
        String entrada = scanner.nextLine().trim();
        if (entrada.equals("0")) return;

        int numeroEscolhido = Utilidades.lerNumero(entrada);
        if (numeroEscolhido < 1 || numeroEscolhido > cachePerguntas.size()) {
            Utilidades.pausar(scanner, "Número inválido.");
            return;
        }

        // Obtém a pergunta real através do vetor de associação (índice - 1)
        alterarPerguntaSelecionada(scanner, idUsuarioLogado, cachePerguntas.get(numeroEscolhido - 1));
    }

    // Altera texto e palavras-chave de uma pergunta já escolhida
    private void alterarPerguntaSelecionada(Scanner scanner, int idUsuarioLogado, Pergunta perguntaAlvo) throws Exception {
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

        try {
            boolean sucesso = arqPerguntas.atualizarConteudo(idUsuarioLogado, perguntaAlvo.getId(), novoTexto, novasPalavras);
            Utilidades.pausar(scanner, sucesso ? "Pergunta atualizada com sucesso." : "Erro ao atualizar a pergunta.");
        } catch (Exception e) {
            Utilidades.pausar(scanner, "Erro: " + e.getMessage());
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
        String entrada = scanner.nextLine().trim();
        if (entrada.equals("0")) return;

        int numeroEscolhido = Utilidades.lerNumero(entrada);
        if (numeroEscolhido < 1 || numeroEscolhido > cachePerguntas.size()) {
            Utilidades.pausar(scanner, "Número inválido.");
            return;
        }

        arquivarPerguntaSelecionada(scanner, idUsuarioLogado, cachePerguntas.get(numeroEscolhido - 1));
    }

    // Arquiva uma pergunta já escolhida. Retorna true se ela foi arquivada.
    private boolean arquivarPerguntaSelecionada(Scanner scanner, int idUsuarioLogado, Pergunta perguntaAlvo) throws Exception {
        System.out.print(Utilidades.COR_TEXTO + "\nTem certeza que deseja arquivar esta pergunta? (S/N): " + Utilidades.RESET);
        String confirmacao = scanner.nextLine().trim().toUpperCase();
        if (!confirmacao.equals("S")) {
            return false;
        }
        try {
            boolean sucesso = arqPerguntas.arquivar(idUsuarioLogado, perguntaAlvo.getId());
            if (sucesso) {
                Utilidades.pausar(scanner, "Pergunta arquivada com sucesso! Ela deixará de receber respostas.");
            } else {
                Utilidades.pausar(scanner, "Erro ao arquivar a pergunta ou ela já estava arquivada.");
            }
            return sucesso;
        } catch (Exception e) {
            Utilidades.pausar(scanner, "Erro: " + e.getMessage());
            return false;
        }
    }

    // A) LISTAR (versão com seleção): lista as perguntas do usuário e abre a escolhida
    private void consultarMinhasPerguntas(Scanner scanner, int idUsuarioLogado) throws Exception {
        while (true) {
            listarPerguntas(scanner, idUsuarioLogado);
            if (cachePerguntas.isEmpty()) {
                Utilidades.pausar(scanner, "");
                return;
            }
            System.out.println(Utilidades.COR_TEXTO + "Digite o número de uma pergunta para ver os detalhes e as respostas.");
            System.out.println("\n(R) Retornar");
            System.out.print("\nOpção: " + Utilidades.RESET);
            String opcao = scanner.nextLine().trim().toUpperCase();
            if (opcao.equals("R")) {
                return;
            }
            int numero = Utilidades.lerNumero(opcao);
            if (numero < 1 || numero > cachePerguntas.size()) {
                Utilidades.pausar(scanner, "Opção inválida.");
                continue;
            }
            visualizarMinhaPergunta(scanner, idUsuarioLogado, cachePerguntas.get(numero - 1).getId());
        }
    }

    // Tela "Minha área > Minhas perguntas > Pergunta": dados da pergunta + respostas de outros usuários
    private void visualizarMinhaPergunta(Scanner scanner, int idUsuarioLogado, int idPergunta) throws Exception {
        String caminho = "> Início > Minha área > Minhas perguntas > Pergunta";
        while (true) {
            Pergunta pergunta = arqPerguntas.read(idPergunta);
            if (pergunta == null) {
                Utilidades.pausar(scanner, "Pergunta indisponível.");
                return;
            }
            ArrayList<Resposta> respostas = arqRespostas.readAllByPergunta(idPergunta);

            Utilidades.exibirCabecalho(caminho);
            System.out.println(Utilidades.formatarData(pergunta.getCriacao())
                + (pergunta.isAtiva() ? "" : "  [ARQUIVADA]"));
            System.out.println(pergunta.getPergunta());
            System.out.println("Palavras chave: " + pergunta.getPalavrasChave() + "\n");
            controleResposta.mostrarRespostas(respostas, idUsuarioLogado);

            System.out.println();
            if (!respostas.isEmpty()) {
                System.out.println("Digite o número de uma resposta para visualizá-la ou arquivá-la.\n");
            }
            if (pergunta.isAtiva()) {
                System.out.println("(A) Alterar dados da pergunta");
                System.out.println("(B) Arquivar pergunta");
            }
            System.out.println("(R) Retornar");
            System.out.print("\nOpção: " + Utilidades.RESET);
            String opcao = scanner.nextLine().trim().toUpperCase();

            switch (opcao) {
                case "A":
                    if (pergunta.isAtiva()) {
                        alterarPerguntaSelecionada(scanner, idUsuarioLogado, pergunta);
                    } else {
                        Utilidades.pausar(scanner, "Opção inválida.");
                    }
                    break;
                case "B":
                    if (pergunta.isAtiva()) {
                        if (arquivarPerguntaSelecionada(scanner, idUsuarioLogado, pergunta)) return;
                    } else {
                        Utilidades.pausar(scanner, "Opção inválida.");
                    }
                    break;
                case "R":
                    return;
                default:
                    int numero = Utilidades.lerNumero(opcao);
                    if (numero < 1 || numero > respostas.size()) {
                        Utilidades.pausar(scanner, "Opção inválida.");
                        break;
                    }
                    controleResposta.visualizarResposta(scanner, idUsuarioLogado,
                        respostas.get(numero - 1).getId(), caminho);
            }
        }
    }

    private void visualizarPergunta(Scanner scanner, int idUsuarioLogado, int idPergunta)throws Exception{
        String caminho = "> Início > Buscar perguntas > Perguntas mais recentes > Pergunta";
        SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm");
        while (true){
            Pergunta pergunta = arqPerguntas.read(idPergunta);
            if (pergunta == null || !pergunta.isAtiva()) {
                Utilidades.pausar(scanner, "Pergunta indisponível.");
                return;
            }
            Usuario autor = arqUsuarios.read(pergunta.getIdUsuario());
            String nomeAutor = autor != null? autor.getNome(): "Usuário indisponível";
            // Já vem ordenada por nota (decrescente) a partir da árvore B+ de relacionamento
            ArrayList<Resposta> respostas = arqRespostas.readAllByPergunta(idPergunta);

            Utilidades.exibirCabecalho(caminho);
            System.out.println(pergunta.getPergunta());
            System.out.println("Criada por " + nomeAutor + " em "+ formatador.format(new Date(pergunta.getCriacao())));
            System.out.println("Palavras-chave: " + pergunta.getPalavrasChave() + "\n");
            controleResposta.mostrarRespostas(respostas, idUsuarioLogado);

            System.out.println();
            if (!respostas.isEmpty()) {
                System.out.println("Digite o número de uma resposta para visualizá-la e avaliá-la.\n");
            }
            System.out.println("(A) Responder à pergunta");
            System.out.println("(R) Retornar");
            System.out.print("\nOpção: " + Utilidades.RESET);
            String opcao = scanner.nextLine().trim().toUpperCase();

            switch (opcao) {
                case "A":
                    controleResposta.responderPergunta(scanner, idUsuarioLogado, idPergunta, caminho);
                    break;
                case "R":
                    return;
                default:
                    int numero = Utilidades.lerNumero(opcao);
                    if (numero < 1 || numero > respostas.size()) {
                        Utilidades.pausar(scanner, "Opção inválida.");
                        break;
                    }
                    controleResposta.visualizarResposta(scanner, idUsuarioLogado,
                        respostas.get(numero - 1).getId(), caminho);
            }
        }
    }
}
