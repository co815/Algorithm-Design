package model;

import jakarta.persistence.*;

@Entity
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "tourist_attraction", nullable = false)
    private String touristAttraction;

    @Column(name = "transport_company", nullable = false)
    private String transportCompany;

    @Column(name = "departure_time", nullable = false)
    private String departureTime;

    @Column(nullable = false)
    private double price;

    @Column(name = "available_seats", nullable = false)
    private int availableSeats;

    protected Trip() {}

    public Trip(int id, String touristAttraction, String transportCompany,
                String departureTime, double price, int availableSeats) {
        this.id = id;
        this.touristAttraction = touristAttraction;
        this.transportCompany = transportCompany;
        this.departureTime = departureTime;
        this.price = price;
        this.availableSeats = availableSeats;
    }

    public int getId() { return id; }
    public String getTouristAttraction() { return touristAttraction; }
    public String getTransportCompany() { return transportCompany; }
    public String getDepartureTime() { return departureTime; }
    public double getPrice() { return price; }
    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    @Override
    public String toString() {
        return "Trip{id=" + id +
                ", attraction='" + touristAttraction + '\'' +
                ", company='" + transportCompany + '\'' +
                ", departure='" + departureTime + '\'' +
                ", price=" + price +
                ", seats=" + availableSeats + '}';
    }
}
