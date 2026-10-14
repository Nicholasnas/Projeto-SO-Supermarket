package escalonador;

import modelo.Compra;
import java.util.ArrayDeque;

/** First Come, First Served: quem chegou primeiro é atendido primeiro (fila comum). */
public class Fcfs implements Escalonador {
    private final ArrayDeque<Compra> fila = new ArrayDeque<>();

    public synchronized void adicionar(Compra c) { fila.addLast(c); }
    public synchronized Compra proxima() { return fila.pollFirst(); }
    public String nome() { return "FCFS"; }
}
