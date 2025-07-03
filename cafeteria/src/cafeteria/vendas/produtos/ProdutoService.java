package cafeteria.vendas.produtos;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cafeteria.DatabaseConnection;
import cafeteria.vendas.relatorios.RelatorioExportavelEmArquivoTexto;

public class ProdutoService implements IProdutoService, RelatorioExportavelEmArquivoTexto {

    @Override
    public Produto pesquisarProdutoPorId(int id) {
        String sql = "SELECT * FROM produto WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Produto produto = new Produto();
                produto.setId(rs.getInt("id"));
                produto.setNome(rs.getString("nome"));

                // Converte o valor numérico de 'medida' para o enum correspondente
                int medidaIndex = rs.getInt("medida");
                if (medidaIndex >= 1 && medidaIndex <= UnidadeMedida.values().length) {
                    produto.setMedida(UnidadeMedida.values()[medidaIndex - 1]);
                } else {
                    produto.setMedida(UnidadeMedida.UNIDADE); // Valor padrão, caso o índice seja inválido
                }

                produto.setPreco(rs.getDouble("preco"));
                produto.setEstoque(rs.getInt("estoque"));
                return produto;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void salvarProduto(Produto produto) {
        String sql = "INSERT INTO produto (nome, medida, preco, estoque) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, produto.getNome());
            stmt.setInt(2, produto.getMedida().ordinal() + 1);
            stmt.setDouble(3, produto.getPreco());
            stmt.setInt(4, produto.getEstoque());
            stmt.executeUpdate();
            System.out.println("Produto salvo no banco de dados.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void atualizarProduto(Produto produto) {
        String sql = "UPDATE produto SET nome = ?, medida = ?, preco = ?, estoque = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, produto.getNome());
            stmt.setInt(2, produto.getMedida().ordinal() + 1);
            stmt.setDouble(3, produto.getPreco());
            stmt.setInt(4, produto.getEstoque());
            stmt.setInt(5, produto.getId());
            stmt.executeUpdate();
            System.out.println("Produto atualizado no banco de dados.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Produto> buscarProdutosComEstoque() {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT * FROM produto WHERE estoque > 0";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Produto produto = new Produto();
                produto.setId(rs.getInt("id"));
                produto.setNome(rs.getString("nome"));
                int medidaIndex = rs.getInt("medida");
                if (medidaIndex >= 1 && medidaIndex <= UnidadeMedida.values().length) {
                    produto.setMedida(UnidadeMedida.values()[medidaIndex - 1]); // Subtrai 1 para alinhar ao índice do enum
                } else {
                    produto.setMedida(UnidadeMedida.UNIDADE); // Define um valor padrão, caso o índice seja inválido
                }
                produto.setPreco(rs.getDouble("preco"));
                produto.setEstoque(rs.getInt("estoque"));
                produtos.add(produto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return produtos;
    }

    @Override
    public void exportar(File destino) {
        String sql = "SELECT * FROM produto ORDER BY nome";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            BufferedWriter writer = new BufferedWriter(new FileWriter(destino))) {

            writer.write("Relatório de Produtos\n");
            writer.write("=====================\n");

            while (rs.next()) {
                writer.write("ID: " + rs.getInt("id") + " | Nome: " + rs.getString("nome") + " | Estoque: " + rs.getInt("estoque") + "\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNomeRelatorio() {
        return "Relatório de Produtos";
    }
}
