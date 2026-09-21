# Ajuda Aí 1.0

Trabalho prático da disciplina de Algoritmos e Estruturas de Dados III (AEDs III) da PUC Minas. 

Este projeto é um sistema de perguntas e respostas executado no terminal. Todo o armazenamento é feito "do zero" utilizando ficheiros binários em Java, sem o uso de bases de dados prontas (como MySQL).

## Autores

* Arthur Soares
* Gabriel Lucas
* Lucas Santiago
* Matheus Arantes

## O que o sistema faz

* **Utilizadores:** Registo, login seguro (senhas criptografadas com SHA-256) e validação de e-mails usando um índice de Hash Extensível.
* **Perguntas:** Criação, edição e arquivamento das próprias perguntas.
* **Relacionamento:** As perguntas são associadas a cada utilizador através de uma Árvore B+.

## Como compilar e executar

Certifique-se de que tem o Java (JDK) instalado. Abra o terminal na pasta onde está o ficheiro `Principal.java` e digite os seguintes comandos:

1. **Compilar o código:**
   ```bash
   javac Principal.java