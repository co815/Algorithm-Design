package rest;

import model.Agency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgencySpringRepository extends JpaRepository<Agency, Integer> {
    Optional<Agency> findByUsernameAndPassword(String username, String password);
}
