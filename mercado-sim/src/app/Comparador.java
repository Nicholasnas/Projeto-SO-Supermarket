package app;

import mercado.Mercado;
import mercado.Resultado;
import terminal.Cor;

/**
 * Experimentos, cada um variando UM fator por vez:
 * 1) sincronização (escalonador fixo): afeta a CORRETUDE do estoque e do cupom;
 * 2) escalonador (sincronização fixa e correta): afeta os TEMPOS de espera;
 * 3) quantidade de caixas (threads) e 4) tamanho da carga (modo "escala").
 * Linhas em verde não tiveram conflito; linhas em amarelo tiveram.
 */
public class Comparador {

    /** Identifica a máquina: serve para comparar resultados de computadores diferentes. */
    private static void cabecalhoMaquina() {
        Runtime rt = Runtime.getRuntime();
        System.out.println(Cor.cinza(String.format("Máquina: %d núcleos lógicos, %d MB de heap máx., Java %s, %s %s",
                rt.availableProcessors(), rt.maxMemory() / (1024 * 1024), System.getProperty("java.version"),
                System.getProperty("os.name"), System.getProperty("os.arch"))));
    }

    private static String ms(double v) { return String.format("%.0f ms", v); }

    private static void cabecalho(String fmt, Object... args) {
        System.out.println(Cor.negrito(String.format(fmt, args)));
    }

    private static void linha(boolean conflito, String fmt, Object... args) {
        System.out.println(Cor.status(conflito, String.format(fmt, args)));
    }

    public static void executar(Config base) throws InterruptedException {
        int rep = base.repeticoes;
        cabecalhoMaquina();
        System.out.println(Cor.cinza(String.format("Carga: %d clientes, %d produtos, estoque %d un cada, %d caixas, seed=%d, %d repetições por linha",
                base.clientes, base.produtos, base.estoque, base.caixas, base.seed, rep)));
        System.out.println(Cor.legenda());

        System.out.println();
        System.out.println(Cor.secao("EXPERIMENTO 1: sincronização (escalonador fixo = FCFS)"));
        cabecalho("%-22s %-24s %-22s %-14s %s", "Sincronização", "Execuções c/ conflito",
                "Un. vendidas a mais", "Tempo total", "Throughput");
        for (String sync : new String[]{"SEM", "GLOBAL", "POR_PRODUTO"}) {
            Config c = base.copia();
            c.escalonador = "FCFS";
            c.sync = sync;
            int conflitos = 0;
            double aMais = 0, tempo = 0, thr = 0;
            for (int i = 0; i < rep; i++) {
                Resultado r = Mercado.executarPadrao(c);
                if (r.verificador.haConflito()) conflitos++;
                aMais += r.verificador.unidadesVendidasAMais();
                tempo += r.tempoMs;
                thr += r.throughput();
            }
            linha(conflitos > 0, "%-22s %-24s %-22.1f %-14s %.0f compras/s", sync, conflitos + " de " + rep,
                    aMais / rep, ms(tempo / rep), thr / rep);
        }

        System.out.println();
        System.out.println(Cor.secao("EXPERIMENTO 2: escalonador (sincronização fixa = POR_PRODUTO)"));
        cabecalho("%-14s %-14s %-16s %-22s %-14s %s", "Escalonador", "Espera média",
                "Espera NORMAL", "Espera PREFERENCIAL", "Espera máx.", "Turnaround médio");
        for (String esc : new String[]{"FCFS", "QUANTIDADE", "PREFERENCIAL"}) {
            Config c = base.copia();
            c.escalonador = esc;
            c.sync = "POR_PRODUTO";
            double m = 0, n = 0, p = 0, mx = 0, ta = 0;
            boolean conflito = false;
            for (int i = 0; i < rep; i++) {
                Resultado r = Mercado.executarPadrao(c);
                if (r.verificador.haConflito()) conflito = true;
                m += r.esperaMedia; n += r.esperaNormal; p += r.esperaPreferencial;
                mx += r.esperaMaxima; ta += r.turnaroundMedio;
            }
            linha(conflito, "%-14s %-14s %-16s %-22s %-14s %s", esc, ms(m / rep), ms(n / rep), ms(p / rep),
                    ms(mx / rep), ms(ta / rep));
        }
    }

    /** Experimentos de escala: quantidade de caixas (threads) e tamanho da carga. */
    public static void escala(Config base) throws InterruptedException {
        int rep = base.repeticoes;
        cabecalhoMaquina();
        System.out.println(Cor.cinza(String.format("Carga base: %d clientes, %d produtos, estoque %d un cada, escalonador FCFS, seed=%d, %d repetições por linha",
                base.clientes, base.produtos, base.estoque, base.seed, rep)));
        System.out.println(Cor.legenda());

        System.out.println();
        System.out.println(Cor.secao("EXPERIMENTO 3: quantidade de caixas (threads)"));
        cabecalho("%-8s %-22s %-22s %-14s %-16s %s", "Caixas", "SEM: c/ conflito", "SEM: vendido a mais",
                "SEM: tempo", "POR_PRODUTO", "POR_PRODUTO: conflitos");
        for (int caixas : new int[]{1, 2, 4, 8, 16}) {
            double[] sem = media(base, "SEM", caixas, base.clientes, rep);
            double[] sync = media(base, "POR_PRODUTO", caixas, base.clientes, rep);
            linha(sem[0] > 0 || sync[0] > 0, "%-8d %-22s %-22.1f %-14s %-16s %.0f de %d", caixas,
                    (int) sem[0] + " de " + rep, sem[1], ms(sem[2]), ms(sync[2]), sync[0], rep);
        }

        System.out.println();
        System.out.println(Cor.secao("EXPERIMENTO 4: tamanho da carga (" + base.caixas + " caixas)"));
        cabecalho("%-10s %-14s %-16s %-14s %s", "Clientes", "SEM: tempo", "GLOBAL: tempo",
                "POR_PRODUTO", "SEM: vendido a mais");
        for (int clientes : new int[]{30, 60, 120, 240}) {
            double[] sem = media(base, "SEM", base.caixas, clientes, rep);
            double[] glob = media(base, "GLOBAL", base.caixas, clientes, rep);
            double[] prod = media(base, "POR_PRODUTO", base.caixas, clientes, rep);
            linha(sem[1] > 0, "%-10d %-14s %-16s %-14s %.1f un", clientes, ms(sem[2]), ms(glob[2]), ms(prod[2]), sem[1]);
        }
    }

    /** Devolve {execuções com conflito, unidades vendidas a mais (média), tempo total (média em ms)}. */
    private static double[] media(Config base, String sync, int caixas, int clientes, int rep) throws InterruptedException {
        Config c = base.copia();
        c.escalonador = "FCFS";
        c.sync = sync;
        c.caixas = caixas;
        c.clientes = clientes;
        double conflitos = 0, aMais = 0, tempo = 0;
        for (int i = 0; i < rep; i++) {
            Resultado r = Mercado.executarPadrao(c);
            if (r.verificador.haConflito()) conflitos++;
            aMais += r.verificador.unidadesVendidasAMais();
            tempo += r.tempoMs;
        }
        return new double[]{conflitos, aMais / rep, tempo / rep};
    }
}
