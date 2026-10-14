package mercado;

import app.Config;
import escalonador.*;
import modelo.Compra;
import modelo.Produto;
import modelo.TipoCliente;
import monitor.Verificador;
import sync.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Monta e executa uma simulação: clientes (produtor) -> fila de prontos -> caixas (consumidores) -> estoque. */
public class Mercado {

    public static Escalonador criarEscalonador(String nome) {
        return switch (nome) {
            case "FCFS" -> new Fcfs();
            case "QUANTIDADE" -> new PorQuantidade();
            case "PREFERENCIAL" -> new PorPreferencia();
            default -> throw new IllegalArgumentException("Escalonador desconhecido: " + nome);
        };
    }

    public static EstrategiaSync criarSync(Config c) {
        return switch (c.sync) {
            case "SEM" -> new SemSync();
            case "GLOBAL" -> new SemaforoGlobal();
            case "POR_PRODUTO" -> new SemaforoPorProduto();
            default -> throw new IllegalArgumentException("Sincronização desconhecida: " + c.sync);
        };
    }

    public static Resultado executar(Config c, List<Produto> produtos, List<Compra> compras)
            throws InterruptedException {
        FilaProntos fila = new FilaProntos(criarEscalonador(c.escalonador), c.buffer);
        EstrategiaSync sync = criarSync(c);
        AtomicInteger concluidas = new AtomicInteger();
        AtomicInteger[] execucoes = new AtomicInteger[compras.size()];
        for (int i = 0; i < execucoes.length; i++) execucoes[i] = new AtomicInteger();

        // Thread dos clientes: chegam com um intervalo e entram na fila de prontos.
        Thread clientes = new Thread(() -> {
            try {
                for (Compra compra : compras) {
                    fila.produzir(compra);
                    if (c.intervaloMs > 0) Thread.sleep(c.intervaloMs);
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "clientes");
        clientes.setDaemon(true);

        Caixa[] caixas = new Caixa[c.caixas];
        for (int i = 0; i < caixas.length; i++)
            caixas[i] = new Caixa(i, fila, sync, produtos, c.janelaMs, concluidas, execucoes, compras.size());

        long ini = System.nanoTime();
        clientes.start();
        for (Caixa cx : caixas) cx.start();

        // Espera todos os caixas terminarem antes de ler o estoque.
        for (Caixa cx : caixas) cx.join();
        long tempoMs = (System.nanoTime() - ini) / 1_000_000;

        Verificador v = new Verificador(produtos, compras, execucoes);
        return montar(concluidas.get(), tempoMs, v, compras);
    }

    private static Resultado montar(int concluidas, long tempoMs, Verificador v, List<Compra> compras) {
        double soma = 0, somaN = 0, somaP = 0, max = 0, ta = 0;
        int n = 0, nN = 0, nP = 0;
        for (Compra c : compras) {
            if (c.fim == 0) continue;
            double espera = (c.inicio - c.chegada) / 1e6;
            soma += espera; n++;
            ta += (c.fim - c.chegada) / 1e6;
            max = Math.max(max, espera);
            if (c.tipo == TipoCliente.PREFERENCIAL) { somaP += espera; nP++; } else { somaN += espera; nN++; }
        }
        return new Resultado(concluidas, compras.size(), tempoMs, v, compras,
                n == 0 ? 0 : soma / n, nN == 0 ? 0 : somaN / nN, nP == 0 ? 0 : somaP / nP, max, n == 0 ? 0 : ta / n);
    }

    /** Atalho: gera a carga padrão a partir da seed e executa. */
    public static Resultado executarPadrao(Config c) throws InterruptedException {
        List<Produto> produtos = GeradorCompras.criarProdutos(c);
        List<Compra> compras = GeradorCompras.criarCompras(c, produtos.size());
        return executar(c, produtos, compras);
    }
}
