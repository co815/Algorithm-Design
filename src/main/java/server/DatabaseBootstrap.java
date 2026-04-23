package server;

import model.Agency;
import model.Trip;
import repository.AgencyDbRepository;
import repository.TripDbRepository;
import utils.JdbcUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

public final class DatabaseBootstrap {
    private DatabaseBootstrap() {
    }

    public static void initialize(JdbcUtils dbUtils) throws Exception {
        initSchema(dbUtils);
        seed(dbUtils);
    }

    private static void initSchema(JdbcUtils dbUtils) throws Exception {
        try (InputStream is = DatabaseBootstrap.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (is == null) {
                throw new RuntimeException("schema.sql not found on classpath");
            }
            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
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

    private static void seed(JdbcUtils dbUtils) {
        AgencyDbRepository agencyRepo = new AgencyDbRepository(dbUtils);
        TripDbRepository tripRepo = new TripDbRepository(dbUtils);

        agencyRepo.save(new Agency(0, "Sunshine Travel", "sunshine", "secret123"));
        agencyRepo.save(new Agency(0, "Ocean Waves", "oceanwaves", "pass456"));

        tripRepo.save(new Trip(0, "Paris", "Air France", "2024-06-10 08:00", 1200.0, 50));
        tripRepo.save(new Trip(0, "Rome", "RyanAir", "2024-06-15 12:00", 800.0, 30));
        tripRepo.save(new Trip(0, "Paris", "EasyJet", "2024-06-20 09:00", 950.0, 40));
    }
}
