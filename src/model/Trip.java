package model;

public class Trip {
    private final int id;
    private final String touristAttraction;
    private final String transportCompany;
    private final String departureTime;
    private final double price;
    private int availableSeats;

    public Trip(int id, String touristAttraction, String transportCompany, String departureTime, double price, int availableSeats) {
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