import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeleteDataBase {

    public static void deleteData(Connection connection, String nazwa) throws SQLException {
        String query = "DELETE FROM shoppinglist WHERE Nazwa = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, nazwa);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
}