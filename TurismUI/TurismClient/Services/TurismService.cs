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

    private static Trip ToTrip(TripDto dto) =>
        new Trip(
            dto.Id,
            dto.TouristAttraction,
            dto.TransportCompany,
            dto.DepartureTime,
            dto.Price,
            dto.AvailableSeats);

    public void Dispose() => _socket.Dispose();
}
