import java.util.ArrayList;
import java.util.Scanner;

import entidades.ArquivoAvaliacao;
import entidades.ArquivoPergunta;
import entidades.ArquivoResposta;
import entidades.ArquivoUsuario;
import entidades.Avaliacao;
import entidades.Pergunta;
import entidades.Resposta;
import entidades.Usuario;

// Interface (menus e telas) das operações com respostas e avaliações
public class ControleResposta {

    private ArquivoResposta arqRespostas;
    private ArquivoPergunta arqPerguntas;
    private ArquivoUsuario arqUsuarios;
    private ArquivoAvaliacao arqAvaliacoes;

    public ControleResposta(
        ArquivoResposta arqRespostas,
        ArquivoPergunta arqPerguntas,
        ArquivoUsuario arqUsuarios,
        ArquivoAvaliacao arqAvaliacoes) {

        this.arqRespostas = arqRespostas;
        this.arqPerguntas = arqPerguntas;
        this.arqUsuarios = arqUsuarios;
        this.arqAvaliacoes = arqAvaliacoes;
    }

    // ------------------------------------------------------------------
    // Métodos auxiliares de exibição
    // ------------------------------------------------------------------

    private String nomeUsuario(int idUsuario) throws Exception {
        Usuario usuario = arqUsuarios.read(idUsuario);
        return usuario != null ? usuario.getNome() : "Usuário indisponível";
    }

    private void mostrarPergunta(Pergunta pergunta) throws Exception {
        System.out.println(pergunta.getPergunta());
        System.out.println("Criada por " + nomeUsuario(pergunta.getIdUsuario()) + " em "
                + Utilidades.formatarData(pergunta.getCriacao()));
    }

    // Mostra a lista numerada de respostas (usada na tela da pergunta). A ordem recebida é mantida.
    public void mostrarRespostas(ArrayList<Resposta> respostas, int idUsuarioLogado) throws Exception {
        System.out.println("RESPOSTAS");
        System.out.println("---------");
        if (respostas.isEmpty()) {
            System.out.println("Esta pergunta ainda não tem respostas.");
            return;
        }
        for (int i = 0; i < respostas.size(); i++) {
            Resposta resposta = respostas.get(i);
            String autor = resposta.getIdUsuario() == idUsuarioLogado
                ? "você" : nomeUsuario(resposta.getIdUsuario());
            System.out.println("\n(" + (i + 1) + ") " + resposta.getResposta());
            System.out.println("Respondido por " + autor + " em " + Utilidades.formatarData(resposta.getCriacao()));
            System.out.println(Utilidades.textoNota(resposta.getNota(), resposta.getAvaliacoes()));
        }
    }

    // Procura a avaliação feita pelo usuário em uma resposta (null se ainda não avaliou)
    private Avaliacao buscarAvaliacao(int idUsuario, int idResposta) throws Exception {
        for (Avaliacao avaliacao : arqAvaliacoes.readAvaliacoesPorUsuario(idUsuario)) {
            if (avaliacao.getIdResposta() == idResposta) {
                return avaliacao;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Responder uma pergunta
    // ------------------------------------------------------------------

    // 'caminho' é o breadcrumb da tela da pergunta (ex.: "> Início > Buscar perguntas > ... > Pergunta")
    public void responderPergunta(Scanner scanner, int idUsuarioLogado, int idPergunta, String caminho) throws Exception {
        Pergunta pergunta = arqPerguntas.read(idPergunta);
        if (pergunta == null || !pergunta.isAtiva()) {
            Utilidades.pausar(scanner, "Esta pergunta não está disponível para respostas.");
            return;
        }

        Utilidades.exibirCabecalho(caminho + " > Responder");
        mostrarPergunta(pergunta);
        System.out.println("\nDigite sua resposta.");
        System.out.println("Pode usar várias linhas e deixar linhas em branco.");
        System.out.println("Para finalizar, digite /fim em uma linha separada.");
        System.out.println("Para cancelar, digite /cancelar.");
        System.out.println("\nResposta:" + Utilidades.RESET);

        StringBuilder texto = new StringBuilder();
        boolean primeiraLinha = true;
        while (true) {
            String linha = scanner.nextLine();
            if (linha.trim().equalsIgnoreCase("/cancelar")) {
                return;
            }
            if (linha.trim().equalsIgnoreCase("/fim")) {
                break;
            }
            if (!primeiraLinha) {
                texto.append("\n");
            }
            texto.append(linha);
            primeiraLinha = false;
        }

        if (texto.toString().trim().isEmpty()) {
            Utilidades.pausar(scanner, "A resposta não pode ficar vazia.");
            return;
        }

        try {
            // Apenas o texto vem do usuário; criação, nota, avaliações e ativa são preenchidos automaticamente
            Resposta novaResposta = new Resposta(-1, idPergunta, idUsuarioLogado,
                System.currentTimeMillis(), texto.toString(), (short) 0, (short) 0, true);
            arqRespostas.create(novaResposta);
            Utilidades.pausar(scanner, "Resposta incluída com sucesso.");
        } catch (Exception e) {
            Utilidades.pausar(scanner, "Erro ao incluir a resposta: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Visualizar uma resposta (avaliar / arquivar, conforme o usuário)
    // ------------------------------------------------------------------

    // 'caminho' é o breadcrumb da tela anterior; esta tela acrescenta " > Resposta"
    public void visualizarResposta(Scanner scanner, int idUsuarioLogado, int idResposta, String caminho) throws Exception {
        while (true) {
            Resposta resposta = arqRespostas.read(idResposta);
            if (resposta == null || !resposta.isAtiva()) {
                Utilidades.pausar(scanner, "Resposta indisponível.");
                return;
            }
            Pergunta pergunta = arqPerguntas.read(resposta.getIdPergunta());
            if (pergunta == null) {
                Utilidades.pausar(scanner, "A pergunta desta resposta está indisponível.");
                return;
            }

            boolean propria = resposta.getIdUsuario() == idUsuarioLogado;
            boolean autorDaPergunta = pergunta.getIdUsuario() == idUsuarioLogado;
            Avaliacao minhaAvaliacao = propria ? null : buscarAvaliacao(idUsuarioLogado, idResposta);
            boolean podeAvaliar = !propria && minhaAvaliacao == null;
            // O autor da resposta ou o autor da pergunta podem arquivar a resposta
            boolean podeArquivar = propria || autorDaPergunta;

            Utilidades.exibirCabecalho(caminho + " > Resposta");
            mostrarPergunta(pergunta);
            System.out.println("\nRESPOSTA");
            System.out.println("--------");
            System.out.println(resposta.getResposta());
            String autor = propria ? "você" : nomeUsuario(resposta.getIdUsuario());
            System.out.println("Respondido por " + autor + " em " + Utilidades.formatarData(resposta.getCriacao()));
            System.out.println(Utilidades.textoNota(resposta.getNota(), resposta.getAvaliacoes()));
            System.out.println();

            if (propria) {
                System.out.println("(A) Arquivar resposta");
            } else if (podeAvaliar) {
                System.out.println("(A) Concordar com a resposta");
                System.out.println("(B) Discordar da resposta");
            } else {
                System.out.println("Você já " + (minhaAvaliacao.isAvaliacao() ? "concordou com" : "discordou de")
                    + " esta resposta.");
                System.out.println("Para mudar a avaliação, cancele-a em Minha área > Minhas avaliações.");
            }
            if (!propria && autorDaPergunta) {
                System.out.println("(C) Arquivar resposta (você é o autor da pergunta)");
            }
            System.out.println("\n(R) Retornar ao menu anterior");
            System.out.print("\nOpção: " + Utilidades.RESET);
            String opcao = scanner.nextLine().trim().toUpperCase();

            switch (opcao) {
                case "A":
                    if (propria) {
                        if (arquivarResposta(scanner, idUsuarioLogado, idResposta)) return;
                    } else if (podeAvaliar) {
                        avaliarResposta(scanner, idUsuarioLogado, idResposta, true);
                    } else {
                        Utilidades.pausar(scanner, "Opção inválida.");
                    }
                    break;
                case "B":
                    if (podeAvaliar) {
                        avaliarResposta(scanner, idUsuarioLogado, idResposta, false);
                    } else {
                        Utilidades.pausar(scanner, "Opção inválida.");
                    }
                    break;
                case "C":
                    if (!propria && podeArquivar) {
                        if (arquivarResposta(scanner, idUsuarioLogado, idResposta)) return;
                    } else {
                        Utilidades.pausar(scanner, "Opção inválida.");
                    }
                    break;
                case "R":
                    return;
                default:
                    Utilidades.pausar(scanner, "Opção inválida.");
            }
        }
    }

    // Retorna true se a resposta foi arquivada (a tela deve ser encerrada)
    private boolean arquivarResposta(Scanner scanner, int idUsuarioLogado, int idResposta) throws Exception {
        System.out.print(Utilidades.COR_TEXTO + "\nTem certeza que deseja arquivar esta resposta? (S/N): " + Utilidades.RESET);
        String confirmacao = scanner.nextLine().trim().toUpperCase();
        if (!confirmacao.equals("S")) {
            return false;
        }
        try {
            if (arqRespostas.arquivar(idUsuarioLogado, idResposta)) {
                Utilidades.pausar(scanner, "Resposta arquivada com sucesso.");
                return true;
            }
            Utilidades.pausar(scanner, "Não foi possível arquivar a resposta (ela já estava arquivada?).");
        } catch (Exception e) {
            Utilidades.pausar(scanner, "Erro: " + e.getMessage());
        }
        return false;
    }

    private void avaliarResposta(Scanner scanner, int idUsuarioLogado, int idResposta, boolean positiva) throws Exception {
        try {
            if (arqAvaliacoes.avaliar(idUsuarioLogado, idResposta, positiva)) {
                Utilidades.pausar(scanner, positiva
                    ? "Você concordou com a resposta. A nota foi atualizada."
                    : "Você discordou da resposta. A nota foi atualizada.");
            } else {
                Utilidades.pausar(scanner, "Não foi possível registrar a avaliação.");
            }
        } catch (Exception e) {
            // Ex.: "Você já avaliou esta resposta." ou "Você não pode avaliar sua própria resposta."
            Utilidades.pausar(scanner, "Erro: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Minha área > Minhas respostas
    // ------------------------------------------------------------------

    public void menuMinhasRespostas(Scanner scanner, int idUsuarioLogado) throws Exception {
        String caminho = "> Início > Minha área > Minhas respostas";
        while (true) {
            ArrayList<Resposta> respostas = arqRespostas.readAllByUsuario(idUsuarioLogado);
            // Mais recentes primeiro
            respostas.sort((a, b) -> Long.compare(b.getCriacao(), a.getCriacao()));

            Utilidades.exibirCabecalho(caminho);
            if (respostas.isEmpty()) {
                Utilidades.pausar(scanner, "Você ainda não escreveu nenhuma resposta.");
                return;
            }

            for (int i = 0; i < respostas.size(); i++) {
                Resposta resposta = respostas.get(i);
                Pergunta pergunta = arqPerguntas.read(resposta.getIdPergunta());
                String textoPergunta = pergunta != null ? pergunta.getPergunta() : "Pergunta indisponível";
                System.out.println("(" + (i + 1) + ") " + Utilidades.resumir(textoPergunta, 70));
                System.out.println("    Sua resposta: " + Utilidades.resumir(resposta.getResposta(), 70));
                System.out.println("    Em " + Utilidades.formatarData(resposta.getCriacao()) + " | "
                    + Utilidades.textoNota(resposta.getNota(), resposta.getAvaliacoes()));
                System.out.println();
            }
            System.out.println("Digite o número de uma resposta para visualizá-la.");
            System.out.println("\n(R) Retornar ao menu anterior");
            System.out.print("\nOpção: " + Utilidades.RESET);
            String opcao = scanner.nextLine().trim().toUpperCase();

            if (opcao.equals("R")) {
                return;
            }
            int numero = Utilidades.lerNumero(opcao);
            if (numero < 1 || numero > respostas.size()) {
                Utilidades.pausar(scanner, "Opção inválida.");
                continue;
            }
            visualizarResposta(scanner, idUsuarioLogado, respostas.get(numero - 1).getId(), caminho);
        }
    }

    // ------------------------------------------------------------------
    // Minha área > Minhas avaliações
    // ------------------------------------------------------------------

    public void menuMinhasAvaliacoes(Scanner scanner, int idUsuarioLogado) throws Exception {
        String caminho = "> Início > Minha área > Minhas avaliações";
        while (true) {
            // Mantém apenas avaliações de respostas ainda ativas (respostas arquivadas não aparecem nas listagens)
            ArrayList<Avaliacao> avaliacoes = new ArrayList<>();
            for (Avaliacao avaliacao : arqAvaliacoes.readAvaliacoesPorUsuario(idUsuarioLogado)) {
                Resposta resposta = arqRespostas.read(avaliacao.getIdResposta());
                if (resposta != null && resposta.isAtiva()) {
                    avaliacoes.add(avaliacao);
                }
            }
            // Mais recentes primeiro (IDs sequenciais)
            avaliacoes.sort((a, b) -> Integer.compare(b.getId(), a.getId()));

            Utilidades.exibirCabecalho(caminho);
            if (avaliacoes.isEmpty()) {
                Utilidades.pausar(scanner, "Você ainda não avaliou nenhuma resposta.");
                return;
            }

            for (int i = 0; i < avaliacoes.size(); i++) {
                Avaliacao avaliacao = avaliacoes.get(i);
                Resposta resposta = arqRespostas.read(avaliacao.getIdResposta());
                Pergunta pergunta = arqPerguntas.read(resposta.getIdPergunta());
                String textoPergunta = pergunta != null ? pergunta.getPergunta() : "Pergunta indisponível";
                System.out.println("(" + (i + 1) + ") " + Utilidades.resumir(textoPergunta, 70));
                System.out.println("    Resposta: " + Utilidades.resumir(resposta.getResposta(), 70));
                System.out.println("    Sua avaliação: " + (avaliacao.isAvaliacao() ? "Positiva" : "Negativa"));
                System.out.println();
            }
            System.out.println("Digite o número de uma avaliação para visualizá-la.");
            System.out.println("\n(R) Retornar ao menu anterior");
            System.out.print("\nOpção: " + Utilidades.RESET);
            String opcao = scanner.nextLine().trim().toUpperCase();

            if (opcao.equals("R")) {
                return;
            }
            int numero = Utilidades.lerNumero(opcao);
            if (numero < 1 || numero > avaliacoes.size()) {
                Utilidades.pausar(scanner, "Opção inválida.");
                continue;
            }
            visualizarAvaliacao(scanner,idUsuarioLogado,avaliacoes.get(numero - 1).getId(),caminho);
        }
    }

    // Tela de uma avaliação: pergunta, resposta avaliada e a avaliação feita. Única ação: cancelar.
    private void visualizarAvaliacao(Scanner scanner,int idUsuarioLogado, int idAvaliacao, String caminho) throws Exception {
        while (true) {
            Avaliacao avaliacao = arqAvaliacoes.read(idAvaliacao);
            Resposta resposta = avaliacao != null ? arqRespostas.read(avaliacao.getIdResposta()) : null;
            if (avaliacao == null || resposta == null || !resposta.isAtiva()) {
                Utilidades.pausar(scanner, "Avaliação indisponível.");
                return;
            }
            Pergunta pergunta = arqPerguntas.read(resposta.getIdPergunta());
            if (pergunta == null) {
                Utilidades.pausar(scanner, "A pergunta desta resposta está indisponível.");
                return;
            }

            Utilidades.exibirCabecalho(caminho + " > Avaliação");
            mostrarPergunta(pergunta);
            System.out.println("\nRESPOSTA AVALIADA");
            System.out.println("-----------------");
            System.out.println(resposta.getResposta());
            System.out.println("Respondido por " + nomeUsuario(resposta.getIdUsuario()) + " em "
                + Utilidades.formatarData(resposta.getCriacao()));
            System.out.println(Utilidades.textoNota(resposta.getNota(), resposta.getAvaliacoes()));
            System.out.println("\nSua avaliação: "
                + (avaliacao.isAvaliacao() ? "Positiva (você concordou)" : "Negativa (você discordou)"));
            System.out.println("\n(A) Cancelar avaliação");
            System.out.println("\n(R) Retornar ao menu anterior");
            System.out.print("\nOpção: " + Utilidades.RESET);
            String opcao = scanner.nextLine().trim().toUpperCase();

            switch (opcao) {
                case "A":
                    System.out.print(Utilidades.COR_TEXTO + "\nTem certeza que deseja cancelar esta avaliação? (S/N): " + Utilidades.RESET);
                    if (scanner.nextLine().trim().equalsIgnoreCase("S")) {
                        try {
                            if (arqAvaliacoes.cancelar(idUsuarioLogado, idAvaliacao)) {
                                Utilidades.pausar(scanner, "Avaliação cancelada. A nota da resposta foi ajustada.");
                                return;
                            }
                            Utilidades.pausar(scanner, "Não foi possível cancelar a avaliação.");
                        } catch (Exception e) {
                            Utilidades.pausar(scanner, "Erro: " + e.getMessage());
                        }
                    }
                    break;
                case "R":
                    return;
                default:
                    Utilidades.pausar(scanner, "Opção inválida.");
            }
        }
    }
}
