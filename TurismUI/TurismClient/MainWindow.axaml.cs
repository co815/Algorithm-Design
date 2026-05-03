using Avalonia.Controls;
using Avalonia.Threading;
using System;
using System.Linq;
using TurismClient.Models;
using TurismClient.Services;

namespace TurismClient;

public partial class MainWindow : Window
{
    private readonly TurismService _service;
    private readonly Agency _currentAgency;

    public MainWindow()
    {
        InitializeComponent();
        _service = null!;
        _currentAgency = null!;
    }

    public MainWindow(TurismService service, Agency currentAgency)
    {
        InitializeComponent();
        _service = service;
        _currentAgency = currentAgency;

        var userInfoText = this.FindControl<TextBlock>("UserInfoText");
        if (userInfoText != null)
        {
            userInfoText.Text = $"Autentificat ca: {_currentAgency.Name}";
        }

        _service.TripsUpdated += OnTripsUpdated;

        LoadTrips();
        SetStatus($"Conectat la server. Agenție: {_currentAgency.Name}");
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

    private void OnTripsUpdated()
    {
        Dispatcher.UIThread.Post(() =>
        {
            LoadTrips();
            SetStatus("Lista excursiilor a fost actualizată automat.");
        });
    }

    private void BookTripButton_OnClick(object? sender, Avalonia.Interactivity.RoutedEventArgs e)
    {
        try
        {
            var tripsListBox = this.FindControl<ListBox>("TripsListBox");
            var selectedTrip = tripsListBox?.SelectedItem as Trip;
            if (selectedTrip == null)
            {
                throw new Exception("Selectați o excursie înainte de rezervare.");
            }

            var customerName = this.FindControl<TextBox>("CustomerNameBox")?.Text?.Trim() ?? string.Empty;
            var customerPhone = this.FindControl<TextBox>("CustomerPhoneBox")?.Text?.Trim() ?? string.Empty;
            var ticketsText = this.FindControl<TextBox>("TicketsCountBox")?.Text?.Trim() ?? string.Empty;

            if (string.IsNullOrWhiteSpace(customerName) || string.IsNullOrWhiteSpace(customerPhone))
            {
                throw new Exception("Completați numele și telefonul clientului.");
            }

            if (!int.TryParse(ticketsText, out var ticketsCount))
            {
                throw new Exception("Numărul de bilete trebuie să fie un număr valid.");
            }

            _service.BookTrip(selectedTrip.Id, _currentAgency, customerName, customerPhone, ticketsCount);
            SetStatus("Rezervare salvată cu succes.");
            LoadTrips();
        }
        catch (Exception ex)
        {
            SetStatus(ex.Message);
        }
    }

    private void LogoutButton_OnClick(object? sender, Avalonia.Interactivity.RoutedEventArgs e)
    {
        var loginWindow = new LoginWindow();
        
        if (Avalonia.Application.Current?.ApplicationLifetime is Avalonia.Controls.ApplicationLifetimes.IClassicDesktopStyleApplicationLifetime desktop)
        {
            desktop.MainWindow = loginWindow;
        }

        loginWindow.Show();
        this.Close();
    }

    private void SetStatus(string message)
    {
        var statusText = this.FindControl<TextBlock>("StatusText");
        if (statusText != null)
        {
            statusText.Text = message;
        }
    }

    protected override void OnClosed(EventArgs e)
    {
        _service.TripsUpdated -= OnTripsUpdated;
        _service.Dispose();
        base.OnClosed(e);
    }
}
