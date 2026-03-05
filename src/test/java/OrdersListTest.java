import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class OrdersListTest {
    private api.OrderApi orderApi;
    private Integer track;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
        RestAssured.filters(new AllureRestAssured());

        orderApi = new api.OrderApi();

        // Создаём заказ, чтобы был валидный track
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("firstName", "Ivan");
        orderData.put("lastName", "Pupkin");
        orderData.put("address", "Москва, Просторная 4, 2");
        orderData.put("metroStation", 4);
        orderData.put("phone", "+7 999 999-99-99");
        orderData.put("rentTime", 1);
        orderData.put("deliveryDate", "2026-03-01");
        orderData.put("comment", "Test order");
        orderData.put("color", new String[]{"BLACK", "GREY"});

        Response orderResponse = orderApi.createOrderWithMap(orderData);
        orderResponse.then().statusCode(201);

        track = orderResponse.path("track");
        assertNotNull(track, "track не должен быть null");
    }

    @Test
    @DisplayName("Успешный запрос по существующему треку возвращает объект order")
    void successfulRequestByTrackReturnsOrder() {
        Response response = orderApi.getOrderByTrack(track);

        response.then()
                .statusCode(200)
                .body("order", notNullValue())
                .body("order.track", equalTo(track));
    }

    @Test
    @DisplayName("Запрос без номера заказа возвращает 400 и сообщение об ошибке")
    void requestWithoutTrackReturnsBadRequest() {
        Response response = orderApi.getOrderWithoutTrack();

        response.then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для поиска"));
    }

    @Test
    @DisplayName("Запрос по несуществующему номеру возвращает 404 и сообщение об ошибке")
    void requestWithNonExistingTrackReturnsNotFound() {
        int nonExistingTrack = 999999999;

        Response response = orderApi.getOrderByTrack(nonExistingTrack);

        response.then()
                .statusCode(404)
                .body("message", equalTo("Заказ не найден"));
    }
}
