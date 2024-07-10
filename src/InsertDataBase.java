import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertDataBase {

    public static void insertData(Connection connection, String nazwa, int ilość, String jednostkaMiary) throws SQLException {
        String query = "INSERT INTO shoppinglist (Nazwa, Ilość, JednostkaMiary) VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, nazwa);
        preparedStatement.setInt(2, ilość);
        preparedStatement.setString(3, jednostkaMiary);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    public static void updateQuantity(Connection connection, String nazwa, int nowaIlość) throws SQLException {
        String query = "UPDATE shoppinglist SET Ilość = ? WHERE Nazwa = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, nowaIlość);
        preparedStatement.setString(2, nazwa);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
}