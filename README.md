26.02.2026
- added src/HelloWorld.java file 

12.03.2026
- added src/model/Trip.java
    - fields: id, touristAttraction, transportCompany, departureTime, price, availableSeats
    - id, touristAttraction, transportCompany, departureTime, price are final
    - availableSeats has a setter

- added src/model/Reservation.java
    - fields: id, customerName, customerPhone, numberOfTickets, trip, agency
    - all fields are final
    - references Trip and Agency objects directly

- added src/model/Agency.java
    - fields: id, name, username, password
    - all fields are final
    - username and password are used for login

- added src/repository/ITripRepository.java
    - save(Trip trip)
    - findById(int id)
    - findAll()
    - findByAttractionAndDepartureInterval(String attraction, String startTime, String endTime)
    - updateAvailableSeats(int tripId, int newAvailableSeats)

- added src/repository/IReservationRepository.java
    - save(Reservation reservation)
    - findById(int id)
    - findAll()
    - findByTrip(int tripId)

- added src/repository/IAgencyRepository.java
    - save(Agency agency)
    - findById(int id)
    - findByUsername(String username)

18.03.2026
- modified pom.xml
    - added SQLite JDBC driver dependency
    - added Log4j2 core and API dependencies
    - configured Maven Compiler Plugin for Java 17

- added src/main/resources/db.properties
    - holds database connection settings (driver, URL)

- added src/main/resources/log4j2.xml
    - configures Log4j2 logging (Console and File appenders, log levels)

- added src/main/resources/schema.sql
    - SQL schema for creating Agency, Trip, and Reservation tables

- added src/main/java/utils/JdbcUtils.java
    - manages JDBC connection lifecycle using db.properties
    - provides getConnection() and closeConnection() helpers

- added src/main/java/model/Agency.java
    - fields: id, name, username, password
    - all fields are final; username and password used for login

- added src/main/java/model/Trip.java
    - fields: id, touristAttraction, transportCompany, departureTime, price, availableSeats
    - id, touristAttraction, transportCompany, departureTime, price are final
    - availableSeats has a setter

- added src/main/java/model/Reservation.java
    - fields: id, customerName, customerPhone, numberOfTickets, trip, agency
    - all fields are final; references Trip and Agency objects directly

- added src/main/java/repository/IAgencyRepository.java
    - save(Agency agency), findById(int id), findByUsername(String username)

- added src/main/java/repository/ITripRepository.java
    - save(Trip trip), findById(int id), findAll()
    - findByAttractionAndDepartureInterval(String attraction, String startTime, String endTime)
    - updateAvailableSeats(int tripId, int newAvailableSeats)

- added src/main/java/repository/IReservationRepository.java
    - save(Reservation reservation), findById(int id), findAll(), findByTrip(int tripId)

- added src/main/java/repository/AgencyDbRepository.java
    - implements IAgencyRepository using JDBC and PreparedStatement
    - uses JdbcUtils for connection management; logs operations via Log4j2

- added src/main/java/repository/TripDbRepository.java
    - implements ITripRepository using JDBC and PreparedStatement
    - uses JdbcUtils for connection management; logs operations via Log4j2

- added src/main/java/repository/ReservationDbRepository.java
    - implements IReservationRepository using JDBC and PreparedStatement
    - uses JdbcUtils for connection management; logs operations via Log4j2

01.04.2026
- Initialized TurismClient project in C# using Avalonia UI:
  - Configured .gitignore to ignore bin/, obj/, and .idea/ folders.
  - Structured the project into layers: Models, Repositories, Services, and UI.

- Added C# models (Models folder):
  - Trip.cs: properties for trips (Id, Tourist Attraction, Transport Company, Departure Time, Price, Available Seats).
  - Agency.cs: agency data for authentication (Id, Name, Username, Password).
  - Reservation.cs: reservation details with references to Trip and Agency objects.

- Added interfaces and in-memory repositories (Repositories folder):
  - ITripRepository, IAgencyRepository, IReservationRepository: defined core CRUD operations. 
  - TripInMemoryRepository, AgencyInMemoryRepository, ReservationInMemoryRepository: implementations using internal lists (List<T>) populated with dummy data for testing purposes.

- Added business logic (Services folder):
  - TurismService.cs: a Facade-type class managing authentication, trip searching, and the reservation process (seat verification, inventory deduction, and saving).

 - Implemented Graphical User Interface (UI):
   - MainWindow.axaml: main window design using XAML and Data Binding to display the list of trips. 
   - MainWindow.axaml.cs: the window controller interacting with TurismService for loading and preparing data for display.

23.04.2026
- Added Protocol Buffers contract:
  - `src/main/proto/turism.proto` with request/response envelopes and `TripsUpdatedNotification` push event.
  - Generated Java classes through Maven protobuf plugin and C# classes through `Grpc.Tools`.

- Implemented socket networking on server (Java):
  - Added `server.ServerMain` startup entry point.
  - Added threaded TCP server (`TurismSocketServer`) that serves multiple clients simultaneously (one thread per client).
  - Added per-client request handler (`ClientHandler`) using protobuf delimited messages.
  - Added notification broadcast to all connected clients when a reservation changes seats.
  - Added server-side service layer (`service.TurismServerService`) for login, listing/searching trips, and booking with synchronized seat update flow.
  - Added DB bootstrap (`server.DatabaseBootstrap`) for schema init + seed data.

- Implemented socket networking on client (C#):
  - Replaced in-memory access path in `TurismService` with `ProtobufSocketClient` (TCP + protobuf).
  - Added background read loop for server push notifications (`TripsUpdated` event).
  - Added request/response correlation by `request_id`.

- Updated Avalonia UI for live updates and authentication:
  - Added `LoginWindow.axaml` and `LoginWindow.axaml.cs` for agency authentication before accessing the main application.
  - Configured `App.axaml.cs` to set `LoginWindow` as the startup window.
  - Refactored `MainWindow` constructor to receive the authenticated `TurismService` and `Agency` instance instead of performing a hardcoded login.
  - MainWindow now connects to server (`TURISM_SERVER_HOST` / `TURISM_SERVER_PORT`, defaults `127.0.0.1:55556`).
  - Added booking form (customer, phone, tickets) and reserve button.
  - Added automatic trip list refresh for server notifications.