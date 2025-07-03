package cafeteria;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5438/cafeteria";
    private static final String USER = "postgres";
    private static final String PASSWORD = "sa";

    private DatabaseConnection() {
        // Construtor privado para evitar instanciação
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
