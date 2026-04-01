namespace TurismClient.Models;

public class Agency
{
    public int Id { get; }
    public string Name { get; }
    public string Username { get; }
    public string Password { get; }
    
    public Agency(int id, string name, string username, string password)
    {
        Id = id;
        Name = name;
        Username = username;
        Password = password;
    }

    public override string ToString()
    {
        return $"Agency{{id={Id}, name={Name}}}";
    }
}