package com.sap.cucumber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.dto.CreateTableRowRequest;
import com.sap.entity.TableRow;
import com.sap.repository.TableRowRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TableRowStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TableRowRepository repository;

    private MvcResult lastResult;
    private Long lastCreatedId;
    private List<Long> createdIds = new ArrayList<>();

    @Before
    public void setUp() {
        createdIds.clear();
        lastCreatedId = null;
    }

    @Given("the database is empty")
    public void theDatabaseIsEmpty() {
        repository.deleteAll();
    }

    @Given("the following rows exist:")
    public void theFollowingRowsExist(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        for (Map<String, String> row : rows) {
            TableRow tableRow = new TableRow();
            tableRow.setTypeNumber(Integer.parseInt(row.get("typeNumber")));
            tableRow.setTypeSelector(row.get("typeSelector"));
            tableRow.setTypeFreeText(row.get("typeFreeText"));
            TableRow saved = repository.save(tableRow);
            createdIds.add(saved.getId());
            lastCreatedId = saved.getId();
        }
    }

    @When("I create a table row with:")
    public void iCreateATableRowWith(DataTable dataTable) throws Exception {
        Map<String, String> row = dataTable.asMaps().get(0);
        CreateTableRowRequest request = new CreateTableRowRequest();
        request.setTypeNumber(Integer.parseInt(row.get("typeNumber")));
        request.setTypeSelector(row.get("typeSelector"));
        request.setTypeFreeText(row.get("typeFreeText"));

        lastResult = mockMvc.perform(post("/rows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        if (lastResult.getResponse().getStatus() == 200) {
            JsonNode json = objectMapper.readTree(lastResult.getResponse().getContentAsString());
            if (json.has("id")) {
                lastCreatedId = json.get("id").asLong();
            }
        }
    }

    @When("I create a table row with empty request")
    public void iCreateATableRowWithEmptyRequest() throws Exception {
        CreateTableRowRequest request = new CreateTableRowRequest();
        lastResult = mockMvc.perform(post("/rows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();
    }

    @When("I request rows with page {int} and size {int}")
    public void iRequestRowsWithPageAndSize(int page, int size) throws Exception {
        lastResult = mockMvc.perform(get("/rows")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andReturn();
    }

    @When("I delete the row with id from last created")
    public void iDeleteTheRowWithIdFromLastCreated() throws Exception {
        lastResult = mockMvc.perform(delete("/rows/" + lastCreatedId))
                .andReturn();
    }

    @When("I bulk create rows:")
    public void iBulkCreateRows(DataTable dataTable) throws Exception {
        List<Map<String, String>> rows = dataTable.asMaps();
        List<CreateTableRowRequest> requests = new ArrayList<>();

        for (Map<String, String> row : rows) {
            CreateTableRowRequest request = new CreateTableRowRequest();
            request.setTypeNumber(Integer.parseInt(row.get("typeNumber")));
            request.setTypeSelector(row.get("typeSelector"));
            request.setTypeFreeText(row.get("typeFreeText"));
            requests.add(request);
        }

        lastResult = mockMvc.perform(post("/rows/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andReturn();
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertThat(lastResult.getResponse().getStatus()).isEqualTo(expectedStatus);
    }

    @Then("the response should contain {string}")
    public void theResponseShouldContain(String field) throws Exception {
        String content = lastResult.getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(content);
        assertThat(json.has(field)).isTrue();
    }

    @Then("the response should have {string} equal to {int}")
    public void theResponseShouldHaveFieldEqualToInt(String field, int value) throws Exception {
        String content = lastResult.getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(content);
        assertThat(json.get(field).asInt()).isEqualTo(value);
    }

    @Then("the response should have {string} equal to {string}")
    public void theResponseShouldHaveFieldEqualToString(String field, String value) throws Exception {
        String content = lastResult.getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(content);
        assertThat(json.get(field).asText()).isEqualTo(value);
    }

    @Then("the database should contain {int} row(s)")
    public void theDatabaseShouldContainRows(int count) {
        assertThat(repository.count()).isEqualTo(count);
    }

    @Then("the response should have {int} rows in data")
    public void theResponseShouldHaveRowsInData(int count) throws Exception {
        String content = lastResult.getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(content);
        assertThat(json.get("data").size()).isEqualTo(count);
    }

    @Then("the total count should be {int}")
    public void theTotalCountShouldBe(int count) throws Exception {
        String content = lastResult.getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(content);
        assertThat(json.get("totalCount").asInt()).isEqualTo(count);
    }

    @Then("the response should contain error {string}")
    public void theResponseShouldContainError(String errorMessage) throws Exception {
        String content = lastResult.getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(content);
        assertThat(json.get("error").asText()).isEqualTo(errorMessage);
    }
}
