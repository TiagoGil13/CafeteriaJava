package cafeteria.vendas;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import cafeteria.DatabaseConnection;
import cafeteria.vendas.produtos.Produto;
import cafeteria.vendas.relatorios.RelatorioExportavelEmArquivoTexto;

import java.sql.Timestamp;

public class VendaService implements IVendaService, RelatorioExportavelEmArquivoTexto {

    @Override
public void registrarVenda(Venda venda) {
    String sqlVenda = "INSERT INTO venda (cliente_id, data_hora, desconto) VALUES (?, ?, ?)";
    String sqlItem = "INSERT INTO item_venda (nome, medida, quantidade, preco, venda_id) VALUES (?, ?, ?, ?, ?)";
    String sqlUpdateEstoque = "UPDATE produto SET estoque = ? WHERE id = ?";

    try (Connection conn = DatabaseConnection.getConnection()) {
        conn.setAutoCommit(false);

        try (PreparedStatement stmtVenda = conn.prepareStatement(sqlVenda, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmtVenda.setInt(1, venda.getClienteId());

            // Define a data e hora atual
            LocalDateTime dataHoraAtual = LocalDateTime.now();
            stmtVenda.setTimestamp(2, Timestamp.valueOf(dataHoraAtual));

            stmtVenda.setDouble(3, venda.getDesconto());
            stmtVenda.executeUpdate();

            ResultSet generatedKeys = stmtVenda.getGeneratedKeys();
            if (generatedKeys.next()) {
                int vendaId = generatedKeys.getInt(1);
                venda.setId(vendaId);

                try (PreparedStatement stmtItem = conn.prepareStatement(sqlItem);
                     PreparedStatement stmtUpdateEstoque = conn.prepareStatement(sqlUpdateEstoque)) {
                    for (ItemVenda item : venda.getItens()) {
                        Produto produto = item.getProduto();
                        
                        // Verifica se o estoque antes de registrar
                        if (produto.getEstoque() < item.getQuantidade()) {
                            throw new SQLException("Qualidade insuficiente em estoque para o produto: " + produto.getNome());
                        }

                        // Atualiza o estoque do produto
                        int novoEstoque = produto.getEstoque() - item.getQuantidade();
                        stmtUpdateEstoque.setInt(1, novoEstoque);
                        stmtUpdateEstoque.setInt(2, produto.getId());
                        stmtUpdateEstoque.addBatch();

                        // Adiciona o item na venda
                        stmtItem.setString(1, produto.getNome());
                        stmtItem.setInt(2, produto.getMedida().ordinal() + 1);
                        stmtItem.setInt(3, item.getQuantidade());
                        stmtItem.setDouble(4, item.getValorUnitario());
                        stmtItem.setInt(5, vendaId);
                        stmtItem.addBatch();
                    }
                    // Executa o batch para atualizar o estoque
                    stmtUpdateEstoque.executeBatch();
                    // Executa o batch para adicionar os itens na venda
                    stmtItem.executeBatch();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback(); // Reverte todas as operações se algo falhar
            throw e;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

    @Override
    public void exportar(File destino) {
        String sql = "SELECT * FROM venda WHERE DATE(data_hora) = CURRENT_DATE ORDER BY data_hora DESC";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            BufferedWriter writer = new BufferedWriter(new FileWriter(destino))) {

            writer.write("Relatório de Vendas\n");
            writer.write("===================\n");

            while (rs.next()) {
                writer.write("ID Venda: " + rs.getInt("id") + " | Cliente ID: " + rs.getInt("cliente_id") + " | Data: " + rs.getTimestamp("data_hora") + " | Desconto: " + rs.getDouble("desconto") + "\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNomeRelatorio() {
        return "Relatório de Vendas";
    }

    public void exportarMelhoresClientes(File destino) {
        String sql = "SELECT cliente.id AS cliente_id, cliente.nome AS cliente_nome, " +
                     "SUM(item_venda.quantidade * item_venda.preco) AS total_comprado " +
                     "FROM cliente " +
                     "JOIN venda ON cliente.id = venda.cliente_id " +
                     "JOIN item_venda ON venda.id = item_venda.venda_id " +
                     "GROUP BY cliente.id, cliente.nome " +
                     "ORDER BY total_comprado DESC " +
                     "LIMIT 10";
    
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery();
             BufferedWriter writer = new BufferedWriter(new FileWriter(destino))) {
    
            writer.write("Relatório de Melhores Clientes\n");
            writer.write("=================================\n");
    
            while (rs.next()) {
                int clienteId = rs.getInt("cliente_id");
                String clienteNome = rs.getString("cliente_nome");
                double totalComprado = rs.getDouble("total_comprado");
    
                writer.write("Cliente ID: " + clienteId +
                             " | Nome: " + clienteNome +
                             " | Total Comprado: R$ " + String.format("%.2f", totalComprado) + "\n");
            }
    
            writer.flush();
            writer.close();
            System.out.println("Relatório de Melhores Clientes exportado com sucesso.");
    
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
