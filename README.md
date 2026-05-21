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

03.05.2026
- Migrated Java backend persistence layer to Hibernate ORM:
  - Deleted `src/main/resources/db.properties` and `src/main/java/utils/JdbcUtils.java` in favor of `hibernate.cfg.xml` and `HibernateUtil`.
  - Deleted JDBC repository implementations: `AgencyDbRepository.java`, `TripDbRepository.java`, and `ReservationDbRepository.java`.
  - Replaced the deleted JDBC repositories with Hibernate equivalents (`AgencyHibernateRepository`, `TripHibernateRepository`, `ReservationHibernateRepository`).
  - Deleted `src/main/java/Main.java` as the entry point is now exclusively `ServerMain.java`.
  - Added `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, and `@ManyToOne` annotations to Java models (`Agency`, `Trip`, `Reservation`).
  - Added JUnit 5 tests for the new Hibernate repositories (`AgencyHibernateRepositoryTest`, `TripHibernateRepositoryTest`, `ReservationHibernateRepositoryTest`) using a separate in-memory database (`hibernate-test.cfg.xml` configuration).
  - Updated `DatabaseBootstrap` and `ServerMain` to initialize Hibernate `SessionFactory` on application startup.

- Updated Avalonia UI with Logout feature and User Information display:
  - Modified `MainWindow.axaml` to include a top bar displaying the currently logged-in agency's name and a "Deconectare" (Logout) button.
  - Updated `MainWindow.axaml.cs` to set the agency name on initialization and handle the logout action.
  - The logout action creates a new `LoginWindow`, sets it as the application's main window, shows it, and closes the current `MainWindow`, which properly disposes the server connection.

04.05.2026
- Finalized C# Avalonia UI integration with Protobuf socket client:
  - Deleted obsolete in-memory repositories (`AgencyInMemoryRepository`, `TripInMemoryRepository`, `ReservationInMemoryRepository`) and their interfaces (`IAgencyRepository`, `ITripRepository`, `IReservationRepository`).
  - Deleted obsolete `HeadlessSmokeClient.cs`.
  - Added `SeatsColorConverter.cs` in Avalonia UI to dynamically color trip seats based on availability.
  - Added `TurismServiceException.cs` for robust error handling across the service layer.
  - Refactored `App`, `Program`, `LoginWindow`, and `MainWindow` to fully rely on the `ProtobufSocketClient` and `TurismService` for all data and authentication logic.
  - Cleaned up models (`Agency`, `Reservation`, `Trip`) and `TurismService.cs` to align with the remote server architecture.

08.05.2026
- Added Spring Boot REST layer for Trip management:
  - Modified `pom.xml` to import Spring Boot BOM (`spring-boot-dependencies:3.2.5`) and added starters: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-test`.
  - Added `src/main/resources/application.properties` with SQLite datasource settings and `server.port=8080`.
  - Added `src/main/resources/application-test.properties` with isolated test DB (`target/test-rest.db`) and `create-drop` schema strategy.
  - Modified `src/main/java/model/Trip.java`:
    - made no-arg constructor public for JSON/JPA deserialization
    - added setters for `touristAttraction`, `transportCompany`, `departureTime`, and `price`
  - Added `src/main/java/rest/TripSpringRepository.java` (`JpaRepository<Trip, Integer>`).
  - Added `src/main/java/rest/RestMain.java` (`@SpringBootApplication` + `@EntityScan("model")`) as REST entry point.
  - Added `src/main/java/rest/TripRestController.java` with CRUD endpoints:
    - `GET /trips`
    - `GET /trips/{id}`
    - `POST /trips`
    - `PUT /trips/{id}`
    - `DELETE /trips/{id}`
- Added REST integration tests:
  - Added `src/test/java/rest/TripRestControllerTest.java` with 8 MockMvc integration tests covering all CRUD + not-found scenarios.
  - Tightened 404 assertions to distinguish entity-level 404 from missing-route/static-resource 404.
- Verified REST workflow end-to-end:
  - executed full CRUD flow on port 8080 (create → read → update → delete → 404 after delete).

14.05.2026
- Enabled SQLite WAL journal mode for concurrent access between socket server and REST server:
  - Modified `src/main/resources/hibernate.cfg.xml`: appended `?journal_mode=WAL` to the connection URL so the socket server opens the database in WAL mode.
  - Modified `src/main/resources/application.properties`: appended `?journal_mode=WAL` to the datasource URL so the REST server opens the same `turism.db` in WAL mode.
  - Both servers now share `turism.db` without blocking each other on reads.
  - Updated `.gitignore` to exclude `/turism.db-shm` and `/turism.db-wal` (WAL mode side-files created at runtime).

- Fixed `POST /trips` to prevent client-supplied IDs from conflicting with the database-assigned sequence:
  - Modified `src/main/java/rest/TripRestController.java`: refactored `createTrip()` to construct a fresh `Trip` object and copy only domain fields (`touristAttraction`, `transportCompany`, `departureTime`, `price`, `availableSeats`) from the request body; the `id` field is now always assigned by the database.

14.05.2026 (part 2)
- Added Angular 21 web client in `web-client/` with full CRUD for trips
- Added filter params to `GET /trips` (attraction, company, minSeats, maxPrice)
- Added SSE endpoint `GET /trips/updates` for real-time push notifications
- Added TripChangeEvent + TripNotifier for Spring event-driven SSE broadcasting
- Added CorsConfig allowing http://localhost:4200
- Expanded TripSpringRepository with 4 derived query methods
- Extended TripRestControllerTest with 9 new tests (4 repo + 4 filter + 1 SSE)

15.05.2026
- Added agency authentication: `POST /auth/login` validates username/password against `agencies` table, returns `{id, name}` or 401.
- Added trip booking: `POST /trips/{id}/book` with `{"seats": N}` decrements available seats or returns 409 if insufficient. Publishes SSE change event on success.
- Added Maven Angular build embedding: `mvn package` now runs `npm install` + `npm run build` in `web-client/`, then copies `dist/web-client/browser/` into Spring Boot static resources — Angular app served at `http://localhost:8080/` after `java -jar target/turism-1.0-SNAPSHOT.jar`.
- Added LoginComponent: sign-in form using agency credentials; session persisted in localStorage.
- Added AuthService: wraps login/logout with Angular signal for reactive template updates.
- Updated AppComponent: shows LoginComponent until authenticated, then TripListComponent with agency name + Logout button in header.
- Added booking UI to TripListComponent: 🎫 button per row expands inline seat selector; confirms via `POST /trips/{id}/book`.

17.05.2026
- Refactored trip editing in the Angular web client: replaced the out-of-band `TripFormComponent` panel with inline expanded-row editing (consistent with booking and reservation editing). ✏ button expands an inline row below the trip; + Add Trip button expands an add row at the top of the table. `TripFormComponent` deleted.
- Fixed Angular NG2008 build error (stale `.angular/cache`) caused by missing template file detection.
- Added full trip CRUD (Add / Edit / Delete) to the Avalonia desktop client via three new socket protocol messages (CreateTrip, UpdateTrip, DeleteTrip). A popup `EditTripWindow` dialog handles both add and edit modes. The server broadcasts `TripsUpdatedNotification` after each mutation so all connected clients refresh automatically.
- Extended `ITripRepository` with `update(Trip)` and `delete(int)` methods backed by Hibernate `merge`/`remove`.
- Added `createTrip`, `updateTrip`, `deleteTrip` to `TurismServerService` with input validation and 7 new tests (53 total, all passing).
