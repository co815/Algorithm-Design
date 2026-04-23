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
    private readonly TcpClient _tcpClient;
    private readonly NetworkStream _stream;
    private readonly object _writeLock = new();
    private readonly ConcurrentDictionary<string, TaskCompletionSource<ResponseEnvelope>> _pendingResponses = new();
    private readonly CancellationTokenSource _cts = new();
    private readonly Task _readerTask;

    public event Action? TripsUpdated;

    public ProtobufSocketClient(string host, int port)
    {
        _tcpClient = new TcpClient();
        _tcpClient.Connect(host, port);
        _stream = _tcpClient.GetStream();
        _readerTask = Task.Run(ReadLoop, _cts.Token);
    }

    public ResponseEnvelope SendRequest(RequestEnvelope requestBody)
    {
        var requestId = Guid.NewGuid().ToString("N");
        var request = requestBody.Clone();
        request.RequestId = requestId;

        var tcs = new TaskCompletionSource<ResponseEnvelope>(TaskCreationOptions.RunContinuationsAsynchronously);
        if (!_pendingResponses.TryAdd(requestId, tcs))
        {
            throw new IOException("Duplicate request id generated.");
        }

        try
        {
            lock (_writeLock)
            {
                request.WriteDelimitedTo(_stream);
                _stream.Flush();
            }

            using var timeoutCts = new CancellationTokenSource(TimeSpan.FromSeconds(10));
            using var registration = timeoutCts.Token.Register(() =>
                tcs.TrySetException(new TimeoutException("Timed out waiting for server response.")));

            return tcs.Task.GetAwaiter().GetResult();
        }
        finally
        {
            _pendingResponses.TryRemove(requestId, out _);
        }
    }

    private void ReadLoop()
    {
        try
        {
            while (!_cts.IsCancellationRequested)
            {
                var serverEnvelope = ServerEnvelope.Parser.ParseDelimitedFrom(_stream);
                if (serverEnvelope == null)
                {
                    break;
                }

                switch (serverEnvelope.PayloadCase)
                {
                    case ServerEnvelope.PayloadOneofCase.Response:
                    {
                        var response = serverEnvelope.Response;
                        if (_pendingResponses.TryGetValue(response.RequestId, out var tcs))
                        {
                            tcs.TrySetResult(response);
                        }
                        break;
                    }
                    case ServerEnvelope.PayloadOneofCase.TripsUpdatedNotification:
                        TripsUpdated?.Invoke();
                        break;
                    case ServerEnvelope.PayloadOneofCase.None:
                        break;
                }
            }
        }
        catch (InvalidProtocolBufferException ex)
        {
            FailPendingRequests(ex);
        }
        catch (IOException ex)
        {
            FailPendingRequests(ex);
        }
        catch (ObjectDisposedException ex)
        {
            FailPendingRequests(ex);
        }
    }

    public void Dispose()
    {
        _cts.Cancel();
        _stream.Dispose();
        _tcpClient.Dispose();
        _cts.Dispose();
    }

    private void FailPendingRequests(Exception ex)
    {
        foreach (var pending in _pendingResponses.Values)
        {
            pending.TrySetException(ex);
        }
    }
}
