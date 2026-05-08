package rest;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    @BeforeEach
    void setUp() {
        repo.deleteAll();
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
}
