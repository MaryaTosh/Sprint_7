package api;
import POJO.CourierCreateRequest;
import POJO.CourierLoginRequest;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;


public class CourierApi {
    private static final String COURIER_BASE_PATH = "/api/v1/courier";

    @Step("Создать курьера: {login}")
    public Response createCourier(String login, String password, String firstName) {
        CourierCreateRequest courier =
                new CourierCreateRequest(login, password, firstName);

        return given()
                .header("Content-type", "application/json")
                .body(courier)
                .when()
                .post(COURIER_BASE_PATH);
    }

    @Step("Логин курьера: {login}")
    public Response loginCourier(String login, String password) {
        CourierLoginRequest credentials =
                new CourierLoginRequest(login, password);

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
