package server;

import model.Agency;
import model.Reservation;
import model.Trip;
import protocol.TurismProtos;
import service.TurismServerService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable, ClientConnection {
    private final Socket socket;
    private final TurismServerService service;
    private final TurismSocketServer server;
    private final InputStream in;
    private final OutputStream out;
    private volatile boolean running = true;

    public ClientHandler(Socket socket, TurismServerService service, TurismSocketServer server) throws IOException {
        this.socket = socket;
        this.service = service;
        this.server = server;
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
    }

    @Override
    public void run() {
        try {
            while (running) {
                TurismProtos.RequestEnvelope request = TurismProtos.RequestEnvelope.parseDelimitedFrom(in);
                if (request == null) {
                    break;
                }
                TurismProtos.ResponseEnvelope response = handleRequest(request);
                sendServerEnvelope(
                        TurismProtos.ServerEnvelope.newBuilder()
                                .setResponse(response)
                                .build()
                );
            }
        } catch (IOException ignored) {
        } finally {
            running = false;
            server.removeClient(this);
            closeSilently();
        }
    }

    private TurismProtos.ResponseEnvelope handleRequest(TurismProtos.RequestEnvelope request) {
        try {
            return switch (request.getPayloadCase()) {
                case LOGIN_REQUEST -> handleLogin(request);
                case GET_ALL_TRIPS_REQUEST -> handleGetAllTrips(request);
                case SEARCH_TRIPS_REQUEST -> handleSearchTrips(request);
                case BOOK_TRIP_REQUEST                   -> handleBookTrip(request);
                case GET_RESERVATIONS_BY_AGENCY_REQUEST -> handleGetReservationsByAgency(request);
                case EDIT_RESERVATION_REQUEST           -> handleEditReservation(request);
                case DELETE_RESERVATION_REQUEST         -> handleDeleteReservation(request);
                case CREATE_TRIP_REQUEST -> handleCreateTrip(request);
                case UPDATE_TRIP_REQUEST -> handleUpdateTrip(request);
                case DELETE_TRIP_REQUEST -> handleDeleteTrip(request);
                case PAYLOAD_NOT_SET ->
                        errorResponse(request.getRequestId(), "Invalid request: no payload was provided.");
            };
        } catch (RuntimeException ex) {
            return errorResponse(request.getRequestId(), ex.getMessage() == null ? "Request failed." : ex.getMessage());
        }
    }

    private TurismProtos.ResponseEnvelope handleLogin(TurismProtos.RequestEnvelope request) {
        TurismProtos.LoginRequest loginRequest = request.getLoginRequest();
        Agency agency = service.login(loginRequest.getUsername(), loginRequest.getPassword());
        return TurismProtos.ResponseEnvelope.newBuilder()
                .setRequestId(request.getRequestId())
                .setSuccess(true)
                .setLoginResponse(
                        TurismProtos.LoginResponse.newBuilder()
                                .setAgency(ProtobufMapper.toAgencyDto(agency))
                                .build()
                )
                .build();
    }

    private TurismProtos.ResponseEnvelope handleGetAllTrips(TurismProtos.RequestEnvelope request) {
        return TurismProtos.ResponseEnvelope.newBuilder()
                .setRequestId(request.getRequestId())
                .setSuccess(true)
                .setTripsResponse(ProtobufMapper.toTripsResponse(service.getAllTrips()))
                .build();
    }

    private TurismProtos.ResponseEnvelope handleSearchTrips(TurismProtos.RequestEnvelope request) {
        TurismProtos.SearchTripsRequest searchRequest = request.getSearchTripsRequest();
        return TurismProtos.ResponseEnvelope.newBuilder()
                .setRequestId(request.getRequestId())
                .setSuccess(true)
                .setTripsResponse(
                        ProtobufMapper.toTripsResponse(
                                service.searchTrips(
                                        searchRequest.getAttraction(),
                                        searchRequest.getStartTime(),
                                        searchRequest.getEndTime()
                                )
                        )
                )
                .build();
    }

    private TurismProtos.ResponseEnvelope handleBookTrip(TurismProtos.RequestEnvelope request) {
        TurismProtos.BookTripRequest bookRequest = request.getBookTripRequest();
        service.bookTrip(
                bookRequest.getTripId(),
                bookRequest.getAgencyId(),
                bookRequest.getCustomerName(),
                bookRequest.getCustomerPhone(),
                bookRequest.getTicketsCount()
        );
        server.broadcastTripsUpdated();
        return TurismProtos.ResponseEnvelope.newBuilder()
                .setRequestId(request.getRequestId())
                .setSuccess(true)
                .setBookTripResponse(TurismProtos.BookTripResponse.newBuilder().build())
                .build();
    }

    private TurismProtos.ResponseEnvelope handleGetReservationsByAgency(
            TurismProtos.RequestEnvelope request) {
        int agencyId = request.getGetReservationsByAgencyRequest().getAgencyId();
        return TurismProtos.ResponseEnvelope.newBuilder()
                .setRequestId(request.getRequestId())
                .setSuccess(true)
                .setGetReservationsByAgencyResponse(
                        ProtobufMapper.toReservationsResponse(service.getReservationsByAgency(agencyId)))
                .build();
    }

    private TurismProtos.ResponseEnvelope handleEditReservation(
            TurismProtos.RequestEnvelope request) {
        TurismProtos.EditReservationRequest req = request.getEditReservationRequest();
        Reservation updated = service.editReservation(
                req.getReservationId(),
                req.getCustomerName(),
                req.getCustomerPhone(),
                req.getNumberOfTickets());
        return TurismProtos.ResponseEnvelope.newBuilder()
                .setRequestId(request.getRequestId())
                .setSuccess(true)
                .setEditReservationResponse(
                        TurismProtos.EditReservationResponse.newBuilder()
                                .setReservation(ProtobufMapper.toReservationDto(updated))
                                .build())
                .build();
    }

    private TurismProtos.ResponseEnvelope handleDeleteReservation(
            TurismProtos.RequestEnvelope request) {
        service.deleteReservation(request.getDeleteReservationRequest().getReservationId());
        return TurismProtos.ResponseEnvelope.newBuilder()
                .setRequestId(request.getRequestId())
                .setSuccess(true)
                .setDeleteReservationResponse(
                        TurismProtos.DeleteReservationResponse.newBuilder()
                                .setSuccess(true)
                                .build())
                .build();
    }

    private TurismProtos.ResponseEnvelope handleCreateTrip(TurismProtos.RequestEnvelope request) {
        TurismProtos.CreateTripRequest req = request.getCreateTripRequest();
        Trip trip = service.createTrip(
                req.getTouristAttraction(),
                req.getTransportCompany(),
                req.getDepartureTime(),
                req.getPrice(),
                req.getAvailableSeats()
        );
        server.broadcastTripsUpdated();
        return TurismProtos.ResponseEnvelope.newBuilder()
                .setRequestId(request.getRequestId())
                .setSuccess(true)
                .setCreateTripResponse(
                        TurismProtos.CreateTripResponse.newBuilder()
                                .setTrip(ProtobufMapper.toTripDto(trip))
                                .build()
                )
                .build();
    }

    private TurismProtos.ResponseEnvelope handleUpdateTrip(TurismProtos.RequestEnvelope request) {
        TurismProtos.UpdateTripRequest req = request.getUpdateTripRequest();
        Trip trip = service.updateTrip(
                req.getId(),
                req.getTouristAttraction(),
                req.getTransportCompany(),
                req.getDepartureTime(),
                req.getPrice(),
                req.getAvailableSeats()
        );
        server.broadcastTripsUpdated();
        return TurismProtos.ResponseEnvelope.newBuilder()
                .setRequestId(request.getRequestId())
                .setSuccess(true)
                .setUpdateTripResponse(
                        TurismProtos.UpdateTripResponse.newBuilder()
                                .setTrip(ProtobufMapper.toTripDto(trip))
                                .build()
                )
                .build();
    }

    private TurismProtos.ResponseEnvelope handleDeleteTrip(TurismProtos.RequestEnvelope request) {
        service.deleteTrip(request.getDeleteTripRequest().getTripId());
        server.broadcastTripsUpdated();
        return TurismProtos.ResponseEnvelope.newBuilder()
                .setRequestId(request.getRequestId())
                .setSuccess(true)
                .setDeleteTripResponse(
                        TurismProtos.DeleteTripResponse.newBuilder()
                                .setSuccess(true)
                                .build()
                )
                .build();
    }

    private TurismProtos.ResponseEnvelope errorResponse(String requestId, String errorMessage) {
        return TurismProtos.ResponseEnvelope.newBuilder()
                .setRequestId(requestId)
                .setSuccess(false)
                .setErrorMessage(errorMessage)
                .build();
    }

    private synchronized void sendServerEnvelope(TurismProtos.ServerEnvelope serverEnvelope) {
        try {
            serverEnvelope.writeDelimitedTo(out);
            out.flush();
        } catch (IOException e) {
            running = false;
            closeSilently();
        }
    }

    @Override
    public void sendTripsUpdatedNotification() {
        sendServerEnvelope(
                TurismProtos.ServerEnvelope.newBuilder()
                        .setTripsUpdatedNotification(TurismProtos.TripsUpdatedNotification.newBuilder().build())
                        .build()
        );
    }

    private void closeSilently() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
