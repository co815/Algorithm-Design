package rest;

import model.Reservation;
import model.Trip;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationSpringRepository reservationRepo;
    private final TripSpringRepository tripRepo;

    public ReservationController(ReservationSpringRepository reservationRepo,
                                 TripSpringRepository tripRepo) {
        this.reservationRepo = reservationRepo;
        this.tripRepo        = tripRepo;
    }

    @GetMapping
    public List<ReservationDto> getReservations(@RequestParam(name = "agencyId") int agencyId) {
        return reservationRepo.findByAgency_Id(agencyId)
                .stream()
                .map(ReservationDto::from)
                .toList();
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateReservation(@PathVariable("id") int id,
                                               @RequestBody UpdateRequest body) {
        Optional<Reservation> opt = reservationRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Reservation r      = opt.get();
        int         oldTix = r.getNumberOfTickets();
        int         newTix = body.numberOfTickets();
        int         delta  = newTix - oldTix;

        if (delta != 0) {
            Trip trip = r.getTrip();
            if (delta > 0 && trip.getAvailableSeats() < delta) {
                return ResponseEntity.status(409).body("Not enough seats available");
            }
            trip.setAvailableSeats(trip.getAvailableSeats() - delta);
            tripRepo.save(trip);
        }

        r.setCustomerName(body.customerName());
        r.setCustomerPhone(body.customerPhone());
        r.setNumberOfTickets(newTix);
        Reservation saved = reservationRepo.save(r);
        return ResponseEntity.ok(ReservationDto.from(saved));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") int id) {
        Optional<Reservation> opt = reservationRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Reservation r    = opt.get();
        Trip        trip = r.getTrip();
        trip.setAvailableSeats(trip.getAvailableSeats() + r.getNumberOfTickets());
        tripRepo.save(trip);

        reservationRepo.delete(r);
        return ResponseEntity.noContent().build();
    }

    record UpdateRequest(String customerName, String customerPhone, int numberOfTickets) {}
}
