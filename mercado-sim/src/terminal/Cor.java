package terminal;

import java.io.Console;

/**
 * Cores ANSI para o terminal. Convenção do projeto:
 * verde = sem conflito, amarelo = conflito de estoque, ciano = títulos.
 * Desligadas automaticamente quando a saída não é um terminal (arquivo, pipe) ou com NO_COLOR.
 */
public final class Cor {
    private static final String RESET = "\u001B[0m";
    private static final String VERDE = "\u001B[32m";
    private static final String AMARELO = "\u001B[33m";
    private static final String CIANO = "\u001B[36m";
    private static final String NEGRITO = "\u001B[1m";
    private static final String CINZA = "\u001B[90m";

    private static boolean ativo = detectar();

    private Cor() { }

    /** modo: "auto" (padrão), "sim" (força cor, útil no console do IntelliJ) ou "nao". */
    public static void configurar(String modo) {
        ativo = switch (modo) {
            case "sim" -> true;
            case "nao" -> false;
            default -> detectar();
        };
    }

    public static boolean ativo() { return ativo; }

    private static boolean detectar() {
        if (System.getenv("NO_COLOR") != null) return false;
        if ("dumb".equals(System.getenv("TERM"))) return false;
        Console console = System.console();
        if (console == null) return false;
        try { // JDK 22+: console() pode existir mesmo com saída redirecionada; isTerminal() resolve
            return (boolean) Console.class.getMethod("isTerminal").invoke(console);
        } catch (ReflectiveOperationException e) {
            return true;
        }
    }

    private static String envolver(String codigo, String texto) {
        return ativo ? codigo + texto + RESET : texto;
    }

    public static String verde(String t) { return envolver(VERDE, t); }
    public static String amarelo(String t) { return envolver(AMARELO, t); }
    public static String ciano(String t) { return envolver(CIANO, t); }
    public static String cinza(String t) { return envolver(CINZA, t); }
    public static String negrito(String t) { return envolver(NEGRITO, t); }
    public static String titulo(String t) { return envolver(NEGRITO + CIANO, t); }

    /** Amarelo quando há conflito, verde quando está tudo certo. */
    public static String status(boolean conflito, String t) { return conflito ? amarelo(t) : verde(t); }

    /** Título de seção com linhas, ex.: "== EXPERIMENTO 1 ==". */
    public static String secao(String t) {
        String barra = "=".repeat(Math.max(8, t.length() + 6));
        return titulo(barra + "\n   " + t + "\n" + barra);
    }

    public static String legenda() {
        return cinza("Legenda: ") + verde("verde = sem conflito") + cinza("  |  ") + amarelo("amarelo = conflito de estoque");
    }
}
