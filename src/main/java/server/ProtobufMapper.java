package server;

import model.Agency;
import model.Trip;
import protocol.TurismProtos;

import java.util.List;

public final class ProtobufMapper {
    private ProtobufMapper() {
    }

    public static TurismProtos.AgencyDto toAgencyDto(Agency agency) {
        return TurismProtos.AgencyDto.newBuilder()
                .setId(agency.getId())
                .setName(agency.getName())
                .setUsername(agency.getUsername())
                .build();
    }

    public static TurismProtos.TripDto toTripDto(Trip trip) {
        return TurismProtos.TripDto.newBuilder()
                .setId(trip.getId())
                .setTouristAttraction(trip.getTouristAttraction())
                .setTransportCompany(trip.getTransportCompany())
                .setDepartureTime(trip.getDepartureTime())
                .setPrice(trip.getPrice())
                .setAvailableSeats(trip.getAvailableSeats())
                .build();
    }

    public static TurismProtos.TripsResponse toTripsResponse(List<Trip> trips) {
        TurismProtos.TripsResponse.Builder builder = TurismProtos.TripsResponse.newBuilder();
        for (Trip trip : trips) {
            builder.addTrips(toTripDto(trip));
        }
        return builder.build();
    }
}
