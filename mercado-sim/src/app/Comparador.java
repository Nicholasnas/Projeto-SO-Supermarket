package app;

import mercado.Mercado;
import mercado.Resultado;

/**
 * Dois experimentos, cada um variando UM fator por vez:
 * 1) sincronização (escalonador fixo): afeta a CORRETUDE do estoque e do cupom;
 * 2) escalonador (sincronização fixa e correta): afeta os TEMPOS de espera.
 */
public class Comparador {

    public static void executar(Config base) throws InterruptedException {
        int rep = base.repeticoes;
        System.out.printf("Carga: %d clientes, %d produtos, estoque %d un cada, %d caixas, seed=%d, %d repetições por linha%n",
                base.clientes, base.produtos, base.estoque, base.caixas, base.seed, rep);

        System.out.println();
        System.out.println("=== EXPERIMENTO 1: sincronização (escalonador fixo = FCFS) ===");
        System.out.printf("%-22s %-18s %-22s %-16s %s%n", "Sincronização", "Execuções c/ conflito",
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
            System.out.printf("%-22s %-18s %-22.1f %-16s %.0f compras/s%n", sync, conflitos + " de " + rep,
                    aMais / rep, String.format("%.0f ms", tempo / rep), thr / rep);
        }

        System.out.println();
        System.out.println("=== EXPERIMENTO 2: escalonador (sincronização fixa = POR_PRODUTO) ===");
        System.out.printf("%-14s %-14s %-18s %-22s %-14s %s%n", "Escalonador", "Espera média",
                "Espera NORMAL", "Espera PREFERENCIAL", "Espera máx.", "Turnaround médio");
        for (String esc : new String[]{"FCFS", "QUANTIDADE", "PREFERENCIAL"}) {
            Config c = base.copia();
            c.escalonador = esc;
            c.sync = "POR_PRODUTO";
            double m = 0, n = 0, p = 0, mx = 0, ta = 0;
            for (int i = 0; i < rep; i++) {
                Resultado r = Mercado.executarPadrao(c);
                m += r.esperaMedia; n += r.esperaNormal; p += r.esperaPreferencial;
                mx += r.esperaMaxima; ta += r.turnaroundMedio;
            }
            System.out.printf("%-14s %-14s %-18s %-22s %-14s %s%n", esc, ms(m / rep), ms(n / rep), ms(p / rep),
                    ms(mx / rep), ms(ta / rep));
        }
    }

    private static String ms(double v) { return String.format("%.0f ms", v); }
}
