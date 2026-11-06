package aed3;

import java.io.*;
import java.nio.file.*;
import java.util.*;


public class ListaInvertida {

    private final File arquivo;
    private final TreeMap<String, TreeMap<Integer, Float>> termos = new TreeMap<>();
    private int entidades = 0;

    // Mantém a mesma assinatura da classe do professor (n, dicionário, blocos).
    // Nesta versão simplificada, só o arquivo do dicionário é usado.
    public ListaInvertida(int quantidadeDadosPorBloco, String nomeArquivoDicionario,
                          String nomeArquivoBlocos) throws Exception {
        arquivo = new File(nomeArquivoDicionario);
        if (arquivo.exists() && arquivo.length() > 0) {
            try (DataInputStream in = new DataInputStream(
                    new BufferedInputStream(new FileInputStream(arquivo)))) {
                entidades = in.readInt();
                int qtdTermos = in.readInt();
                for (int i = 0; i < qtdTermos; i++) {
                    String termo = in.readUTF();
                    int qtd = in.readInt();
                    TreeMap<Integer, Float> lista = new TreeMap<>();
                    for (int j = 0; j < qtd; j++) {
                        ElementoLista e = ElementoLista.ler(in);
                        lista.put(e.getId(), e.getFrequencia());
                    }
                    termos.put(termo, lista);
                }
            }
        }
    }

    // Insere o par; devolve false se esse ID já existir na lista do termo
    public boolean create(String termo, ElementoLista e) throws Exception {
        TreeMap<Integer, Float> lista = termos.computeIfAbsent(termo, k -> new TreeMap<>());
        if (lista.containsKey(e.getId())) return false;
        lista.put(e.getId(), e.getFrequencia());
        salvar();
        return true;
    }

    // Lista completa do termo (vazia se o termo não existe)
    public ElementoLista[] read(String termo) {
        TreeMap<Integer, Float> lista = termos.get(termo);
        if (lista == null) return new ElementoLista[0];
        ElementoLista[] r = new ElementoLista[lista.size()];
        int i = 0;
        for (Map.Entry<Integer, Float> en : lista.entrySet())
            r[i++] = new ElementoLista(en.getKey(), en.getValue());
        return r;
    }

    // Par do ID na lista do termo, ou null
    public ElementoLista read(String termo, int id) {
        TreeMap<Integer, Float> lista = termos.get(termo);
        if (lista == null || !lista.containsKey(id)) return null;
        return new ElementoLista(id, lista.get(id));
    }

    // Troca a frequência de um ID já existente na lista do termo
    public boolean update(String termo, ElementoLista e) throws Exception {
        TreeMap<Integer, Float> lista = termos.get(termo);
        if (lista == null || !lista.containsKey(e.getId())) return false;
        lista.put(e.getId(), e.getFrequencia());
        salvar();
        return true;
    }

    // Remove o ID da lista do termo (e o termo, se a lista ficar vazia)
    public boolean delete(String termo, int id) throws Exception {
        TreeMap<Integer, Float> lista = termos.get(termo);
        if (lista == null || lista.remove(id) == null) return false;
        if (lista.isEmpty()) termos.remove(termo);
        salvar();
        return true;
    }

    public void incrementaEntidades() throws Exception {
        entidades++;
        salvar();
    }

    public void decrementaEntidades() throws Exception {
        if (entidades > 0) entidades--;
        salvar();
    }

    public int numeroEntidades() {
        return entidades;
    }

    public void close() throws IOException {
        // Nada a fechar: o arquivo é gravado a cada alteração.
    }

    // Grava em arquivo temporário e troca, para não corromper o índice se o programa cair
    private void salvar() throws IOException {
        File tmp = new File(arquivo.getPath() + ".tmp");
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(tmp)))) {
            out.writeInt(entidades);
            out.writeInt(termos.size());
            for (Map.Entry<String, TreeMap<Integer, Float>> t : termos.entrySet()) {
                out.writeUTF(t.getKey());
                out.writeInt(t.getValue().size());
                for (Map.Entry<Integer, Float> en : t.getValue().entrySet())
                    new ElementoLista(en.getKey(), en.getValue()).escrever(out);
            }
        }
        Files.move(tmp.toPath(), arquivo.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}