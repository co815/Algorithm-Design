package repository;

import model.Trip;
import java.util.List;

public interface ITripRepository {
    void save(Trip trip);
    Trip findById(int id);
    List<Trip> findAll();
    List<Trip> findByAttractionAndDepartureInterval(String attraction, String startTime, String endTime);
    void updateAvailableSeats(int tripId, int newAvailableSeats);
}
