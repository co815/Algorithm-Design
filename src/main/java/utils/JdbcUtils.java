package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcUtils {

    private static final Logger logger = LogManager.getLogger(JdbcUtils.class);

    private final Properties jdbcProps;

    public JdbcUtils(Properties props) {
        this.jdbcProps = props;
    }

    public static JdbcUtils fromClasspath() {
        logger.traceEntry();
        Properties props = new Properties();
        try (InputStream is = JdbcUtils.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is == null) {
                logger.error("db.properties not found on classpath");
                throw new RuntimeException("db.properties not found on classpath");
            }
            props.load(is);
            logger.info("db.properties loaded successfully");
        } catch (IOException e) {
            logger.error("Failed to load db.properties", e);
            throw new RuntimeException("Failed to load db.properties", e);
        }
        return logger.traceExit(new JdbcUtils(props));
    }

    public Connection getConnection() throws SQLException {
        logger.traceEntry();
        String url = jdbcProps.getProperty("jdbc.url");
        logger.info("Opening connection to: {}", url);
        Connection conn = DriverManager.getConnection(url);
        return logger.traceExit(conn);
    }
}
