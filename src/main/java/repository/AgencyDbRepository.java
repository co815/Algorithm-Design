package repository;

import model.Agency;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.JdbcUtils;

import java.sql.*;

public class AgencyDbRepository implements IAgencyRepository {

    private static final Logger logger = LogManager.getLogger(AgencyDbRepository.class);

    private final JdbcUtils dbUtils;

    public AgencyDbRepository(JdbcUtils dbUtils) {
        this.dbUtils = dbUtils;
    }

    @Override
    public void save(Agency agency) {
        logger.traceEntry("save({})", agency);

        String sql = "INSERT INTO agencies (name, username, password) VALUES (?, ?, ?)";

        try (Connection conn = dbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, agency.getName());
            ps.setString(2, agency.getUsername());
            ps.setString(3, agency.getPassword());

            int rows = ps.executeUpdate();
            logger.info("save: {} row(s) inserted for agency '{}'", rows, agency.getName());

        } catch (SQLException e) {
            logger.error("save failed for agency '{}'", agency.getName(), e);
            throw new RuntimeException("Failed to save agency", e);
        }

        logger.traceExit();
    }

    @Override
    public Agency findById(int id) {
        logger.traceEntry("findById({})", id);

        String sql = "SELECT id, name, username, password FROM agencies WHERE id = ?";

        try (Connection conn = dbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Agency agency = extractAgency(rs);
                    logger.info("findById: found {}", agency);
                    return logger.traceExit(agency);
                }
            }

        } catch (SQLException e) {
            logger.error("findById failed for id={}", id, e);
            throw new RuntimeException("Failed to find agency by id", e);
        }

        logger.info("findById: no agency found with id={}", id);
        return logger.traceExit((Agency) null);
    }

    @Override
    public Agency findByUsername(String username) {
        logger.traceEntry("findByUsername({})", username);

        String sql = "SELECT id, name, username, password FROM agencies WHERE username = ?";

        try (Connection conn = dbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Agency agency = extractAgency(rs);
                    logger.info("findByUsername: found {}", agency);
                    return logger.traceExit(agency);
                }
            }

        } catch (SQLException e) {
            logger.error("findByUsername failed for username='{}'", username, e);
            throw new RuntimeException("Failed to find agency by username", e);
        }

        logger.info("findByUsername: no agency found with username='{}'", username);
        return logger.traceExit((Agency) null);
    }

    private Agency extractAgency(ResultSet rs) throws SQLException {
        return new Agency(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("username"),
                rs.getString("password")
        );
    }
}
