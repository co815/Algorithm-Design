package server;

import repository.AgencyHibernateRepository;
import repository.ReservationHibernateRepository;
import repository.TripHibernateRepository;
import service.TurismServerService;
import utils.HibernateUtil;

public class ServerMain {

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 55556;

        DatabaseBootstrap.seed();

        TurismServerService service = new TurismServerService(
                new AgencyHibernateRepository(),
                new TripHibernateRepository(),
                new ReservationHibernateRepository()
        );

        Runtime.getRuntime().addShutdownHook(new Thread(HibernateUtil::shutdown));

        TurismSocketServer server = new TurismSocketServer(port, service);
        System.out.println("Turism protobuf socket server started on port " + port);
        server.start();
    }
}
