using TurismClient.Models;

namespace TurismClient.Repositories;

public interface IAgencyRepository
{
    void Save(Agency agency);
    Agency? FindById(int id);
    Agency? FindByUsername(string username);
}