using System.Collections.Generic;
using System.Linq;
using TurismClient.Models;

namespace TurismClient.Repositories;

public class AgencyInMemoryRepository : IAgencyRepository
{
    private readonly List<Agency> _agencies;

    public AgencyInMemoryRepository()
    {
        _agencies = new List<Agency>
        {
            new Agency(1, "Sunshine Travel", "sunshine", "secret123"),
            new Agency(2, "Ocean Waves", "oceanwaves", "pass456")
        };
    }

    public void Save(Agency agency)
    {
        _agencies.Add(agency);
    }

    public Agency? FindById(int id)
    {
        return _agencies.FirstOrDefault(a => a.Id == id);
    }

    public Agency? FindByUsername(string username)
    {
        return _agencies.FirstOrDefault(a => a.Username == username);
    }
}