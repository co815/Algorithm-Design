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