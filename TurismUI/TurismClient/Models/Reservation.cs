namespace TurismClient.Models;

public sealed class Reservation
{
    public int    Id              { get; }
    public string CustomerName    { get; }
    public string CustomerPhone   { get; }
    public int    NumberOfTickets { get; }
    public Trip   Trip            { get; }
    public Agency Agency          { get; }

    public Reservation(
        int    id,
        string customerName,
        string customerPhone,
        int    numberOfTickets,
        Trip   trip,
        Agency agency)
    {
        Id              = id;
        CustomerName    = customerName;
        CustomerPhone   = customerPhone;
        NumberOfTickets = numberOfTickets;
        Trip            = trip;
        Agency          = agency;
    }

    public override string ToString() =>
        $"Reservation {{ Id={Id}, Customer='{CustomerName}', Phone='{CustomerPhone}', " +
        $"Tickets={NumberOfTickets}, TripId={Trip.Id}, AgencyId={Agency.Id} }}";
}
