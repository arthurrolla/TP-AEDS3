package aed3;

import java.io.*;

public class ElementoLista {

    private int id;
    private float frequencia;

    public ElementoLista(int id, float frequencia) {
        this.id = id;
        this.frequencia = frequencia;
    }

    public int getId() {
        return id;
    }

    public float getFrequencia() {
        return frequencia;
    }

    public void escrever(DataOutputStream saida) throws IOException {
        saida.writeInt(id);
        saida.writeFloat(frequencia);
    }

    public static ElementoLista ler(DataInputStream entrada) throws IOException {
        return new ElementoLista(entrada.readInt(), entrada.readFloat());
    }

    @Override
    public String toString() {
        return "(" + id + "; " + String.format("%.3f", frequencia) + ")";
    }
}