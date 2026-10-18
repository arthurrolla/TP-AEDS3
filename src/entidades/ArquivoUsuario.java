package entidades;
import aed3.*;

public class ArquivoUsuario extends Arquivo<Usuario> {

    HashExtensivel<ParEmailId> indiceEmail;
    private ArquivoPergunta arquivoPergunta;
    private ArquivoResposta arquivoResposta;
    private ArquivoAvaliacao arquivoAvaliacao;

    // Construtor que inicializa o arquivo de usuários e o índice de emails.
    public ArquivoUsuario() throws Exception {
        super("usuarios", Usuario.class.getConstructor());
        indiceEmail = new HashExtensivel<>(ParEmailId.class.getConstructor(), 
            4,
            "./dados/usuarios/indiceEmail.diretorio.db",
            "./dados/usuarios/indiceEmail.cestos.db");
    }

    // Define o arquivo de perguntas associado ao arquivo de usuários.
    public void setArquivoPergunta(ArquivoPergunta arquivoPergunta) {
        this.arquivoPergunta = arquivoPergunta;
    }
    public void setArquivoResposta(ArquivoResposta arquivoResposta) {
        this.arquivoResposta = arquivoResposta;
    }
    public void setArquivoAvaliacao(ArquivoAvaliacao arquivoAvaliacao) {
        this.arquivoAvaliacao = arquivoAvaliacao;
    }

    // Cria um novo usuário, garantindo que o email seja único.
    @Override
    public int create(Usuario usuario) throws Exception {
        String email = usuario.getEmail();
        if(email == null || email.trim().isEmpty()) {
            throw new Exception("Email não pode ficar vazio.");
        }
        // Valida o tamanho antes de gravar o usuário
        ParEmailId pNI = new ParEmailId(email, -1);
        // Verifica se a chave já está ocupada no índice
        ParEmailId existente = indiceEmail.read(ParEmailId.hash(email));
        if(existente != null) {
            if(existente.getEmail().equals(email)) {
                throw new Exception("Já existe um usuário com esse email.");
            }
            throw new Exception("Não foi possível cadastrar");
        }

        int id = super.create(usuario);
        pNI = new ParEmailId(email, id);
        indiceEmail.create(pNI);
        return id;
    }

    // Lê um usuário pelo email, utilizando o índice de emails.
    public Usuario readByEmail(String email) throws Exception {
        ParEmailId par = indiceEmail.read(ParEmailId.hash(email));
        if(par == null || !par.getEmail().equals(email)) {
            return null;
        }

        Usuario usuario = super.read(par.getId());
        if (usuario != null && usuario.getEmail().equals(email)) {
            return usuario;
        }

        return null;
    }

    // Atualiza um usuário, garantindo que o email seja único.
    @Override
    public boolean update(Usuario usuarioAtualizado) throws Exception {
        int id = usuarioAtualizado.getId();
        Usuario usuarioAntigo = super.read(id);
        if(usuarioAntigo == null) {
            return false;
        }

        String novoEmail = usuarioAtualizado.getEmail();
        if(novoEmail == null || novoEmail.trim().isEmpty()) {
            throw new Exception("Email não pode ficar vazio.");
        }
        // Se o email não mudou, o índice permanece igual
        if(usuarioAntigo.getEmail().equals(novoEmail)) {
            return super.update(usuarioAtualizado);
        }
        // Valida o tamanho antes de alterar os dados
        ParEmailId novoPar = new ParEmailId(novoEmail, id);
        int hashAntigo = ParEmailId.hash(usuarioAntigo.getEmail());
        int hashNovo = ParEmailId.hash(novoEmail);

        ParEmailId existente = indiceEmail.read(hashNovo);
        if(existente != null && existente.getId() != id) {
            if(existente.getEmail().equals(novoEmail)) {
                throw new Exception("Já existe um usuário com esse email.");
            }
            throw new Exception("Não foi possível alterar: colisão no índice de email.");
        }

        if(!super.update(usuarioAtualizado)) {
            return false;
        }
        if(hashAntigo == hashNovo) {
            indiceEmail.update(novoPar);
        }else{
            indiceEmail.delete(hashAntigo);
            indiceEmail.create(novoPar);
        }
        return true;
    }
  
    // Exclui um usuário, removendo também suas perguntas e vínculos.
    @Override
    public boolean delete(int id) throws Exception {
        Usuario usuario = super.read(id);
        if(usuario == null) {
            return false;
        }
        if(arquivoPergunta == null) {
            throw new Exception("Abra o arquivo de perguntas antes de excluir um usuário.");
        }
        if (arquivoResposta == null) {
            throw new Exception("Abra o arquivo de respostas antes de excluir um usuário.");
        }
        if (arquivoAvaliacao == null) {
            throw new Exception("Abra o arquivo de avaliações antes de excluir um usuário.");
        }
        arquivoAvaliacao.excluirPorUsuario(id);
        arquivoResposta.excluirPorUsuario(id);

        // Remove as perguntas e seus vínculos antes de excluir o autor
        arquivoPergunta.excluirPorUsuario(id);
        if(!super.delete(id)) {
            return false;
        }
        boolean removeuEmail = indiceEmail.delete(
            ParEmailId.hash(usuario.getEmail())
        );
        if(!removeuEmail) {
            throw new Exception("Usuário excluído, mas não foi possível remover seu email do índice.");
        }

        return true;
    }
}
