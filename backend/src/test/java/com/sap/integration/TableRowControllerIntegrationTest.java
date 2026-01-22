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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
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
        CreateTableRowRequest request = new CreateTableRowRequest();
        request.setTypeNumber(1);
        request.setTypeSelector("A");
        request.setTypeFreeText("Integration test text");

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
        for (int i = 1; i <= 15; i++) {
            TableRow row = new TableRow();
            row.setTypeNumber(i);
            row.setTypeSelector("Type" + i);
            row.setTypeFreeText("Text " + i);
            repository.save(row);
        }

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
        TableRow row = new TableRow();
        row.setTypeNumber(1);
        row.setTypeSelector("A");
        row.setTypeFreeText("To be deleted");
        row = repository.save(row);

        mockMvc.perform(delete("/rows/" + row.getId()))
                .andExpect(status().isNoContent());

        assertThat(repository.findById(row.getId())).isEmpty();
    }

    @Test
    @DisplayName("POST /rows - Should validate required fields")
    void shouldValidateRequiredFields() throws Exception {
        CreateTableRowRequest request = new CreateTableRowRequest();

        mockMvc.perform(post("/rows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /rows/bulk - Should accept bulk creation")
    void shouldAcceptBulkCreation() throws Exception {
        String bulkRequest = "[" +
                "{\"typeNumber\":1,\"typeSelector\":\"A\",\"typeFreeText\":\"Text1\"}," +
                "{\"typeNumber\":2,\"typeSelector\":\"B\",\"typeFreeText\":\"Text2\"}" +
                "]";

        mockMvc.perform(post("/rows/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bulkRequest))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("processing"))
                .andExpect(jsonPath("$.count").value(2));
    }
}
