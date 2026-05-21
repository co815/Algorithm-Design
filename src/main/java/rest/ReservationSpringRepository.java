package rest;

import model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationSpringRepository extends JpaRepository<Reservation, Integer> {
    List<Reservation> findByAgency_Id(int agencyId);
}
