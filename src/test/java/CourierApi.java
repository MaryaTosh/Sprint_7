import io.qameta.allure.Step;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import java.util.HashMap;
import java.util.Map;

public class CourierApi {
    private static final String COURIER_BASE_PATH = "/api/v1/courier";

    @Step("Создать курьера: {login}")
    public Response createCourier(String login, String password, String firstName) {
        Map<String, String> courier = new HashMap<>();
        courier.put("login", login);
        courier.put("password", password);
        courier.put("firstName", firstName);

        return given()
                .header("Content-type", "application/json")
                .body(courier)
                .when()
                .post(COURIER_BASE_PATH);
    }

    @Step("Логин курьера: {login}")
    public Response loginCourier(String login, String password) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("login", login);
        credentials.put("password", password);

        return given()
                .header("Content-type", "application/json")
                .body(credentials)
                .when()
                .post(COURIER_BASE_PATH + "/login");
    }

    @Step("Удалить курьера с id: {courierId}")
    public Response deleteCourier(int courierId) {
        return given()
                .pathParam("id", courierId)
                .when()
                .delete(COURIER_BASE_PATH + "/{id}");
    }

    @Step("Логин курьера и получение ID")
    public Integer loginAndGetId(String login, String password) {
        Response response = loginCourier(login, password);
        response.then()
                .statusCode(200)
                .body("id", notNullValue());
        return response.path("id");
    }
}
