package testes;

import modelo.Compra;
import modelo.ItemCompra;
import modelo.TipoCliente;
import java.util.List;

/** Atalhos para montar objetos nos testes. */
final class Fabrica {
    private Fabrica() { }

    static Compra compra(int id, TipoCliente tipo, int produtoId, int quantidade) {
        return new Compra(id, "Cliente " + id, tipo, List.of(new ItemCompra(produtoId, quantidade)), 2);
    }
}
