using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using TurismClient.Models;
using TurismClient.Protocol;

namespace TurismClient.Services;

public sealed class TurismService : IDisposable
{
    private readonly ProtobufSocketClient _socket;
    
    public event Action? TripsUpdated;

    public TurismService(string host, int port)
    {
        _socket = new ProtobufSocketClient(host, port);
        _socket.TripsUpdated += () => TripsUpdated?.Invoke();
    }
    
    public async Task<Agency> LoginAsync(string username, string password)
    {
        var response = await SendAndEnsureSuccessAsync(new RequestEnvelope
        {
            LoginRequest = new LoginRequest
            {
                Username = username,
                Password = password
            }
        }).ConfigureAwait(false);

        if (response.PayloadCase != ResponseEnvelope.PayloadOneofCase.LoginResponse)
            throw new InvalidOperationException("Unexpected server response for login.");

        var dto = response.LoginResponse.Agency;
        return new Agency(dto.Id, dto.Name, dto.Username);
    }

    public async Task<IReadOnlyList<Trip>> GetAllTripsAsync()
    {
        var response = await SendAndEnsureSuccessAsync(new RequestEnvelope
        {
            GetAllTripsRequest = new GetAllTripsRequest()
        }).ConfigureAwait(false);

        if (response.PayloadCase != ResponseEnvelope.PayloadOneofCase.TripsResponse)
            throw new InvalidOperationException("Unexpected server response for GetAllTrips.");

        return response.TripsResponse.Trips.Select(ToTrip).ToList();
    }
    
    public async Task<IReadOnlyList<Trip>> SearchTripsAsync(
        string attraction,
        string startTime,
        string endTime)
    {
        var response = await SendAndEnsureSuccessAsync(new RequestEnvelope
        {
            SearchTripsRequest = new SearchTripsRequest
            {
                Attraction = attraction,
                StartTime  = startTime,
                EndTime    = endTime
            }
        }).ConfigureAwait(false);

        if (response.PayloadCase != ResponseEnvelope.PayloadOneofCase.TripsResponse)
            throw new InvalidOperationException("Unexpected server response for SearchTrips.");

        return response.TripsResponse.Trips.Select(ToTrip).ToList();
    }
    
    public async Task BookTripAsync(
        int    tripId,
        Agency agency,
        string customerName,
        string customerPhone,
        int    ticketsCount)
    {
        await SendAndEnsureSuccessAsync(new RequestEnvelope
        {
            BookTripRequest = new BookTripRequest
            {
                TripId        = tripId,
                AgencyId      = agency.Id,
                CustomerName  = customerName,
                CustomerPhone = customerPhone,
                TicketsCount  = ticketsCount
            }
        }).ConfigureAwait(false);
    }

    private async Task<ResponseEnvelope> SendAndEnsureSuccessAsync(RequestEnvelope request)
    {
        var response = await _socket.SendRequestAsync(request).ConfigureAwait(false);

        if (!response.Success)
            throw new TurismServiceException(response.ErrorMessage);

        return response;
    }

    public async Task<IReadOnlyList<Reservation>> GetReservationsAsync(int agencyId)
    {
        var response = await SendAndEnsureSuccessAsync(new RequestEnvelope
        {
            GetReservationsByAgencyRequest = new GetReservationsByAgencyRequest { AgencyId = agencyId }
        }).ConfigureAwait(false);

        if (response.PayloadCase != ResponseEnvelope.PayloadOneofCase.GetReservationsByAgencyResponse)
            throw new InvalidOperationException("Unexpected server response for GetReservations.");

        return response.GetReservationsByAgencyResponse.Reservations
            .Select(ToReservation)
            .ToList();
    }

    public async Task<Reservation> EditReservationAsync(int id, string name, string phone, int tickets)
    {
        var response = await SendAndEnsureSuccessAsync(new RequestEnvelope
        {
            EditReservationRequest = new EditReservationRequest
            {
                ReservationId   = id,
                CustomerName    = name,
                CustomerPhone   = phone,
                NumberOfTickets = tickets
            }
        }).ConfigureAwait(false);

        if (response.PayloadCase != ResponseEnvelope.PayloadOneofCase.EditReservationResponse)
            throw new InvalidOperationException("Unexpected server response for EditReservation.");

        return ToReservation(response.EditReservationResponse.Reservation);
    }

    public async Task DeleteReservationAsync(int id)
    {
        await SendAndEnsureSuccessAsync(new RequestEnvelope
        {
            DeleteReservationRequest = new DeleteReservationRequest { ReservationId = id }
        }).ConfigureAwait(false);
    }

    public async Task<Trip> CreateTripAsync(
        string touristAttraction, string transportCompany,
        string departureTime, double price, int availableSeats)
    {
        var response = await SendAndEnsureSuccessAsync(new RequestEnvelope
        {
            CreateTripRequest = new CreateTripRequest
            {
                TouristAttraction = touristAttraction,
                TransportCompany  = transportCompany,
                DepartureTime     = departureTime,
                Price             = price,
                AvailableSeats    = availableSeats
            }
        }).ConfigureAwait(false);

        if (response.PayloadCase != ResponseEnvelope.PayloadOneofCase.CreateTripResponse)
            throw new InvalidOperationException("Unexpected server response for CreateTrip.");

        return ToTrip(response.CreateTripResponse.Trip);
    }

    public async Task<Trip> UpdateTripAsync(
        int id, string touristAttraction, string transportCompany,
        string departureTime, double price, int availableSeats)
    {
        var response = await SendAndEnsureSuccessAsync(new RequestEnvelope
        {
            UpdateTripRequest = new UpdateTripRequest
            {
                Id                = id,
                TouristAttraction = touristAttraction,
                TransportCompany  = transportCompany,
                DepartureTime     = departureTime,
                Price             = price,
                AvailableSeats    = availableSeats
            }
        }).ConfigureAwait(false);

        if (response.PayloadCase != ResponseEnvelope.PayloadOneofCase.UpdateTripResponse)
            throw new InvalidOperationException("Unexpected server response for UpdateTrip.");

        return ToTrip(response.UpdateTripResponse.Trip);
    }

    public async Task DeleteTripAsync(int tripId)
    {
        await SendAndEnsureSuccessAsync(new RequestEnvelope
        {
            DeleteTripRequest = new DeleteTripRequest { TripId = tripId }
        }).ConfigureAwait(false);
    }

    private static Trip ToTrip(TripDto dto) =>
        new Trip(
            dto.Id,
            dto.TouristAttraction,
            dto.TransportCompany,
            dto.DepartureTime,
            dto.Price,
            dto.AvailableSeats);

    private static Reservation ToReservation(ReservationDto dto) =>
        new Reservation(
            dto.Id,
            dto.CustomerName,
            dto.CustomerPhone,
            dto.NumberOfTickets,
            dto.TripId,
            dto.TripAttraction,
            dto.AgencyId);

    public void Dispose() => _socket.Dispose();
}
