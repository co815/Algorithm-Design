using System.Collections.Generic;
using TurismClient.Models;

namespace TurismClient.Repositories;

public interface ITripRepository
{
    void Save(Trip trip);
    Trip? FindById(int id);
    IEnumerable<Trip> FindAll();
    IEnumerable<Trip> FindByAttractionAndDepartureInterval(string attraction, string startDate, string endDate);
    void UpdateAvailableSeats(int tripId, int newAvailableSeats);
}