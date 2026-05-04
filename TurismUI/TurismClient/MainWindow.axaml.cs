using System;
using System.Collections.Generic;
using Avalonia.Controls;
using Avalonia.Threading;
using TurismClient.Models;
using TurismClient.Services;

namespace TurismClient;

public partial class MainWindow : Window
{
    private readonly TurismService _service;
    private readonly Agency        _agency;
    
    public MainWindow()
    {
        InitializeComponent();
        _service = null!;
        _agency  = null!;
    }

    public MainWindow(TurismService service, Agency agency)
    {
        InitializeComponent();
        _service = service;
        _agency  = agency;

        AgencyNameText.Text = agency.Name;

        _service.TripsUpdated += OnTripsUpdated;

        _ = LoadTripsAsync();
        SetStatus("Conectat la server.", isError: false);
    }

    private async System.Threading.Tasks.Task LoadTripsAsync(
        string? attraction = null,
        string? start      = null,
        string? end        = null)
    {
        SetBusy(true);
        try
        {
            IReadOnlyList<Trip> trips;

            if (!string.IsNullOrWhiteSpace(attraction))
                trips = await _service.SearchTripsAsync(attraction, start ?? "", end ?? "");
            else
                trips = await _service.GetAllTripsAsync();

            TripsListBox.ItemsSource = trips;

            SetStatus(trips.Count == 0
                ? "Nicio excursie găsită."
                : $"{trips.Count} excursii încărcate.", isError: false);
        }
        catch (TurismServiceException ex)
        {
            SetStatus(ex.Message, isError: true);
        }
        catch (Exception ex)
        {
            SetStatus($"Eroare: {ex.Message}", isError: true);
        }
        finally
        {
            SetBusy(false);
        }
    }

    private void OnTripsUpdated()
    {
        Dispatcher.UIThread.Post(() =>
        {
            SetStatus("Lista excursiilor a fost actualizată.", isError: false);
            _ = LoadTripsAsync(
                SearchAttractionBox.Text?.Trim(),
                SearchStartBox.Text?.Trim(),
                SearchEndBox.Text?.Trim());
        });
    }

    private void SearchButton_OnClick(object? sender, Avalonia.Interactivity.RoutedEventArgs e)
    {
        _ = LoadTripsAsync(
            SearchAttractionBox.Text?.Trim(),
            SearchStartBox.Text?.Trim(),
            SearchEndBox.Text?.Trim());
    }

    private void ClearSearchButton_OnClick(object? sender, Avalonia.Interactivity.RoutedEventArgs e)
    {
        SearchAttractionBox.Text = string.Empty;
        SearchStartBox.Text      = string.Empty;
        SearchEndBox.Text        = string.Empty;
        _ = LoadTripsAsync();
    }

    private async void BookButton_OnClick(object? sender, Avalonia.Interactivity.RoutedEventArgs e)
    {
        if (TripsListBox.SelectedItem is not Trip selectedTrip)
        {
            SetStatus("Selectați o excursie din listă înainte de rezervare.", isError: true);
            return;
        }

        var customerName  = CustomerNameBox.Text?.Trim()  ?? string.Empty;
        var customerPhone = CustomerPhoneBox.Text?.Trim()  ?? string.Empty;
        var ticketsText   = TicketsCountBox.Text?.Trim()   ?? string.Empty;

        if (string.IsNullOrWhiteSpace(customerName) || string.IsNullOrWhiteSpace(customerPhone))
        {
            SetStatus("Completați numele și telefonul clientului.", isError: true);
            return;
        }

        if (!int.TryParse(ticketsText, out var ticketsCount) || ticketsCount <= 0)
        {
            SetStatus("Numărul de bilete trebuie să fie un număr întreg pozitiv.", isError: true);
            return;
        }

        SetBusy(true);
        try
        {
            await _service.BookTripAsync(selectedTrip.Id, _agency, customerName, customerPhone, ticketsCount);

            SetStatus($"Rezervare confirmată — {ticketsCount} bilet(e) pentru {customerName}.", isError: false);
            ClearBookingForm();
            await LoadTripsAsync(
                SearchAttractionBox.Text?.Trim(),
                SearchStartBox.Text?.Trim(),
                SearchEndBox.Text?.Trim());
        }
        catch (TurismServiceException ex)
        {
            SetStatus(ex.Message, isError: true);
        }
        catch (Exception ex)
        {
            SetStatus($"Eroare la rezervare: {ex.Message}", isError: true);
        }
        finally
        {
            SetBusy(false);
        }
    }

    private void LogoutButton_OnClick(object? sender, Avalonia.Interactivity.RoutedEventArgs e)
    {
        var loginWindow = new LoginWindow();

        if (Avalonia.Application.Current?.ApplicationLifetime
                is Avalonia.Controls.ApplicationLifetimes.IClassicDesktopStyleApplicationLifetime desktop)
            desktop.MainWindow = loginWindow;

        loginWindow.Show();
        Close();
    }

    private void SetStatus(string message, bool isError)
    {
        StatusText.Text = message;
        StatusText.Classes.Set("status-error", isError);
        StatusText.Classes.Set("status-ok",    !isError);
    }

    private void SetBusy(bool busy)
    {
        SearchButton.IsEnabled = !busy;
        BookButton.IsEnabled   = !busy;
    }

    private void ClearBookingForm()
    {
        CustomerNameBox.Text  = string.Empty;
        CustomerPhoneBox.Text = string.Empty;
        TicketsCountBox.Text  = string.Empty;
    }

    protected override void OnClosed(EventArgs e)
    {
        _service.TripsUpdated -= OnTripsUpdated;
        _service.Dispose();
        base.OnClosed(e);
    }
}
