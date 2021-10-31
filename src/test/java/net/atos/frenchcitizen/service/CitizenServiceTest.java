package net.atos.frenchcitizen.service;

import net.atos.frenchcitizen.config.TestConfig;
import net.atos.frenchcitizen.model.Citizen;
import net.atos.frenchcitizen.util.PasswordUtils;
import org.awaitility.Durations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.transaction.TestTransaction;

import javax.transaction.Transactional;
import java.time.LocalDate;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;


@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestConfig.class)
public class CitizenServiceTest {

    @Autowired
    private CitizenService citizenService;

    @Test
    public void testCitizenCreationMinimal() {
        Citizen citizen = new Citizen();
        citizen.setUsername("test");
        citizen.setPassword(PasswordUtils.encrypt("BabaYaga1", "wick"));
        citizen.setBirthdate(LocalDate.of(1979, 10, 10));
        citizen.setResidenceCountry("France");

        citizen = citizenService.save(citizen);
        assertNotNull(citizen.getId());
        assertEquals(PasswordUtils.decrypt(citizen.getPassword(), "wick"), "BabaYaga1");
        assertNotNull(citizen.getTimestampCreation());
        assertEquals(citizen.getTimestampCreation(), citizen.getTimestampModification());
    }

    @Test
    public void testCitizenMissingNotNullableField() {
        Citizen citizen1 = new Citizen();
        citizen1.setUsername("test");
        citizen1.setBirthdate(LocalDate.of(1979, 10, 10));
        citizen1.setResidenceCountry("France");
        assertThrows(DataIntegrityViolationException.class, () -> citizenService.save(citizen1));

        Citizen citizen2 = new Citizen();
        citizen2.setPassword(PasswordUtils.encrypt("BabaYaga1", "wick"));
        citizen2.setBirthdate(LocalDate.of(1979, 10, 10));
        citizen2.setResidenceCountry("France");
        assertThrows(DataIntegrityViolationException.class, () -> citizenService.save(citizen2));

        Citizen citizen3 = new Citizen();
        citizen3.setUsername("test");
        citizen3.setPassword(PasswordUtils.encrypt("BabaYaga1", "wick"));
        citizen3.setResidenceCountry("France");
        assertThrows(DataIntegrityViolationException.class, () -> citizenService.save(citizen3));

        Citizen citizen4 = new Citizen();
        citizen4.setUsername("test");
        citizen4.setPassword(PasswordUtils.encrypt("BabaYaga1", "wick"));
        citizen4.setBirthdate(LocalDate.of(1979, 10, 10));
        assertThrows(DataIntegrityViolationException.class, () -> citizenService.save(citizen4));
    }

    @Test
    public void testCitizenUsernameAlreadyExists() {
        Citizen citizen = new Citizen();
        citizen.setUsername("unique");
        citizen.setPassword(PasswordUtils.encrypt("BabaYaga1", "wick"));
        citizen.setBirthdate(LocalDate.of(1979, 10, 10));
        citizen.setResidenceCountry("France");
        citizenService.save(citizen);

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Citizen citizen2 = new Citizen();
        citizen2.setUsername("unique");
        citizen2.setPassword(PasswordUtils.encrypt("BabaYaga2", "wick"));
        citizen2.setBirthdate(LocalDate.of(1979, 2, 10));
        citizen2.setResidenceCountry("France");
        assertThrows(DataIntegrityViolationException.class, () -> citizenService.save(citizen2));
    }

    @Test
    public void testCitizenUpdateTriggerModificationDate() {
        Citizen citizen = new Citizen();
        citizen.setUsername("unicorn");
        citizen.setPassword(PasswordUtils.encrypt("BabaYaga1", "wick"));
        citizen.setBirthdate(LocalDate.of(1979, 10, 10));
        citizen.setResidenceCountry("France");
        citizen = citizenService.save(citizen);

        assertNotNull(citizen.getId());
        assertEquals(PasswordUtils.decrypt(citizen.getPassword(), "wick"), "BabaYaga1");
        assertNotNull(citizen.getTimestampCreation());
        assertEquals(citizen.getTimestampCreation(), citizen.getTimestampModification());

        await().during(Durations.ONE_SECOND).until(() -> true);

        citizen.setFirstname("unicorn");
        citizen = citizenService.save(citizen);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        assertEquals("unicorn", citizen.getFirstname());
        assertNotEquals(citizen.getTimestampCreation(), citizen.getTimestampModification());
    }
}
