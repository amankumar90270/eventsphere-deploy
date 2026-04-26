package com.example.eventsphere.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Category {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String icon;        // Bootstrap icon class, e.g. "bi-music-note"
    private String color;       // CSS class or hex, e.g. "blue"
    private String description;

    @Builder.Default
    private boolean active = true;

    public long getEventCount() { return 0L; } // computed by service
}
