// CRUD de produtos sobre um vetor de bytes que simula o arquivo.
// Formato: [ultimoId:int32][primeiroLivre:int64] + registros [lapide:1][tamanho:uint16][dados:tamanho]
// Números em big-endian. primeiroLivre = -1 indica lista de espaços livres vazia.
// Registro excluído: lápide = 1, dados zerados e, nos 8 primeiros bytes, o ponteiro para o próximo livre.
// Dependências: Produto (serializar / Produto.deserializar) e Armazenamento (carregar / salvar).

class ArquivoProdutos {
    static TAM_CABECALHO = 12;
    static POS_ULTIMO_ID = 0;
    static POS_PRIMEIRO_LIVRE = 4;
    static TAM_REGISTRO_CAB = 3; // lápide (1) + tamanho (2)
    static TAM_PONTEIRO = 8;
    static TAM_MAX = 0xFFFF;
    static TOLERANCIA = 50; // sobra máxima aceita ao reaproveitar um espaço livre

    constructor() {
        this.acessos = [];
        this.bytes = Armazenamento.carregar();
        if (!this.bytes || this.bytes.length < ArquivoProdutos.TAM_CABECALHO) {
            this.bytes = new Uint8Array(ArquivoProdutos.TAM_CABECALHO);
            this.escreverNum(ArquivoProdutos.POS_PRIMEIRO_LIVRE, 8, -1);
            this.salvar();
        }
        this.acessos = [];
    }

    // ---------- Operações públicas ----------
    // Todas retornam { sucesso, ..., acessos }, com acessos = [{ tipo: 'leitura'|'escrita', inicio, tamanho }]

    incluir(produto) {
        this.iniciar();
        const id = this.lerNum(ArquivoProdutos.POS_ULTIMO_ID, 4) + 1;
        produto.id = id;
        const dados = produto.serializar();
        this.validarTamanho(dados);

        const reg = this.gravarRegistro(dados);
        this.escreverNum(ArquivoProdutos.POS_ULTIMO_ID, 4, id);
        this.salvar();
        return { sucesso: true, id, posicao: reg.pos, tamanho: reg.tam, acessos: this.acessos };
    }

    buscar(id) {
        this.iniciar();
        const reg = this.buscarRegistro(id);
        return {
            sucesso: reg !== null,
            produto: reg ? reg.produto : null,
            posicao: reg ? reg.pos : -1,
            tamanho: reg ? reg.tam : 0,
            acessos: this.acessos
        };
    }

    alterar(produto) {
        this.iniciar();
        const antigo = this.buscarRegistro(produto.id);
        if (!antigo) return { sucesso: false, acessos: this.acessos };

        const dados = produto.serializar();
        this.validarTamanho(dados);

        let novo;
        let movido = false;
        if (dados.length <= antigo.tam) {
            // cabe no espaço atual: sobrescreve e zera a sobra
            this.escrever(antigo.pos + ArquivoProdutos.TAM_REGISTRO_CAB, this.preencher(dados, antigo.tam));
            novo = { pos: antigo.pos, tam: antigo.tam };
        } else {
            // não cabe: libera o espaço antigo e grava em outro lugar
            this.liberar(antigo.pos, antigo.tam);
            novo = this.gravarRegistro(dados);
            movido = true;
        }
        this.salvar();
        return {
            sucesso: true,
            movido,
            posicaoAnterior: antigo.pos,
            posicao: novo.pos,
            tamanho: novo.tam,
            acessos: this.acessos
        };
    }

    excluir(id) {
        this.iniciar();
        const reg = this.buscarRegistro(id);
        if (!reg) return { sucesso: false, acessos: this.acessos };

        this.liberar(reg.pos, reg.tam);
        this.salvar();
        return { sucesso: true, posicao: reg.pos, tamanho: reg.tam, acessos: this.acessos };
    }

    listar() {
        this.iniciar();
        const produtos = [];
        for (const reg of this.registros()) {
            if (!reg.lapide) produtos.push(this.lerProduto(reg));
        }
        return { sucesso: true, produtos, acessos: this.acessos };
    }

    // ---------- Percurso e busca ----------

    // Percorre sequencialmente os registros, lendo lápide e tamanho de cada um
    *registros() {
        let pos = ArquivoProdutos.TAM_CABECALHO;
        while (pos < this.bytes.length) {
            const lapide = this.lerNum(pos, 1) === 1;
            const tam = this.lerNum(pos + 1, 2);
            yield { pos, lapide, tam };
            pos += ArquivoProdutos.TAM_REGISTRO_CAB + tam;
        }
    }

    lerProduto(reg) {
        const dados = this.ler(reg.pos + ArquivoProdutos.TAM_REGISTRO_CAB, reg.tam);
        return Produto.deserializar(dados);
    }

    // Retorna { pos, lapide, tam, produto } do registro ativo com o ID, ou null
    buscarRegistro(id) {
        for (const reg of this.registros()) {
            if (reg.lapide) continue;
            const produto = this.lerProduto(reg);
            if (produto.id === id) return { ...reg, produto };
        }
        return null;
    }

    // ---------- Gravação de registros e lista de espaços livres ----------

    // Reaproveita um espaço livre compatível ou grava no fim do arquivo
    gravarRegistro(dados) {
        const livre = this.reservarEspacoLivre(dados.length);
        if (livre) {
            this.escreverNum(livre.pos, 1, 0);
            this.escrever(livre.pos + ArquivoProdutos.TAM_REGISTRO_CAB, this.preencher(dados, livre.tam));
            return livre;
        }

        const pos = this.bytes.length;
        const ampliado = new Uint8Array(pos + ArquivoProdutos.TAM_REGISTRO_CAB + dados.length);
        ampliado.set(this.bytes);
        this.bytes = ampliado;
        this.escreverNum(pos, 1, 0);
        this.escreverNum(pos + 1, 2, dados.length);
        this.escrever(pos + ArquivoProdutos.TAM_REGISTRO_CAB, dados);
        return { pos, tam: dados.length };
    }

    // Procura na lista um espaço com tamanho suficiente e sobra < TOLERANCIA; remove-o da lista
    reservarEspacoLivre(tamNecessario) {
        let anterior = -1;
        let pos = this.lerNum(ArquivoProdutos.POS_PRIMEIRO_LIVRE, 8, true);
        while (pos !== -1) {
            const tam = this.lerNum(pos + 1, 2);
            const proximo = this.lerNum(pos + ArquivoProdutos.TAM_REGISTRO_CAB, 8, true);
            if (tam >= tamNecessario && tam - tamNecessario < ArquivoProdutos.TOLERANCIA) {
                const posPonteiro = anterior === -1
                    ? ArquivoProdutos.POS_PRIMEIRO_LIVRE
                    : anterior + ArquivoProdutos.TAM_REGISTRO_CAB;
                this.escreverNum(posPonteiro, 8, proximo);
                return { pos, tam };
            }
            anterior = pos;
            pos = proximo;
        }
        return null;
    }

    // Marca o registro como excluído, zera os dados e o insere no início da lista de livres
    liberar(pos, tam) {
        this.escreverNum(pos, 1, 1);
        this.escrever(pos + ArquivoProdutos.TAM_REGISTRO_CAB, new Uint8Array(tam));
        if (tam >= ArquivoProdutos.TAM_PONTEIRO) {
            const primeiroLivre = this.lerNum(ArquivoProdutos.POS_PRIMEIRO_LIVRE, 8, true);
            this.escreverNum(pos + ArquivoProdutos.TAM_REGISTRO_CAB, 8, primeiroLivre);
            this.escreverNum(ArquivoProdutos.POS_PRIMEIRO_LIVRE, 8, pos);
        }
    }

    validarTamanho(dados) {
        if (dados.length > ArquivoProdutos.TAM_MAX) {
            throw new Error('Registro excede o tamanho máximo de ' + ArquivoProdutos.TAM_MAX + ' bytes.');
        }
    }

    // Copia os dados para um vetor de tamanho fixo, completando com zeros
    preencher(dados, tam) {
        const saida = new Uint8Array(tam);
        saida.set(dados);
        return saida;
    }

    // ---------- Acesso aos bytes com registro dos acessos ----------

    iniciar() {
        this.acessos = [];
    }

    // Agrupa acessos contíguos do mesmo tipo
    registrar(tipo, inicio, tamanho) {
        const ultimo = this.acessos[this.acessos.length - 1];
        if (ultimo && ultimo.tipo === tipo && ultimo.inicio + ultimo.tamanho === inicio) {
            ultimo.tamanho += tamanho;
        } else {
            this.acessos.push({ tipo, inicio, tamanho });
        }
    }

    ler(pos, tam) {
        this.registrar('leitura', pos, tam);
        return this.bytes.slice(pos, pos + tam);
    }

    escrever(pos, dados) {
        this.bytes.set(dados, pos);
        this.registrar('escrita', pos, dados.length);
    }

    lerNum(pos, tam, comSinal = false) {
        this.registrar('leitura', pos, tam);
        let valor = 0n;
        for (let i = 0; i < tam; i++) valor = (valor << 8n) | BigInt(this.bytes[pos + i]);
        return Number(comSinal ? BigInt.asIntN(tam * 8, valor) : valor);
    }

    escreverNum(pos, tam, numero) {
        const valor = BigInt.asUintN(tam * 8, BigInt(numero));
        const dados = new Uint8Array(tam);
        for (let i = 0; i < tam; i++) {
            dados[i] = Number((valor >> BigInt(8 * (tam - 1 - i))) & 0xFFn);
        }
        this.escrever(pos, dados);
    }

    salvar() {
        Armazenamento.salvar(this.bytes);
    }
}