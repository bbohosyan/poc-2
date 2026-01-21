package com.sap.unit;

import com.sap.service.SanitizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SanitizationService Unit Tests")
class SanitizationServiceTest {

    private SanitizationService sanitizationService;

    @BeforeEach
    void setUp() {
        sanitizationService = new SanitizationService();
    }

    @Test
    @DisplayName("Should remove HTML tags from input")
    void shouldRemoveHtmlTags() {
        // Given
        String input = "<p>Hello <b>World</b></p>";

        // When
        String result = sanitizationService.sanitize(input);

        // Then
        assertThat(result).doesNotContain("<p>", "</p>", "<b>", "</b>");
    }

    @Test
    @DisplayName("Should remove script tags - XSS prevention")
    void shouldRemoveScriptTags() {
        // Given
        String input = "<script>alert('xss')</script>Normal text";

        // When
        String result = sanitizationService.sanitize(input);

        // Then
        assertThat(result).doesNotContain("<script>", "</script>");
        assertThat(result).contains("Normal text");
    }

    @ParameterizedTest
    @DisplayName("Should handle various malicious inputs")
    @CsvSource({
        "'<img src=x onerror=alert(1)>', false",
        "'<a href=\"javascript:alert(1)\">click</a>', false",
        "'<div onmouseover=\"alert(1)\">hover</div>', false"
    })
    void shouldHandleMaliciousInputs(String input, boolean shouldContainTags) {
        // When
        String result = sanitizationService.sanitize(input);

        // Then
        assertThat(result.contains("<")).isEqualTo(shouldContainTags);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should handle null and empty inputs")
    void shouldHandleNullAndEmpty(String input) {
        // When
        String result = sanitizationService.sanitize(input);

        // Then
        assertThat(result).isNullOrEmpty();
    }

    @Test
    @DisplayName("Should preserve normal text")
    void shouldPreserveNormalText() {
        // Given
        String input = "Hello World! This is normal text with numbers 12345.";

        // When
        String result = sanitizationService.sanitize(input);

        // Then
        assertThat(result).isEqualTo(input);
    }
}
