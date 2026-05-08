package rest;

import model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripSpringRepository extends JpaRepository<Trip, Integer> {
}
