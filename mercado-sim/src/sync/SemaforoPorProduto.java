package sync;

import modelo.Produto;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Um semáforo binário por produto: compras de produtos diferentes andam em paralelo.
 * Os semáforos são sempre pegos pelo menor id primeiro, para que duas compras nunca fiquem
 * esperando uma pela outra.
 */
public class SemaforoPorProduto implements EstrategiaSync {

    public void executar(List<Produto> produtos, Runnable corpo) throws InterruptedException {
        List<Produto> ordem = new ArrayList<>(produtos);
        ordem.sort(Comparator.comparingInt(p -> p.id));
        int travados = 0;
        try {
            for (Produto p : ordem) {
                p.semaforo.acquire();   // P()
                travados++;
            }
            corpo.run();
        } finally {
            for (int i = travados - 1; i >= 0; i--) ordem.get(i).semaforo.release(); // V()
        }
    }

    public String nome() { return "POR_PRODUTO"; }
}
