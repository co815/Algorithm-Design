package repository;

import model.Trip;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.JdbcUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TripDbRepository implements ITripRepository {

    private static final Logger logger = LogManager.getLogger(TripDbRepository.class);

    private final JdbcUtils dbUtils;

    public TripDbRepository(JdbcUtils dbUtils) {
        this.dbUtils = dbUtils;
    }

    @Override
    public void save(Trip trip) {
        logger.traceEntry("save({})", trip);

        String sql = "INSERT INTO trips (tourist_attraction, transport_company, departure_time, price, available_seats) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, trip.getTouristAttraction());
            ps.setString(2, trip.getTransportCompany());
            ps.setString(3, trip.getDepartureTime());
            ps.setDouble(4, trip.getPrice());
            ps.setInt(5, trip.getAvailableSeats());

            int rows = ps.executeUpdate();
            logger.info("save: {} row(s) inserted for trip to '{}'", rows, trip.getTouristAttraction());

        } catch (SQLException e) {
            logger.error("save failed for trip '{}'", trip, e);
            throw new RuntimeException("Failed to save trip", e);
        }

        logger.traceExit();
    }

    @Override
    public Trip findById(int id) {
        logger.traceEntry("findById({})", id);

        String sql = "SELECT id, tourist_attraction, transport_company, departure_time, price, available_seats " +
                     "FROM trips WHERE id = ?";

        try (Connection conn = dbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Trip trip = extractTrip(rs);
                    logger.info("findById: found {}", trip);
                    return logger.traceExit(trip);
                }
            }

        } catch (SQLException e) {
            logger.error("findById failed for id={}", id, e);
            throw new RuntimeException("Failed to find trip by id", e);
        }

        logger.info("findById: no trip found with id={}", id);
        return logger.traceExit((Trip) null);
    }

    @Override
    public List<Trip> findAll() {
        logger.traceEntry("findAll()");

        String sql = "SELECT id, tourist_attraction, transport_company, departure_time, price, available_seats FROM trips";

        List<Trip> trips = new ArrayList<>();

        try (Connection conn = dbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                trips.add(extractTrip(rs));
            }
            logger.info("findAll: retrieved {} trip(s)", trips.size());

        } catch (SQLException e) {
            logger.error("findAll failed", e);
            throw new RuntimeException("Failed to retrieve all trips", e);
        }

        return logger.traceExit(trips);
    }

    @Override
    public List<Trip> findByAttractionAndDepartureInterval(String attraction, String startTime, String endTime) {
        logger.traceEntry("findByAttractionAndDepartureInterval({}, {}, {})", attraction, startTime, endTime);

        String sql = "SELECT id, tourist_attraction, transport_company, departure_time, price, available_seats " +
                     "FROM trips " +
                     "WHERE tourist_attraction = ? " +
                     "  AND departure_time >= ? " +
                     "  AND departure_time <= ?";

        List<Trip> trips = new ArrayList<>();

        try (Connection conn = dbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, attraction);
            ps.setString(2, startTime);
            ps.setString(3, endTime);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    trips.add(extractTrip(rs));
                }
            }
            logger.info("findByAttractionAndDepartureInterval: found {} trip(s) for attraction='{}'", trips.size(), attraction);

        } catch (SQLException e) {
            logger.error("findByAttractionAndDepartureInterval failed for attraction='{}', start='{}', end='{}'",
                    attraction, startTime, endTime, e);
            throw new RuntimeException("Failed to find trips by attraction and interval", e);
        }

        return logger.traceExit(trips);
    }

    @Override
    public void updateAvailableSeats(int tripId, int newAvailableSeats) {
        logger.traceEntry("updateAvailableSeats(tripId={}, newSeats={})", tripId, newAvailableSeats);

        String sql = "UPDATE trips SET available_seats = ? WHERE id = ?";

        try (Connection conn = dbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, newAvailableSeats);
            ps.setInt(2, tripId);

            int rows = ps.executeUpdate();
            logger.info("updateAvailableSeats: {} row(s) updated for tripId={}", rows, tripId);

        } catch (SQLException e) {
            logger.error("updateAvailableSeats failed for tripId={}", tripId, e);
            throw new RuntimeException("Failed to update available seats", e);
        }

        logger.traceExit();
    }

    private Trip extractTrip(ResultSet rs) throws SQLException {
        return new Trip(
                rs.getInt("id"),
                rs.getString("tourist_attraction"),
                rs.getString("transport_company"),
                rs.getString("departure_time"),
                rs.getDouble("price"),
                rs.getInt("available_seats")
        );
    }
}
