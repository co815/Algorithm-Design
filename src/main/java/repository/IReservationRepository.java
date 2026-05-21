package repository;

import model.Reservation;
import java.util.List;

public interface IReservationRepository {
    void save(Reservation reservation);
    Reservation findById(int id);
    List<Reservation> findAll();
    List<Reservation> findByTrip(int tripId);
    List<Reservation> findByAgency(int agencyId);
    void delete(int id);
    void update(Reservation reservation);
    void resequenceIds();
}
