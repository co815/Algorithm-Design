package repository;

import model.Agency;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class AgencyHibernateRepositoryTest {

    private static SessionFactory sf;
    private AgencyHibernateRepository repo;

    @BeforeAll
    static void setUpSessionFactory() {
        sf = new Configuration()
                .configure()
                .setProperty("hibernate.connection.url", "jdbc:sqlite:target/test-agency.db")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .buildSessionFactory();
    }

    @AfterAll
    static void tearDownSessionFactory() {
        sf.close();
    }

    @BeforeEach
    void setUp() {
        repo = new AgencyHibernateRepository(sf);
    }

    @Test
    void saveThenFindByUsername() {
        Agency a = new Agency(0, "Sunshine Travel", "sunshine_" + System.nanoTime(), "secret");
        repo.save(a);
        Agency found = repo.findByUsername(a.getUsername());
        assertNotNull(found);
        assertEquals("Sunshine Travel", found.getName());
    }

    @Test
    void findByUsernameReturnsNullWhenMissing() {
        assertNull(repo.findByUsername("nobody_" + System.nanoTime()));
    }

    @Test
    void saveThenFindById() {
        Agency a = new Agency(0, "Ocean Waves", "ocean_" + System.nanoTime(), "pass");
        repo.save(a);
        assertTrue(a.getId() > 0);
        Agency found = repo.findById(a.getId());
        assertNotNull(found);
        assertEquals("Ocean Waves", found.getName());
    }

    @Test
    void findByIdReturnsNullWhenMissing() {
        assertNull(repo.findById(999999));
    }
}
