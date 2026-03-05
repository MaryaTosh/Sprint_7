import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import static org.hamcrest.Matchers.*;

public class CreateCourierTest {

    private CourierApi courierApi;
    private Integer courierId;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
        RestAssured.filters(new AllureRestAssured());
        courierApi = new CourierApi();
    }

    @AfterEach
    void tearDown() {
        if (courierId != null) {
            courierApi.deleteCourier(courierId).then().statusCode(200);
        }
    }

    @Test
    @DisplayName("Успешное создание курьера со всеми полями")
    public void createNewCourierValidTest() {
        String login = "balerina_" + System.currentTimeMillis();
        String password = "capuchina";
        String firstName = "volochkova";

        Response response = courierApi.createCourier(login, password, firstName);

        response.then()
                .statusCode(201)
                .body("ok", equalTo(true));

        courierId = courierApi.loginAndGetId(login, password);
    }

    @Test
    @DisplayName("Создание курьера без поля login возвращает 400")
    public void createNewCourierWithoutLoginTest() {
        Response response = courierApi.createCourier(null, "capuchina", "volochkova");

        response.then()
                .statusCode(400)
        .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера без пароля возвращает 400")
    public void createNewCourierWithoutPasswordTest() {
        String login = "balerina_" + System.currentTimeMillis();
        Response response = courierApi.createCourier(login, null, "volochkova");

        response.then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));;
    }

    @Test
    @DisplayName("Повторное создание курьера с теми же данными возвращает 409")
    public void cannotCreateTwoIdenticalCouriers() {
        String login = "balerina_" + System.currentTimeMillis();
        String password = "capuchina";
        String firstName = "volochkova";

        // Первый курьер создаётся
        courierApi.createCourier(login, password, firstName)
                .then().statusCode(201);
        courierId = courierApi.loginAndGetId(login, password);

        // Второй с теми же данными — конфликт
        courierApi.createCourier(login, password, firstName)
                .then()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    @Test
    @DisplayName("Создание курьера с уже существующим логином возвращает 409")
    public void cannotCreateSameLoginCouriers() {
        String login = "balerina_" + System.currentTimeMillis();
        String password = "capuchina";
        String firstName = "volochkova";

        // Первый курьер
        courierApi.createCourier(login, password, firstName)
                .then().statusCode(201);
        courierId = courierApi.loginAndGetId(login, password);

        // Второй с тем же логином, но другим паролем/именем
        courierApi.createCourier(login, "Bobmardilo", "Crokodilo")
                .then()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }
}
