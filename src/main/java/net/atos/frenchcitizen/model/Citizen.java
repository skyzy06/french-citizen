package net.atos.frenchcitizen.model;

import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Data
@ToString(onlyExplicitlyIncluded = true)
@Entity
@DynamicUpdate
@Table(name = "citizens", indexes = {@Index(name = "username_idx", columnList = "username")})
public class Citizen {

    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ToString.Include
    @Column(nullable = false, unique = true, length = 32)
    public String username;

    @Column(nullable = false)
    public String password;

    @Column
    public String firstname;

    @Column
    public String lastname;

    @Column
    public String phoneNumber;

    @Column(nullable = false, updatable = false)
    public LocalDate birthdate;

    @Column(nullable = false)
    public String residenceCountry;

    @Column(length = 1)
    @Enumerated(EnumType.STRING)
    public Gender gender;

    @CreationTimestamp
    @Column(name = "timestamp_creation", updatable = false)
    private Instant timestampCreation;

    @UpdateTimestamp
    @Column(name = "timestamp_modification")
    private Instant timestampModification;
}
