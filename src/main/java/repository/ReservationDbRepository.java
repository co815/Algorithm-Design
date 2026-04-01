package repository;

import model.Agency;
import model.Reservation;
import model.Trip;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.JdbcUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDbRepository implements IReservationRepository {

    private static final Logger logger = LogManager.getLogger(ReservationDbRepository.class);

    private final JdbcUtils dbUtils;

    public ReservationDbRepository(JdbcUtils dbUtils) {
        this.dbUtils = dbUtils;
    }

    @Override
    public void save(Reservation reservation) {
        logger.traceEntry("save({})", reservation);

        String sql = "INSERT INTO reservations (customer_name, customer_phone, number_of_tickets, trip_id, agency_id) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, reservation.getCustomerName());
            ps.setString(2, reservation.getCustomerPhone());
            ps.setInt(3, reservation.getNumberOfTickets());
            ps.setInt(4, reservation.getTrip().getId());
            ps.setInt(5, reservation.getAgency().getId());

            int rows = ps.executeUpdate();
            logger.info("save: {} row(s) inserted for reservation of customer '{}'",
                    rows, reservation.getCustomerName());

        } catch (SQLException e) {
            logger.error("save failed for reservation '{}'", reservation, e);
            throw new RuntimeException("Failed to save reservation", e);
        }

        logger.traceExit();
    }

    @Override
    public Reservation findById(int id) {
        logger.traceEntry("findById({})", id);

        String sql = buildSelectWithJoins() + " WHERE r.id = ?";

        try (Connection conn = dbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Reservation reservation = extractReservation(rs);
                    logger.info("findById: found {}", reservation);
                    return logger.traceExit(reservation);
                }
            }

        } catch (SQLException e) {
            logger.error("findById failed for id={}", id, e);
            throw new RuntimeException("Failed to find reservation by id", e);
        }

        logger.info("findById: no reservation found with id={}", id);
        return logger.traceExit((Reservation) null);
    }

    @Override
    public List<Reservation> findAll() {
        logger.traceEntry("findAll()");

        String sql = buildSelectWithJoins();
        List<Reservation> reservations = new ArrayList<>();

        try (Connection conn = dbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                reservations.add(extractReservation(rs));
            }
            logger.info("findAll: retrieved {} reservation(s)", reservations.size());

        } catch (SQLException e) {
            logger.error("findAll failed", e);
            throw new RuntimeException("Failed to retrieve all reservations", e);
        }

        return logger.traceExit(reservations);
    }

    @Override
    public List<Reservation> findByTrip(int tripId) {
        logger.traceEntry("findByTrip(tripId={})", tripId);

        String sql = buildSelectWithJoins() + " WHERE r.trip_id = ?";
        List<Reservation> reservations = new ArrayList<>();

        try (Connection conn = dbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tripId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reservations.add(extractReservation(rs));
                }
            }
            logger.info("findByTrip: found {} reservation(s) for tripId={}", reservations.size(), tripId);

        } catch (SQLException e) {
            logger.error("findByTrip failed for tripId={}", tripId, e);
            throw new RuntimeException("Failed to find reservations by trip", e);
        }

        return logger.traceExit(reservations);
    }

    private String buildSelectWithJoins() {
        return  "SELECT " +
                "  r.id            AS r_id, " +
                "  r.customer_name AS r_customer_name, " +
                "  r.customer_phone AS r_customer_phone, " +
                "  r.number_of_tickets AS r_number_of_tickets, " +
                "  t.id            AS t_id, " +
                "  t.tourist_attraction AS t_attraction, " +
                "  t.transport_company  AS t_company, " +
                "  t.departure_time     AS t_departure, " +
                "  t.price              AS t_price, " +
                "  t.available_seats    AS t_seats, " +
                "  a.id            AS a_id, " +
                "  a.name          AS a_name, " +
                "  a.username      AS a_username, " +
                "  a.password      AS a_password " +
                "FROM reservations r " +
                "  INNER JOIN trips t     ON r.trip_id   = t.id " +
                "  INNER JOIN agencies a  ON r.agency_id = a.id";
    }

    private Reservation extractReservation(ResultSet rs) throws SQLException {
        Trip trip = new Trip(
                rs.getInt("t_id"),
                rs.getString("t_attraction"),
                rs.getString("t_company"),
                rs.getString("t_departure"),
                rs.getDouble("t_price"),
                rs.getInt("t_seats")
        );
        Agency agency = new Agency(
                rs.getInt("a_id"),
                rs.getString("a_name"),
                rs.getString("a_username"),
                rs.getString("a_password")
        );
        return new Reservation(
                rs.getInt("r_id"),
                rs.getString("r_customer_name"),
                rs.getString("r_customer_phone"),
                rs.getInt("r_number_of_tickets"),
                trip,
                agency
        );
    }
}
