package testes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** Executa todos os métodos @Teste das classes listadas. Código de saída 1 se algum falhar. */
public class Runner {
    private static final Class<?>[] CLASSES = {
            EscalonadorTest.class, ModeloTest.class, FilaProntosTest.class,
            SincronizacaoTest.class, VerificadorTest.class,
            ConflitoE2eTest.class, EscalonamentoE2eTest.class
    };

    public static void main(String[] args) throws Exception {
        int ok = 0, falhas = 0;
        for (Class<?> c : CLASSES) {
            System.out.println(c.getSimpleName());
            for (Method m : c.getDeclaredMethods()) {
                if (!m.isAnnotationPresent(Teste.class)) continue;
                long ini = System.nanoTime();
                try {
                    m.invoke(c.getDeclaredConstructor().newInstance());
                    ok++;
                    System.out.printf("  PASSOU  %s (%d ms)%n", m.getName(), (System.nanoTime() - ini) / 1_000_000);
                } catch (InvocationTargetException e) {
                    falhas++;
                    System.out.printf("  FALHOU  %s -> %s%n", m.getName(), e.getCause());
                }
            }
        }
        System.out.printf("%n%d passaram, %d falharam%n", ok, falhas);
        System.exit(falhas == 0 ? 0 : 1);
    }
}
