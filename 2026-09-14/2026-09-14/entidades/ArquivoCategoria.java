package entidades;
import aed3.*;
import java.util.ArrayList;

public class ArquivoCategoria extends Arquivo<Categoria> {

    ArvoreBMais<ParNomeId> indiceNome;

    public ArquivoCategoria() throws Exception {
        super("categorias", Categoria.class.getConstructor());
        indiceNome = new ArvoreBMais<>(ParNomeId.class.getConstructor(), 
            4,
            "./dados/categorias/indiceNome.db");
    }

    @Override
    public int create(Categoria categoria) throws Exception {
        int id = super.create(categoria);
        ParNomeId pNI = new ParNomeId(categoria.getNome(), id);
        indiceNome.create(pNI);
        return id;
    }

    public Categoria[] readAllByName(String nome) throws Exception {
        // A busca na árvore B+ precisa ser feita por meio do 
        // hashCode do nome (sempre usa o hashCode de uma chave)
        ArrayList<ParNomeId> lista = indiceNome.read(new ParNomeId(nome, -1));
        ArrayList<Categoria> categorias = new ArrayList<>();
        if(lista != null && lista.size() > 0) {
            for(ParNomeId p : lista) {
                Categoria c = super.read(p.getId());
                if(c != null) {
                    categorias.add(c);
                }
            }
            return categorias.toArray(new Categoria[0]);
        }
        return null;
    }

    @Override
    public boolean update(Categoria categoriaAtualizada) throws Exception {
        int id = categoriaAtualizada.getId();
        Categoria categoriaAntiga = super.read(id);
        if(categoriaAntiga != null) {

            // Se o nome mudou, precisamos atualizar o índice
            if(!categoriaAntiga.getNome().equals(categoriaAtualizada.getNome())) {
                indiceNome.delete(new ParNomeId(categoriaAntiga.getNome(), id));
                indiceNome.create(new ParNomeId(categoriaAtualizada.getNome(), id));
            }
            return super.update(categoriaAtualizada);
        }
        return false;
    }

  
    @Override
    public boolean delete(int id) throws Exception {
        Categoria categoria = super.read(id);
        if(categoria != null) {
            ParNomeId pNI = new ParNomeId(categoria.getNome(), id);
            indiceNome.delete(pNI);
            return super.delete(id);
        }
        return false;
    }
    
}
