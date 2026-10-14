package app;

import java.util.HashMap;
import java.util.Map;

/** Parâmetros da execução, no formato --chave=valor. */
public class Config {
    public String modo = "simular";          // simular | demo | comparar
    public String escalonador = "FCFS";      // FCFS | QUANTIDADE | PREFERENCIAL
    public String sync = "SEM";              // SEM | GLOBAL | POR_PRODUTO
    public int clientes = 60;
    public int produtos = 6;
    public long estoque = 40;                // unidades de cada produto
    public int caixas = 3;
    public long seed = 42;
    public int repeticoes = 5;
    public long janelaMs = 3;                // pausa entre ler e gravar o estoque (abre a janela da corrida)
    public int buffer = 20;                  // vagas da fila de prontos
    public long intervaloMs = 2;             // tempo entre chegadas de clientes
    public long msPorUnidade = 2;            // tempo de atendimento por unidade no carrinho

    public static Config parse(String[] args) {
        Map<String, String> m = new HashMap<>();
        for (String a : args) {
            if (!a.startsWith("--") || !a.contains("="))
                throw new IllegalArgumentException("Argumento inválido: " + a + " (use --chave=valor)");
            int i = a.indexOf('=');
            m.put(a.substring(2, i), a.substring(i + 1));
        }
        Config c = new Config();
        c.modo = m.getOrDefault("modo", c.modo).toLowerCase();
        c.escalonador = m.getOrDefault("escalonador", c.escalonador).toUpperCase();
        c.sync = m.getOrDefault("sync", c.sync).toUpperCase();
        c.clientes = Integer.parseInt(m.getOrDefault("clientes", "" + c.clientes));
        c.produtos = Integer.parseInt(m.getOrDefault("produtos", "" + c.produtos));
        c.estoque = Long.parseLong(m.getOrDefault("estoque", "" + c.estoque));
        c.caixas = Integer.parseInt(m.getOrDefault("caixas", "" + c.caixas));
        c.seed = Long.parseLong(m.getOrDefault("seed", "" + c.seed));
        c.repeticoes = Integer.parseInt(m.getOrDefault("repeticoes", "" + c.repeticoes));
        c.janelaMs = Long.parseLong(m.getOrDefault("janela", "" + c.janelaMs));
        c.buffer = Integer.parseInt(m.getOrDefault("buffer", "" + c.buffer));
        c.intervaloMs = Long.parseLong(m.getOrDefault("intervalo", "" + c.intervaloMs));
        c.msPorUnidade = Long.parseLong(m.getOrDefault("msporunidade", "" + c.msPorUnidade));
        return c;
    }

    /** Cópia para os experimentos variarem um parâmetro sem alterar o original. */
    public Config copia() {
        Config c = new Config();
        c.modo = modo; c.escalonador = escalonador; c.sync = sync; c.clientes = clientes; c.produtos = produtos;
        c.estoque = estoque; c.caixas = caixas; c.seed = seed; c.repeticoes = repeticoes; c.janelaMs = janelaMs;
        c.buffer = buffer; c.intervaloMs = intervaloMs; c.msPorUnidade = msPorUnidade;
        return c;
    }
}
