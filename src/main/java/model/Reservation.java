package model;

import jakarta.persistence.*;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    @Column(name = "number_of_tickets", nullable = false)
    private int numberOfTickets;

    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    protected Reservation() {}

    public Reservation(int id, String customerName, String customerPhone,
                       int numberOfTickets, Trip trip, Agency agency) {
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
