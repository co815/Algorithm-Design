using System;
using System.Collections.Generic;
using System.Linq;
using TurismClient.Models;
using TurismClient.Protocol;

namespace TurismClient.Services;

public class TurismService : IDisposable
{
    private readonly ProtobufSocketClient _socketClient;

    public event Action? TripsUpdated;

    public TurismService(string host, int port)
    {
        _socketClient = new ProtobufSocketClient(host, port);
        _socketClient.TripsUpdated += () => TripsUpdated?.Invoke();
    }

    public Agency? Login(string username, string password)
    {
        var response = SendAndEnsureSuccess(
            new RequestEnvelope
            {
                LoginRequest = new LoginRequest
                {
                    Username = username,
                    Password = password
                }
            });

        if (response.PayloadCase != ResponseEnvelope.PayloadOneofCase.LoginResponse)
        {
            throw new Exception("Invalid server response for login.");
        }

        var agencyDto = response.LoginResponse.Agency;
        return new Agency(agencyDto.Id, agencyDto.Name, agencyDto.Username, password);
    }

    public IEnumerable<Trip> GetAllTrips()
    {
        var response = SendAndEnsureSuccess(
            new RequestEnvelope
            {
                GetAllTripsRequest = new GetAllTripsRequest()
            });

        if (response.PayloadCase != ResponseEnvelope.PayloadOneofCase.TripsResponse)
        {
            throw new Exception("Invalid server response for get all trips.");
        }

        return response.TripsResponse.Trips.Select(ToTripModel).ToList();
    }

    public IEnumerable<Trip> SearchTrips(string attraction, string startTime, string endTime)
    {
        var response = SendAndEnsureSuccess(
            new RequestEnvelope
            {
                SearchTripsRequest = new SearchTripsRequest
                {
                    Attraction = attraction,
                    StartTime = startTime,
                    EndTime = endTime
                }
            });

        if (response.PayloadCase != ResponseEnvelope.PayloadOneofCase.TripsResponse)
        {
            throw new Exception("Invalid server response for search trips.");
        }

        return response.TripsResponse.Trips.Select(ToTripModel).ToList();
    }

    public void BookTrip(int tripId, Agency currentAgency, string customerName, string customerPhone, int ticketsCount)
    {
        SendAndEnsureSuccess(
            new RequestEnvelope
            {
                BookTripRequest = new BookTripRequest
                {
                    TripId = tripId,
                    AgencyId = currentAgency.Id,
                    CustomerName = customerName,
                    CustomerPhone = customerPhone,
                    TicketsCount = ticketsCount
                }
            });
    }

    private ResponseEnvelope SendAndEnsureSuccess(RequestEnvelope requestBody)
    {
        var response = _socketClient.SendRequest(requestBody);
        if (!response.Success)
        {
            throw new Exception(response.ErrorMessage);
        }
        return response;
    }

    private static Trip ToTripModel(TripDto dto)
    {
        return new Trip(
            dto.Id,
            dto.TouristAttraction,
            dto.TransportCompany,
            dto.DepartureTime,
            dto.Price,
            dto.AvailableSeats
        );
    }

    public void Dispose()
    {
        _socketClient.Dispose();
    }
}
