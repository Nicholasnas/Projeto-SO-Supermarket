package mercado;

import modelo.Compra;
import monitor.Verificador;
import java.util.List;

/** Resultado de uma execução: corretude (verificador) e métricas de tempo (escalonamento). */
public class Resultado {
    public final int concluidas, total;
    public final long tempoMs;
    public final Verificador verificador;
    public final List<Compra> compras;
    public final double esperaMedia, esperaNormal, esperaPreferencial, esperaMaxima, turnaroundMedio;

    public Resultado(int concluidas, int total, long tempoMs, Verificador v, List<Compra> compras,
                     double esperaMedia, double esperaNormal, double esperaPreferencial,
                     double esperaMaxima, double turnaroundMedio) {
        this.concluidas = concluidas;
        this.total = total;
        this.tempoMs = tempoMs;
        this.verificador = v;
        this.compras = compras;
        this.esperaMedia = esperaMedia;
        this.esperaNormal = esperaNormal;
        this.esperaPreferencial = esperaPreferencial;
        this.esperaMaxima = esperaMaxima;
        this.turnaroundMedio = turnaroundMedio;
    }

    public double throughput() { return tempoMs == 0 ? 0 : concluidas * 1000.0 / tempoMs; }
}
