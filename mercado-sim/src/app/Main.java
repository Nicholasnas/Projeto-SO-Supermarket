package app;

import mercado.Mercado;
import mercado.Resultado;

public class Main {
    public static void main(String[] args) throws Exception {
        Config c = Config.parse(args);
        switch (c.modo) {
            case "demo" -> Demo.conflitoNoCupom();
            case "comparar" -> Comparador.executar(c);
            case "simular" -> simular(c);
            default -> throw new IllegalArgumentException("Modo desconhecido: " + c.modo
                    + " (use simular, demo ou comparar)");
        }
    }

    private static void simular(Config c) throws InterruptedException {
        System.out.printf("Escalonador=%s  sync=%s  clientes=%d  produtos=%d  estoque=%d  caixas=%d  seed=%d%n",
                Mercado.criarEscalonador(c.escalonador).nome(), Mercado.criarSync(c).nome(),
                c.clientes, c.produtos, c.estoque, c.caixas, c.seed);
        int conflitos = 0;
        for (int i = 1; i <= c.repeticoes; i++) {
            Resultado r = Mercado.executarPadrao(c);
            if (r.verificador.haConflito()) conflitos++;
            System.out.printf("#%d  %s | produtos c/ conflito=%d | vendido a mais=%d un | perdidas/duplicadas=%d | "
                            + "%d ms, %.0f compras/s | espera média=%.0f ms (normal %.0f, preferencial %.0f, máx %.0f)%n",
                    i, "ok", r.verificador.produtosComConflito(),
                    r.verificador.unidadesVendidasAMais(), r.verificador.comprasPerdidasOuDuplicadas,
                    r.tempoMs, r.throughput(), r.esperaMedia, r.esperaNormal, r.esperaPreferencial, r.esperaMaxima);
            if (i == 1) r.verificador.imprimir();
        }
        System.out.printf("Resumo: %d/%d execuções com conflito de estoque%n",
                conflitos, c.repeticoes);
    }
}
