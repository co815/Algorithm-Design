import repository.AgencyDbRepository;
import repository.ReservationDbRepository;
import repository.TripDbRepository;
import model.Agency;
import model.Trip;
import model.Reservation;
import utils.JdbcUtils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;

public class Main {

    public static void main(String[] args) throws Exception {
        JdbcUtils dbUtils = JdbcUtils.fromClasspath();

        initSchema(dbUtils);

        AgencyDbRepository agencyRepo = new AgencyDbRepository(dbUtils);
        TripDbRepository tripRepo = new TripDbRepository(dbUtils);
        ReservationDbRepository resRepo = new ReservationDbRepository(dbUtils);

        agencyRepo.save(new Agency(0, "Sunshine Travel", "sunshine", "secret123"));
        agencyRepo.save(new Agency(0, "Ocean Waves", "oceanwaves", "pass456"));

        tripRepo.save(new Trip(0, "Paris", "Air France", "2024-06-10 08:00", 1200.0, 50));
        tripRepo.save(new Trip(0, "Rome", "RyanAir", "2024-06-15 12:00", 800.0, 30));
        tripRepo.save(new Trip(0, "Paris", "EasyJet", "2024-06-20 09:00", 950.0, 40));

        System.out.println("All trips");
        tripRepo.findAll().forEach(System.out::println);

        System.out.println("\nParis trips 10-Jun to 21-Jun");
        tripRepo.findByAttractionAndDepartureInterval("Paris", "2024-06-10 00:00", "2024-06-21 00:00")
                .forEach(System.out::println);

        System.out.println("\nAgency by username 'sunshine'");
        Agency agency = agencyRepo.findByUsername("sunshine");
        System.out.println(agency);

        Trip trip1 = tripRepo.findById(1);
        resRepo.save(new Reservation(0, "Ion Popescu", "0722-111-222", 2, trip1, agency));
        resRepo.save(new Reservation(0, "Maria Ionescu", "0733-333-444", 3, trip1, agency));

        System.out.println("\nAll reservations");
        resRepo.findAll().forEach(System.out::println);

        System.out.println("\nReservations for trip 1");
        resRepo.findByTrip(1).forEach(System.out::println);

        tripRepo.updateAvailableSeats(1, 45);
        System.out.println("\nTrip 1 after seat update");
        System.out.println(tripRepo.findById(1));
    }

    private static void initSchema(JdbcUtils dbUtils) throws Exception {
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (is == null)
                throw new RuntimeException("schema.sql not found on classpath");
            String sql = new String(is.readAllBytes());
            try (Connection conn = dbUtils.getConnection();
                    Statement stmt = conn.createStatement()) {
                for (String statement : sql.split(";")) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                        stmt.execute(trimmed);
                    }
                }
            }
        }
    }
}
