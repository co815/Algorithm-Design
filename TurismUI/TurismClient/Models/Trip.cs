namespace TurismClient.Models;

public class Trip
{
    public int Id { get; }
    public string TouristAttraction { get; }
    public string TransportCompany { get; }
    public string DepartureTime { get; }
    public double Price { get; }
    public int AvailableSeats { get; set; }
    
    public Trip(int id, string touristAttraction, string transportCompany, string departureTime, double price, int availableSeats)
    {
        Id = id;
        TouristAttraction = touristAttraction;
        TransportCompany = transportCompany;
        DepartureTime = departureTime;
        Price = price;
        AvailableSeats = availableSeats;
    }

    public override string ToString()
    {
        return $"Trip{{id={Id}, attraction='{TouristAttraction}', company='{TransportCompany}', departure='{DepartureTime}', price={Price}, seats={AvailableSeats}}}";
    }
}