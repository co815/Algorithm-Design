package rest;

import model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripSpringRepository extends JpaRepository<Trip, Integer> {
    List<Trip> findByTouristAttraction(String touristAttraction);
    List<Trip> findByTransportCompany(String transportCompany);
    List<Trip> findByAvailableSeatsGreaterThan(int seats);
    List<Trip> findByPriceLessThanEqual(double maxPrice);
}
