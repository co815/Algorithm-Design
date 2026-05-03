package repository;

import model.Agency;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import utils.HibernateUtil;

public class AgencyHibernateRepository implements IAgencyRepository {

    private final SessionFactory sf;

    public AgencyHibernateRepository() {
        this.sf = HibernateUtil.getSessionFactory();
    }

    AgencyHibernateRepository(SessionFactory sf) {
        this.sf = sf;
    }

    @Override
    public void save(Agency agency) {
        try (Session s = sf.openSession()) {
            Transaction tx = s.beginTransaction();
            s.persist(agency);
            tx.commit();
        }
    }

    @Override
    public Agency findById(int id) {
        try (Session s = sf.openSession()) {
            return s.get(Agency.class, id);
        }
    }

    @Override
    public Agency findByUsername(String username) {
        try (Session s = sf.openSession()) {
            return s.createQuery("from Agency where username = :u", Agency.class)
                    .setParameter("u", username)
                    .uniqueResult();
        }
    }
}
