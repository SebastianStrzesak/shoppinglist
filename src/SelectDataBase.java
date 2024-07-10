import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class SelectDataBase {

    public static ArrayList<String> retrieveData(Connection connection) throws SQLException {
        ArrayList<String> items = new ArrayList<>();
        String query = "SELECT Nazwa, Ilość, JednostkaMiary FROM shoppinglist";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        while (resultSet.next()) {
            String nazwa = resultSet.getString("Nazwa");
            int ilość = resultSet.getInt("Ilość");
            String jednostkaMiary = resultSet.getString("JednostkaMiary");
            items.add(String.format("%s (%d-%s)", nazwa, ilość, jednostkaMiary));
        }

        statement.close();
        resultSet.close();

        return items;
    }
}