package sync;

import modelo.Produto;
import java.util.List;

/**
 * Define como a seção crítica de uma compra é protegida.
 * {@code produtos} são os produtos do carrinho, na ordem em que o cliente os colocou.
 */
public interface EstrategiaSync {
    void executar(List<Produto> produtos, Runnable corpo) throws InterruptedException;

    String nome();
}
