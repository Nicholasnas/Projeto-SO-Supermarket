package app;

import mercado.Mercado;
import mercado.Resultado;
import terminal.Cor;

public class Main {
    public static void main(String[] args) throws Exception {
        Config c = Config.parse(args);
        Cor.configurar(c.cor);
        switch (c.modo) {
            case "demo" -> Demo.conflitoNoCupom();
            case "comparar" -> Comparador.executar(c);
            case "escala" -> Comparador.escala(c);
            case "simular" -> simular(c);
            default -> throw new IllegalArgumentException("Modo desconhecido: " + c.modo
                    + " (use simular, demo, comparar ou escala)");
        }
    }

    private static void simular(Config c) throws InterruptedException {
        System.out.println(Cor.secao("SIMULAÇÃO DO MERCADO"));
        System.out.println(Cor.cinza(String.format("escalonador=%s  sync=%s  clientes=%d  produtos=%d  estoque=%d  caixas=%d  seed=%d",
                Mercado.criarEscalonador(c.escalonador).nome(), Mercado.criarSync(c).nome(),
                c.clientes, c.produtos, c.estoque, c.caixas, c.seed)));
        System.out.println(Cor.legenda());
        System.out.println();
        int conflitos = 0;
        for (int i = 1; i <= c.repeticoes; i++) {
            Resultado r = Mercado.executarPadrao(c);
            boolean conflito = r.verificador.haConflito();
            if (conflito) conflitos++;
            System.out.println(Cor.status(conflito, String.format(
                    "#%d  %s  produtos com conflito: %d | vendido a mais: %d un | perdidas/duplicadas: %d | "
                            + "%d ms, %.0f compras/s | espera média %.0f ms (normal %.0f, preferencial %.0f, máx %.0f)",
                    i, conflito ? "[ CONFLITO ]" : "[    OK    ]", r.verificador.produtosComConflito(),
                    r.verificador.unidadesVendidasAMais(), r.verificador.comprasPerdidasOuDuplicadas,
                    r.tempoMs, r.throughput(), r.esperaMedia, r.esperaNormal, r.esperaPreferencial, r.esperaMaxima)));
            if (i == 1) {
                System.out.println();
                r.verificador.imprimir();
                System.out.println();
            }
        }
        System.out.println();
        System.out.println(Cor.status(conflitos > 0, String.format("Resumo: %d de %d execuções com conflito de estoque", conflitos, c.repeticoes)));
    }
}
