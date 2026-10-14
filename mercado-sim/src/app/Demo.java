package app;

import mercado.Mercado;
import mercado.Resultado;
import modelo.Compra;
import modelo.ItemCompra;
import modelo.Produto;
import modelo.TipoCliente;
import java.util.List;

/** Cenários pequenos e controlados, feitos para serem lidos linha a linha. */
public class Demo {

    /** Dois clientes querem 3 unidades de um produto que só tem 5 em estoque. */
    public static void conflitoNoCupom() throws InterruptedException {
        System.out.println("=== DEMO 1: dois clientes disputam o mesmo produto ===");
        System.out.println("Estoque: Arroz = 5 un. Ana e Bruno compram 3 un cada, ao mesmo tempo, em caixas diferentes.");
        System.out.println("O correto é UM deles levar 3 un e o outro ser avisado de que não há estoque suficiente.");
        for (String sync : new String[]{"SEM", "POR_PRODUTO"}) {
            Config c = new Config();
            c.escalonador = "FCFS";
            c.sync = sync;
            c.caixas = 2;
            c.janelaMs = 100;   // janela grande: garante que os dois leem o estoque antes de qualquer gravação
            c.intervaloMs = 0;
            List<Produto> produtos = List.of(new Produto(0, "Arroz", 2000, 5));
            List<Compra> compras = List.of(
                    new Compra(0, "Ana", TipoCliente.NORMAL, List.of(new ItemCompra(0, 3)), c.msPorUnidade),
                    new Compra(1, "Bruno", TipoCliente.NORMAL, List.of(new ItemCompra(0, 3)), c.msPorUnidade));
            Resultado r = Mercado.executar(c, produtos, compras);
            System.out.println();
            System.out.println("--- Sincronização: " + (sync.equals("SEM") ? "SEM (sem controle)" : "COM semáforo por produto") + " ---");
            for (Compra compra : r.compras) System.out.print(compra.cupom.formatar(compra));
            r.verificador.imprimir();
        }
        System.out.println();
        System.out.println("Conclusão: sem controle, os dois caixas leem 'estoque = 5' antes de qualquer gravação, então");
        System.out.println("os dois cupons vendem 3 un (6 no total) de um produto que só tinha 5.");
    }
}
