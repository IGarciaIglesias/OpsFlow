package com.opsflow.opsflow_backend.domain.catalog;

import jakarta.persistence.*;

@Entity
@Table(name = "catalog")
public class Catalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private boolean active;

    protected Catalog() {
    }

    public Catalog(String code, String category, String description, boolean active) {
        this.code = code;
        this.category = category;
        this.description = description;
        this.active = active;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
}