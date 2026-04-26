package com.example.eventsphere.model;

import com.example.eventsphere.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;

    private String phone;
    private String city;
    private String bio;
    private String profileImage;
    private String organizationName;  // for organizers

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private boolean emailVerified = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public String getFullName()  { return firstName + " " + lastName; }

    public String getInitials() {
        return (firstName != null && !firstName.isEmpty() ? String.valueOf(firstName.charAt(0)) : "") +
               (lastName  != null && !lastName.isEmpty()  ? String.valueOf(lastName.charAt(0))  : "");
    }
}
