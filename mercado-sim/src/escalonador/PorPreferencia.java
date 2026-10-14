package escalonador;

import modelo.Compra;
import modelo.TipoCliente;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Fila de prioridades pelo tipo de cliente: PREFERENCIAL passa na frente de NORMAL.
 * Dentro do mesmo tipo vale a ordem de chegada. Risco: clientes normais podem esperar muito (starvation).
 */
public class PorPreferencia implements Escalonador {
    private final PriorityQueue<Compra> fila = new PriorityQueue<>(
            Comparator.comparingInt((Compra c) -> c.tipo == TipoCliente.PREFERENCIAL ? 0 : 1)
                    .thenComparingInt(c -> c.id));

    public synchronized void adicionar(Compra c) { fila.add(c); }
    public synchronized Compra proxima() { return fila.poll(); }
    public String nome() { return "PREFERENCIAL"; }
}
