using System.Collections.Generic;
using System.Linq;
using TurismClient.Models;

namespace TurismClient.Repositories;

public class ReservationInMemoryRepository : IReservationRepository
{
    private readonly List<Reservation> _reservations;

    public ReservationInMemoryRepository()
    {
        _reservations = new List<Reservation>();
    }

    public void Save(Reservation reservation)
    {
        _reservations.Add(reservation);
    }

    public Reservation? FindById(int id)
    {
        return _reservations.FirstOrDefault(r => r.Id == id);
    }

    public IEnumerable<Reservation> FindAll()
    {
        return _reservations;
    }

    public IEnumerable<Reservation> FindByTrip(int tripId)
    {
        return _reservations.Where(r => r.Trip.Id == tripId).ToList();
    }
}