package escalonador;

import modelo.Compra;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Fila de prioridades pela quantidade de produtos: carrinho menor primeiro
 * (o "caixa rápido"). É o SJF: o menor trabalho passa na frente.
 */
public class PorQuantidade implements Escalonador {
    private final PriorityQueue<Compra> fila = new PriorityQueue<>(
            Comparator.comparingInt((Compra c) -> c.unidades).thenComparingInt(c -> c.id));

    public synchronized void adicionar(Compra c) { fila.add(c); }
    public synchronized Compra proxima() { return fila.poll(); }
    public String nome() { return "QUANTIDADE"; }
}
