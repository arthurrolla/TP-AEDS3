package entidades;
import aed3.*;
import java.util.ArrayList;

public class ArquivoLivro extends Arquivo<Livro> {

    HashExtensivel<ParIsbnID> indiceIsbn;
    ArvoreBMais<ParIdId> relCategoriaLivro;

    public ArquivoLivro() throws Exception {
        super("livros", Livro.class.getConstructor());
        indiceIsbn = new HashExtensivel<>(ParIsbnID.class.getConstructor(), 
            4,
            "./dados/livros/indiceIsbn.diretorio.db",
            "./dados/livros/indiceIsbn.cestos.db");
        relCategoriaLivro = new ArvoreBMais<>(ParIdId.class.getConstructor(), 
            5,
            "./dados/livros/relCategoriaLivro.db");
    }

    @Override
    public int create(Livro livro) throws Exception {
        int id = super.create(livro);
        ParIsbnID pII = new ParIsbnID(livro.getIsbn(), id);
        indiceIsbn.create(pII);
        relCategoriaLivro.create(new ParIdId(livro.getIdCategoria(), id));
        return id;
    }

    public Livro readByIsbn(String isbn) throws Exception {
        // A busca na tabela hash extensível precisa ser feita por meio do 
        // hashCode do ISBN (sempre usa o hashCode de uma chave)
        ParIsbnID pII = indiceIsbn.read(ParIsbnID.hash(isbn));
        if(pII != null) {
            return super.read(pII.getId());
        }
        return null;
    }

    @Override
    public boolean update(Livro livroAtualizado) throws Exception {
        int id = livroAtualizado.getId();
        Livro livroAntigo = super.read(id);
        if(livroAntigo != null) {

            // Se o ISBN mudou, precisamos atualizar o índice
            if(!livroAntigo.getIsbn().equals(livroAtualizado.getIsbn())) {
                ParIsbnID pII = new ParIsbnID(livroAntigo.getIsbn(), id);
                indiceIsbn.delete(pII.hashCode());
                pII = new ParIsbnID(livroAtualizado.getIsbn(), id);
                indiceIsbn.create(pII);
            }

            // Se a categoria mudou, precisamos atualizar o relacionamento
            if(livroAntigo.getIdCategoria() != livroAtualizado.getIdCategoria()) {
                relCategoriaLivro.delete(new ParIdId(livroAntigo.getIdCategoria(), id));
                relCategoriaLivro.create(new ParIdId(livroAtualizado.getIdCategoria(), id));
            }
            return super.update(livroAtualizado);
        }
        return false;
    }

  
    @Override
    public boolean delete(int id) throws Exception {
        Livro livro = super.read(id);
        if(livro != null) {
            ParIsbnID pII = new ParIsbnID(livro.getIsbn(), id);
            indiceIsbn.delete(pII.hashCode());
            relCategoriaLivro.delete(new ParIdId(livro.getIdCategoria(), id));
            return super.delete(id);
        }
        return false;
    }

    public Livro[] readAllByCategoria(int categoriaId) throws Exception {
        ArrayList<Livro> livros = new ArrayList<>();
        ArrayList<ParIdId> listaPII = relCategoriaLivro.read(new ParIdId(categoriaId, -1));
        for (ParIdId pII : listaPII) {
            Livro livro = super.read(pII.getId2());
            livros.add(livro);
        }
        return livros.toArray(new Livro[0]);
    }
    
}
