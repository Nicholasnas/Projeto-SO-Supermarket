package mercado;

import escalonador.Escalonador;
import modelo.Compra;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Fila de prontos: problema do produtor-consumidor com buffer limitado, resolvido com semáforos.
 * <ul>
 *   <li>{@code vazio}: conta as vagas livres. O cliente espera aqui quando a fila está cheia.</li>
 *   <li>{@code cheio}: conta as compras prontas. O caixa espera aqui quando a fila está vazia.</li>
 * </ul>
 * Quem decide QUAL compra sai é o escalonador; os semáforos só controlam QUANTAS há.
 */
public class FilaProntos {
    private final Escalonador escalonador;
    private final Semaphore vazio;
    private final Semaphore cheio = new Semaphore(0);

    public FilaProntos(Escalonador escalonador, int capacidade) {
        this.escalonador = escalonador;
        this.vazio = new Semaphore(capacidade);
    }

    /** Produtor (cliente): bloqueia se não houver vaga. */
    public void produzir(Compra c) throws InterruptedException {
        vazio.acquire();                    // P(vazio): pega uma vaga
        c.chegada = System.nanoTime();
        escalonador.adicionar(c);
        cheio.release();                    // V(cheio): avisa que há trabalho
    }

    /** Consumidor (caixa): bloqueia até haver compra pronta; devolve null se estourar o timeout. */
    public Compra consumir(long timeoutMs) throws InterruptedException {
        if (!cheio.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS)) return null; // P(cheio)
        Compra c = escalonador.proxima();
        vazio.release();                    // V(vazio): a vaga volta a ficar livre
        return c;
    }
}
