package entidades;
import aed3.*;
import java.util.ArrayList;

public class ArquivoUsuario extends Arquivo<Usuario> {

    ArvoreBMais<ParEmailId> indiceEmail;

    public ArquivoUsuario() throws Exception {
        super("usuarios", Usuario.class.getConstructor());
        indiceEmail = new ArvoreBMais<>(ParEmailId.class.getConstructor(), 
            4,
            "./dados/usuarios/indiceEmail.db");
    }

    @Override
    public int create(Usuario usuario) throws Exception {
        int id = super.create(usuario);
        ParEmailId pNI = new ParEmailId(usuario.getEmail(), id);
        indiceEmail.create(pNI);
        return id;
    }

    public Usuario[] readAllByEmail(String email) throws Exception {
        // A busca na árvore B+ precisa ser feita por meio do 
        // hashCode do email (sempre usa o hashCode de uma chave)
        ArrayList<ParEmailId> lista = indiceEmail.read(new ParEmailId(email, -1));
        ArrayList<Usuario> usuarios = new ArrayList<>();
        if(lista != null && lista.size() > 0) {
            for(ParEmailId p : lista) {
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
                indiceEmail.delete(new ParEmailId(usuarioAntigo.getEmail(), id));
                indiceEmail.create(new ParEmailId(usuarioAtualizado.getEmail(), id));
            }
            return super.update(usuarioAtualizado);
        }
        return false;
    }

  
    @Override
    public boolean delete(int id) throws Exception {
        Usuario usuario = super.read(id);
        if(usuario != null) {
            // Cria um par de email e ID temporario para excluir do índice
            ParEmailId pNI = new ParEmailId(usuario.getEmail(), id);
            indiceEmail.delete(pNI);
            return super.delete(id);
        }
        return false;
    }
    
}
