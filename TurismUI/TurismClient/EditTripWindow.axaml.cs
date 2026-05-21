using Avalonia.Controls;
using TurismClient.Models;

namespace TurismClient;

public partial class EditTripWindow : Window
{
    public record TripData(
        string TouristAttraction,
        string TransportCompany,
        string DepartureTime,
        double Price,
        int    AvailableSeats);

    public TripData? Result { get; private set; }

    public EditTripWindow() { InitializeComponent(); }

    public EditTripWindow(Trip? trip)
    {
        InitializeComponent();
        Title = trip == null ? "Adaugă excursie" : "Editează excursie";

        if (trip != null)
        {
            AttractionBox.Text = trip.TouristAttraction;
            CompanyBox.Text    = trip.TransportCompany;
            DepartureBox.Text  = trip.DepartureTime;
            PriceBox.Value     = (decimal)trip.Price;
            SeatsBox.Value     = trip.AvailableSeats;
        }
    }

    private void SaveButton_OnClick(object? sender, Avalonia.Interactivity.RoutedEventArgs e)
    {
        var attraction = AttractionBox.Text?.Trim() ?? string.Empty;
        var company    = CompanyBox.Text?.Trim()    ?? string.Empty;
        var departure  = DepartureBox.Text?.Trim()  ?? string.Empty;

        if (string.IsNullOrWhiteSpace(attraction) ||
            string.IsNullOrWhiteSpace(company)    ||
            string.IsNullOrWhiteSpace(departure))
        {
            ShowError("Completați toate câmpurile obligatorii.");
            return;
        }

        if (!System.Text.RegularExpressions.Regex.IsMatch(departure, @"^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$"))
        {
            ShowError("Format plecare invalid. Folosiți: YYYY-MM-DD HH:mm");
            return;
        }

        var price = (double)(PriceBox.Value ?? 0);
        var seats = (int)(SeatsBox.Value    ?? 0);

        if (price <= 0) { ShowError("Prețul trebuie să fie mai mare ca 0.");    return; }
        if (seats < 1)  { ShowError("Trebuie cel puțin 1 loc disponibil."); return; }

        Result = new TripData(attraction, company, departure, price, seats);
        Close(true);
    }

    private void CancelButton_OnClick(object? sender, Avalonia.Interactivity.RoutedEventArgs e)
        => Close(false);

    private void ShowError(string message)
    {
        ErrorText.Text      = message;
        ErrorText.IsVisible = true;
    }
}
