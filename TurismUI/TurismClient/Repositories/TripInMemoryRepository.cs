using System.Collections.Generic;
using System.Linq;
using TurismClient.Models;

namespace TurismClient.Repositories;

public class TripInMemoryRepository : ITripRepository
{
    private readonly List<Trip> _trips;

    public TripInMemoryRepository()
    {
        _trips = new List<Trip>
        {
            new Trip(1, "Paris", "Air France", "2024-06-10 08:00", 1200.0, 50),
            new Trip(2, "Rome", "RyanAir", "2024-06-15 12:00", 800.0, 30),
            new Trip(3, "Paris", "EasyJet", "2024-06-20 09:00", 950.0, 40)
        };
    }

    public void Save(Trip trip)
    {
        _trips.Add(trip);
    }

    public Trip? FindById(int id)
    {
        return _trips.FirstOrDefault(t => t.Id == id);
    }

    public IEnumerable<Trip> FindAll()
    {
        return _trips;
    }

    public IEnumerable<Trip> FindByAttractionAndDepartureInterval(string attraction, string startTime, string endTime)
    {
        return _trips.Where(t => t.TouristAttraction == attraction).ToList();
    }

    public void UpdateAvailableSeats(int tripId, int newAvailableSeats)
    {
        var trip = FindById(tripId);
        if (trip != null)
        {
            trip.AvailableSeats = newAvailableSeats;
        }
    }
}