package repository;

import model.Trip;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import utils.HibernateUtil;

import java.util.List;

public class TripHibernateRepository implements ITripRepository {

    private final SessionFactory sf;

    public TripHibernateRepository() {
        this.sf = HibernateUtil.getSessionFactory();
    }

    TripHibernateRepository(SessionFactory sf) {
        this.sf = sf;
    }

    @Override
    public void save(Trip trip) {
        try (Session s = sf.openSession()) {
            Transaction tx = s.beginTransaction();
            s.persist(trip);
            tx.commit();
        }
    }

    @Override
    public Trip findById(int id) {
        try (Session s = sf.openSession()) {
            return s.get(Trip.class, id);
        }
    }

    @Override
    public List<Trip> findAll() {
        try (Session s = sf.openSession()) {
            return s.createQuery("from Trip", Trip.class).list();
        }
    }

    @Override
    public List<Trip> findByAttractionAndDepartureInterval(String attraction, String startTime, String endTime) {
        try (Session s = sf.openSession()) {
            return s.createQuery(
                    "from Trip where touristAttraction = :a " +
                    "and departureTime >= :start and departureTime <= :end",
                    Trip.class)
                    .setParameter("a", attraction)
                    .setParameter("start", startTime)
                    .setParameter("end", endTime)
                    .list();
        }
    }

    @Override
    public void updateAvailableSeats(int tripId, int newAvailableSeats) {
        try (Session s = sf.openSession()) {
            Transaction tx = s.beginTransaction();
            Trip trip = s.get(Trip.class, tripId);
            if (trip != null) {
                trip.setAvailableSeats(newAvailableSeats);
            }
            tx.commit();
        }
    }

    @Override
    public void update(Trip trip) {
        try (Session s = sf.openSession()) {
            Transaction tx = s.beginTransaction();
            s.merge(trip);
            tx.commit();
        }
    }

    @Override
    public void delete(int id) {
        try (Session s = sf.openSession()) {
            Transaction tx = s.beginTransaction();
            Trip trip = s.get(Trip.class, id);
            if (trip != null) {
                s.remove(trip);
                s.flush();
                Number exists = (Number) s.createNativeQuery(
                    "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='sqlite_sequence'"
                ).getSingleResult();
                if (exists.intValue() > 0) {
                    s.createNativeQuery(
                        "UPDATE sqlite_sequence SET seq = COALESCE((SELECT MAX(id) FROM trips), 0) WHERE name = 'trips'"
                    ).executeUpdate();
                }
            }
            tx.commit();
        }
    }
}
