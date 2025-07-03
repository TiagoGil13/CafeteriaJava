package cafeteria.vendas;

import cafeteria.vendas.produtos.Produto;

public class ItemVenda {
    private int id;
    private Produto produto;
    private int quantidade;
    private double valorUnitario;
    private double valorTotal;

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
        calcularValorTotal();
    }

    public double getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(double valorUnitario) {
        this.valorUnitario = valorUnitario;
        calcularValorTotal();
    }

    public double getValorTotal() {
        return valorTotal;
    }

    private void calcularValorTotal() {
        this.valorTotal = this.quantidade * this.valorUnitario;
    }
}
