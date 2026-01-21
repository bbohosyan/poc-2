Feature: Table Row Management
  As an API consumer
  I want to manage table rows
  So that I can store and retrieve data efficiently

  Background:
    Given the database is empty

  Scenario: Create a new table row
    When I create a table row with:
      | typeNumber | typeSelector | typeFreeText       |
      | 1          | A            | Test description   |
    Then the response status should be 200
    And the response should contain "id"
    And the response should have "typeNumber" equal to 1
    And the response should have "typeSelector" equal to "A"
    And the database should contain 1 row

  Scenario: Get paginated rows
    Given the following rows exist:
      | typeNumber | typeSelector | typeFreeText |
      | 1          | A            | Text 1       |
      | 2          | B            | Text 2       |
      | 3          | C            | Text 3       |
      | 4          | D            | Text 4       |
      | 5          | E            | Text 5       |
    When I request rows with page 0 and size 3
    Then the response status should be 200
    And the response should have 3 rows in data
    And the total count should be 5

  Scenario: Delete an existing row
    Given the following rows exist:
      | typeNumber | typeSelector | typeFreeText |
      | 1          | A            | To delete    |
    When I delete the row with id from last created
    Then the response status should be 204
    And the database should contain 0 rows

  Scenario: Reject invalid page size
    When I request rows with page 0 and size 200
    Then the response status should be 400
    And the response should contain error "Size cannot exceed 100"

  Scenario: Bulk create rows
    When I bulk create rows:
      | typeNumber | typeSelector | typeFreeText |
      | 1          | A            | Bulk 1       |
      | 2          | B            | Bulk 2       |
      | 3          | C            | Bulk 3       |
    Then the response status should be 202
    And the response should have "status" equal to "processing"
    And the response should have "count" equal to 3

  Scenario: Validate required fields on create
    When I create a table row with empty request
    Then the response status should be 400
