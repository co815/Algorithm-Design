package model;

public class Reservation {
    private final int id;
    private final String customerName;
    private final String customerPhone;
    private final int numberOfTickets;
    private final Trip trip;
    private final Agency agency;

    public Reservation(int id, String customerName, String customerPhone, int numberOfTickets, Trip trip, Agency agency) {
        this.id = id;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.numberOfTickets = numberOfTickets;
        this.trip = trip;
        this.agency = agency;
    }

    public int getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public int getNumberOfTickets() { return numberOfTickets; }
    public Trip getTrip() { return trip; }
    public Agency getAgency() { return agency; }

    @Override
    public String toString() {
        return "Reservation{id=" + id +
                ", customer='" + customerName + '\'' +
                ", phone='" + customerPhone + '\'' +
                ", tickets=" + numberOfTickets +
                ", trip=" + trip.getId() +
                ", agency=" + agency.getId() + '}';
    }
}
