using System;
using System.Collections.Generic;
using TurismClient.Models;
using TurismClient.Repositories;

namespace TurismClient.Services;

public class TurismService
{
    private readonly IAgencyRepository _agencyRepo;
    private readonly ITripRepository _tripRepo;
    private readonly IReservationRepository _reservationRepo;
    
    public TurismService(IAgencyRepository agencyRepo, ITripRepository tripRepo, IReservationRepository reservationRepo)
    {
        _agencyRepo = agencyRepo;
        _tripRepo = tripRepo;
        _reservationRepo = reservationRepo;
    }
    
    public Agency? Login(string username, string password)
    {
        var agency = _agencyRepo.FindByUsername(username);
        
        if (agency != null && agency.Password == password)
        {
            return agency; 
        }
        
        return null; 
    }
    
    public IEnumerable<Trip> GetAllTrips()
    {
        return _tripRepo.FindAll();
    }
    
    public IEnumerable<Trip> SearchTrips(string attraction, string startTime, string endTime)
    {
        return _tripRepo.FindByAttractionAndDepartureInterval(attraction, startTime, endTime);
    }
    
    public void BookTrip(int tripId, Agency currentAgency, string customerName, string customerPhone, int ticketsCount)
    {
        var trip = _tripRepo.FindById(tripId);
        
        if (trip == null)
        {
            throw new Exception("Excursia selectată nu există!");
        }
        
        if (trip.AvailableSeats < ticketsCount)
        {
            throw new Exception("Nu sunt destule locuri disponibile pentru această rezervare!");
        }
        
        _tripRepo.UpdateAvailableSeats(tripId, trip.AvailableSeats - ticketsCount);
        
        int newReservationId = new Random().Next(100, 10000); 
        var reservation = new Reservation(newReservationId, customerName, customerPhone, ticketsCount, trip, currentAgency);
        
        _reservationRepo.Save(reservation);
    }
}