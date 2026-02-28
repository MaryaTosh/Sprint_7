import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class OrdersListTest {
    private String login = "balerina";
    private final String password = "capuchina";
    private Integer courierId;
    private Integer myTrack;

    @Step("Создание тестового курьера с логином: {login}")
    private Response createCourier(String login, String password, String firstName) {
        String json = "{ \"login\": \"" + login + "\", \"password\": \"" + password + "\", \"firstName\": \"" + firstName + "\"}";
        return given()
                .header("Content-type", "application/json")
                .body(json)
                .post("/api/v1/courier");
    }

    @Step("Логин курьера {login}")
    private Response loginCourier(String login, String password) {
        String json = "{\"login\":\"" + login + "\", \"password\":\"" + password + "\"}";
        return given()
                .header("Content-type", "application/json")
                .body(json)
                .post("/api/v1/courier/login");
    }

    @Step("Создание заказа для курьера")
    private Response createOrder() {
        String orderBody = "{ \"firstName\": \"Ivan\", \"lastName\": \"Pupkin\", \"address\": \"Москва, Просторная 4, 2\", \"metroStation\": 4, \"phone\": \"+7 999 999-99-99\", \"rentTime\": 1, \"deliveryDate\": \"2026-03-01\", \"color\": [\"BLACK\", \"GREY\"], \"comment\": \"Test order\" }";
        return given()
                .header("Content-type", "application/json")
                .body(orderBody)
                .post("/api/v1/orders");
    }

    @Step("Поиск заказов курьера с ID {courierId}")
    private Response getOrdersByCourierId(Integer courierId) {
        return given()
                .queryParam("courierId", courierId)
                .get("/api/v1/orders");
    }

    @Step("Запрос списка заказов без параметров")
    private Response getOrdersWithoutParams() {
        return given().get("/api/v1/orders");
    }


    @BeforeEach
    void setUp() {
        RestAssured.filters(new AllureRestAssured());
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";

        login = "balerina_" + System.currentTimeMillis();

        Response courierResponse = createCourier(login, password, "volochkova");
        courierResponse.then().statusCode(201);

        Response loginResponse = loginCourier(login, password);
        loginResponse.then()
                .statusCode(200)
                .body("id", notNullValue());

        courierId = loginResponse.path("id");

        Response createResponse = createOrder();
        createResponse.then().statusCode(201);
        myTrack = createResponse.path("track");
    }

    @Test
    @DisplayName("Успешный запрос по courierId возвращает заказ")
    @Step("Поиск заказов курьера с ID {courierId}")
    void successfulOrderRequestReturnsOrderObject() {
        Response response = getOrdersByCourierId(courierId)
                .then().statusCode(200)
                .extract().response();

        assertEquals(1, response.jsonPath().getList("orders").size());
        assertEquals(courierId, response.jsonPath().getInt("orders[0].courierId"));
    }

    @Test
    @DisplayName("Запрос с несуществующим courierId возвращает пустой список")
    void requestWithNonExistentCourierIdReturnsEmpty() {
        // Жёстко вбиваем несуществующий ID
        Response response = given()
                .queryParam("courierId", 999999)
                .get("/api/v1/orders")
                .then().statusCode(200)
                .extract().response();

        assertTrue(response.jsonPath().getList("orders").isEmpty(),
                "Несуществующий courierId = пустой список");
    }

    @Test
    @DisplayName("Запрос без courierId возвращает ошибку")
    void requestWithoutCourierIdReturnsError() {
        getOrdersWithoutParams()
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для поиска"));
    }

    @AfterEach
    @Step("Удаление тестового курьера {courierId}")
    void tearDown() {
        if (courierId != null) {
            given()
                    .pathParam("id", courierId)
                    .delete("/api/v1/courier/{id}")
                    .then()
                    .statusCode(200);
        }
    }
}
