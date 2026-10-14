package modelo;

import java.util.concurrent.Semaphore;

/**
 * Produto do mercado, com estoque compartilhado por todas as threads (caixas).
 * O estoque é um campo comum de propósito: sem proteção, as corridas aparecem.
 */
public class Produto {
    public final int id;
    public final String nome;
    public final long preco;          // em centavos
    public final long estoqueInicial;
    public long estoque;
    /** Semáforo binário (valor 1): funciona como um "cadeado" deste produto. */
    public final Semaphore semaforo = new Semaphore(1);

    public Produto(int id, String nome, long preco, long estoque) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.estoqueInicial = estoque;
        this.estoque = estoque;
    }
}
