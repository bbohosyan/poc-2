package org.uniqa.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class SanitizationServiceTest {

    @Inject
    SanitizationService sanitizationService;

    @Test
    void Sanitize_NullInput() {
        String result = sanitizationService.sanitize(null);
        assertNull(result, "Null input should return null");
    }

    @Test
    void Sanitize_CleanInput() {
        String input = "This is clean text";
        String result = sanitizationService.sanitize(input);
        assertEquals(input, result, "Clean input should remain unchanged");
    }

    @Test
    void Sanitize_RemovesScriptTags() {
        String input = "Hello <script>alert('XSS')</script> World";
        String result = sanitizationService.sanitize(input);
        assertEquals("Hello  World", result, "Script tags should be removed");
    }

    @Test
    void Sanitize_RemovesScriptTagsWithAttributes() {
        String input = "Test <script type='text/javascript'>alert('XSS')</script> here";
        String result = sanitizationService.sanitize(input);
        assertEquals("Test  here", result, "Script tags with attributes should be removed");
    }

    @Test
    void Sanitize_RemovesHtmlTags() {
        String input = "This is <b>bold</b> and <i>italic</i> text";
        String result = sanitizationService.sanitize(input);
        assertEquals("This is bold and italic text", result, "HTML tags should be removed");
    }

    @Test
    void Sanitize_RemovesJavascriptProtocol() {
        String input = "Click <a href='javascript:alert(1)'>here</a>";
        String result = sanitizationService.sanitize(input);
        assertFalse(result.contains("javascript:"), "javascript: protocol should be removed");
    }

    @Test
    void Sanitize_RemovesEventHandlers() {
        String input = "<div onclick='alert(1)'>Click me</div>";
        String result = sanitizationService.sanitize(input);
        assertFalse(result.contains("onclick"), "Event handlers should be removed");
    }

    @Test
    void Sanitize_RemovesOnloadAttribute() {
        String input = "<img src='x' onload='alert(1)'>";
        String result = sanitizationService.sanitize(input);
        assertFalse(result.contains("onload"), "onload attribute should be removed");
    }

    @Test
    void Sanitize_ComplexXSSAttempt() {
        String input = "<script>alert('XSS')</script><img src=x onerror='alert(1)'>";
        String result = sanitizationService.sanitize(input);
        assertFalse(result.contains("<script>"), "Script tags should be removed");
        assertFalse(result.contains("onerror"), "Event handlers should be removed");
        assertFalse(result.contains("<img"), "Image tags should be removed");
    }

    @Test
    void Sanitize_PreservesPlainText() {
        String input = "Option A is better than Option B";
        String result = sanitizationService.sanitize(input);
        assertEquals(input, result, "Plain text should be preserved");
    }

    @Test
    void Sanitize_HandlesEmptyString() {
        String input = "";
        String result = sanitizationService.sanitize(input);
        assertEquals("", result, "Empty string should remain empty");
    }

    @Test
    void Sanitize_HandlesWhitespace() {
        String input = "   Text with spaces   ";
        String result = sanitizationService.sanitize(input);
        assertEquals(input, result, "Whitespace should be preserved");
    }

    @Test
    void Sanitize_HandlesSpecialCharacters() {
        String input = "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";
        String result = sanitizationService.sanitize(input);
        assertTrue(result.contains("!@#$%"), "Special characters should be preserved");
    }

    @Test
    void Sanitize_HandlesNewlines() {
        String input = "Line 1\nLine 2\rLine 3\r\nLine 4";
        String result = sanitizationService.sanitize(input);
        assertTrue(result.contains("\n"), "Newlines should be preserved");
    }
}
