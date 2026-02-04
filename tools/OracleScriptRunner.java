import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class OracleScriptRunner {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: OracleScriptRunner <jdbcUrl> <user> <password> <scriptPath>");
            System.exit(1);
        }

        String jdbcUrl = args[0];
        String user = args[1];
        String password = args[2];
        String scriptPath = args[3];

        Class.forName("oracle.jdbc.OracleDriver");

        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(new FileInputStream(scriptPath), StandardCharsets.UTF_8))) {

            conn.setAutoCommit(false);

            StringBuilder stmt = new StringBuilder();
            boolean inTrigger = false;
            String line;

            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                    continue;
                }

                if (!inTrigger && trimmed.toUpperCase().startsWith("CREATE OR REPLACE TRIGGER")) {
                    inTrigger = true;
                    stmt.append(line).append("\n");
                    continue;
                }

                if (inTrigger) {
                    if (trimmed.equals("/")) {
                        execute(conn, stmt.toString(), true);
                        stmt.setLength(0);
                        inTrigger = false;
                    } else {
                        stmt.append(line).append("\n");
                    }
                    continue;
                }

                stmt.append(line).append("\n");
                if (trimmed.endsWith(";")) {
                    execute(conn, stmt.toString(), false);
                    stmt.setLength(0);
                }
            }

            if (stmt.length() > 0) {
                execute(conn, stmt.toString(), false);
            }

            conn.commit();
        }
    }

    private static void execute(Connection conn, String sql, boolean keepTrailingSemicolon) throws Exception {
        String trimmed = sql.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        if (!keepTrailingSemicolon && trimmed.endsWith(";")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }
        try (Statement statement = conn.createStatement()) {
            statement.execute(trimmed);
        } catch (java.sql.SQLException ex) {
            if (ex.getErrorCode() == 955) {
                System.out.println("SKIP (already exists): " + firstLine(trimmed));
                return;
            }
            throw ex;
        }
    }

    private static String firstLine(String sql) {
        int idx = sql.indexOf('\n');
        if (idx == -1) {
            return sql.length() > 200 ? sql.substring(0, 200) + "..." : sql;
        }
        return sql.substring(0, idx);
    }
}
