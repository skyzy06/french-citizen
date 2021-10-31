package net.atos.frenchcitizen.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.atos.frenchcitizen.config.TestConfig;
import net.atos.frenchcitizen.model.Citizen;
import net.atos.frenchcitizen.model.CitizenRequest;
import net.atos.frenchcitizen.repository.CitizenRepository;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional // add rollback after each test
@AutoConfigureMockMvc
@SpringBootTest(classes = TestConfig.class)
public class CitizenControllerTest {

    @Autowired
    private MockMvc restMockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CitizenRepository repository;

    @Test
    public void testCreateCitizenOk() throws Exception {
        CitizenRequest request = new CitizenRequest();
        request.setUsername("john");
        request.setPassword("Password1");
        request.setBirthdate(LocalDate.of(1992, 12, 12));
        request.setResidenceCountry("France");

        MvcResult result = restMockMvc.perform(post("/citizen")
                .content(objectMapper.writeValueAsString(request))
                .locale(Locale.FRANCE)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isCreated())
                .andExpect(redirectedUrlPattern("/citizen/*"))
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        assert location != null;
        Long id = Long.parseLong(location.replace("/citizen/", ""));
        Optional<Citizen> citizen = repository.findById(id);
        assertTrue(citizen.isPresent());
        assertEquals(request.getUsername(), citizen.get().getUsername());
    }

    @Test
    public void testCreateCitizenMissingField() throws Exception {
        CitizenRequest request = new CitizenRequest();
        request.setUsername("john");
        request.setPassword("Password1");
        request.setBirthdate(LocalDate.of(1992, 12, 12));

        restMockMvc.perform(post("/citizen")
                .content(objectMapper.writeValueAsString(request))
                .locale(Locale.FRANCE)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("residenceCountry"))
                .andExpect(jsonPath("$.detail").value("must not be null"))
                .andReturn();
    }

    @Test
    public void testCreateCitizenAlreadyExists() throws Exception {
        createCitizen();

        CitizenRequest request = new CitizenRequest();
        request.setUsername("johnny");
        request.setPassword("Password1");
        request.setBirthdate(LocalDate.of(1992, 12, 12));
        request.setResidenceCountry("France");

        restMockMvc.perform(post("/citizen")
                .content(objectMapper.writeValueAsString(request))
                .locale(Locale.FRANCE)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.field").value("username"))
                .andExpect(jsonPath("$.detail").value("already exists"))
                .andReturn();
    }

    @Test
    public void testCreateCitizenInvalidPassword() throws Exception {
        CitizenRequest request = new CitizenRequest();
        request.setUsername("johnny");
        request.setPassword("Password1!");
        request.setBirthdate(LocalDate.of(1992, 12, 12));
        request.setResidenceCountry("France");

        restMockMvc.perform(post("/citizen")
                .content(objectMapper.writeValueAsString(request))
                .locale(Locale.FRANCE)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("password"))
                .andExpect(jsonPath("$.detail").value("Minimum eight characters, at least one uppercase letter, one lowercase letter, one number and no special character"))
                .andReturn();
    }

    @Test
    public void testCreateCitizenNoBody() throws Exception {
        restMockMvc.perform(post("/citizen")
                .locale(Locale.FRANCE)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("body"))
                .andExpect(jsonPath("$.detail").value("must not be null"))
                .andReturn();
    }

    @Test
    public void testUpdateCitizenOk() throws Exception {
        Citizen citizen = createCitizen();

        CitizenRequest request = new CitizenRequest();
        request.setUsername("johnny");
        request.setPassword("Password1");
        request.setFirstname("John");
        request.setLastname("Wick");
        request.setBirthdate(LocalDate.of(1992, 12, 12));
        request.setResidenceCountry("France");

        restMockMvc.perform(post("/citizen/" + citizen.getId())
                .content(objectMapper.writeValueAsString(request))
                .locale(Locale.FRANCE)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isNoContent())
                .andReturn();

        citizen = repository.findById(citizen.getId()).get();
        assertEquals(request.getFirstname(), citizen.getFirstname());
        assertEquals(request.getLastname(), citizen.getLastname());
    }

    @Test
    public void testUpdateCitizenCannotDeleteMandatoryField() throws Exception {
        Citizen citizen = createCitizen();

        CitizenRequest request = new CitizenRequest();
        request.setUsername(null);
        request.setPassword("Password1");
        request.setBirthdate(LocalDate.of(1992, 12, 12));
        request.setResidenceCountry("France");

        restMockMvc.perform(post("/citizen/" + citizen.getId())
                .content(objectMapper.writeValueAsString(request))
                .locale(Locale.FRANCE)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("username"))
                .andExpect(jsonPath("$.detail").value("must not be empty"))
                .andReturn();

        citizen = repository.findById(citizen.getId()).get();
        assertNotNull(citizen.getUsername());
    }

    @Test
    public void testUpdateCitizenErrorWhenUsernameAlreadyExists() throws Exception {
        Citizen citizen = createCitizen();

        Citizen citizenBis = new Citizen();
        citizenBis.setUsername("johnnyBis");
        citizenBis.setPassword("Password2");
        citizenBis.setBirthdate(LocalDate.of(1985, 12, 11));
        citizenBis.setResidenceCountry("France");
        repository.save(citizenBis);

        CitizenRequest request = new CitizenRequest();
        request.setUsername("johnnyBis");
        request.setPassword("Password1");
        request.setBirthdate(LocalDate.of(1992, 12, 12));
        request.setResidenceCountry("France");

        restMockMvc.perform(post("/citizen/" + citizen.getId())
                .content(objectMapper.writeValueAsString(request))
                .locale(Locale.FRANCE)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.field").value("username"))
                .andExpect(jsonPath("$.detail").value("already exists"))
                .andReturn();

        citizen = repository.findById(citizen.getId()).get();
        assertNotNull(citizen.getUsername());
    }

    @Test
    public void testUpdateCitizenNoBody() throws Exception {
        Citizen citizen = createCitizen();

        restMockMvc.perform(post("/citizen/" + citizen.getId())
                .locale(Locale.FRANCE)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("body"))
                .andExpect(jsonPath("$.detail").value("must not be null"))
                .andReturn();
    }

    @Test
    public void testUpdateCitizenInvalidParameter() throws Exception {
        CitizenRequest request = new CitizenRequest();
        request.setUsername("johnny");
        request.setPassword("Password1");
        request.setFirstname("John");
        request.setLastname("Wick");
        request.setBirthdate(LocalDate.of(1992, 12, 12));
        request.setResidenceCountry("France");

        restMockMvc.perform(post("/citizen/random")
                .content(objectMapper.writeValueAsString(request))
                .locale(Locale.FRANCE)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("id"))
                .andExpect(jsonPath("$.detail").value("random is not a valid value"))
                .andReturn();
    }

    @Test
    public void tetsGetCitizenOk() throws Exception {
        Citizen citizen = createCitizen();

        restMockMvc.perform(get("/citizen/" + citizen.getId())
                .locale(Locale.FRANCE)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(citizen.getId()))
                .andExpect(jsonPath("$.username").value(citizen.getUsername()))
                .andExpect(jsonPath("$.residenceCountry").value(citizen.getResidenceCountry()))
                .andReturn();
    }

    @Test
    public void testGetCitizenNoExists() throws Exception {
        restMockMvc.perform(get("/citizen/0")
                .locale(Locale.FRANCE)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.field").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.detail").value("No citizen founded"))
                .andReturn();
    }

    @Test
    public void testGetCitizenInvalidParameter() throws Exception {
        restMockMvc.perform(get("/citizen/a")
                .locale(Locale.FRANCE)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("id"))
                .andExpect(jsonPath("$.detail").value("a is not a valid value"))
                .andReturn();
    }

    @Test
    public void testDeleteCitizenOk() throws Exception {
        Citizen citizen = createCitizen();

        restMockMvc.perform(delete("/citizen/" + citizen.getId())
                .locale(Locale.FRANCE)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isNoContent())
                .andReturn();

        assertTrue(repository.findById(citizen.getId()).isEmpty());
    }

    @Test
    public void testDeleteCitizenNotFound() throws Exception {
        restMockMvc.perform(delete("/citizen/0")
                .locale(Locale.FRANCE)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.field").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.detail").value("This citizen does not exist"))
                .andReturn();
    }

    @Test
    public void testDeleteCitizenInvalidParameter() throws Exception {
        restMockMvc.perform(delete("/citizen/tony")
                .locale(Locale.FRANCE)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("id"))
                .andExpect(jsonPath("$.detail").value("tony is not a valid value"))
                .andReturn();
    }

    private Citizen createCitizen() {
        Citizen citizen = new Citizen();
        citizen.setUsername("johnny");
        citizen.setPassword("Password2");
        citizen.setBirthdate(LocalDate.of(1985, 12, 11));
        citizen.setResidenceCountry("France");
        return repository.save(citizen);
    }
}
