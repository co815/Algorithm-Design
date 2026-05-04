namespace TurismClient.Models;

public sealed class Trip
{
    public int    Id                { get; }
    public string TouristAttraction { get; }
    public string TransportCompany  { get; }
    public string DepartureTime     { get; }
    public double Price             { get; }
    public int    AvailableSeats    { get; init; }

    public Trip(
        int    id,
        string touristAttraction,
        string transportCompany,
        string departureTime,
        double price,
        int    availableSeats)
    {
        Id                = id;
        TouristAttraction = touristAttraction;
        TransportCompany  = transportCompany;
        DepartureTime     = departureTime;
        Price             = price;
        AvailableSeats    = availableSeats;
    }

    public override string ToString() =>
        $"Trip {{ Id={Id}, Attraction='{TouristAttraction}', Company='{TransportCompany}', " +
        $"Departure='{DepartureTime}', Price={Price}, Seats={AvailableSeats} }}";
}
