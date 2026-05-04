using System;
using System.Collections.Concurrent;
using System.IO;
using System.Net.Sockets;
using System.Threading;
using System.Threading.Tasks;
using Google.Protobuf;
using TurismClient.Protocol;

namespace TurismClient.Services;

public sealed class ProtobufSocketClient : IDisposable
{
    private readonly TcpClient    _tcpClient;
    private readonly NetworkStream _stream;
    private readonly object        _writeLock        = new();
    private readonly ConcurrentDictionary<string, TaskCompletionSource<ResponseEnvelope>>
                                   _pending          = new();
    private readonly CancellationTokenSource _cts    = new();
    private readonly Task          _readerTask;

    private const int RequestTimeoutSeconds = 10;
    
    public event Action? TripsUpdated;

    public ProtobufSocketClient(string host, int port)
    {
        _tcpClient = new TcpClient();
        _tcpClient.Connect(host, port);
        _stream    = _tcpClient.GetStream();
        _readerTask = Task.Run(ReadLoopAsync, _cts.Token);
    }
    
    public async Task<ResponseEnvelope> SendRequestAsync(RequestEnvelope request)
    {
        var requestId = Guid.NewGuid().ToString("N");
        var envelope  = request.Clone();
        envelope.RequestId = requestId;

        var tcs = new TaskCompletionSource<ResponseEnvelope>(
            TaskCreationOptions.RunContinuationsAsynchronously);

        if (!_pending.TryAdd(requestId, tcs))
            throw new InvalidOperationException("Duplicate request-id generated — this should never happen.");

        try
        {
            lock (_writeLock)
            {
                envelope.WriteDelimitedTo(_stream);
                _stream.Flush();
            }

            using var timeout = new CancellationTokenSource(
                TimeSpan.FromSeconds(RequestTimeoutSeconds));
            using var _ = timeout.Token.Register(
                () => tcs.TrySetException(
                    new TimeoutException($"Server did not respond within {RequestTimeoutSeconds} s.")));

            return await tcs.Task.ConfigureAwait(false);
        }
        finally
        {
            _pending.TryRemove(requestId, out _);
        }
    }

    private async Task ReadLoopAsync()
    {
        try
        {
            while (!_cts.IsCancellationRequested)
            {
                var serverEnvelope = await Task.Run(
                    () => ServerEnvelope.Parser.ParseDelimitedFrom(_stream),
                    _cts.Token).ConfigureAwait(false);

                if (serverEnvelope is null) break;

                switch (serverEnvelope.PayloadCase)
                {
                    case ServerEnvelope.PayloadOneofCase.Response:
                        if (_pending.TryGetValue(serverEnvelope.Response.RequestId, out var tcs))
                            tcs.TrySetResult(serverEnvelope.Response);
                        break;

                    case ServerEnvelope.PayloadOneofCase.TripsUpdatedNotification:
                        TripsUpdated?.Invoke();
                        break;
                }
            }
        }
        catch (Exception ex) when (ex is InvalidProtocolBufferException
                                       or IOException
                                       or ObjectDisposedException
                                       or OperationCanceledException)
        {
            FailAllPending(ex);
        }
    }

    private void FailAllPending(Exception ex)
    {
        foreach (var tcs in _pending.Values)
            tcs.TrySetException(ex);
    }

    public void Dispose()
    {
        _cts.Cancel();
        _stream.Dispose();
        _tcpClient.Dispose();
        _cts.Dispose();
    }
}
