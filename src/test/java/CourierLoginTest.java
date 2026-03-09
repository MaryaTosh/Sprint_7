import api.BaseTest;
import api.CourierApi;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.*;
import static org.apache.http.HttpStatus.*;

public class CourierLoginTest extends BaseTest {

    private String login;
    private final String password = "capuchina";
    private Integer courierId;
    private CourierApi courierApi;

    @BeforeEach
    void setUp() {
        courierApi = new CourierApi();
        login = "balerina_" + System.currentTimeMillis();

        // Создание курьера для всех тестов
        courierApi.createCourier(login, password, "volochkova")
                .then().statusCode(SC_CREATED);
    }

    @AfterEach
    void tearDown() {
        if (courierId != null) {
            courierApi.deleteCourier(courierId).then().statusCode(SC_OK);
        }
    }

    @Test
    @DisplayName("Успешный логин курьера возвращает ID")
    public void loginCourierTest() {
        Response loginResponse = courierApi.loginCourier(login, password);

        loginResponse.then()
                .statusCode(SC_OK)
                .body("id", notNullValue());

        courierId = loginResponse.path("id");
    }

    @Test
    @DisplayName("Логин без поля login возвращает ошибку")
    public void loginWithoutLoginCourierTest() {
        Response loginResponse = courierApi.loginCourier(null, password);

        loginResponse.then()
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Логин без поля password возвращает ошибку")
    public void loginWithoutPasswordCourierTest() {
        Response loginResponse = courierApi.loginCourier(login, null);

        loginResponse.then()
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Логин с неправильным паролем возвращает 404")
    public void loginShouldFailWithWrongPassword() {
        Response loginResponse = courierApi.loginCourier(login, "wrongPassword123");

        loginResponse.then()
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Логин с неправильным логином возвращает 404")
    public void loginShouldFailWithWrongLogin() {
        Response loginResponse = courierApi.loginCourier("none7485", password);

        loginResponse.then()
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo("Учетная запись не найдена"));
    }
}
