package entidades;
import aed3.*;
import java.util.ArrayList;

public class ArquivoUsuario extends Arquivo<Usuario> {

    ArvoreBMais<ParNomeId> indiceEmail;

    public ArquivoUsuario() throws Exception {
        super("usuarios", Usuario.class.getConstructor());
        indiceEmail = new ArvoreBMais<>(ParNomeId.class.getConstructor(), 
            4,
            "./dados/usuarios/indiceEmail.db");
    }

    @Override
    public int create(Usuario usuario) throws Exception {
        int id = super.create(usuario);
        ParNomeId pNI = new ParNomeId(usuario.getNome(), id);
        indiceEmail.create(pNI);
        return id;
    }

    public Usuario[] readAllByEmail(String email) throws Exception {
        // A busca na árvore B+ precisa ser feita por meio do 
        // hashCode do email (sempre usa o hashCode de uma chave)
        ArrayList<ParNomeId> lista = indiceEmail.read(new ParNomeId(email, -1));
        ArrayList<Usuario> usuarios = new ArrayList<>();
        if(lista != null && lista.size() > 0) {
            for(ParNomeId p : lista) {
                Usuario u = super.read(p.getId());
                if(u != null) {
                    usuarios.add(u);
                }
            }
            return usuarios.toArray(new Usuario[0]);
        }
        return null;
    }

    @Override
    public boolean update(Usuario usuarioAtualizado) throws Exception {
        int id = usuarioAtualizado.getId();
        Usuario usuarioAntigo = super.read(id);
        if(usuarioAntigo != null) {

            // Se o email mudou, precisamos atualizar o índice
            if(!usuarioAntigo.getEmail().equals(usuarioAtualizado.getEmail())) {
                indiceEmail.delete(new ParNomeId(usuarioAntigo.getEmail(), id));
                indiceEmail.create(new ParNomeId(usuarioAtualizado.getEmail(), id));
            }
            return super.update(usuarioAtualizado);
        }
        return false;
    }

  
    @Override
    public boolean delete(int id) throws Exception {
        Usuario usuario = super.read(id);
        if(usuario != null) {
            ParNomeId pNI = new ParNomeId(usuario.getEmail(), id);
            indiceEmail.delete(pNI);
            return super.delete(id);
        }
        return false;
    }
    
}
