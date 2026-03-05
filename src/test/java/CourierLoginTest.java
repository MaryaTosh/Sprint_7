import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

public class CourierLoginTest {

    private String login;
    private final String password = "capuchina";
    private Integer courierId;
    private CourierApi courierApi;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";

        AllureRestAssured allureFilter = new AllureRestAssured();
        RestAssured.filters(allureFilter);

        courierApi = new CourierApi();
        login = "balerina_" + System.currentTimeMillis();

        Response createResponse = courierApi.createCourier(login, password, "volochkova");
        createResponse.then().statusCode(201);

//        courierId = courierApi.loginAndGetId(login, password);
    }

    @AfterEach
    void tearDown() {
        if (courierId != null) {
            Response deleteResponse = courierApi.deleteCourier(courierId);
            deleteResponse.then().statusCode(200);
        }
    }

    @Test
    @DisplayName("Успешный логин курьера возвращает ID")
    public void loginCourierTest() {
        Response loginResponse = courierApi.loginCourier(login, password);

        loginResponse.then()
                .statusCode(200)
                .body("id", notNullValue());

        courierId = loginResponse.path("id");
    }

    @Test
    @DisplayName("Логин без поля login возвращает ошибку")
    public void loginWithoutLoginCourierTest() {
        Response loginResponse = courierApi.loginCourier(null, password);

        loginResponse.then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Логин без поля password возвращает ошибку")
    public void loginWithoutPasswordCourierTest() {
        Response loginResponse = courierApi.loginCourier(login, null);

        loginResponse.then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Логин с неправильным паролем возвращает 404")
    void loginShouldFailWithWrongPassword() {
        Response loginResponse = courierApi.loginCourier(login, "wrongPassword123");

        loginResponse.then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Логин с неправильным логином возвращает 404")
    void loginShouldFailWithWrongLogin() {
        Response loginResponse = courierApi.loginCourier("none7485", password);

        loginResponse.then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }
}
