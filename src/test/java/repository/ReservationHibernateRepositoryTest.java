package repository;

import model.Agency;
import model.Reservation;
import model.Trip;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationHibernateRepositoryTest {

    private static SessionFactory sf;
    private static Agency testAgency;
    private static Trip testTrip;

    private ReservationHibernateRepository repo;

    @BeforeAll
    static void setUpSessionFactory() {
        sf = new Configuration()
                .configure()
                .setProperty("hibernate.connection.url", "jdbc:sqlite:target/test-reservation.db")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .buildSessionFactory();

        AgencyHibernateRepository agencyRepo = new AgencyHibernateRepository(sf);
        TripHibernateRepository tripRepo = new TripHibernateRepository(sf);

        testAgency = new Agency(0, "Test Agency", "testagency", "pass");
        agencyRepo.save(testAgency);

        testTrip = new Trip(0, "Paris", "Air France", "2024-06-10 08:00", 1200.0, 50);
        tripRepo.save(testTrip);
    }

    @AfterAll
    static void tearDownSessionFactory() {
        sf.close();
    }

    @BeforeEach
    void setUp() {
        repo = new ReservationHibernateRepository(sf);
    }

    @Test
    void saveSetsGeneratedId() {
        Reservation r = new Reservation(0, "John Doe", "0700000000", 2, testTrip, testAgency);
        repo.save(r);
        assertTrue(r.getId() > 0);
    }

    @Test
    void saveThenFindById() {
        Reservation r = new Reservation(0, "Jane Smith", "0700000001", 1, testTrip, testAgency);
        repo.save(r);
        Reservation found = repo.findById(r.getId());
        assertNotNull(found);
        assertEquals("Jane Smith", found.getCustomerName());
        assertEquals(1, found.getNumberOfTickets());
        assertEquals(testTrip.getId(), found.getTrip().getId());
        assertEquals(testAgency.getId(), found.getAgency().getId());
    }

    @Test
    void findByIdReturnsNullWhenMissing() {
        assertNull(repo.findById(999999));
    }

    @Test
    void saveThenFindAll() {
        int before = repo.findAll().size();
        repo.save(new Reservation(0, "Bob Brown", "0700000002", 3, testTrip, testAgency));
        assertEquals(before + 1, repo.findAll().size());
    }

    @Test
    void saveThenFindByTrip() {
        repo.save(new Reservation(0, "Alice White", "0700000003", 2, testTrip, testAgency));
        List<Reservation> byTrip = repo.findByTrip(testTrip.getId());
        assertFalse(byTrip.isEmpty());
        assertTrue(byTrip.stream().allMatch(r -> r.getTrip().getId() == testTrip.getId()));
    }
}
