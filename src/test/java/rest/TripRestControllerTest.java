package rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.Agency;
import model.Trip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TripRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TripSpringRepository repo;

    @Autowired
    private AgencySpringRepository agencyRepo;

    @Autowired
    private ReservationSpringRepository reservationRepo;

    private Agency testAgency;

    @BeforeEach
    void setUp() {
        reservationRepo.deleteAll();
        repo.deleteAll();
        agencyRepo.deleteAll();
        testAgency = agencyRepo.save(new Agency(0, "Test Agency", "testuser", "testpass"));
    }

    private Trip sample(String attraction) {
        Trip trip = new Trip();
        trip.setTouristAttraction(attraction);
        trip.setTransportCompany("Test Transport");
        trip.setDepartureTime("2025-06-01 10:00");
        trip.setPrice(120.0);
        trip.setAvailableSeats(25);
        return trip;
    }

    @Test
    void getAllTrips_empty_returnsOkEmptyArray() throws Exception {
        mockMvc.perform(get("/trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createTrip_returnsCreatedWithServerId() throws Exception {
        mockMvc.perform(post("/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample("Paris"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(greaterThan(0)));
    }

    @Test
    void getTripById_exists_returnsOk() throws Exception {
        Trip saved = repo.save(sample("Rome"));

        mockMvc.perform(get("/trips/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.touristAttraction").value("Rome"));
    }

    @Test
    void getTripById_notExists_returns404() throws Exception {
        mockMvc.perform(get("/trips/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    assertFalse(result.getHandler() instanceof ResourceHttpRequestHandler,
                            "Expected /trips/{id} to be mapped to a controller handler, not static resource handler");
                    assertFalse(result.getResolvedException() instanceof NoResourceFoundException,
                            "Expected entity-level 404, not route-level 404");
                });
    }

    @Test
    void updateTrip_exists_returnsOkWithNewPrice() throws Exception {
        Trip saved = repo.save(sample("Madrid"));
        Trip updated = sample("Madrid");
        updated.setPrice(245.5);

        mockMvc.perform(put("/trips/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(245.5));
    }

    @Test
    void updateTrip_notExists_returns404() throws Exception {
        mockMvc.perform(put("/trips/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample("Berlin"))))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    assertFalse(result.getHandler() instanceof ResourceHttpRequestHandler,
                            "Expected /trips/{id} to be mapped to a controller handler, not static resource handler");
                    assertFalse(result.getResolvedException() instanceof NoResourceFoundException,
                            "Expected entity-level 404, not route-level 404");
                });
    }

    @Test
    void deleteTrip_exists_returns204_thenGetReturns404() throws Exception {
        Trip saved = repo.save(sample("Lisbon"));

        mockMvc.perform(delete("/trips/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/trips/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTrip_notExists_returns404() throws Exception {
        mockMvc.perform(delete("/trips/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    assertFalse(result.getHandler() instanceof ResourceHttpRequestHandler,
                            "Expected /trips/{id} to be mapped to a controller handler, not static resource handler");
                    assertFalse(result.getResolvedException() instanceof NoResourceFoundException,
                            "Expected entity-level 404, not route-level 404");
                });
    }

    @Test
    void repo_findByTouristAttraction_returnsOnlyMatching() {
        repo.save(sample("Paris"));
        repo.save(sample("Rome"));

        List<Trip> result = repo.findByTouristAttraction("Paris");

        assertEquals(1, result.size());
        assertEquals("Paris", result.get(0).getTouristAttraction());
    }

    @Test
    void repo_findByTransportCompany_returnsOnlyMatching() {
        Trip t = sample("Kyoto");
        t.setTransportCompany("AirFrance");
        repo.save(t);
        repo.save(sample("Bali")); // "Test Transport"

        List<Trip> result = repo.findByTransportCompany("AirFrance");

        assertEquals(1, result.size());
        assertEquals("AirFrance", result.get(0).getTransportCompany());
    }

    @Test
    void repo_findByAvailableSeatsGreaterThan_returnsTripsAboveThreshold() {
        Trip low = sample("Oslo");
        low.setAvailableSeats(5);
        repo.save(low);
        Trip high = sample("Dublin");
        high.setAvailableSeats(50);
        repo.save(high);

        List<Trip> result = repo.findByAvailableSeatsGreaterThan(10);

        assertEquals(1, result.size());
        assertEquals("Dublin", result.get(0).getTouristAttraction());
    }

    @Test
    void repo_findByPriceLessThanEqual_returnsCheapTrips() {
        Trip cheap = sample("Prague");
        cheap.setPrice(50.0);
        repo.save(cheap);
        Trip expensive = sample("Tokyo");
        expensive.setPrice(500.0);
        repo.save(expensive);

        List<Trip> result = repo.findByPriceLessThanEqual(100.0);

        assertEquals(1, result.size());
        assertEquals("Prague", result.get(0).getTouristAttraction());
    }

    @Test
    void getAllTrips_filterByAttraction_returnsOnlyMatching() throws Exception {
        repo.save(sample("Paris"));
        repo.save(sample("Rome"));

        mockMvc.perform(get("/trips").param("attraction", "Paris"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].touristAttraction").value("Paris"));
    }

    @Test
    void getAllTrips_filterByCompany_returnsOnlyMatching() throws Exception {
        Trip t = sample("Kyoto");
        t.setTransportCompany("AirFrance");
        repo.save(t);
        repo.save(sample("Bali")); // company = "Test Transport"

        mockMvc.perform(get("/trips").param("company", "AirFrance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].touristAttraction").value("Kyoto"));
    }

    @Test
    void getAllTrips_filterByMinSeats_returnsTripsAboveThreshold() throws Exception {
        Trip low = sample("Oslo");
        low.setAvailableSeats(5);
        repo.save(low);
        Trip high = sample("Dublin");
        high.setAvailableSeats(50);
        repo.save(high);

        mockMvc.perform(get("/trips").param("minSeats", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].touristAttraction").value("Dublin"));
    }

    @Test
    void getAllTrips_filterByMaxPrice_returnsCheapTrips() throws Exception {
        Trip cheap = sample("Prague");
        cheap.setPrice(50.0);
        repo.save(cheap);
        Trip expensive = sample("Tokyo");
        expensive.setPrice(500.0);
        repo.save(expensive);

        mockMvc.perform(get("/trips").param("maxPrice", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].touristAttraction").value("Prague"));
    }

    @Test
    void getUpdates_returnsEventStream() throws Exception {
        mockMvc.perform(get("/trips/updates").accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted());
    }

    @Test
    void bookTrip_reducesAvailableSeats_andCreatesReservation() throws Exception {
        Trip trip = sample("Paris");
        trip.setAvailableSeats(10);
        Trip saved = repo.save(trip);

        String body = String.format(
                "{\"agencyId\":%d,\"customerName\":\"Ion Pop\",\"customerPhone\":\"0722000000\",\"seats\":3}",
                testAgency.getId());

        mockMvc.perform(post("/trips/{id}/book", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Ion Pop"))
                .andExpect(jsonPath("$.numberOfTickets").value(3))
                .andExpect(jsonPath("$.tripAttraction").value("Paris"));

        Trip updated = repo.findById(saved.getId()).orElseThrow();
        assertEquals(7, updated.getAvailableSeats());
    }

    @Test
    void bookTrip_insufficientSeats_returns409() throws Exception {
        Trip trip = sample("Rome");
        trip.setAvailableSeats(1);
        Trip saved = repo.save(trip);

        String body = String.format(
                "{\"agencyId\":%d,\"customerName\":\"Ana\",\"customerPhone\":\"0711\",\"seats\":5}",
                testAgency.getId());

        mockMvc.perform(post("/trips/{id}/book", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }
}
