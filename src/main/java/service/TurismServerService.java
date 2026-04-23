package service;

import model.Agency;
import model.Reservation;
import model.Trip;
import repository.IAgencyRepository;
import repository.IReservationRepository;
import repository.ITripRepository;

import java.util.List;

public class TurismServerService {
    private final IAgencyRepository agencyRepository;
    private final ITripRepository tripRepository;
    private final IReservationRepository reservationRepository;
    private final Object bookingLock = new Object();

    public TurismServerService(IAgencyRepository agencyRepository,
                               ITripRepository tripRepository,
                               IReservationRepository reservationRepository) {
        this.agencyRepository = agencyRepository;
        this.tripRepository = tripRepository;
        this.reservationRepository = reservationRepository;
    }

    public Agency login(String username, String password) {
        Agency agency = agencyRepository.findByUsername(username);
        if (agency == null || !agency.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
        return agency;
    }

    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    public List<Trip> searchTrips(String attraction, String startTime, String endTime) {
        return tripRepository.findByAttractionAndDepartureInterval(attraction, startTime, endTime);
    }

    public void bookTrip(int tripId, int agencyId, String customerName, String customerPhone, int ticketsCount) {
        if (ticketsCount <= 0) {
            throw new IllegalArgumentException("Tickets count must be greater than 0.");
        }

        synchronized (bookingLock) {
            Trip trip = tripRepository.findById(tripId);
            if (trip == null) {
                throw new IllegalArgumentException("Selected trip does not exist.");
            }

            Agency agency = agencyRepository.findById(agencyId);
            if (agency == null) {
                throw new IllegalArgumentException("Agency does not exist.");
            }

            if (trip.getAvailableSeats() < ticketsCount) {
                throw new IllegalArgumentException("Not enough available seats for this reservation.");
            }

            tripRepository.updateAvailableSeats(tripId, trip.getAvailableSeats() - ticketsCount);
            Trip updatedTrip = tripRepository.findById(tripId);

            Reservation reservation = new Reservation(
                    0,
                    customerName,
                    customerPhone,
                    ticketsCount,
                    updatedTrip,
                    agency
            );
            reservationRepository.save(reservation);
        }
    }
}
