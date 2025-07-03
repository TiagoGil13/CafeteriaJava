package cafeteria.vendas.clientes;

public interface IClienteService {
    Cliente pesquisarClientePorId(int id);
    void salvarCliente(Cliente cliente);
    void atualizarCliente(Cliente cliente);
}

