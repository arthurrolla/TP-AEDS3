package aed3;
import java.io.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class Arquivo<T extends aed3.InterfaceRegistro> {
    
    RandomAccessFile arquivo; // Arquivo de dados para armazenar registros das entidades
    HashExtensivel<ParIDEndereco> indiceID; // Índice direto baseado no ID da entidade 
    String nomeEntidade; // Nome da entidade associada ao arquivo  
    Constructor<T> construtor; // Construtor da entidade para criar instâncias durante a leitura  
    final int TAMANHO_CABECALHO = 12; // Tamanho do cabeçalho do arquivo (4 bytes para ultimoId + 8 bytes para primeiroLivre)

    /**
     * Construtor que inicializa o arquivo de dados.
     * Cria o diretório 'dados' se não existir e abre o arquivo em modo leitura/escrita.
     * 
     * @param nomeEntidade Nome da entidade para o arquivo de dados
     * @throws IOException Se houver erro ao criar ou acessar o arquivo
     */
    public Arquivo(String nomeEntidade, Constructor<T> construtor) throws Exception {
        File f = new File("./dados");
        if (!f.exists()) {
            f.mkdirs();
        }
        f = new File("./dados/"+nomeEntidade);
        if (!f.exists()) {
            f.mkdirs();
        }
        this.nomeEntidade = nomeEntidade;
        this.construtor = construtor;
        arquivo = new RandomAccessFile("./dados/" + nomeEntidade + "/arquivo.db", "rw");
        indiceID = new HashExtensivel<>(ParIDEndereco.class.getConstructor(), 
            4,
            "./dados/" + nomeEntidade + "/indiceID.diretorio.db",
            "./dados/" + nomeEntidade + "/indiceID.cestos.db");
        if(arquivo.length()<12) {
            arquivo.writeInt(0); // ultimoId: id do último registro criado
            arquivo.writeLong(-1); // primeiroLivre: posição do primeiro slot livre
        }
    }

    /**
     * Cria um novo registro da entidade no arquivo de dados.
     * Incrementa o último ID e escreve a entidade serializada no arquivo.
     * 
     * @param entidade A entidade a ser inserida
     * @return O ID atribuído à nova entidade
     * @throws Exception Se houver erro ao serializar ou escrever no arquivo
     */
    public int create(T entidade) throws Exception {

        // Recupera o último id e incrementa
        arquivo.seek(0);
        int ultimoId = arquivo.readInt();
        int novoId = ultimoId + 1;
        entidade.setId(novoId);
        byte[] registro = entidade.serialize();

        // Atualiza o cabeçalho
        arquivo.seek(0);
        arquivo.writeInt(novoId);

        // Escreve o registro no fim do arquivo (por enquanto...)
        long posicaoLivre = findFreeSlot(registro.length);
        if(posicaoLivre!=-1) {
            arquivo.seek(posicaoLivre);
            arquivo.writeBoolean(false);  // lápide
            arquivo.skipBytes(2);   // length é int; mas writeShort ignora os 2 bytes mais significativos
            arquivo.write(registro);
        } else {
            arquivo.seek(arquivo.length());
            posicaoLivre = arquivo.getFilePointer();
            arquivo.writeBoolean(false);  // lápide
            arquivo.writeShort(registro.length);   // length é int; mas writeShort ignora os 2 bytes mais significativos
            arquivo.write(registro);
        }

        // Insere o par ID e endereço no índice
        ParIDEndereco pIE = new ParIDEndereco(novoId, posicaoLivre);
        indiceID.create(pIE);

        // Retornando o novo id da entidade criada
        return novoId;
    }


    /**
     * Busca uma entidade pelo ID no arquivo de dados.
     * Percorre sequencialmente os registros até encontrar a entidade com o ID especificado.
     * 
     * @param id O ID da entidade a ser procurada
     * @return A entidade encontrada, ou null se não existir
     * @throws Exception Se houver erro ao deserializar ou ler do arquivo
     */
    public T read(int id) throws Exception {

        // Busca o endereço do registro no índice
        // Essa busca usa o hashCode da chave, que, neste caso, é o próprio ID da entidade
        ParIDEndereco pIE = indiceID.read(id);
        if (pIE == null) {
            return null;
        }

        // Lê o registro no arquivo de dados
        long pos = pIE.getEndereco();
        arquivo.seek(pos);
        boolean lapide = arquivo.readBoolean();
        int length = arquivo.readUnsignedShort();
        byte[] registro = new byte[length];
        arquivo.readFully(registro);

        if (!lapide) {
            T entidade = construtor.newInstance(); // Cria uma nova instância da entidade usando o construtor fornecido
            entidade.deserialize(registro);
            if (entidade.getId() == id) {
                return entidade;
            }
        }
        return null; // Retorna null se a entidade não for encontrada
    }
    
    /**
     * Lê todas as entidades do arquivo de dados.
     * 
     * @return Um array contendo todas as entidades encontradas
     * @throws Exception Se houver erro ao deserializar ou ler do arquivo
     */
    public ArrayList<T> readAll() throws Exception {
        arquivo.seek(TAMANHO_CABECALHO); // pula o cabeçalho
        ArrayList<T> entidades = new ArrayList<>();
        while (arquivo.getFilePointer() < arquivo.length()) {
            boolean lapide = arquivo.readBoolean();
            int length = arquivo.readUnsignedShort();
            byte[] registro = new byte[length];
            arquivo.readFully(registro);

            if (!lapide) {
                T entidade = construtor.newInstance(); // Cria uma nova instância da entidade usando o construtor fornecido
                entidade.deserialize(registro);
                entidades.add(entidade);
            }
        }
        return entidades;
    }


    public boolean update(T entidadeAtualizada) throws Exception {

        // Busca o endereço do registro no índice
        int id = entidadeAtualizada.getId();
        ParIDEndereco pIE = indiceID.read(id);
        if (pIE == null) {
            return false;
        }

        // Lê o registro no arquivo de dados
        long pos = pIE.getEndereco();
        arquivo.seek(pos);
        boolean lapide = arquivo.readBoolean();
        int length = arquivo.readUnsignedShort();
        byte[] registro = new byte[length];
        arquivo.readFully(registro);

        if (!lapide) {
            T entidadeExistente = construtor.newInstance();
            entidadeExistente.deserialize(registro);
            if (entidadeExistente.getId() == entidadeAtualizada.getId()) {
                byte[] novoRegistro = entidadeAtualizada.serialize();
                if (novoRegistro.length <= length) {
                    // Atualiza o registro existente
                    arquivo.seek(pos + 3); // Pula lápide e length
                    arquivo.write(novoRegistro);
                    for(int i=novoRegistro.length; i<length; i++) {
                        arquivo.writeByte(0); // Preenche o restante com zeros
                    }
                    return true; // Retorna true se a entidade for encontrada e atualizada
                } else {
                    // Se o novo registro for maior, marca o antigo como excluído e cria um novo registro no final
                    arquivo.seek(pos);
                    arquivo.writeBoolean(true); // Marca a lápide como true (excluído)
                    arquivo.skipBytes(2);       // Pula o campo length
                    for(int i=0; i<length; i++) {
                        arquivo.writeByte(0);   // Preenche o registro com zeros
                    }
                    insertFreeSlot(pos, length); // Insere o slot livre na lista de slots livres

                    // Cria um novo registro no final do arquivo
                    long novaPosicao = findFreeSlot(novoRegistro.length);
                    if(novaPosicao!=-1) {
                        arquivo.seek(novaPosicao);
                        arquivo.writeBoolean(false);  // lápide
                        arquivo.skipBytes(2);   // length é int; mas writeShort ignora os 2 bytes mais significativos
                        arquivo.write(novoRegistro);
                    } else {
                        arquivo.seek(arquivo.length());
                        novaPosicao = arquivo.getFilePointer();
                        arquivo.writeBoolean(false);  // lápide
                        arquivo.writeShort(novoRegistro.length);   // length é int; mas writeShort ignora os 2
                        arquivo.write(novoRegistro);
                    }
                    indiceID.update(new ParIDEndereco(id, novaPosicao)); // Atualiza o índice com a nova posição
                    return true; // Retorna true se a entidade for encontrada e atualizada 
                }
            }
        }
    
        return false; // Retorna false se a entidade não for encontrada 
    }


    /**
     * Exclui uma entidade do arquivo de dados.
     * 
     * @param id O ID da entidade a ser excluída
     * @return true se a entidade for encontrada e excluída, false caso contrário
     * @throws Exception Se houver erro ao ler ou escrever no arquivo
     */
    public boolean delete(int id) throws Exception {

        // Busca o endereço do registro no índice
        ParIDEndereco pIE = indiceID.read(id);
        if (pIE == null) {
            return false;
        }

        // Lê o registro no arquivo de dados
        long pos = pIE.getEndereco();
        arquivo.seek(pos);
        boolean lapide = arquivo.readBoolean();
        int length = arquivo.readUnsignedShort();
        byte[] registro = new byte[length];
        arquivo.readFully(registro);

        if (!lapide) {
            T entidade = construtor.newInstance();
            entidade.deserialize(registro);
            if (entidade.getId() == id) {
                arquivo.seek(pos);
                arquivo.writeBoolean(true); // Marca a lápide como true (excluído)
                arquivo.skipBytes(2);       // Pula o campo length
                for(int i=0; i<length; i++) {
                    arquivo.writeByte(0);   // Preenche o registro com zeros
                }   
                insertFreeSlot(pos, length); // Insere o slot livre na lista de slots livres
                indiceID.delete(id); // Remove o par ID e endereço do índice
                return true; // Retorna true se a entidade for encontrada e marcada como excluída
            }
        }
        return false; // Retorna false se a entidade não for encontrada
    }

    /**
     * Insere um slot livre na lista de slots livres.
     * 
     * @param posicao A posição do slot livre
     * @param tamanho O tamanho do slot livre
     * @throws IOException Se houver erro ao ler ou escrever no arquivo
     */
    public void insertFreeSlot(long posicao, int tamanho) throws IOException {
        arquivo.seek(4); // Pula o último ID
        long primeiroLivre = arquivo.readLong();

        if(primeiroLivre == -1) {
            // Se não houver slots livres, apenas atualiza o cabeçalho para apontar para o novo slot livre
            arquivo.seek(4);
            arquivo.writeLong(posicao);

            arquivo.seek(posicao+3);
            arquivo.writeLong(-1);   // fim da lista de slots livres

        } else {

            // Escreve a posição do próximo slot livre no slot atual
            arquivo.seek(primeiroLivre+1);
            int tamanhoSlot = arquivo.readUnsignedShort();

            // Testa se o tamanho do slot atual é maior que o tamanho do slot a ser inserido
            if (tamanhoSlot > tamanho) {

                // Atualiza a cabeça da lista
                arquivo.seek(4);   // pula o ultimoID no cabeçalho do arquivo
                arquivo.writeLong(posicao);

                // Faz esse slot apontar para o próximo slot livre
                arquivo.seek(posicao + 3); // Pula a lápide e o indicador de tamanho
                arquivo.writeLong(primeiroLivre); // Escreve a posição do próximo slot livre

            } else {

                long posicaoSlotLivre = primeiroLivre;
                arquivo.seek(posicaoSlotLivre + 3); // Pula a lápide e o indicador de tamanho
                long proximoSlotLivre = arquivo.readLong();

                // Percorre a lista de slots livres até encontrar um slot maior
                while(proximoSlotLivre != -1) {

                    // Lê o tamanho do próximo slot livre
                    arquivo.seek(proximoSlotLivre + 1);
                    int tamanhoProximoSlot = arquivo.readUnsignedShort();

                    // Testa se o novo slot deve ser inserido nesta posição da lista
                    if(tamanhoProximoSlot > tamanho) {
                        break;
                    }

                    // Avança para o próximo slot livre
                    posicaoSlotLivre = proximoSlotLivre;
                    arquivo.seek(posicaoSlotLivre + 3); // Pula a lápide e o indicador de tamanho
                    proximoSlotLivre = arquivo.readLong();
                }

                // Atualiza o slot anterior para apontar para o novo slot livre
                arquivo.seek(posicaoSlotLivre + 3); // Pula a lápide e o indicador de tamanho
                arquivo.writeLong(posicao);
                arquivo.seek(posicao + 3); // Pula a lápide e o indicador de tamanho
                arquivo.writeLong(proximoSlotLivre); // Escreve a posição do próximo slot livre
            }
        }
    }
    
    
    /**
     * Encontra um slot livre adequado no arquivo de dados para armazenar um registro de tamanho especificado.
     * Percorre a lista de slots livres e retorna a posição do primeiro slot que seja grande o suficiente para armazenar o registro.
     * Se não houver slots livres adequados, retorna -1.
     * @param tamanhoNecessario
     * @throws IOException
     */
    public long findFreeSlot(int tamanhoNecessario) throws IOException {

        int tolerancia = 50; // Tolerância de bytes desperdiçados para considerar um slot livre adequado

        arquivo.seek(4); // Pula o último ID
        long primeiroLivre = arquivo.readLong();

        if(primeiroLivre == -1) {
            return -1; // Retorna -1 se não houver slots livres
        }

        long posicaoSlotLivre = primeiroLivre;
        arquivo.seek(posicaoSlotLivre + 1); // Pula a lápide
        int tamanhoSlotLivre = arquivo.readUnsignedShort();
        long proximoSlotLivre = arquivo.readLong();

        if(tamanhoSlotLivre >= tamanhoNecessario) {
            if(tamanhoSlotLivre - tamanhoNecessario >= tolerancia) 
                return -1;
            arquivo.seek(4);
            arquivo.writeLong(proximoSlotLivre);
            return posicaoSlotLivre; // Retorna a posição do slot livre encontrado
        }

        long posicaoSlotAnterior;
        while(proximoSlotLivre != -1) {
            posicaoSlotAnterior = posicaoSlotLivre;
            posicaoSlotLivre = proximoSlotLivre;
            arquivo.seek(posicaoSlotLivre + 1); // Pula a lápide
            tamanhoSlotLivre = arquivo.readUnsignedShort();
            proximoSlotLivre = arquivo.readLong();

            if(tamanhoSlotLivre >= tamanhoNecessario) {
                if(tamanhoSlotLivre - tamanhoNecessario >= tolerancia) 
                    return -1;
                arquivo.seek(posicaoSlotAnterior + 3); // Pula a lápide e o indicador de tamanho
                arquivo.writeLong(proximoSlotLivre);
                return posicaoSlotLivre; // Retorna a posição do slot livre encontrado
            }
        }

        return -1; // Retorna -1 se não houver slot livre adequado
    }

    
    /**
     * Reorganiza o arquivo de dados, removendo registros excluídos e compactando os registros válidos.
     * Cria um arquivo temporário, copia os registros válidos e substitui o arquivo original pelo reorganizado.
     * @throws Exception
     */
    public void reorganizar() throws Exception {
        
        // Cria um arquivo temporário para armazenar os registros válidos
        File tempFile = new File("./dados/" + nomeEntidade + "_temp.db");
        RandomAccessFile tempArquivo = new RandomAccessFile(tempFile, "rw");

        // Escreve o cabeçalho no arquivo temporário
        arquivo.seek(0);
        int ultimoID = arquivo.readInt();
        tempArquivo.writeInt(ultimoID); // ultimoId: id do último registro criado
        tempArquivo.writeLong(-1); // primeiroLivre: posição do primeiro slot livre

        // Percorre o arquivo original e copia os registros válidos para o arquivo temporário
        arquivo.seek(TAMANHO_CABECALHO); // pula o cabeçalho
        while (arquivo.getFilePointer() < arquivo.length()) {
            boolean lapide = arquivo.readBoolean();
            int length = arquivo.readUnsignedShort();
            byte[] registro = new byte[length];
            arquivo.readFully(registro);

            if (!lapide) {

                T entidade = construtor.newInstance();
                entidade.deserialize(registro);
                byte[] novoRegistro = entidade.serialize(); // Re-serializa para garantir que o tamanho seja correto

                // Escreve o registro válido no arquivo temporário
                tempArquivo.writeBoolean(false);  // lápide
                tempArquivo.writeShort(novoRegistro.length);   // length é int; mas writeShort ignora os 2 bytes mais significativos
                tempArquivo.write(novoRegistro);
            }
        }

        // Fecha os arquivos
        arquivo.close();
        tempArquivo.close();

        // Substitui o arquivo original pelo arquivo temporário reorganizado
        File originalFile = new File("./dados/" + nomeEntidade + ".db");
        if (!originalFile.delete()) {
            throw new IOException("Não foi possível excluir o arquivo original.");
        }
        if (!tempFile.renameTo(originalFile)) {
            throw new IOException("Não foi possível renomear o arquivo temporário.");
        }

        // Reabre o arquivo reorganizado
        arquivo = new RandomAccessFile(originalFile, "rw");
    }


    /**
     * Fecha o arquivo de dados.
     * Deve ser chamado após finalizar todas as operações com o arquivo.
     * 
     * @throws IOException Se houver erro ao fechar o arquivo
     */
    public void close() throws IOException {
        indiceID.close();
        arquivo.close();
    }


}