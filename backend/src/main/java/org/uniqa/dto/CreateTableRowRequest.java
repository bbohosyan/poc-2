package org.uniqa.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;

public class CreateTableRowRequest {

    @NotNull(message = "Type Number is required")
    @Min(value = 1, message = "Type Number must be at least 1")
    @Max(value = 2147483647, message = "Type Number is too large")
    public Integer typeNumber;

    @NotBlank(message = "Type Selector is required")
    public String typeSelector;

    @NotBlank(message = "Type Free Text is required")
    @Size(max = 1000, message = "Type Free Text must not exceed 1000 characters")
    public String typeFreeText;
}