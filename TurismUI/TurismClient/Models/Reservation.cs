namespace TurismClient.Models;

public sealed record Reservation(
    int    Id,
    string CustomerName,
    string CustomerPhone,
    int    NumberOfTickets,
    int    TripId,
    string TripAttraction,
    int    AgencyId)
{
    public override string ToString() =>
        $"Reservation {{ Id={Id}, Customer='{CustomerName}', Phone='{CustomerPhone}', " +
        $"Tickets={NumberOfTickets}, Trip='{TripAttraction}' }}";
}
