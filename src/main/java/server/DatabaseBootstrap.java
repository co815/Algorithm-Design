package server;

import model.Agency;
import model.Trip;
import repository.AgencyHibernateRepository;
import repository.IAgencyRepository;
import repository.ITripRepository;
import repository.TripHibernateRepository;

public final class DatabaseBootstrap {

    private DatabaseBootstrap() {}

    public static void seed() {
        IAgencyRepository agencyRepo = new AgencyHibernateRepository();
        ITripRepository tripRepo = new TripHibernateRepository();

        if (agencyRepo.findByUsername("sunshine") == null) {
            agencyRepo.save(new Agency(0, "Sunshine Travel", "sunshine", "secret123"));
        }
        if (agencyRepo.findByUsername("oceanwaves") == null) {
            agencyRepo.save(new Agency(0, "Ocean Waves", "oceanwaves", "pass456"));
        }

        if (tripRepo.findAll().isEmpty()) {
            tripRepo.save(new Trip(0, "Paris", "Air France", "2024-06-10 08:00", 1200.0, 50));
            tripRepo.save(new Trip(0, "Rome", "RyanAir", "2024-06-15 12:00", 800.0, 30));
            tripRepo.save(new Trip(0, "Paris", "EasyJet", "2024-06-20 09:00", 950.0, 40));
        }
    }
}
