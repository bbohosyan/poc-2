package com.sap.unit;

import com.sap.dto.CreateTableRowRequest;
import com.sap.entity.TableRow;
import com.sap.event.RowCreatedEvent;
import com.sap.metrics.TableRowMetrics;
import com.sap.repository.TableRowRepository;
import com.sap.service.SanitizationService;
import com.sap.service.TableRowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TableRowService Unit Tests")
class TableRowServiceTest {

    @Mock
    private TableRowRepository repository;

    // Use real instance - SanitizationService is a simple stateless service
    private SanitizationService sanitizationService = new SanitizationService();

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TableRowMetrics metrics;

    private TableRowService tableRowService;

    private CreateTableRowRequest request;

    @BeforeEach
    void setUp() {
        // Manually construct TableRowService with dependencies using ReflectionTestUtils
        tableRowService = new TableRowService();
        ReflectionTestUtils.setField(tableRowService, "repository", repository);
        ReflectionTestUtils.setField(tableRowService, "sanitizationService", sanitizationService);
        ReflectionTestUtils.setField(tableRowService, "eventPublisher", eventPublisher);
        ReflectionTestUtils.setField(tableRowService, "metrics", metrics);

        request = new CreateTableRowRequest();
        request.typeNumber = 1;
        request.typeSelector = "A";
        request.typeFreeText = "Test text";
    }

    @Test
    @DisplayName("Should create row and publish event")
    void shouldCreateRowAndPublishEvent() {
        // Given
        when(repository.save(any(TableRow.class))).thenAnswer(invocation -> {
            TableRow row = invocation.getArgument(0);
            row.id = 1L;
            return row;
        });

        // When
        TableRow result = tableRowService.create(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id).isEqualTo(1L);
        assertThat(result.typeNumber).isEqualTo(1);
        assertThat(result.typeSelector).isEqualTo("A");
        assertThat(result.typeFreeText).isEqualTo("Test text"); // Real sanitization preserves clean text

        verify(repository).save(any(TableRow.class));
        verify(eventPublisher).publishEvent(any(RowCreatedEvent.class));
        verify(metrics).incrementCreated();
    }

    @Test
    @DisplayName("Should sanitize input text")
    void shouldSanitizeInputText() {
        // Given
        request.typeFreeText = "<script>alert('xss')</script>Hello";
        when(repository.save(any(TableRow.class))).thenAnswer(invocation -> {
            TableRow row = invocation.getArgument(0);
            row.id = 1L;
            return row;
        });

        // When
        TableRow result = tableRowService.create(request);

        // Then - real SanitizationService removes script tags
        assertThat(result.typeFreeText).doesNotContain("<script>");
        assertThat(result.typeFreeText).contains("Hello");
    }

    @Test
    @DisplayName("Should publish RowCreatedEvent with correct data")
    void shouldPublishCorrectEvent() {
        // Given
        when(repository.save(any(TableRow.class))).thenAnswer(invocation -> {
            TableRow row = invocation.getArgument(0);
            row.id = 42L;
            return row;
        });

        ArgumentCaptor<RowCreatedEvent> eventCaptor = ArgumentCaptor.forClass(RowCreatedEvent.class);

        // When
        tableRowService.create(request);

        // Then
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        RowCreatedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getRow().id).isEqualTo(42L);
    }
}
