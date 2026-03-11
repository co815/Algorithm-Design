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