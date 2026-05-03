package repository;

import model.Trip;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TripHibernateRepositoryTest {

    private static SessionFactory sf;
    private TripHibernateRepository repo;

    @BeforeAll
    static void setUpSessionFactory() {
        sf = new Configuration()
                .configure()
                .setProperty("hibernate.connection.url", "jdbc:sqlite:target/test-trip.db")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .buildSessionFactory();
    }

    @AfterAll
    static void tearDownSessionFactory() {
        sf.close();
    }

    @BeforeEach
    void setUp() {
        repo = new TripHibernateRepository(sf);
    }

    @Test
    void saveSetsGeneratedId() {
        Trip t = new Trip(0, "Paris", "Air France", "2024-06-10 08:00", 1200.0, 50);
        repo.save(t);
        assertTrue(t.getId() > 0);
    }

    @Test
    void saveThenFindById() {
        Trip t = new Trip(0, "Rome", "RyanAir", "2024-06-15 12:00", 800.0, 30);
        repo.save(t);
        Trip found = repo.findById(t.getId());
        assertNotNull(found);
        assertEquals("Rome", found.getTouristAttraction());
        assertEquals(800.0, found.getPrice());
    }

    @Test
    void findByIdReturnsNullWhenMissing() {
        assertNull(repo.findById(999999));
    }

    @Test
    void saveThenFindAll() {
        int before = repo.findAll().size();
        repo.save(new Trip(0, "Berlin", "Lufthansa", "2024-07-01 10:00", 600.0, 20));
        assertEquals(before + 1, repo.findAll().size());
    }

    @Test
    void findByAttractionAndDepartureInterval() {
        repo.save(new Trip(0, "Venice", "Air France", "2024-08-10 08:00", 1100.0, 45));
        repo.save(new Trip(0, "Venice", "EasyJet", "2024-08-20 09:00", 900.0, 35));
        repo.save(new Trip(0, "Madrid", "Iberia", "2024-08-15 12:00", 700.0, 25));
        List<Trip> venice = repo.findByAttractionAndDepartureInterval(
                "Venice", "2024-08-01 00:00", "2024-08-31 23:59");
        assertEquals(2, venice.size());
        assertTrue(venice.stream().allMatch(t -> t.getTouristAttraction().equals("Venice")));
    }

    @Test
    void updateAvailableSeats() {
        Trip t = new Trip(0, "Lisbon", "TAP Air", "2024-09-01 07:00", 500.0, 20);
        repo.save(t);
        repo.updateAvailableSeats(t.getId(), 15);
        assertEquals(15, repo.findById(t.getId()).getAvailableSeats());
    }
}
