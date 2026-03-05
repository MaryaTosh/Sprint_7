package api;

import io.restassured.response.Response;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class OrderApi {

    private static final String BASE_PATH = "/api/v1/orders";

    /**
     * Создание заказа из Map
     */
    public Response createOrderWithMap(Map<String, Object> orderData) {
        return given()
                .header("Content-Type", "application/json")
                .body(orderData)
                .post(BASE_PATH);
    }

    /**
     * Получить заказ по треку /api/v1/orders/track?t={track}
     */
    public Response getOrderByTrack(int track) {
        return given()
                .log().uri()  // Логируем URI для отладки
                .get(BASE_PATH + "/track?t=" + track);
    }

    /**
     * Запрос без трека /api/v1/orders/track → 400
     */
    public Response getOrderWithoutTrack() {
        return given()
                .log().uri()
                .get(BASE_PATH + "/track");
    }

    /**
     * Получить список всех заказов без параметров
     */
    public Response getAllOrders() {
        return given()
                .get(BASE_PATH);
    }

    /**
     * Получить заказы курьера по courierId
     */
    public Response getOrdersByCourierId(int courierId) {
        return given()
                .param("courierId", courierId)
                .get(BASE_PATH);
    }

    /**
     * Отмена заказа по треку
     */
    public Response cancelOrder(int track) {
        return given()
                .put(BASE_PATH + "/" + track + "/cancel");
    }

    /**
     * Получить список заказов с пагинацией
     */
    public Response getOrdersWithParams(int courierId, int metroStation, int page, int limit) {
        return given()
                .param("courierId", courierId)
                .param("metroStation", metroStation)
                .param("page", page)
                .param("limit", limit)
                .get(BASE_PATH);
    }

    /**
     * Утилита: принимает ответ и возвращает track номера заказа
     */
    public Integer getTrackFromResponse(Response response) {
        return response.jsonPath().getInt("track");
    }
}
