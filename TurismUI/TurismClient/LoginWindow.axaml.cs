using Avalonia;
using Avalonia.Controls;
using Avalonia.Controls.ApplicationLifetimes;
using Avalonia.Interactivity;
using System;
using TurismClient.Services;
using TurismClient.Models;

namespace TurismClient;

public partial class LoginWindow : Window
{
    private TurismService? _service;

    public LoginWindow()
    {
        InitializeComponent();
    }

    private void LoginButton_OnClick(object? sender, RoutedEventArgs e)
    {
        var errorText = this.FindControl<TextBlock>("ErrorText");
        if (errorText != null) errorText.Text = "";

        var username = this.FindControl<TextBox>("UsernameBox")?.Text?.Trim() ?? string.Empty;
        var password = this.FindControl<TextBox>("PasswordBox")?.Text?.Trim() ?? string.Empty;

        if (string.IsNullOrWhiteSpace(username) || string.IsNullOrWhiteSpace(password))
        {
            if (errorText != null) errorText.Text = "Introduceți numele de utilizator și parola.";
            return;
        }

        try
        {
            if (_service == null)
            {
                var host = Environment.GetEnvironmentVariable("TURISM_SERVER_HOST") ?? "127.0.0.1";
                var portText = Environment.GetEnvironmentVariable("TURISM_SERVER_PORT");
                var port = int.TryParse(portText, out var parsedPort) ? parsedPort : 55556;
                _service = new TurismService(host, port);
            }

            var agency = _service.Login(username, password);
            if (agency != null)
            {
                var mainWindow = new MainWindow(_service, agency);
                if (Application.Current?.ApplicationLifetime is IClassicDesktopStyleApplicationLifetime desktop)
                {
                    desktop.MainWindow = mainWindow;
                }
                mainWindow.Show();
                
                // Clear the reference so it's not disposed when LoginWindow is closed
                _service = null; 
                this.Close();
            }
            else
            {
                if (errorText != null) errorText.Text = "Autentificare eșuată.";
            }
        }
        catch (Exception ex)
        {
            if (errorText != null) errorText.Text = $"Eroare: {ex.Message}";
            
            _service?.Dispose();
            _service = null;
        }
    }

    protected override void OnClosed(EventArgs e)
    {
        _service?.Dispose();
        base.OnClosed(e);
    }
}
