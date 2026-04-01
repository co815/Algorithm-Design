using System.Collections.Generic;
using TurismClient.Models;

namespace TurismClient.Repositories;

public interface IReservationRepository
{
    void Save(Reservation reservation);
    Reservation? FindById(int id);
    IEnumerable<Reservation> FindAll();
    IEnumerable<Reservation> FindByTrip(int tripId);
}