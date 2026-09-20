package entidades;
import aed3.*;
import java.util.ArrayList;

public class ArquivoAutor extends Arquivo<Autor> {

    ArvoreBMais<ParNomeId> indiceNome;

    public ArquivoAutor() throws Exception {
        super("autores", Autor.class.getConstructor());
        indiceNome = new ArvoreBMais<>(ParNomeId.class.getConstructor(), 
            4,
            "./dados/autores/indiceNome.db");
    }

    @Override
    public int create(Autor autor) throws Exception {
        int id = super.create(autor);
        ParNomeId pNI = new ParNomeId(autor.getNome(), id);
        indiceNome.create(pNI);
        return id;
    }

    public Autor[] readAllByName(String nome) throws Exception {
        // A busca na árvore B+ precisa ser feita por meio do 
        // hashCode do nome (sempre usa o hashCode de uma chave)
        ArrayList<ParNomeId> lista = indiceNome.read(new ParNomeId(nome, -1));
        ArrayList<Autor> autores = new ArrayList<>();
        if(lista != null && lista.size() > 0) {
            for(ParNomeId p : lista) {
                Autor a = super.read(p.getId());
                if(a != null) {
                    autores.add(a);
                }
            }
            return autores.toArray(new Autor[0]);
        }
        return null;
    }

    @Override
    public boolean update(Autor autorAtualizado) throws Exception {
        int id = autorAtualizado.getId();
        Autor autorAntigo = super.read(id);
        if(autorAntigo != null) {

            // Se o nome mudou, precisamos atualizar o índice
            if(!autorAntigo.getNome().equals(autorAtualizado.getNome())) {
                indiceNome.delete(new ParNomeId(autorAntigo.getNome(), id));
                indiceNome.create(new ParNomeId(autorAtualizado.getNome(), id));
            }
            return super.update(autorAtualizado);
        }
        return false;
    }

  
    @Override
    public boolean delete(int id) throws Exception {
        Autor autor = super.read(id);
        if(autor != null) {
            ParNomeId pNI = new ParNomeId(autor.getNome(), id);
            indiceNome.delete(pNI);
            return super.delete(id);
        }
        return false;
    }
    
}
