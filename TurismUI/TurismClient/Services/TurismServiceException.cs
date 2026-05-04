using System;

namespace TurismClient.Services;

public sealed class TurismServiceException : Exception
{
    public TurismServiceException(string message) : base(message) { }
    public TurismServiceException(string message, Exception inner) : base(message, inner) { }
}
