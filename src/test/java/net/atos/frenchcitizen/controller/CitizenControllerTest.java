package net.atos.frenchcitizen.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.atos.frenchcitizen.helper.PasswordHelper;
import net.atos.frenchcitizen.helper.TokenHelper;
import net.atos.frenchcitizen.mapper.CitizenMapper;
import net.atos.frenchcitizen.model.*;
import net.atos.frenchcitizen.repository.CitizenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class CitizenControllerTest {

    @Autowired
    private MockMvc restMockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CitizenMapper citizenMapper;

    @Autowired
    private CitizenRepository repository;

    @Autowired
    private TokenHelper tokenHelper;

    @Autowired
    private PasswordHelper passwordHelper;

    @Value("${token.encryption.secret}")
    private String tokenSecret;

    @Test
    public void testCreateCitizenOk() throws Exception {
        CitizenCreationRequest request = new CitizenCreationRequest();
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
        CitizenCreationRequest request = new CitizenCreationRequest();
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
        Citizen citizen = createCitizen();

        CitizenCreationRequest request = toCitizenCreationRequest(citizen);

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
        CitizenCreationRequest request = new CitizenCreationRequest();
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
    public void testCreateCitizenNoFrench() throws Exception {
        CitizenCreationRequest request = new CitizenCreationRequest();
        request.setUsername("johnny");
        request.setPassword("Password1");
        request.setBirthdate(LocalDate.now().minusYears(21));
        request.setResidenceCountry("German");

        restMockMvc.perform(post("/citizen")
                        .content(objectMapper.writeValueAsString(request))
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.field").value("residenceCountry"))
                .andExpect(jsonPath("$.detail").value("Only french citizens can register"))
                .andReturn();
    }

    @Test
    public void testCreateCitizenMinorCitizen() throws Exception {
        CitizenCreationRequest request = new CitizenCreationRequest();
        request.setUsername("johnny");
        request.setPassword("Password1");
        request.setBirthdate(LocalDate.now().minusYears(15));
        request.setResidenceCountry("France");

        restMockMvc.perform(post("/citizen")
                        .content(objectMapper.writeValueAsString(request))
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.field").value("birthdate"))
                .andExpect(jsonPath("$.detail").value("Only adults can register"))
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
    public void testRequestTokenOk() throws Exception {
        Citizen citizen = createCitizen();

        CitizenTokenRequest request = new CitizenTokenRequest();
        String decodedPassword = passwordHelper.decrypt(citizen.getPassword());
        request.setUsername(citizen.getUsername());
        request.setPassword(decodedPassword);

        MvcResult result = restMockMvc.perform(post("/citizen/token")
                        .content(objectMapper.writeValueAsString(request))
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String bodyResponse = result.getResponse().getContentAsString();
        DecodedJWT jwt = tokenHelper.decode(bodyResponse);
        assertEquals(citizen.getId().toString(), jwt.getSubject());
    }

    @Test
    public void testRequestTokenMissingField() throws Exception {
        Citizen citizen = createCitizen();

        CitizenTokenRequest request = new CitizenTokenRequest();
        String decodedPassword = passwordHelper.decrypt(citizen.getPassword());
        request.setPassword(decodedPassword);

        restMockMvc.perform(post("/citizen/token")
                        .content(objectMapper.writeValueAsString(request))
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("username"))
                .andExpect(jsonPath("$.detail").value("must not be empty"))
                .andReturn();
    }

    @Test
    public void testRequestTokenInvalidFormat() throws Exception {
        Citizen citizen = createCitizen();

        CitizenTokenRequest request = new CitizenTokenRequest();
        request.setUsername(citizen.getUsername());
        request.setPassword("Password2!");

        restMockMvc.perform(post("/citizen/token")
                        .content(objectMapper.writeValueAsString(request))
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("password"))
                .andExpect(jsonPath("$.detail").value("Minimum eight characters, at least one uppercase letter, one lowercase letter, one number and no special character"))
                .andReturn();
    }

    @Test
    public void testRequestTokenNoBody() throws Exception {
        restMockMvc.perform(post("/citizen/token")
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("body"))
                .andExpect(jsonPath("$.detail").value("must not be null"))
                .andReturn();
    }

    @Test
    public void testRequestTokenWrongCredential() throws Exception {
        Citizen citizen = createCitizen();

        CitizenTokenRequest request = new CitizenTokenRequest();
        request.setUsername(citizen.getUsername());
        request.setPassword("Password1");

        restMockMvc.perform(post("/citizen/token")
                        .content(objectMapper.writeValueAsString(request))
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").isEmpty())
                .andExpect(jsonPath("$.detail").value("wrong username or password"))
                .andReturn();

        request.setUsername(citizen.getUsername() + "Bad");
        String decodedPassword = passwordHelper.decrypt(citizen.getPassword());
        request.setPassword(decodedPassword);

        restMockMvc.perform(post("/citizen/token")
                        .content(objectMapper.writeValueAsString(request))
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").isEmpty())
                .andExpect(jsonPath("$.detail").value("wrong username or password"))
                .andReturn();
    }

    @Test
    public void testUpdateCitizenOk() throws Exception {
        Citizen citizen = createCitizen();

        CitizenUpdateRequest request = toCitizenUpdateRequest(citizen);
        request.setFirstname("John");
        request.setLastname("Wick");

        restMockMvc.perform(patch("/citizen/" + citizen.getId())
                        .header("Authorization", "Bearer " + tokenHelper.encode(citizen.getId()))
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

        CitizenUpdateRequest request = toCitizenUpdateRequest(citizen);
        request.setUsername(null);

        restMockMvc.perform(patch("/citizen/" + citizen.getId())
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

        CitizenUpdateRequest request = toCitizenUpdateRequest(citizen);
        request.setUsername(citizenBis.getUsername());

        restMockMvc.perform(patch("/citizen/" + citizen.getId())
                        .header("Authorization", "Bearer " + tokenHelper.encode(citizen.getId()))
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

        restMockMvc.perform(patch("/citizen/" + citizen.getId())
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("body"))
                .andExpect(jsonPath("$.detail").value("must not be null"))
                .andReturn();
    }

    @Test
    public void testUpdateCitizenInvalidParameter() throws Exception {
        CitizenUpdateRequest request = new CitizenUpdateRequest();
        request.setUsername("johnny");
        request.setFirstname("John");
        request.setLastname("Wick");

        restMockMvc.perform(patch("/citizen/random")
                        .content(objectMapper.writeValueAsString(request))
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("id"))
                .andExpect(jsonPath("$.detail").value("random is not a valid value"))
                .andReturn();
    }

    @Test
    public void testUpdateCitizenInvalidToken() throws Exception {
        CitizenUpdateRequest request = new CitizenUpdateRequest();
        request.setUsername("johnny");
        request.setFirstname("John");
        request.setLastname("Wick");

        restMockMvc.perform(patch("/citizen/1")
                        .header("Authorization", "Bearer " + tokenHelper.encode(2L))
                        .content(objectMapper.writeValueAsString(request))
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.field").isEmpty())
                .andExpect(jsonPath("$.detail").value("Unauthorized access"))
                .andReturn();
    }

    @Test
    public void testGetCitizenOk() throws Exception {
        Citizen citizen = createCitizen();

        restMockMvc.perform(get("/citizen/" + citizen.getId())
                        .header("Authorization", "Bearer " + tokenHelper.encode(citizen.getId()))
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.username").value(citizen.getUsername()))
                .andExpect(jsonPath("$.residenceCountry").value(citizen.getResidenceCountry()))
                .andReturn();
    }

    @Test
    public void testGetCitizenNoExists() throws Exception {
        restMockMvc.perform(get("/citizen/0")
                        .header("Authorization", "Bearer " + tokenHelper.encode(0L))
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.field").isEmpty())
                .andExpect(jsonPath("$.detail").value("This citizen does not exist"))
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
    public void testGetCitizenNoToken() throws Exception {
        restMockMvc.perform(get("/citizen/1")
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.field").isEmpty())
                .andExpect(jsonPath("$.detail").value("Invalid authorization header"))
                .andReturn();
    }

    @Test
    public void testDeleteCitizenOk() throws Exception {
        Citizen citizen = createCitizen();

        restMockMvc.perform(delete("/citizen/" + citizen.getId())
                        .header("Authorization", "Bearer " + tokenHelper.encode(citizen.getId()))
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isNoContent())
                .andReturn();

        assertTrue(repository.findById(citizen.getId()).isEmpty());
    }

    @Test
    public void testDeleteCitizenNotFound() throws Exception {
        restMockMvc.perform(delete("/citizen/0")
                        .header("Authorization", "Bearer " + tokenHelper.encode(0L))
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.field").isEmpty())
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

    @Test
    public void testDeleteCitizenInvalidToken() throws Exception {
        restMockMvc.perform(delete("/citizen/1")
                        .header("Authorization", "Bearer " + tokenHelper.encode(2L))
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.field").isEmpty())
                .andExpect(jsonPath("$.detail").value("Unauthorized access"))
                .andReturn();
    }

    @Test
    public void testUpdateCitizenPasswordOk() throws Exception {
        Citizen citizen = createCitizen();

        CitizenPasswordUpdateRequest request = new CitizenPasswordUpdateRequest();
        String decodedPassword = passwordHelper.decrypt(citizen.getPassword());
        request.setOldPassword(decodedPassword);
        request.setPassword("Password1");

        restMockMvc.perform(patch("/citizen/" + citizen.getId() + "/password")
                        .header("Authorization", "Bearer " + tokenHelper.encode(citizen.getId()))
                        .content(objectMapper.writeValueAsString(request))
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isNoContent())
                .andReturn();

        citizen = repository.findById(citizen.getId()).get();
        decodedPassword = passwordHelper.decrypt(citizen.getPassword());
        assertEquals(request.getPassword(), decodedPassword);
    }

    @Test
    public void testUpdateCitizenBadRequest() throws Exception {
        CitizenPasswordUpdateRequest request = new CitizenPasswordUpdateRequest();
        request.setOldPassword("Password1");
        request.setPassword("Password1");

        restMockMvc.perform(patch("/citizen/1/password")
                        .header("Authorization", "Bearer " + tokenHelper.encode(1L))
                        .content(objectMapper.writeValueAsString(request))
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("password"))
                .andExpect(jsonPath("$.detail").value("identical to oldPassword"))
                .andReturn();

        request.setPassword("Password1!");

        restMockMvc.perform(patch("/citizen/1/password")
                        .content(objectMapper.writeValueAsString(request))
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("password"))
                .andExpect(jsonPath("$.detail").value("Minimum eight characters, at least one uppercase letter, one lowercase letter, one number and no special character"))
                .andReturn();

        request.setPassword(null);

        restMockMvc.perform(patch("/citizen/1/password")
                        .content(objectMapper.writeValueAsString(request))
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("password"))
                .andExpect(jsonPath("$.detail").value("must not be null"))
                .andReturn();
    }

    @Test
    public void testUpdateCitizenPasswordNotFound() throws Exception {
        CitizenPasswordUpdateRequest request = new CitizenPasswordUpdateRequest();
        request.setOldPassword("Password1");
        request.setPassword("Password2");

        restMockMvc.perform(patch("/citizen/0/password")
                        .header("Authorization", "Bearer " + tokenHelper.encode(0L))
                        .content(objectMapper.writeValueAsString(request))
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.field").isEmpty())
                .andExpect(jsonPath("$.detail").value("This citizen does not exist"))
                .andReturn();
    }

    @Test
    public void testUpdateCitizenPasswordWrongOldPwd() throws Exception {
        Citizen citizen = createCitizen();

        CitizenPasswordUpdateRequest request = new CitizenPasswordUpdateRequest();
        request.setOldPassword("Password22");
        request.setPassword("Password1");

        restMockMvc.perform(patch("/citizen/" + citizen.getId() + "/password")
                        .header("Authorization", "Bearer " + tokenHelper.encode(citizen.getId()))
                        .content(objectMapper.writeValueAsString(request))
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("oldPassword"))
                .andExpect(jsonPath("$.detail").value("wrong value"))
                .andReturn();
    }

    @Test
    public void testUpdateCitizenNoToken() throws Exception {

        CitizenPasswordUpdateRequest request = new CitizenPasswordUpdateRequest();
        request.setOldPassword("Password2");
        request.setPassword("Password1");

        restMockMvc.perform(patch("/citizen/1/password")
                        .content(objectMapper.writeValueAsString(request))
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.field").isEmpty())
                .andExpect(jsonPath("$.detail").value("Invalid authorization header"))
                .andReturn();
    }

    @Test
    public void testUpdateCitizenInvalidTokenSecret() throws Exception {
        CitizenPasswordUpdateRequest request = new CitizenPasswordUpdateRequest();
        request.setOldPassword("Password2");
        request.setPassword("Password1");

        restMockMvc.perform(patch("/citizen/1/password")
                        .header("Authorization", "Bearer " + invalidTokenSecret())
                        .content(objectMapper.writeValueAsString(request))
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.field").isEmpty())
                .andExpect(jsonPath("$.detail").value("Unauthorized access"))
                .andReturn();
    }

    @Test
    public void testUpdateCitizenExpiredToken() throws Exception {
        CitizenPasswordUpdateRequest request = new CitizenPasswordUpdateRequest();
        request.setOldPassword("Password2");
        request.setPassword("Password1");

        restMockMvc.perform(patch("/citizen/1/password")
                        .header("Authorization", "Bearer " + expiredToken())
                        .content(objectMapper.writeValueAsString(request))
                        .locale(Locale.FRANCE)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.field").isEmpty())
                .andExpect(jsonPath("$.detail").value("Unauthorized access"))
                .andReturn();
    }

    private Citizen createCitizen() {
        Citizen citizen = new Citizen();
        citizen.setUsername("johnny");
        citizen.setPassword(passwordHelper.encrypt("Password2"));
        citizen.setBirthdate(LocalDate.of(1985, 12, 11));
        citizen.setResidenceCountry("France");
        return repository.save(citizen);
    }

    private CitizenCreationRequest toCitizenCreationRequest(Citizen citizen) {
        CitizenCreationRequest request = citizenMapper.toCitizenCreationRequest(citizen);
        request.setPassword(passwordHelper.decrypt(request.getPassword()));
        return request;
    }

    private CitizenUpdateRequest toCitizenUpdateRequest(Citizen citizen) {
        return citizenMapper.toCitizenUpdateRequest(citizen);
    }

    private String expiredToken() {
        return JWT.create().withSubject("1").withExpiresAt(Date.from(Instant.now().minusSeconds(5))).sign(Algorithm.HMAC256(tokenSecret));
    }

    private String invalidTokenSecret() {
        return JWT.create().withSubject("1").sign(Algorithm.HMAC256("badSecret"));
    }
}
