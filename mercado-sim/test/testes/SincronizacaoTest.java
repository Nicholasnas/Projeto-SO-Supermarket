package testes;

import modelo.Produto;
import sync.EstrategiaSync;
import sync.SemSync;
import sync.SemaforoGlobal;
import sync.SemaforoPorProduto;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/** Testes unitários das estratégias de sincronização. */
public class SincronizacaoTest {

    /** 4 threads fazem ler-pausa-gravar no mesmo contador, 10 vezes cada. */
    private int contarEmParalelo(EstrategiaSync sync, List<Produto> produtos) throws Exception {
        int[] contador = {0};
        Thread[] ts = new Thread[4];
        for (int t = 0; t < ts.length; t++) {
            ts[t] = new Thread(() -> {
                try {
                    for (int i = 0; i < 10; i++) {
                        sync.executar(produtos, () -> {
                            int lido = contador[0];
                            try { Thread.sleep(1); } catch (InterruptedException e) { }
                            contador[0] = lido + 1;
                        });
                    }
                } catch (InterruptedException e) { }
            });
            ts[t].start();
        }
        for (Thread t : ts) t.join();
        return contador[0];
    }

    @Teste
    public void semSincronizacaoPerdeAtualizacoes() throws Exception {
        int total = contarEmParalelo(new SemSync(), List.of(new Produto(0, "A", 100, 1)));
        Afirma.verdadeiro(total < 40, "sem controle deveria perder atualizações (contou " + total + " de 40)");
    }

    @Teste
    public void semaforoGlobalGarantiaExclusaoMutua() throws Exception {
        int total = contarEmParalelo(new SemaforoGlobal(), List.of(new Produto(0, "A", 100, 1)));
        Afirma.igual(40, total, "contagem com semáforo global");
    }

    @Teste
    public void semaforoPorProdutoGarantiaExclusaoMutuaNoMesmoProduto() throws Exception {
        int total = contarEmParalelo(new SemaforoPorProduto(), List.of(new Produto(0, "A", 100, 1)));
        Afirma.igual(40, total, "contagem com semáforo por produto");
    }

    @Teste
    public void semaforoPorProdutoLiberaTudoNoFinal() throws Exception {
        Produto a = new Produto(0, "A", 100, 1);
        Produto b = new Produto(1, "B", 100, 1);
        new SemaforoPorProduto().executar(List.of(b, a), () -> {
            Afirma.igual(0, a.semaforo.availablePermits(), "A travado dentro da seção crítica");
            Afirma.igual(0, b.semaforo.availablePermits(), "B travado dentro da seção crítica");
        });
        Afirma.igual(1, a.semaforo.availablePermits(), "A liberado");
        Afirma.igual(1, b.semaforo.availablePermits(), "B liberado");
    }

    @Teste
    public void semaforoPorProdutoLiberaMesmoQuandoOCorpoFalha() throws Exception {
        Produto a = new Produto(0, "A", 100, 1);
        try {
            new SemaforoPorProduto().executar(List.of(a), () -> { throw new IllegalStateException("falha"); });
        } catch (IllegalStateException esperado) { }
        Afirma.igual(1, a.semaforo.availablePermits(), "semáforo deve ser liberado mesmo com exceção");
    }

    /** Dois caixas entram ao mesmo tempo na seção crítica? A barreira só abre se os dois estiverem lá dentro. */
    private boolean doisEntramJuntos(EstrategiaSync sync, Produto p1, Produto p2) throws Exception {
        CyclicBarrier barreira = new CyclicBarrier(2);
        AtomicInteger passaram = new AtomicInteger();
        Runnable corpo = () -> {
            try {
                barreira.await(400, TimeUnit.MILLISECONDS);
                passaram.incrementAndGet();
            } catch (InterruptedException | BrokenBarrierException | TimeoutException e) { }
        };
        Thread t1 = new Thread(() -> { try { sync.executar(List.of(p1), corpo); } catch (InterruptedException e) { } });
        Thread t2 = new Thread(() -> { try { sync.executar(List.of(p2), corpo); } catch (InterruptedException e) { } });
        t1.start(); t2.start(); t1.join(); t2.join();
        return passaram.get() == 2;
    }

    @Teste
    public void semaforoPorProdutoPermiteProdutosDiferentesEmParalelo() throws Exception {
        boolean juntos = doisEntramJuntos(new SemaforoPorProduto(), new Produto(0, "A", 1, 1), new Produto(1, "B", 1, 1));
        Afirma.verdadeiro(juntos, "produtos diferentes deveriam ser atendidos ao mesmo tempo");
    }

    @Teste
    public void semaforoGlobalSerializaAteProdutosDiferentes() throws Exception {
        boolean juntos = doisEntramJuntos(new SemaforoGlobal(), new Produto(0, "A", 1, 1), new Produto(1, "B", 1, 1));
        Afirma.verdadeiro(!juntos, "com semáforo global, um caixa espera o outro");
    }
}
