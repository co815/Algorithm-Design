package rest;

import model.Trip;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/trips")
public class TripRestController {

    private final TripSpringRepository repo;

    public TripRestController(TripSpringRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Trip> getAllTrips() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripById(@PathVariable("id") Integer id) {
        Optional<Trip> trip = repo.findById(id);
        return trip.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Trip> createTrip(@RequestBody Trip tripRequest) {
        Trip trip = new Trip();
        trip.setTouristAttraction(tripRequest.getTouristAttraction());
        trip.setTransportCompany(tripRequest.getTransportCompany());
        trip.setDepartureTime(tripRequest.getDepartureTime());
        trip.setPrice(tripRequest.getPrice());
        trip.setAvailableSeats(tripRequest.getAvailableSeats());
        Trip saved = repo.save(trip);
        return ResponseEntity.created(URI.create("/trips/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable("id") Integer id, @RequestBody Trip tripRequest) {
        Optional<Trip> existingTrip = repo.findById(id);
        if (existingTrip.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Trip trip = existingTrip.get();
        trip.setTouristAttraction(tripRequest.getTouristAttraction());
        trip.setTransportCompany(tripRequest.getTransportCompany());
        trip.setDepartureTime(tripRequest.getDepartureTime());
        trip.setPrice(tripRequest.getPrice());
        trip.setAvailableSeats(tripRequest.getAvailableSeats());

        Trip saved = repo.save(trip);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable("id") Integer id) {
        Optional<Trip> trip = repo.findById(id);
        if (trip.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        repo.delete(trip.get());
        return ResponseEntity.noContent().build();
    }
}
