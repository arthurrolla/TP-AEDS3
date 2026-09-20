/*
Esta classe representa um objeto para uma entidade
que será armazenado em uma árvore B+

Neste caso em particular, este objeto é representado
por uma string e um inteiro para que possa ser usado
como índice indireto de nomes para uma entidade qualquer.

Implementado pelo Prof. Marcos Kutova
v1.0 - 2024
*/
package aed3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.regex.Pattern;

public class ParEmailId implements aed3.InterfaceArvoreBMais<ParEmailId> {

  private String email;
  private int id;
  private short TAMANHO = 50;

  public ParEmailId() throws Exception {
    this("", -1);
  }

  public ParEmailId(String n) throws Exception {
    this(n, -1);
  }

  public ParEmailId(String n, int i) throws Exception {
    if(n.getBytes().length>46)
      throw new Exception("Email extenso demais. Diminua o número de caracteres.");
    this.email = n; // ID do Usuário
    this.id = i; // ID da Pergunta
  }

  @Override
  public ParEmailId clone() {
    try {
      return new ParEmailId(this.email, this.id);
    } catch (Exception e) {
      System.out.println("Erro na clonagem do objeto ParEmailId");
    }
    return null;
  }

  public short size() {
    return this.TAMANHO;
  }


    public String getEmail() {
        return email;
    }

  public int getId() {
    return id;
  }

  public int compareTo(ParEmailId a) {  
    String str1 = transforma(this.email);
    String str2 = transforma(a.email);

    // reduz o tamanho da segunda string (somente para as buscas)
    if(str2.length() > str1.length())
      if(this.id == -1)
        str2 = str2.substring(0, str1.length());
        
    // compara as strings
    if(str1.compareTo(str2)==0)
      if(this.id == -1)
        return 0;
      else
        return this.id - a.id;
    else
      return str1.compareTo(str2);
  }

  public String toString() {
    return this.email + ";" + String.format("%-3d", this.id);
  }

  public byte[] serialize() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    byte[] vb = new byte[46];
    byte[] vbNome = this.email.getBytes();
    int i=0;
    while(i<vbNome.length && i<46) {
      vb[i] = vbNome[i];
      i++;
    }
    while(i<46) {
      vb[i] = ' ';
      i++;
    }
    dos.write(vb);
    dos.writeInt(this.id);
    return baos.toByteArray();
  }

  public void deserialize(byte[] ba) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(ba);
    DataInputStream dis = new DataInputStream(bais);
    byte[] vb = new byte[46];
    dis.read(vb);
    this.email = (new String(vb)).trim();
    this.id = dis.readInt();
  }

  public static String transforma(String str) {
    String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    return pattern.matcher(nfdNormalizedString).replaceAll("").toLowerCase();
  }

}