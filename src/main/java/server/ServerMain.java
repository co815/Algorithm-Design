package server;

import repository.AgencyDbRepository;
import repository.ReservationDbRepository;
import repository.TripDbRepository;
import service.TurismServerService;
import utils.JdbcUtils;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 55556;

        JdbcUtils dbUtils = JdbcUtils.fromClasspath();
        DatabaseBootstrap.initialize(dbUtils);

        TurismServerService service = new TurismServerService(
                new AgencyDbRepository(dbUtils),
                new TripDbRepository(dbUtils),
                new ReservationDbRepository(dbUtils)
        );

        TurismSocketServer server = new TurismSocketServer(port, service);
        System.out.println("Turism protobuf socket server started on port " + port);
        server.start();
    }
}
