import api.BaseTest;
import api.CourierApi;
import static org.apache.http.HttpStatus.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import static org.hamcrest.Matchers.*;

public class CreateCourierTest extends BaseTest {

        private CourierApi courierApi;
        private Integer courierId;

        @BeforeEach
        void setUp() {
            courierApi = new CourierApi();
        }

        @AfterEach
        void tearDown() {
            if (courierId != null) {
                courierApi.deleteCourier(courierId).then().statusCode(SC_OK);
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
                .statusCode(SC_CREATED)
                .body("ok", equalTo(true));

        courierId = courierApi.loginAndGetId(login, password);
    }

    @Test
    @DisplayName("Создание курьера без поля login возвращает 400")
    public void createNewCourierWithoutLoginTest() {
        Response response = courierApi.createCourier(null, "capuchina", "volochkova");

        response.then()
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера без пароля возвращает 400")
    public void createNewCourierWithoutPasswordTest() {
        String login = "balerina_" + System.currentTimeMillis();
        Response response = courierApi.createCourier(login, null, "volochkova");

        response.then()
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Повторное создание курьера с теми же данными возвращает 409")
    public void cannotCreateTwoIdenticalCouriers() {
        String login = "balerina_" + System.currentTimeMillis();
        String password = "capuchina";
        String firstName = "volochkova";

        courierApi.createCourier(login, password, firstName)
                .then().statusCode(SC_CREATED);

        courierId = courierApi.loginAndGetId(login, password);

        courierApi.createCourier(login, password, firstName)
                .then()
                .statusCode(SC_CONFLICT)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    @Test
    @DisplayName("Создание курьера с уже существующим логином возвращает 409")
    public void cannotCreateSameLoginCouriers() {
        String login = "balerina_" + System.currentTimeMillis();
        String password = "capuchina";
        String firstName = "volochkova";

        courierApi.createCourier(login, password, firstName)
                .then().statusCode(SC_CREATED);

        courierId = courierApi.loginAndGetId(login, password);

        courierApi.createCourier(login, "Bobmardilo", "Crokodilo")
                .then()
                .statusCode(SC_CONFLICT)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }
}
