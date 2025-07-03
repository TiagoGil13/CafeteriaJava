package cafeteria.vendas.clientes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cafeteria.DatabaseConnection;
import cafeteria.vendas.relatorios.RelatorioExportavelEmArquivoTexto;

public class ClienteService implements IClienteService, RelatorioExportavelEmArquivoTexto  {

    @Override
    public Cliente pesquisarClientePorId(int id) {
        String sql = "SELECT * FROM cliente WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setId(rs.getInt("id"));
                cliente.setNome(rs.getString("nome"));
                cliente.setTelefone(rs.getString("telefone"));
                return cliente;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void salvarCliente(Cliente cliente) {
        String sql = "INSERT INTO cliente (nome, telefone) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getTelefone());
            stmt.executeUpdate();
            System.out.println("Cliente salvo no banco de dados.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void atualizarCliente(Cliente cliente) {
        String sql = "UPDATE cliente SET nome = ?, telefone = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getTelefone());
            stmt.setInt(3, cliente.getId());
            stmt.executeUpdate();
            System.out.println("Cliente atualizado no banco de dados.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exportar(File destino) {
        String sql = "SELECT * FROM cliente ORDER BY nome";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery();
             BufferedWriter writer = new BufferedWriter(new FileWriter(destino))) {

            writer.write("Relatório de Clientes\n");
            writer.write("=====================\n");

            while (rs.next()) {
                writer.write("ID: " + rs.getInt("id") + " | Nome: " + rs.getString("nome") + " | Telefone: " + rs.getString("telefone") + "\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNomeRelatorio() {
        return "Relatório de Clientes";
    }
}
