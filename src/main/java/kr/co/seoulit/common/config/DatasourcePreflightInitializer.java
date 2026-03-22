package kr.co.seoulit.common.config;

import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatasourcePreflightInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final int LOGIN_TIMEOUT_SECONDS = 5;
    private static final int VALIDATION_TIMEOUT_SECONDS = 5;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Environment environment = applicationContext.getEnvironment();
        boolean enabled = environment.getProperty(
                "startup.datasource-preflight.enabled",
                Boolean.class,
                true
        );
        if (!enabled) {
            return;
        }

        String jdbcUrl = trimToNull(environment.getProperty("spring.datasource.url"));
        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:")) {
            return;
        }

        loadDriverIfPresent(trimToNull(environment.getProperty("spring.datasource.driver-class-name")));

        Properties properties = new Properties();
        putIfPresent(properties, "user", trimToNull(environment.getProperty("spring.datasource.username")));
        putIfPresent(properties, "password", trimToNull(environment.getProperty("spring.datasource.password")));

        DriverManager.setLoginTimeout(LOGIN_TIMEOUT_SECONDS);

        try (Connection connection = DriverManager.getConnection(jdbcUrl, properties)) {
            if (!connection.isValid(VALIDATION_TIMEOUT_SECONDS)) {
                throw new SQLException("JDBC connection validation returned false.");
            }
        } catch (SQLException ex) {
            throw new ApplicationContextException(buildFailureMessage(jdbcUrl, ex), ex);
        }
    }

    private static void loadDriverIfPresent(String driverClassName) {
        if (driverClassName == null) {
            return;
        }

        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException ex) {
            throw new ApplicationContextException(
                    "Datasource preflight failed because the JDBC driver could not be loaded: " + driverClassName,
                    ex
            );
        }
    }

    private static void putIfPresent(Properties properties, String key, String value) {
        if (value != null) {
            properties.setProperty(key, value);
        }
    }

    private static String buildFailureMessage(String jdbcUrl, SQLException ex) {
        String details = collectMessages(ex);
        if (details.contains("ORA-01653") && details.contains("SYS.AUD$")) {
            return "Datasource preflight failed before binding port 8283 because Oracle cannot write to SYS.AUD$ "
                    + "in the SYSTEM tablespace. A DBA needs to free, purge, move, or extend the Oracle audit "
                    + "trail storage before the reception backend can start.";
        }

        String firstLine = firstNonBlankLine(details);
        if (firstLine == null) {
            firstLine = ex.getClass().getSimpleName();
        }

        return "Datasource preflight failed for " + jdbcUrl + ": " + firstLine;
    }

    private static String collectMessages(Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        Throwable current = throwable;
        while (current != null) {
            String message = trimToNull(current.getMessage());
            if (message != null) {
                if (builder.length() > 0) {
                    builder.append(System.lineSeparator());
                }
                builder.append(message);
            }
            current = current.getCause();
        }
        return builder.toString();
    }

    private static String firstNonBlankLine(String value) {
        if (value == null) {
            return null;
        }

        for (String line : value.split("\\R")) {
            String trimmed = trimToNull(line);
            if (trimmed != null) {
                return trimmed;
            }
        }
        return null;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
