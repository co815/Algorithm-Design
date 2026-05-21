package repository;

import model.Agency;
import model.Trip;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import service.TurismServerService;

import static org.junit.jupiter.api.Assertions.*;

class TurismServerServiceTest {

    private static SessionFactory sf;
    private TurismServerService service;
    private AgencyHibernateRepository agencyRepo;

    @BeforeAll
    static void setUpSessionFactory() {
        sf = new Configuration()
                .configure()
                .setProperty("hibernate.connection.url", "jdbc:sqlite:target/test-server-service.db")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .buildSessionFactory();
    }

    @AfterAll
    static void tearDownSessionFactory() {
        sf.close();
    }

    @BeforeEach
    void setUp() {
        agencyRepo = new AgencyHibernateRepository(sf);
        service = new TurismServerService(
                agencyRepo,
                new TripHibernateRepository(sf),
                new ReservationHibernateRepository(sf)
        );
    }

    @Test
    void createTripReturnsPersistedTripWithId() {
        Trip created = service.createTrip("Paris", "Air France", "2026-06-01 10:00", 500.0, 50);
        assertTrue(created.getId() > 0);
        assertEquals("Paris", created.getTouristAttraction());
        assertEquals("Air France", created.getTransportCompany());
        assertEquals("2026-06-01 10:00", created.getDepartureTime());
        assertEquals(500.0, created.getPrice());
        assertEquals(50, created.getAvailableSeats());
    }

    @Test
    void createTripThrowsOnBlankAttraction() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createTrip("", "Air France", "2026-06-01 10:00", 500.0, 50));
    }

    @Test
    void createTripThrowsOnNonPositivePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createTrip("Rome", "RyanAir", "2026-06-01 10:00", 0.0, 50));
    }

    @Test
    void updateTripChangesFields() {
        Trip created = service.createTrip("London", "British Airways", "2026-07-01 09:00", 600.0, 40);
        Trip updated = service.updateTrip(
                created.getId(), "Edinburgh", "EasyJet", "2026-07-02 10:00", 700.0, 35);
        assertEquals("Edinburgh", updated.getTouristAttraction());
        assertEquals("EasyJet", updated.getTransportCompany());
        assertEquals(700.0, updated.getPrice());
        assertEquals(35, updated.getAvailableSeats());
    }

    @Test
    void updateTripThrowsOnMissingId() {
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTrip(999999, "X", "Y", "2026-01-01 00:00", 1.0, 1));
    }

    @Test
    void deleteTripRemovesIt() {
        Trip created = service.createTrip("Berlin", "Lufthansa", "2026-08-01 08:00", 450.0, 30);
        service.deleteTrip(created.getId());
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTrip(created.getId(), "X", "Y", "2026-01-01 00:00", 1.0, 1));
    }

    @Test
    void deleteTripThrowsOnMissingId() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteTrip(999999));
    }

    @Test
    void deleteTripCascadesReservations() {
        Agency agency = new Agency(0, "CascadeAgency", "cascade_agency_test", "pass");
        agencyRepo.save(agency);

        Trip trip = service.createTrip("CascadeDestination", "CascadeCo", "2026-11-01 10:00", 100.0, 10);
        service.bookTrip(trip.getId(), agency.getId(), "TestClient", "0700000000", 1);

        assertFalse(service.getReservationsByAgency(agency.getId()).isEmpty());

        service.deleteTrip(trip.getId());

        assertTrue(service.getReservationsByAgency(agency.getId()).isEmpty());
    }

    @Test
    void updateTripThrowsOnBlankAttraction() {
        Trip created = service.createTrip("ValidTrip", "ValidCo", "2026-12-01 10:00", 100.0, 5);
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTrip(created.getId(), "", "ValidCo", "2026-12-01 10:00", 100.0, 5));
    }

    @Test
    void updateTripThrowsOnNonPositivePrice() {
        Trip created = service.createTrip("ValidTrip2", "ValidCo2", "2026-12-02 10:00", 100.0, 5);
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTrip(created.getId(), "ValidTrip2", "ValidCo2", "2026-12-02 10:00", -1.0, 5));
    }
}
