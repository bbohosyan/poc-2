package com.sap.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.dto.CreateTableRowRequest;
import com.sap.entity.TableRow;
import com.sap.repository.TableRowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("TableRowController Integration Tests")
class TableRowControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TableRowRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("POST /rows - Should create new row")
    void shouldCreateNewRow() throws Exception {
        // Given
        CreateTableRowRequest request = new CreateTableRowRequest();
        request.typeNumber = 1;
        request.typeSelector = "A";
        request.typeFreeText = "Integration test text";

        // When & Then
        mockMvc.perform(post("/rows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.typeNumber").value(1))
                .andExpect(jsonPath("$.typeSelector").value("A"))
                .andExpect(jsonPath("$.typeFreeText").value("Integration test text"));

        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("GET /rows - Should return paginated rows")
    void shouldReturnPaginatedRows() throws Exception {
        // Given
        for (int i = 0; i < 15; i++) {
            TableRow row = new TableRow();
            row.typeNumber = i;
            row.typeSelector = "Type" + i;
            row.typeFreeText = "Text " + i;
            repository.save(row);
        }

        // When & Then
        mockMvc.perform(get("/rows")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(10))
                .andExpect(jsonPath("$.totalCount").value(15))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    @DisplayName("GET /rows - Should reject invalid size parameter")
    void shouldRejectInvalidSize() throws Exception {
        mockMvc.perform(get("/rows")
                .param("size", "200"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Size cannot exceed 100"));
    }

    @Test
    @DisplayName("DELETE /rows/{id} - Should delete row")
    void shouldDeleteRow() throws Exception {
        // Given
        TableRow row = new TableRow();
        row.typeNumber = 1;
        row.typeSelector = "A";
        row.typeFreeText = "To be deleted";
        row = repository.save(row);

        // When & Then
        mockMvc.perform(delete("/rows/" + row.id))
                .andExpect(status().isNoContent());

        assertThat(repository.findById(row.id)).isEmpty();
    }

    @Test
    @DisplayName("POST /rows - Should validate required fields")
    void shouldValidateRequiredFields() throws Exception {
        // Given - empty request
        CreateTableRowRequest request = new CreateTableRowRequest();

        // When & Then
        mockMvc.perform(post("/rows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /rows/bulk - Should accept bulk creation")
    void shouldAcceptBulkCreation() throws Exception {
        // Given
        String bulkRequest = "[" +
                "{\"typeNumber\":1,\"typeSelector\":\"A\",\"typeFreeText\":\"Text1\"}," +
                "{\"typeNumber\":2,\"typeSelector\":\"B\",\"typeFreeText\":\"Text2\"}" +
                "]";

        // When & Then
        mockMvc.perform(post("/rows/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bulkRequest))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("processing"))
                .andExpect(jsonPath("$.count").value(2));
    }
}
