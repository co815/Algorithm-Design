package rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.Agency;
import model.Reservation;
import model.Trip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TripSpringRepository tripRepo;
    @Autowired AgencySpringRepository agencyRepo;
    @Autowired ReservationSpringRepository reservationRepo;

    private Agency agency;
    private Trip trip;

    @BeforeEach
    void setUp() {
        reservationRepo.deleteAll();
        tripRepo.deleteAll();
        agencyRepo.deleteAll();

        agency = agencyRepo.save(new Agency(0, "Test Agency", "testuser", "testpass"));

        Trip t = new Trip();
        t.setTouristAttraction("London");
        t.setTransportCompany("Bus Co");
        t.setDepartureTime("2026-07-01 10:00");
        t.setPrice(100.0);
        t.setAvailableSeats(10);
        trip = tripRepo.save(t);
    }

    private Reservation savedReservation(String name, String phone, int tickets) {
        return reservationRepo.save(new Reservation(0, name, phone, tickets, trip, agency));
    }

    @Test
    void getReservations_empty_returnsOkEmptyArray() throws Exception {
        mockMvc.perform(get("/reservations").param("agencyId", String.valueOf(agency.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getReservations_returnsOnlyAgencyReservations() throws Exception {
        savedReservation("Ion Pop", "0722", 2);

        Agency other = agencyRepo.save(new Agency(0, "Other", "other", "pass"));
        reservationRepo.save(new Reservation(0, "Ana", "0711", 1, trip, other));

        mockMvc.perform(get("/reservations").param("agencyId", String.valueOf(agency.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].customerName").value("Ion Pop"));
    }

    @Test
    void updateReservation_changesFieldsAndAdjustsSeats() throws Exception {
        Reservation r = savedReservation("Ion Pop", "0722", 2);

        String body = "{\"customerName\":\"Ion Updated\",\"customerPhone\":\"0800\",\"numberOfTickets\":3}";

        mockMvc.perform(put("/reservations/{id}", r.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Ion Updated"))
                .andExpect(jsonPath("$.numberOfTickets").value(3));

        // trip starts at 10 seats; savedReservation() doesn't decrement seats;
        // PUT changes tickets 2->3 (delta=+1), so controller deducts 1 more -> 9
        Trip updated = tripRepo.findById(trip.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(9, updated.getAvailableSeats());
    }

    @Test
    void updateReservation_insufficientSeats_returns409() throws Exception {
        Reservation r = savedReservation("Ion", "0722", 2);

        String body = "{\"customerName\":\"Ion\",\"customerPhone\":\"0722\",\"numberOfTickets\":20}";

        mockMvc.perform(put("/reservations/{id}", r.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteReservation_removesItAndRestoresSeats() throws Exception {
        Reservation r = savedReservation("Ion", "0722", 3);
        trip.setAvailableSeats(7);
        tripRepo.save(trip);

        mockMvc.perform(delete("/reservations/{id}", r.getId()))
                .andExpect(status().isNoContent());

        Trip updated = tripRepo.findById(trip.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(10, updated.getAvailableSeats());
        org.junit.jupiter.api.Assertions.assertTrue(reservationRepo.findById(r.getId()).isEmpty());
    }

    @Test
    void updateReservation_notFound_returns404() throws Exception {
        String body = "{\"customerName\":\"X\",\"customerPhone\":\"0\",\"numberOfTickets\":1}";
        mockMvc.perform(put("/reservations/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReservation_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/reservations/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}
