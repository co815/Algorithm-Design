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

    public List<Reservation> getReservationsByAgency(int agencyId) {
        return reservationRepository.findByAgency(agencyId);
    }

    public Reservation editReservation(int id, String name, String phone, int tickets) {
        if (tickets <= 0) throw new IllegalArgumentException("Tickets must be greater than 0.");
        synchronized (bookingLock) {
            Reservation r = reservationRepository.findById(id);
            if (r == null) throw new IllegalArgumentException("Reservation not found.");
            int delta = tickets - r.getNumberOfTickets();
            if (delta > 0) {
                Trip trip = tripRepository.findById(r.getTrip().getId());
                if (trip.getAvailableSeats() < delta)
                    throw new IllegalArgumentException("Not enough available seats.");
                tripRepository.updateAvailableSeats(trip.getId(), trip.getAvailableSeats() - delta);
            } else if (delta < 0) {
                Trip trip = tripRepository.findById(r.getTrip().getId());
                tripRepository.updateAvailableSeats(trip.getId(), trip.getAvailableSeats() + (-delta));
            }
            r.setCustomerName(name);
            r.setCustomerPhone(phone);
            r.setNumberOfTickets(tickets);
            reservationRepository.update(r);
            return reservationRepository.findById(id);
        }
    }

    public void deleteReservation(int id) {
        synchronized (bookingLock) {
            Reservation r = reservationRepository.findById(id);
            if (r == null) throw new IllegalArgumentException("Reservation not found.");
            Trip trip = tripRepository.findById(r.getTrip().getId());
            tripRepository.updateAvailableSeats(trip.getId(), trip.getAvailableSeats() + r.getNumberOfTickets());
            reservationRepository.delete(id);
            reservationRepository.resequenceIds();
        }
    }

    public Trip createTrip(String touristAttraction, String transportCompany,
                            String departureTime, double price, int availableSeats) {
        if (touristAttraction == null || touristAttraction.isBlank())
            throw new IllegalArgumentException("Tourist attraction is required.");
        if (transportCompany == null || transportCompany.isBlank())
            throw new IllegalArgumentException("Transport company is required.");
        if (departureTime == null || departureTime.isBlank())
            throw new IllegalArgumentException("Departure time is required.");
        if (price <= 0)
            throw new IllegalArgumentException("Price must be greater than 0.");
        if (availableSeats < 1)
            throw new IllegalArgumentException("Available seats must be at least 1.");

        Trip trip = new Trip(0, touristAttraction, transportCompany, departureTime, price, availableSeats);
        tripRepository.save(trip);
        return trip;
    }

    public Trip updateTrip(int id, String touristAttraction, String transportCompany,
                            String departureTime, double price, int availableSeats) {
        if (touristAttraction == null || touristAttraction.isBlank())
            throw new IllegalArgumentException("Tourist attraction is required.");
        if (transportCompany == null || transportCompany.isBlank())
            throw new IllegalArgumentException("Transport company is required.");
        if (departureTime == null || departureTime.isBlank())
            throw new IllegalArgumentException("Departure time is required.");
        if (price <= 0)
            throw new IllegalArgumentException("Price must be greater than 0.");
        if (availableSeats < 1)
            throw new IllegalArgumentException("Available seats must be at least 1.");
        synchronized (bookingLock) {
            Trip existing = tripRepository.findById(id);
            if (existing == null)
                throw new IllegalArgumentException("Trip not found: " + id);
            existing.setTouristAttraction(touristAttraction);
            existing.setTransportCompany(transportCompany);
            existing.setDepartureTime(departureTime);
            existing.setPrice(price);
            existing.setAvailableSeats(availableSeats);
            tripRepository.update(existing);
            return tripRepository.findById(id);
        }
    }

    public void deleteTrip(int id) {
        synchronized (bookingLock) {
            Trip existing = tripRepository.findById(id);
            if (existing == null)
                throw new IllegalArgumentException("Trip not found: " + id);
            List<Integer> reservationIds = reservationRepository.findByTrip(id)
                    .stream().map(Reservation::getId).toList();
            reservationIds.forEach(reservationRepository::delete);
            if (!reservationIds.isEmpty()) reservationRepository.resequenceIds();
            tripRepository.delete(id);
        }
    }
}
