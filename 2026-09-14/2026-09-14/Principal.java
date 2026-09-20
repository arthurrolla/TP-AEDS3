import entidades.*;
import java.io.File;
import java.time.LocalDate;

/**
 * Classe principal para testar as operações de armazenamento e recuperação de livros.
 * Cria alguns livros de exemplo, os insere em um arquivo de dados e os recupera.
 */
public class Principal {

    /**
     * Método principal do programa.
     * Demonstra o funcionamento dos métodos de criação e leitura de livros no arquivo.
     * 
     * @param args Argumentos da linha de comando (não utilizados)
     */
    public static void main(String[] args) {

        File f = new File("./dados");
        if (!f.exists()) {
            f.mkdirs();
        }
        f = new File("./dados/livros");
        if (!f.exists()) {
            f.mkdirs();
        }
        f = new File("./dados/categorias");
        if (!f.exists()) {
            f.mkdirs();
        }
        f = new File("./dados/autores");
        if (!f.exists()) {
            f.mkdirs();
        }
        f = new File("./dados/autorias");
        if (!f.exists()) {
            f.mkdirs();
        }

        (new File("./dados/livros/arquivo.db")).delete();
        (new File("./dados/livros/indiceID.diretorio.db")).delete();
        (new File("./dados/livros/indiceID.cestos.db")).delete();
        (new File("./dados/livros/indiceIsbn.diretorio.db")).delete();
        (new File("./dados/livros/indiceIsbn.cestos.db")).delete();
        (new File("./dados/livros/relCategoriaLivro.db")).delete();
        (new File("./dados/categorias/arquivo.db")).delete();
        (new File("./dados/categorias/indiceID.diretorio.db")).delete();
        (new File("./dados/categorias/indiceID.cestos.db")).delete();
        (new File("./dados/categorias/indiceNome.db")).delete();
        (new File("./dados/autores/arquivo.db")).delete();
        (new File("./dados/autores/indiceID.diretorio.db")).delete();
        (new File("./dados/autores/indiceID.cestos.db")).delete();
        (new File("./dados/autores/indiceNome.db")).delete();
        (new File("./dados/autorias/arquivo.db")).delete();
        (new File("./dados/autorias/indiceID.diretorio.db")).delete();
        (new File("./dados/autorias/relLivroAutoria.db")).delete();
        (new File("./dados/autorias/relAutorAutoria.db")).delete();



        ArquivoLivro arqLivros;
        ArquivoCategoria arqCategorias;
        ArquivoAutor arqAutores;
        ArquivoAutoria arqAutorias;

        try {
            arqLivros = new ArquivoLivro();
            arqCategorias = new ArquivoCategoria();
            arqAutores = new ArquivoAutor();
            arqAutorias = new ArquivoAutoria();

            // Cria todas as categorias
            int idCategoria1 = arqCategorias.create(new Categoria("Tecnologia"));
            int idCategoria2 = arqCategorias.create(new Categoria("Gestão"));
            int idCategoria3 = arqCategorias.create(new Categoria("Inovação"));
            int idCategoria4 = arqCategorias.create(new Categoria("Desenvolvimento Pessoal"));
            int idCategoria5 = arqCategorias.create(new Categoria("Comunicação"));

            // Cria todos os autores
            int idAutor1 = arqAutores.create(new Autor("Kai-Fu Lee"));
            int idAutor2 = arqAutores.create(new Autor("Chen Qiufan"));
            int idAutor3 = arqAutores.create(new Autor("Jeff Sutherland"));
            int idAutor4 = arqAutores.create(new Autor("J. J. Sutherland"));
            int idAutor5 = arqAutores.create(new Autor("Tim Brown"));
            int idAutor6 = arqAutores.create(new Autor("Stephen R. Covey"));
            int idAutor7 = arqAutores.create(new Autor("Dale Carnegie"));

            // Cria os livros e associa os autores a eles
            Livro livro1 = new Livro(
                "9786559870530",
                "2041: Como a inteligência artificial vai mudar sua vida nas próximas décadas",
                idCategoria1,
                "Globo Livros",
                (short) 1,
                LocalDate.of(2022, 7, 27),
                480,
                5559,
                false
            );
            int idLivro1 = arqLivros.create(livro1);
            arqAutorias.create(new Autoria(idLivro1, idAutor1));
            arqAutorias.create(new Autoria(idLivro1, idAutor2));

            Livro livro2 = new Livro(
                "9788543107165",
                "Scrum: A arte de fazer o dobro do trabalho na metade do tempo",
                idCategoria2,
                "Editora Sextante",
                (short) 1,
                LocalDate.of(2019, 2, 18),
                256,
                4314,
                false
            );
            int idLivro2 = arqLivros.create(livro2);
            arqAutorias.create(new Autoria(idLivro2, idAutor3));
            arqAutorias.create(new Autoria(idLivro2, idAutor4));

            Livro livro3 = new Livro(
                "9788550814360",
                "Design Thinking: uma metodologia poderosa para decretar o fim das velhas ideias",
                idCategoria3,
                "Alta Books",
                (short) 1,
                LocalDate.of(2020, 9, 30),
                304,
                5000,
                false
            );
            int idLivro3 = arqLivros.create(livro3);
            arqAutorias.create(new Autoria(idLivro3, idAutor5));

            Livro livro4 = new Livro(
                "9788576840626",
                "Os 7 Hábitos das Pessoas Altamente Eficazes",
                idCategoria4,
                "Best Seller",
                (short) 60,
                LocalDate.of(2017, 1, 1),
                462,
                4194,
                false
            );
            int idLivro4 = arqLivros.create(livro4);
            arqAutorias.create(new Autoria(idLivro4, idAutor6));

            // Livro livro5 = new Livro(
            //     "9788576849948",
            //     "Como fazer amigos e influenciar pessoas",
            //     idCategoria5,
            //     "Best Seller",
            //     (short) 1,
            //     LocalDate.of(2017, 1, 1),
            //     288,
            //     4194,
            //     false
            // );
            // int idLivro5 = arqLivros.create(livro5);
            // arqAutorias.create(new Autoria(idLivro5, idAutor7));


            // Buscar os livros dos autores cujos nomes começam com J
            System.out.println("Livros dos autores cujos nomes começam com 'J':");
            for (Autor autor : arqAutores.readAllByName("J")) {
                System.out.println("Autor: " + autor.getNome());
                Autoria[] autorias = arqAutorias.readAllByAutor(autor.getId());
                for (Autoria autoria : autorias) {
                    Livro livro = arqLivros.read(autoria.getIdLivro());
                    System.out.println("  - Livro: " + livro.getTitulo());
                }
            }



            arqCategorias.close();
            arqAutores.close();
            arqAutorias.close();
            arqLivros.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro no acesso aos arquivos: " + e.getMessage());
            return;
        }


    }
}