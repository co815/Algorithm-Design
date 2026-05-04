using System;
using System.Globalization;
using Avalonia.Data.Converters;
using Avalonia.Media;

namespace TurismClient.Converters;

public sealed class SeatsColorConverter : IValueConverter
{
    public static readonly SeatsColorConverter Instance = new();

    private static readonly IBrush GreenBrush = new SolidColorBrush(Color.Parse("#1A7A3C"));
    private static readonly IBrush AmberBrush = new SolidColorBrush(Color.Parse("#B06A00"));
    private static readonly IBrush RedBrush   = new SolidColorBrush(Color.Parse("#C0392B"));

    public object Convert(object? value, Type targetType, object? parameter, CultureInfo culture)
    {
        if (value is int seats)
        {
            return seats switch
            {
                0     => RedBrush,
                <= 10 => AmberBrush,
                _     => GreenBrush
            };
        }
        return GreenBrush;
    }

    public object ConvertBack(object? value, Type targetType, object? parameter, CultureInfo culture)
        => throw new NotSupportedException();
}
