package mercado;

import modelo.Compra;
import modelo.Cupom;
import modelo.ItemCompra;
import modelo.Produto;
import sync.EstrategiaSync;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Thread caixa: pega a próxima compra da fila, "passa os itens" e baixa o estoque. */
public class Caixa extends Thread {
    private final FilaProntos fila;
    private final EstrategiaSync sync;
    private final List<Produto> produtos;
    private final long janelaMs;
    private final AtomicInteger concluidas;
    private final AtomicInteger[] execucoes;
    private final int total;
    private final boolean servicoCpu;

    public Caixa(int n, FilaProntos fila, EstrategiaSync sync, List<Produto> produtos, long janelaMs,
                 AtomicInteger concluidas, AtomicInteger[] execucoes, int total, boolean servicoCpu) {
        super("caixa-" + n);
        setDaemon(true);
        this.fila = fila;
        this.sync = sync;
        this.produtos = produtos;
        this.janelaMs = janelaMs;
        this.concluidas = concluidas;
        this.execucoes = execucoes;
        this.total = total;
        this.servicoCpu = servicoCpu;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Compra c = fila.consumir(50);
                if (c == null) {
                    if (concluidas.get() >= total) return; // nada na fila e tudo concluído: encerra
                    continue;
                }
                atender(c);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void atender(Compra c) throws InterruptedException {
        c.inicio = System.nanoTime();
        atendimento(c.burst); // tempo de passar os itens no caixa (fora da seção crítica)

        List<Produto> doCarrinho = new ArrayList<>();
        for (ItemCompra i : c.itens) doCarrinho.add(produtos.get(i.produtoId()));

        Cupom cupom = new Cupom();
        sync.executar(doCarrinho, () -> {      // ===== seção crítica =====
            for (ItemCompra i : c.itens) {
                Produto p = produtos.get(i.produtoId());
                long visto = p.estoque;         // 1) lê o estoque
                if (visto >= i.quantidade()) {
                    pausa();                    // 2) janela da corrida: outro caixa pode ler o mesmo valor
                    p.estoque = visto - i.quantidade(); // 3) grava o valor antigo menos a quantidade
                    cupom.adicionar(p.nome, i.quantidade(), i.quantidade(), p.preco);
                } else {
                    cupom.adicionar(p.nome, i.quantidade(), 0, p.preco);
                }
            }
        });                                     // ===== fim da seção crítica =====

        c.cupom = cupom;
        c.fim = System.nanoTime();
        execucoes[c.id].incrementAndGet();
        concluidas.incrementAndGet();
    }

    /**
     * Simula o atendimento. Em "sleep" a thread dorme (não usa CPU, então o número de núcleos quase não importa).
     * Em "cpu" ela gasta tempo de CPU de verdade: com menos núcleos que caixas, as threads disputam a CPU e o
     * tempo total aumenta, como em um computador menos potente.
     */
    private void atendimento(long ms) throws InterruptedException {
        if (!servicoCpu) { Thread.sleep(ms); return; }
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        long alvo = mx.getCurrentThreadCpuTime() + ms * 1_000_000L;
        while (mx.getCurrentThreadCpuTime() < alvo) {
            if (Thread.interrupted()) throw new InterruptedException();
        }
    }

    private void pausa() {
        try { Thread.sleep(janelaMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
