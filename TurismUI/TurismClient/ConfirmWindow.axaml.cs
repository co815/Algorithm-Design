using Avalonia.Controls;

namespace TurismClient;

public partial class ConfirmWindow : Window
{
    public ConfirmWindow() { InitializeComponent(); }

    public ConfirmWindow(string message)
    {
        InitializeComponent();
        MessageText.Text = message;
    }

    private void YesButton_OnClick(object? sender, Avalonia.Interactivity.RoutedEventArgs e)
        => Close(true);

    private void NoButton_OnClick(object? sender, Avalonia.Interactivity.RoutedEventArgs e)
        => Close(false);
}
