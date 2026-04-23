using System;
using System.Linq;
using System.Threading;
using TurismClient.Models;

namespace TurismClient.Services;

public static class HeadlessSmokeClient
{
    public static void Run(string[] args)
    {
        var name = GetArgValue(args, "--name", "client");
        var host = GetArgValue(args, "--host", "127.0.0.1");
        var port = int.Parse(GetArgValue(args, "--port", "55556"));
        var waitSeconds = int.Parse(GetArgValue(args, "--wait-seconds", "8"));
        var username = GetArgValue(args, "--username", "sunshine");
        var password = GetArgValue(args, "--password", "secret123");
        var shouldBook = args.Contains("--book");

        using var service = new TurismService(host, port);
        var updatedEvent = new ManualResetEventSlim(false);
        service.TripsUpdated += () =>
        {
            Console.WriteLine($"[{name}] Notification received: trips updated.");
            updatedEvent.Set();
        };

        Agency agency = service.Login(username, password)
                        ?? throw new InvalidOperationException("Login failed in smoke client.");

        var initialTrip1 = service.GetAllTrips().First(t => t.Id == 1);
        Console.WriteLine($"[{name}] Initial trip 1 seats: {initialTrip1.AvailableSeats}");

        if (shouldBook)
        {
            service.BookTrip(1, agency, $"{name}-customer", "0700000000", 1);
            Console.WriteLine($"[{name}] Booked 1 seat on trip 1.");
        }

        updatedEvent.Wait(TimeSpan.FromSeconds(waitSeconds));
        var finalTrip1 = service.GetAllTrips().First(t => t.Id == 1);
        Console.WriteLine($"[{name}] Final trip 1 seats: {finalTrip1.AvailableSeats}");
    }

    private static string GetArgValue(string[] args, string key, string defaultValue)
    {
        var index = Array.IndexOf(args, key);
        if (index >= 0 && index + 1 < args.Length)
        {
            return args[index + 1];
        }
        return defaultValue;
    }
}
