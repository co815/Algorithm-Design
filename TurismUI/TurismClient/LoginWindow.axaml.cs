using System;
using Avalonia.Controls;
using Avalonia.Controls.ApplicationLifetimes;
using Avalonia.Interactivity;
using TurismClient.Services;

namespace TurismClient;

public partial class LoginWindow : Window
{
    private TurismService? _service;

    public LoginWindow() => InitializeComponent();

    private async void LoginButton_OnClick(object? sender, RoutedEventArgs e)
    {
        ClearError();
        SetBusy(true);

        var username = UsernameBox.Text?.Trim() ?? string.Empty;
        var password = PasswordBox.Text?.Trim() ?? string.Empty;

        if (string.IsNullOrWhiteSpace(username) || string.IsNullOrWhiteSpace(password))
        {
            ShowError("Introduceți numele de utilizator și parola.");
            SetBusy(false);
            return;
        }

        try
        {
            _service ??= CreateService();

            var agency = await _service.LoginAsync(username, password);
            
            var mainWindow = new MainWindow(_service, agency);
            _service = null;

            if (Avalonia.Application.Current?.ApplicationLifetime
                    is IClassicDesktopStyleApplicationLifetime desktop)
                desktop.MainWindow = mainWindow;

            mainWindow.Show();
            Close();
        }
        catch (TurismServiceException ex)
        {
            ShowError(ex.Message);
            DisposeService();
        }
        catch (Exception ex)
        {
            ShowError($"Eroare de conexiune: {ex.Message}");
            DisposeService();
        }
        finally
        {
            SetBusy(false);
        }
    }

    private static TurismService CreateService()
    {
        var host = Environment.GetEnvironmentVariable("TURISM_SERVER_HOST") ?? "127.0.0.1";
        var port = int.TryParse(
            Environment.GetEnvironmentVariable("TURISM_SERVER_PORT"), out var p) ? p : 55556;
        return new TurismService(host, port);
    }

    private void ShowError(string message)
    {
        ErrorText.Text      = message;
        ErrorText.IsVisible = true;
    }

    private void ClearError()
    {
        ErrorText.Text      = string.Empty;
        ErrorText.IsVisible = false;
    }

    private void SetBusy(bool busy)
    {
        LoginButton.IsEnabled = !busy;
        UsernameBox.IsEnabled = !busy;
        PasswordBox.IsEnabled = !busy;
        LoginButton.Content   = busy ? "Se conectează…" : "Autentificare";
    }

    private void DisposeService()
    {
        _service?.Dispose();
        _service = null;
    }

    protected override void OnClosed(EventArgs e)
    {
        DisposeService();
        base.OnClosed(e);
    }
}
