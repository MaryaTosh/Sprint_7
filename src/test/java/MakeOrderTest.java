import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class MakeOrderTest {

    private CourierApi courierApi;
    private api.OrderApi orderApi;
    private Integer courierId;
    private String login;
    private final String password = "capuchina";

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
        RestAssured.filters(new AllureRestAssured());

        courierApi = new CourierApi();
        orderApi = new api.OrderApi();

        login = "balerina_" + System.currentTimeMillis();
        courierApi.createCourier(login, password, "volochkova").then().statusCode(201);
        courierId = courierApi.loginAndGetId(login, password);
    }

    @AfterEach
    void tearDown() {
        if (courierId != null) {
            courierApi.deleteCourier(courierId).then().statusCode(200);
        }
    }

    @Test
    @DisplayName("Успешное создание заказа возвращает track")
    void createOrderSuccessfully() {
        Map<String, Object> orderData = createOrderMap("Ivan", "Pupkin",
                "Москва, Просторная 4, 2", 4, "+7 999 999-99-99",
                1, "2026-03-01", "Aloha");

        Response response = orderApi.createOrderWithMap(orderData);
        response.then()
                .statusCode(201)
                .body("track", notNullValue());
    }

    @ParameterizedTest
    @DisplayName("Создание заказа с разными цветами самоката")
    @ValueSource(strings = {
            "[\"BLACK\"]",
            "[\"GREY\"]",
            "[\"BLACK\", \"GREY\"]"
    })
    void createOrderWithDifferentColors(String colorJson) {
        String[] colors;
        if ("[]".equals(colorJson)) {
            colors = new String[0];
        } else {

            String cleanColors = colorJson.replaceAll("[\\[\\]\"]", "");
            if (cleanColors.isEmpty()) {
                colors = null;
            } else {
                colors = cleanColors.split(",");
            }
        }

        Map<String, Object> orderData = createOrderMap("Ivan", "Pupkin",
                "Москва, Просторная 4, 2", 4, "+7 999 999-99-99",
                1, "2026-03-01", "Parameterized " + colorJson, colors);

        Response response = orderApi.createOrderWithMap(orderData);
        response.then()
                .statusCode(201)
                .body("track", notNullValue());
    }

    private Map<String, Object> createOrderMap(String firstName, String lastName,
                                               String address, int metroStation, String phone, int rentTime,
                                               String deliveryDate, String comment) {
        Map<String, Object> order = new HashMap<>();
        order.put("firstName", firstName);
        order.put("lastName", lastName);
        order.put("address", address);
        order.put("metroStation", metroStation);
        order.put("phone", phone);
        order.put("rentTime", rentTime);
        order.put("deliveryDate", deliveryDate);
        order.put("comment", comment);
        order.put("color", null);  // По умолчанию без цветов
        return order;
    }

    private Map<String, Object> createOrderMap(String firstName, String lastName,
                                               String address, int metroStation, String phone, int rentTime,
                                               String deliveryDate, String comment, String[] colors) {
        Map<String, Object> order = createOrderMap(firstName, lastName, address, metroStation,
                phone, rentTime, deliveryDate, comment);
        order.put("color", colors);
        return order;
    }
}
