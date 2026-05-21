package rest;

import model.Reservation;

public record ReservationDto(
        int    id,
        String customerName,
        String customerPhone,
        int    numberOfTickets,
        int    tripId,
        String tripAttraction,
        int    agencyId
) {
    static ReservationDto from(Reservation r) {
        return new ReservationDto(
                r.getId(),
                r.getCustomerName(),
                r.getCustomerPhone(),
                r.getNumberOfTickets(),
                r.getTrip().getId(),
                r.getTrip().getTouristAttraction(),
                r.getAgency().getId()
        );
    }
}
