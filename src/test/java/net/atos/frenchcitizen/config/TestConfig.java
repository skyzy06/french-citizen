package net.atos.frenchcitizen.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@DataJpaTest
@EntityScan("net.atos.frenchcitizen")
@EnableJpaRepositories(basePackages = "net.atos.frenchcitizen.repository")
@ComponentScan(basePackages = {"net.atos.frenchcitizen"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TestConfig {

}

