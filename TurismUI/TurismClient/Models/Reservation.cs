namespace TurismClient.Models;

public class Reservation
{
    public int Id { get; }
    public string CustomerName { get; }
    public string CustomerPhone { get; }
    public int NumberOfTickets { get; }
    public Trip Trip { get; }
    public Agency Agency { get; }
    
    public Reservation(int id, string customerName, string customerPhone, int numberOfTickets, Trip trip, Agency agency)
    {
        Id = id;
        CustomerName = customerName;
        CustomerPhone = customerPhone;
        NumberOfTickets = numberOfTickets;
        Trip = trip;
        Agency = agency;
    }

    public override string ToString()
    {
        return
            $"Reservation{{id={Id}, customername={CustomerName}, customerphone={CustomerPhone}, numberoftickets={NumberOfTickets}, trip={Trip.Id}, agency={Agency.Id}}}";
    }
}