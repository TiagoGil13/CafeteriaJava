package cafeteria.vendas.produtos;

import java.util.List;

public interface IProdutoService {
    Produto pesquisarProdutoPorId(int id);
    void salvarProduto(Produto produto);
    void atualizarProduto(Produto produto);
    List<Produto> buscarProdutosComEstoque();
}
