package rest;

import model.Agency;
import model.Reservation;
import model.Trip;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/trips")
public class TripRestController {

    private final TripSpringRepository repo;
    private final AgencySpringRepository agencyRepo;
    private final ReservationSpringRepository reservationRepo;
    private final ApplicationEventPublisher publisher;
    private final TripNotifier notifier;

    public TripRestController(TripSpringRepository repo,
                              AgencySpringRepository agencyRepo,
                              ReservationSpringRepository reservationRepo,
                              ApplicationEventPublisher publisher,
                              TripNotifier notifier) {
        this.repo            = repo;
        this.agencyRepo      = agencyRepo;
        this.reservationRepo = reservationRepo;
        this.publisher       = publisher;
        this.notifier        = notifier;
    }

    @GetMapping
    public List<Trip> getAllTrips(
            @RequestParam(name = "attraction", required = false) String attraction,
            @RequestParam(name = "company", required = false) String company,
            @RequestParam(name = "minSeats", required = false) Integer minSeats,
            @RequestParam(name = "maxPrice", required = false) Double maxPrice) {
        return repo.findAll().stream()
                .filter(t -> attraction == null || t.getTouristAttraction().equals(attraction))
                .filter(t -> company == null || t.getTransportCompany().equals(company))
                .filter(t -> minSeats == null || t.getAvailableSeats() > minSeats)
                .filter(t -> maxPrice == null || t.getPrice() <= maxPrice)
                .toList();
    }

    @GetMapping(value = "/updates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamUpdates() {
        return notifier.register();
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
        publisher.publishEvent(new TripChangeEvent(this));
        return ResponseEntity.created(URI.create("/trips/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable("id") Integer id,
                                           @RequestBody Trip tripRequest) {
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
        publisher.publishEvent(new TripChangeEvent(this));
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable("id") Integer id) {
        Optional<Trip> trip = repo.findById(id);
        if (trip.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        repo.delete(trip.get());
        publisher.publishEvent(new TripChangeEvent(this));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/book")
    @Transactional
    public ResponseEntity<?> bookTrip(@PathVariable("id") Integer id,
                                      @RequestBody BookingRequest body) {
        Optional<Trip> opt = repo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Trip trip = opt.get();
        if (trip.getAvailableSeats() < body.seats()) {
            return ResponseEntity.status(409).body("Not enough seats available");
        }

        Optional<Agency> agencyOpt = agencyRepo.findById(body.agencyId());
        if (agencyOpt.isEmpty()) return ResponseEntity.notFound().build();

        trip.setAvailableSeats(trip.getAvailableSeats() - body.seats());
        repo.save(trip);

        Reservation reservation = new Reservation(
                0,
                body.customerName(),
                body.customerPhone(),
                body.seats(),
                trip,
                agencyOpt.get()
        );
        Reservation saved = reservationRepo.save(reservation);
        publisher.publishEvent(new TripChangeEvent(this));
        return ResponseEntity.ok(ReservationDto.from(saved));
    }

    record BookingRequest(int agencyId, String customerName, String customerPhone, int seats) {}
}
