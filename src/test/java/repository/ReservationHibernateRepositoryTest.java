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

    @Test
    void findByAgency_returnsOnlyThatAgency() {
        AgencyHibernateRepository agencyRepo2 = new AgencyHibernateRepository(sf);
        Agency other = new Agency(0, "Other Agency", "otheragency_unique", "pass");
        agencyRepo2.save(other);
        Agency otherSaved = agencyRepo2.findByUsername("otheragency_unique");

        repo.save(new Reservation(0, "ForOther", "0100", 1, testTrip, otherSaved));

        List<Reservation> result = repo.findByAgency(otherSaved.getId());
        assertEquals(1, result.size());
        assertEquals("ForOther", result.get(0).getCustomerName());
    }

    @Test
    void delete_removesReservation() {
        Reservation r = new Reservation(0, "ToDelete", "0200", 1, testTrip, testAgency);
        repo.save(r);
        int id = r.getId();

        repo.delete(id);

        assertNull(repo.findById(id));
    }

    @Test
    void update_changesFields() {
        Reservation r = new Reservation(0, "OldName", "0300", 1, testTrip, testAgency);
        repo.save(r);

        r.setCustomerName("NewName");
        r.setCustomerPhone("9999");
        r.setNumberOfTickets(3);
        repo.update(r);

        Reservation updated = repo.findById(r.getId());
        assertEquals("NewName", updated.getCustomerName());
        assertEquals("9999", updated.getCustomerPhone());
        assertEquals(3, updated.getNumberOfTickets());
    }

    @Test
    void resequenceIds_fillsGapsAndStartsFrom1() {
        Reservation r1 = new Reservation(0, "SeqA", "0401", 1, testTrip, testAgency);
        Reservation r2 = new Reservation(0, "SeqB", "0402", 1, testTrip, testAgency);
        Reservation r3 = new Reservation(0, "SeqC", "0403", 1, testTrip, testAgency);
        repo.save(r1);
        repo.save(r2);
        repo.save(r3);

        repo.delete(r2.getId());

        repo.resequenceIds();

        List<Reservation> all = repo.findAll();
        List<Integer> ids = all.stream().map(Reservation::getId).sorted().toList();
        for (int i = 0; i < ids.size(); i++) {
            assertEquals(i + 1, ids.get(i),
                    "Expected sequential id " + (i + 1) + " but got " + ids.get(i));
        }
    }
}
