using System;
using System.Threading.Tasks;
using Avalonia.Controls;
using Avalonia.Interactivity;
using TurismClient.Models;
using TurismClient.Services;

namespace TurismClient;

public partial class ReservationsWindow : Window
{
    private readonly TurismService _service;
    private readonly Agency        _agency;

    public ReservationsWindow()
    {
        InitializeComponent();
        _service = null!;
        _agency  = null!;
    }

    public ReservationsWindow(TurismService service, Agency agency)
    {
        InitializeComponent();
        _service = service;
        _agency  = agency;
        TitleText.Text = $"Rezervările agenției {agency.Name}";
        _ = LoadReservationsAsync();
    }

    private async Task LoadReservationsAsync()
    {
        try
        {
            var list = await _service.GetReservationsAsync(_agency.Id);
            ReservationsListBox.ItemsSource = list;
            SetStatus(list.Count == 0 ? "Nicio rezervare." : $"{list.Count} rezervări.", isError: false);
        }
        catch (Exception ex)
        {
            SetStatus($"Eroare: {ex.Message}", isError: true);
        }
    }

    private void EditButton_OnClick(object? sender, RoutedEventArgs e)
    {
        if (ReservationsListBox.SelectedItem is not Reservation r)
        {
            SetStatus("Selectați o rezervare.", isError: true);
            return;
        }
        EditNameBox.Text    = r.CustomerName;
        EditPhoneBox.Text   = r.CustomerPhone;
        EditTicketsBox.Text = r.NumberOfTickets.ToString();
        SetStatus("Modificați câmpurile și apăsați Salvează.", isError: false);
    }

    private async void SaveButton_OnClick(object? sender, RoutedEventArgs e)
    {
        if (ReservationsListBox.SelectedItem is not Reservation r)
        {
            SetStatus("Selectați o rezervare.", isError: true);
            return;
        }

        var name  = EditNameBox.Text?.Trim()  ?? string.Empty;
        var phone = EditPhoneBox.Text?.Trim() ?? string.Empty;

        if (string.IsNullOrWhiteSpace(name) || string.IsNullOrWhiteSpace(phone))
        {
            SetStatus("Completați numele și telefonul.", isError: true);
            return;
        }

        if (!int.TryParse(EditTicketsBox.Text, out var tickets) || tickets <= 0)
        {
            SetStatus("Nr. de bilete trebuie să fie un număr întreg pozitiv.", isError: true);
            return;
        }

        try
        {
            await _service.EditReservationAsync(r.Id, name, phone, tickets);
            await LoadReservationsAsync();
            SetStatus($"Rezervarea #{r.Id} actualizată.", isError: false);
        }
        catch (TurismServiceException ex) { SetStatus(ex.Message, isError: true); }
        catch (Exception ex)             { SetStatus($"Eroare: {ex.Message}", isError: true); }
    }

    private async void DeleteButton_OnClick(object? sender, RoutedEventArgs e)
    {
        if (ReservationsListBox.SelectedItem is not Reservation r)
        {
            SetStatus("Selectați o rezervare.", isError: true);
            return;
        }

        try
        {
            await _service.DeleteReservationAsync(r.Id);
            await LoadReservationsAsync();
            SetStatus($"Rezervarea #{r.Id} ștearsă.", isError: false);
        }
        catch (TurismServiceException ex) { SetStatus(ex.Message, isError: true); }
        catch (Exception ex)             { SetStatus($"Eroare: {ex.Message}", isError: true); }
    }

    private void SetStatus(string message, bool isError)
    {
        StatusText.Text = message;
        StatusText.Classes.Set("status-error", isError);
        StatusText.Classes.Set("status-ok",    !isError);
    }
}
