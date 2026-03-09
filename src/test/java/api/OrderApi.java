package api;
import POJO.OrderCreateRequest;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class OrderApi {

    private static final String BASE_PATH = "/api/v1/orders";

    @Step("Создать заказ")
    public Response createOrder(OrderCreateRequest order) {
        return given()
                .header("Content-Type", "application/json")
                .body(order)
                .post(BASE_PATH);
    }

    @Step("Получить заказ по треку: {track}")
    public Response getOrderByTrack(int track) {
        return given()
                .log().uri()
                .get(BASE_PATH + "/track?t=" + track);
    }

    @Step("Запрос заказа без трека")
    public Response getOrderWithoutTrack() {
        return given()
                .log().uri()
                .get(BASE_PATH + "/track");
    }
}
