using Avalonia.Controls;
using System.Linq;
using TurismClient.Repositories;
using TurismClient.Services;

namespace TurismClient;

public partial class MainWindow : Window
{
    private readonly TurismService _service;

    public MainWindow()
    {
        InitializeComponent();
        
        var agencyRepo = new AgencyInMemoryRepository();
        var tripRepo = new TripInMemoryRepository();
        var reservationRepo = new ReservationInMemoryRepository();
        
        _service = new TurismService(agencyRepo, tripRepo, reservationRepo);
        
        LoadTrips();
    }

    private void LoadTrips()
    {
        var trips = _service.GetAllTrips().ToList();
        
        var tripsListBox = this.FindControl<ListBox>("TripsListBox");
        if (tripsListBox != null)
        {
            tripsListBox.ItemsSource = trips;
        }
    }
}