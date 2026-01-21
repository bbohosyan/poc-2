package org.uniqa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "table_row")
public class TableRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull(message = "Type Number is required")
    @Min(value = 1, message = "Type Number must be at least 1")
    @Max(value = 2147483647, message = "Type Number is too large")
    @Column(name = "type_number")
    public Integer typeNumber;

    @NotBlank(message = "Type Selector is required")
    @Column(name = "type_selector")
    public String typeSelector;

    @NotBlank(message = "Type Free Text is required")
    @Size(max = 1000, message = "Type Free Text must not exceed 1000 characters")
    @Column(name = "type_free_text", length = 1000, columnDefinition = "TEXT")
    public String typeFreeText;
}
