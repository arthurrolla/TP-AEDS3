package entidades;
import aed3.*;
import java.util.ArrayList;

public class ArquivoAutoria extends Arquivo<Autoria> {

    ArvoreBMais<ParIdId> relLivroAutoria;
    ArvoreBMais<ParIdId> relAutorAutoria;

    public ArquivoAutoria() throws Exception {
        super("autorias", Autoria.class.getConstructor());
        relLivroAutoria = new ArvoreBMais<>(ParIdId.class.getConstructor(), 
            5,
            "./dados/autorias/relLivroAutoria.db");
        relAutorAutoria = new ArvoreBMais<>(ParIdId.class.getConstructor(), 
            5,
            "./dados/autorias/relAutorAutoria.db");
    }

    @Override
    public int create(Autoria autoria) throws Exception {
        int id = super.create(autoria);
        relLivroAutoria.create(new ParIdId(autoria.getIdLivro(), id));
        relAutorAutoria.create(new ParIdId(autoria.getIdAutor(), id));
        return id;
    }

    public Autoria[] readAllByLivro(int idLivro) throws Exception {
        // Retorna as autorias associadas a um livro específico
        ArrayList<Autoria> autorias = new ArrayList<>();
        ArrayList<ParIdId> listaPII = relLivroAutoria.read(new ParIdId(idLivro, -1));
        for (ParIdId pII : listaPII) {
            Autoria autoria = super.read(pII.getId2());
            autorias.add(autoria);
        }
        return autorias.toArray(new Autoria[0]);
    }

    public Autoria[] readAllByAutor(int idAutor) throws Exception {
        // Retorna as autorias associadas a um autor específico
        ArrayList<Autoria> autorias = new ArrayList<>();
        ArrayList<ParIdId> listaPII = relAutorAutoria.read(new ParIdId(idAutor, -1));
        for (ParIdId pII : listaPII) {
            Autoria autoria = super.read(pII.getId2());
            autorias.add(autoria);
        }
        return autorias.toArray(new Autoria[0]);
    }

    @Override
    public boolean update(Autoria autoriaAtualizada) throws Exception {
        throw new Exception("Não é permitido atualizar uma autoria. Para alterar, exclua e crie uma nova.");
    }

  
    @Override
    public boolean delete(int id) throws Exception {
        Autoria autoria = super.read(id);
        if(autoria != null) {
            relLivroAutoria.delete(new ParIdId(autoria.getIdLivro(), id));
            relAutorAutoria.delete(new ParIdId(autoria.getIdAutor(), id));
            return super.delete(id);
        }
        return false;
    }

}
