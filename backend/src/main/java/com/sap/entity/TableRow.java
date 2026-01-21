package com.sap.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "table_row", indexes = {
    @Index(name = "idx_type_selector", columnList = "type_selector"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_selector_number", columnList = "type_selector, type_number")
})
public class TableRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Type Number is required")
    @Min(value = 1, message = "Type Number must be at least 1")
    @Max(value = 2147483647, message = "Type Number is too large")
    @Column(name = "type_number")
    private Integer typeNumber;

    @NotBlank(message = "Type Selector is required")
    @Column(name = "type_selector")
    private String typeSelector;

    @NotBlank(message = "Type Free Text is required")
    @Size(max = 1000, message = "Type Free Text must not exceed 1000 characters")
    @Column(name = "type_free_text", length = 1000, columnDefinition = "TEXT")
    private String typeFreeText;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTypeNumber() {
        return typeNumber;
    }

    public void setTypeNumber(Integer typeNumber) {
        this.typeNumber = typeNumber;
    }

    public String getTypeSelector() {
        return typeSelector;
    }

    public void setTypeSelector(String typeSelector) {
        this.typeSelector = typeSelector;
    }

    public String getTypeFreeText() {
        return typeFreeText;
    }

    public void setTypeFreeText(String typeFreeText) {
        this.typeFreeText = typeFreeText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
