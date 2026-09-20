/*
Esta classe representa um objeto para uma entidade
que será armazenado em uma tabela hash extensivel

Neste caso em particular, este objeto é representado
por uma string e um inteiro para que possa ser usado
como índice indireto de emails para uma entidade qualquer.

Implementado pelo Prof. Marcos Kutova
v1.0 - 2024
*/
package aed3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ParEmailId implements aed3.InterfaceHashExtensivel {

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
    this.email = n; // email do Usuário
    this.id = i; // ID da Usuário
  }

  @Override
  public int hashCode() {
      return hash(this.email);
  }

  public static int hash(String email) {
      return email.hashCode() & 0x7fffffff;
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
    dis.readFully(vb);
    this.email = (new String(vb)).trim();
    this.id = dis.readInt();
  }

}