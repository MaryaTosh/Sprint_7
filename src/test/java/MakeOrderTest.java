import api.BaseTest;
import api.CourierApi;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.hamcrest.Matchers.*;
import api.OrderApi;
import POJO.OrderCreateRequest;
import org.junit.jupiter.api.*;
import static org.apache.http.HttpStatus.*;

public class MakeOrderTest extends BaseTest {

    private CourierApi courierApi;
    private OrderApi orderApi;
    private Integer courierId;
    private String login;
    private final String password = "capuchina";

    @BeforeEach
    void setUp() {
        courierApi = new CourierApi();
        orderApi = new OrderApi();

        login = "balerina_" + System.currentTimeMillis();
        courierApi.createCourier(login, password, "volochkova")
                .then().statusCode(SC_CREATED);
        courierId = courierApi.loginAndGetId(login, password);
    }

    @AfterEach
    void tearDown() {
        if (courierId != null) {
            courierApi.deleteCourier(courierId).then().statusCode(SC_OK);
        }
    }

    @Test
    @DisplayName("Успешное создание заказа возвращает track")
    public void createOrderSuccessfully() {
        OrderCreateRequest order = new OrderCreateRequest(
                1,
                2,
                "Москва, Просторная 4, 2",
                4,
                79999999999L,
                1,
                "2026-03-10",
                "Aloha"
        );

        Response response = orderApi.createOrder(order);
        response.then()
                .statusCode(SC_CREATED)
                .body("track", notNullValue());
    }

    @ParameterizedTest
    @DisplayName("Создание заказа с разными цветами самоката")
    @ValueSource(strings = {
            "[\"BLACK\"]",           // Только чёрный
            "[\"GREY\"]",            // Только серый
            "[\"BLACK\", \"GREY\"]", // Оба цвета
            ""                       // Без цвета
    })
    public void createOrderWithDifferentColors(String colorJson) {
        String[] colors = null;

        if (!colorJson.isEmpty()) {
            String cleanColors = colorJson.replaceAll("[\\[\\]\"]", "");
            if (!cleanColors.isEmpty()) {
                colors = cleanColors.split(",");
            }
        }

        OrderCreateRequest order = new OrderCreateRequest(
                1, 2, "Москва, Просторная 4, 2", 4, 79999999999L, 1,
                "2026-03-10", "Тест цвета: " + (colors != null ? java.util.Arrays.toString(colors) : "нет")
        );
        order.setColor(colors);

        Response response = orderApi.createOrder(order);
        response.then()
                .statusCode(SC_CREATED)
                .body("track", notNullValue());
    }
}
