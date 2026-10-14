package sync;

import modelo.Produto;
import java.util.List;
import java.util.concurrent.Semaphore;

/** Um único semáforo binário para a loja inteira: correto, mas só uma compra por vez mexe no estoque. */
public class SemaforoGlobal implements EstrategiaSync {
    private final Semaphore semaforo = new Semaphore(1);

    public void executar(List<Produto> produtos, Runnable corpo) throws InterruptedException {
        semaforo.acquire();          // P(): entra na seção crítica
        try { corpo.run(); }
        finally { semaforo.release(); } // V(): sai
    }
    public String nome() { return "GLOBAL"; }
}
