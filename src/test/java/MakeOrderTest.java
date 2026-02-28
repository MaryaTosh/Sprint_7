import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class MakeOrderTest {
    private String login = "balerina";
    private final String password = "capuchina";
    private Integer courierId;

    @BeforeEach
    void setUp() {
        RestAssured.filters(new AllureRestAssured());
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
        login = "balerina_" + System.currentTimeMillis();
        String json = "{ \"login\": \"" + login + "\", " +
                "\"password\": \"" + password + "\", " +
                "\"firstName\": \"volochkova\" }";

        given()
                .header("Content-type", "application/json")
                .body(json)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(201);
        Response loginResponse =
                given()
                        .header("Content-type", "application/json")
                        .body("{\"login\":\"" + login + "\", " +
                                "\"password\":\"" + password + "\"}")
                        .when()
                        .post("/api/v1/courier/login")
                        .then()
                        .statusCode(200)
                        .body("id", notNullValue())
                        .extract()
                        .response();

        courierId = loginResponse.path("id");
    }

    @AfterEach
    void tearDown() {
        if (courierId != null) {
            given()
                    .pathParam("id", courierId)
                    .when()
                    .delete("/api/v1/courier/{id}")
                    .then()
                    .statusCode(200);
        }
    }


        @Test
        @DisplayName("Успешное создание заказа возвращает track")
        @Step("Создание заказа с валидными данными")
        public void createOrderSuccessfully() {
            String orderBody = "{ " +
                    "  \"firstName\": \"Ivan\", " +
                    "  \"lastName\": \"Pupkin\", " +
                    "  \"address\": \"Москва, Простарная 4, 2\", " +
                    "  \"metroStation\": 4, " +
                    "  \"phone\": \"+7 999 999-99-99\", " +
                    "  \"rentTime\": 1, " +
                    "  \"deliveryDate\": \"2026-03-01\", " +
                    "  \"comment\": \"Aloha\" " +
                    "}";

            Response response =
                    given()
                            .header("Content-Type", "application/json")
                            .body(orderBody)
                            .when()
                            .post("/api/v1/orders");
            response.then().assertThat().body("track", notNullValue())
                    .and()
                    .statusCode(201);

        }

    @ParameterizedTest
    @DisplayName("Создание заказа с разными цветами самоката")
    @ValueSource(strings = {
            "[\"BLACK\"]",
            "[\"GREY\"]",
            "[\"BLACK\", \"GREY\"]"
    })
    void createOrderWithDifferentColors(String colorJson) {
        String orderBody = "{ " +
                "  \"firstName\": \"Ivan\", " +
                "  \"lastName\": \"Pupkin\", " +
                "  \"address\": \"Москва, Простарная 4, 2\", " +
                "  \"metroStation\": 4, " +
                "  \"phone\": \"+7 999 999-99-99\", " +
                "  \"rentTime\": 1, " +
                "  \"deliveryDate\": \"2026-03-01\", " +
                "  \"color\": " + colorJson + ", " +
                "  \"comment\": \"Parameterized\" " +
                "}";

        given()
                .header("Content-Type", "application/json")
                .body(orderBody)
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(201)
                .body("track", notNullValue());
    }
}


