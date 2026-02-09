import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class OracleQuery {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: OracleQuery <jdbcUrl> <user> <password> <sql>");
            System.exit(1);
        }
        String jdbcUrl = args[0];
        String user = args[1];
        String password = args[2];
        String sql = args[3];

        Class.forName("oracle.jdbc.OracleDriver");
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int cols = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                StringBuilder line = new StringBuilder();
                for (int i = 1; i <= cols; i++) {
                    if (i > 1) line.append(" | ");
                    line.append(rs.getString(i));
                }
                System.out.println(line);
            }
        }
    }
}
