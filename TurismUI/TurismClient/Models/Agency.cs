namespace TurismClient.Models;

public sealed class Agency
{
    public int    Id       { get; }
    public string Name     { get; }
    public string Username { get; }

    public Agency(int id, string name, string username)
    {
        Id       = id;
        Name     = name;
        Username = username;
    }

    public override string ToString() => $"Agency {{ Id={Id}, Name={Name} }}";
}
