package repository;

import model.Reservation;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import utils.HibernateUtil;

import java.util.List;

public class ReservationHibernateRepository implements IReservationRepository {

    private final SessionFactory sf;

    public ReservationHibernateRepository() {
        this.sf = HibernateUtil.getSessionFactory();
    }

    ReservationHibernateRepository(SessionFactory sf) {
        this.sf = sf;
    }

    @Override
    public void save(Reservation reservation) {
        try (Session s = sf.openSession()) {
            Transaction tx = s.beginTransaction();
            s.persist(reservation);
            tx.commit();
        }
    }

    @Override
    public Reservation findById(int id) {
        try (Session s = sf.openSession()) {
            return s.get(Reservation.class, id);
        }
    }

    @Override
    public List<Reservation> findAll() {
        try (Session s = sf.openSession()) {
            return s.createQuery("from Reservation", Reservation.class).list();
        }
    }

    @Override
    public List<Reservation> findByTrip(int tripId) {
        try (Session s = sf.openSession()) {
            return s.createQuery("from Reservation where trip.id = :tid", Reservation.class)
                    .setParameter("tid", tripId)
                    .list();
        }
    }
}
