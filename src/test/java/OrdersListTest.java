import api.BaseTest;
import io.restassured.response.Response;
import api.OrderApi;
import POJO.OrderCreateRequest;
import org.junit.jupiter.api.*;
import static org.hamcrest.Matchers.*;
import static org.apache.http.HttpStatus.*;

public class OrdersListTest extends BaseTest {

    private OrderApi orderApi;
    private Integer track;

    @BeforeEach
    void setUp() {
        orderApi = new OrderApi();

        OrderCreateRequest order = new OrderCreateRequest(
                1,
                2,
                "Москва, Просторная 4, 2",
                4,
                79999999999L,
                1,
                "2026-03-10",
                "Test order"
        );
        order.setColor(new String[]{"BLACK", "GREY"});  // Цвета

        Response orderResponse = orderApi.createOrder(order);
        orderResponse.then().statusCode(SC_CREATED);

        track = orderResponse.path("track");
        Assertions.assertNotNull(track, "track не должен быть null");
    }

    @Test
    @DisplayName("Успешный запрос по существующему треку возвращает объект order")
    public void successfulRequestByTrackReturnsOrder() {  // public!
        Response response = orderApi.getOrderByTrack(track);

        response.then()
                .statusCode(SC_OK)
                .body("order", notNullValue())
                .body("order.track", equalTo(track));
    }

    @Test
    @DisplayName("Запрос без номера заказа возвращает 400 и сообщение об ошибке")
    public void requestWithoutTrackReturnsBadRequest() {  // public!
        Response response = orderApi.getOrderWithoutTrack();

        response.then()
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для поиска"));
    }

    @Test
    @DisplayName("Запрос по несуществующему номеру возвращает 404 и сообщение об ошибке")
    public void requestWithNonExistingTrackReturnsNotFound() {  // public!
        int nonExistingTrack = 999999999;

        Response response = orderApi.getOrderByTrack(nonExistingTrack);

        response.then()
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo("Заказ не найден"));
    }
}