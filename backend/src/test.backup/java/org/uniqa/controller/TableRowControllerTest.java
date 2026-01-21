package org.uniqa.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

@QuarkusTest
class TableRowControllerTest {

    @BeforeEach
    void setup() {
        given()
            .when()
            .get("/rows?page=0&size=100")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getList("data.id", Long.class)
            .forEach(id -> {
                given()
                    .when()
                    .delete("/rows/" + id)
                    .then()
                    .statusCode(204);
            });
    }

    @Test
    void getAllRows_DefaultPagination() {
        given()
            .when()
            .get("/rows")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("page", equalTo(0))
            .body("size", equalTo(10))
            .body("totalCount", notNullValue())
            .body("data", notNullValue());
    }

    @Test
    void getAllRows_CustomPagination() {
        given()
            .queryParam("page", 0)
            .queryParam("size", 5)
            .when()
            .get("/rows")
            .then()
            .statusCode(200)
            .body("page", equalTo(0))
            .body("size", equalTo(5));
    }

    @Test
    void getAllRows_SizeExceedsMaximum() {
        given()
            .queryParam("page", 0)
            .queryParam("size", 101)
            .when()
            .get("/rows")
            .then()
            .statusCode(400)
            .body("error", equalTo("Size cannot exceed 100"));
    }

    @Test
    void getAllRows_SizeLessThanMinimum() {
        given()
            .queryParam("page", 0)
            .queryParam("size", 0)
            .when()
            .get("/rows")
            .then()
            .statusCode(400)
            .body("error", equalTo("Size must be at least 1"));
    }

    @Test
    void createRow_ValidData() {
        String requestBody = """
            {
                "typeNumber": 42,
                "typeSelector": "A",
                "typeFreeText": "Test free text"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/rows")
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("typeNumber", equalTo(42))
            .body("typeSelector", equalTo("A"))
            .body("typeFreeText", equalTo("Test free text"));
    }

    @Test
    void createRow_WithXSSAttempt() {
        String requestBody = """
            {
                "typeNumber": 1,
                "typeSelector": "B",
                "typeFreeText": "<script>alert('XSS')</script>Hello"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/rows")
            .then()
            .statusCode(200)
            .body("typeFreeText", not(containsString("<script>")))
            .body("typeFreeText", containsString("Hello"));
    }

    @Test
    void createRow_MissingTypeNumber() {
        String requestBody = """
            {
                "typeSelector": "A",
                "typeFreeText": "Test"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/rows")
            .then()
            .statusCode(400);
    }

    @Test
    void createRow_TypeNumberTooSmall() {
        String requestBody = """
            {
                "typeNumber": 0,
                "typeSelector": "A",
                "typeFreeText": "Test"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/rows")
            .then()
            .statusCode(400);
    }

    @Test
    void createRow_TypeNumberAtMinimum() {
        String requestBody = """
            {
                "typeNumber": 1,
                "typeSelector": "A",
                "typeFreeText": "Test"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/rows")
            .then()
            .statusCode(200)
            .body("typeNumber", equalTo(1));
    }

    @Test
    void createRow_TypeNumberAtMaximum() {
        String requestBody = """
            {
                "typeNumber": 2147483647,
                "typeSelector": "A",
                "typeFreeText": "Test"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/rows")
            .then()
            .statusCode(200)
            .body("typeNumber", equalTo(2147483647));
    }

    @Test
    void createRow_MissingTypeSelector() {
        String requestBody = """
            {
                "typeNumber": 1,
                "typeFreeText": "Test"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/rows")
            .then()
            .statusCode(400);
    }

    @Test
    void createRow_EmptyTypeSelector() {
        String requestBody = """
            {
                "typeNumber": 1,
                "typeSelector": "",
                "typeFreeText": "Test"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/rows")
            .then()
            .statusCode(400);
    }

    @Test
    void createRow_MissingTypeFreeText() {
        String requestBody = """
            {
                "typeNumber": 1,
                "typeSelector": "A"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/rows")
            .then()
            .statusCode(400);
    }

    @Test
    void createRow_EmptyTypeFreeText() {
        String requestBody = """
            {
                "typeNumber": 1,
                "typeSelector": "A",
                "typeFreeText": ""
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/rows")
            .then()
            .statusCode(400);
    }

    @Test
    void createRow_TypeFreeTextTooLong() {
        String longText = "a".repeat(1001);
        String requestBody = String.format("""
            {
                "typeNumber": 1,
                "typeSelector": "A",
                "typeFreeText": "%s"
            }
            """, longText);

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/rows")
            .then()
            .statusCode(400);
    }

    @Test
    void createRow_TypeFreeTextAtMaxLength() {
        String maxText = "a".repeat(1000);
        String requestBody = String.format("""
            {
                "typeNumber": 1,
                "typeSelector": "A",
                "typeFreeText": "%s"
            }
            """, maxText);

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/rows")
            .then()
            .statusCode(200)
            .body("typeFreeText.length()", equalTo(1000));
    }

    @Test
    void deleteRow_ExistingRow() {
        String requestBody = """
            {
                "typeNumber": 1,
                "typeSelector": "A",
                "typeFreeText": "To be deleted"
            }
            """;

        Integer idInt = given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/rows")
            .then()
            .statusCode(200)
            .extract()
            .path("id");

        Long id = idInt.longValue();

        given()
            .when()
            .delete("/rows/" + id)
            .then()
            .statusCode(204);
        given()
            .when()
            .get("/rows")
            .then()
            .statusCode(200)
            .body("totalCount", equalTo(0));
    }

    @Test
    void deleteRow_NonExistingRow() {
        given()
            .when()
            .delete("/rows/99999")
            .then()
            .statusCode(204);
    }

    @Test
    void createMultipleRows_AndPagination() {
        for (int i = 1; i <= 15; i++) {
            String requestBody = String.format("""
                {
                    "typeNumber": %d,
                    "typeSelector": "A",
                    "typeFreeText": "Row %d"
                }
                """, i, i);

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/rows")
                .then()
                .statusCode(200);
        }

        given()
            .queryParam("page", 0)
            .queryParam("size", 10)
            .when()
            .get("/rows")
            .then()
            .statusCode(200)
            .body("data.size()", equalTo(10))
            .body("totalCount", equalTo(15))
            .body("page", equalTo(0));

        given()
            .queryParam("page", 1)
            .queryParam("size", 10)
            .when()
            .get("/rows")
            .then()
            .statusCode(200)
            .body("data.size()", equalTo(5))
            .body("totalCount", equalTo(15))
            .body("page", equalTo(1));
    }

    @Test
    void createRow_AllValidSelectors() {
        String[] selectors = {"A", "B", "C"};

        for (String selector : selectors) {
            String requestBody = String.format("""
                {
                    "typeNumber": 1,
                    "typeSelector": "%s",
                    "typeFreeText": "Testing selector %s"
                }
                """, selector, selector);

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/rows")
                .then()
                .statusCode(200)
                .body("typeSelector", equalTo(selector));
        }
    }

    @Test
    void createRow_SpecialCharactersInFreeText() {
        String requestBody = """
            {
                "typeNumber": 1,
                "typeSelector": "A",
                "typeFreeText": "Special chars: !@#$%^&*()_+-=[]{}|;':,.<>?"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/rows")
            .then()
            .statusCode(200)
            .body("typeFreeText", containsString("!@#$%"));
    }

    @Test
    void createRow_UnicodeCharacters() {
        String requestBody = """
            {
                "typeNumber": 1,
                "typeSelector": "A",
                "typeFreeText": "Unicode: 你好世界 مرحبا 🚀"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/rows")
            .then()
            .statusCode(200)
            .body("typeFreeText", containsString("你好世界"));
    }
}
