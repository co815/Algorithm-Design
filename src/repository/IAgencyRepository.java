package repository;

import model.Agency;

public interface IAgencyRepository {
    void save(Agency agency);
    Agency findById(int id);
    Agency findByUsername(String username);
}