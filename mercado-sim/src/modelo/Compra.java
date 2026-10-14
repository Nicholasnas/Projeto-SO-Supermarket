package modelo;

import java.util.List;

/** Uma compra de um cliente: é o "processo" que o escalonador coloca na fila. */
public class Compra {
    public final int id;                 // também é a ordem de chegada
    public final String cliente;
    public final TipoCliente tipo;
    public final List<ItemCompra> itens;
    public final int unidades;           // quantidade total de produtos no carrinho
    public final long burst;             // tempo de atendimento no caixa, em ms (proporcional às unidades)

    public long chegada, inicio, fim;    // timestamps em nanossegundos
    public Cupom cupom;                  // preenchido pelo caixa

    public Compra(int id, String cliente, TipoCliente tipo, List<ItemCompra> itens, long msPorUnidade) {
        this.id = id;
        this.cliente = cliente;
        this.tipo = tipo;
        this.itens = itens;
        this.unidades = itens.stream().mapToInt(ItemCompra::quantidade).sum();
        this.burst = unidades * msPorUnidade;
    }
}
