
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


public class CreateCourierTest {
    private Integer courierId;

    @BeforeEach
    public void setUp() {
        RestAssured.filters(new AllureRestAssured());
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @AfterEach
    public void tearDown() {
        if (courierId != null) {
            given()
                    .pathParam("id", courierId)
                    .when()
                    .delete("/api/v1/courier/{id}")
                    .then()
                    .statusCode(200);
        }
    }
    @Step("Логин курьера и получение ID")
    private Integer loginAndGetId(String login, String password) {
        Response loginResponse =
                given()
                        .header("Content-type", "application/json")
                        .body("{\"login\":\"" + login + "\", " +
                                "\"password\":\"" + password + "\"}")
                        .when()
                        .post("/api/v1/courier/login")
                        .then()
                        .statusCode(200)
                        .body("id", notNullValue())
                        .extract()
                        .response();

        return loginResponse.path("id");
    }

    @Test
    @DisplayName("Успешное создание курьера со всеми полями")
    @Step("Создание курьера с валидными данными")
    public void createNewCourierValidTest() {
        String login = "balerina_" + System.currentTimeMillis();
        String password = "capuchina";
        String firstName = "volochkova";
        String body = "{ \"login\": \"" + login + "\"," +
                "  \"password\": \"" + password + "\"," +
                "  \"firstName\": \"" + firstName + "\"}";

        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(body)
                        .when()
                        .post("/api/v1/courier");
        response.then()
                .statusCode(201)
                .body("ok", equalTo(true));
        courierId = loginAndGetId(login, password);
    }

    @Test
    @DisplayName("Создание курьера без поля login возвращает 400")
    @Step("Создание курьера без логина")
    public void createNewCourierWithoutLoginTest() {
        String json = "{\"password\": \"capuchina\", \"firstName\": \"volochkova\"}";
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(json)
                        .when()
                        .post("/api/v1/courier");
        response.then().assertThat().statusCode(400);
    }
@Test
@DisplayName("Создание курьера без пароля возвращает 400")
@Step("Создание курьера без пароля")
public void createNewCourierWithoutPasswordTest() {
    String login = "balerina_" + System.currentTimeMillis();
    String firstName = "volochkova";
    String body = "{ \"login\": \"" + login + "\"," +
            "  \"firstName\": \"" + firstName + "\"}";
    Response response =
            given()
                    .header("Content-type", "application/json")
                    .and()
                    .body(body)
                    .when()
                    .post("/api/v1/courier");
    response.then().assertThat().statusCode(400);
}

    @Test
    @DisplayName("Повторное создание курьера с теми же данными возвращает 409")
    @Step("Попытка создать двух идентичных курьеров")
    public void cannotCreateTwoIdenticalCouriers() {
        String login = "balerina_" + System.currentTimeMillis();
        String password = "capuchina";
        String firstName = "volochkova";

        String body = "{ \"login\": \"" + login + "\"," +
                "  \"password\": \"" + password + "\"," +
                "  \"firstName\": \"" + firstName + "\"}";

        // первый запрос – курьер успешно создаётся
        given()
                .header("Content-type", "application/json")
                .body(body)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));

        courierId = loginAndGetId(login, password);
        // второй запрос с теми же данными
        given()
                .header("Content-type", "application/json")
                .body(body)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));

    }
    @Test
    @DisplayName("Создание курьера с уже существующим логином возвращает 409")
    @Step("Проверка ошибки при дублированном логине (разные пароль/имя)")
    public void cannotCreateSameLoginCouriers() {
        String login = "balerina_" + System.currentTimeMillis();
        String password = "capuchina";
        String firstName = "volochkova";

        String firstBody = "{ \"login\": \"" + login + "\"," +
                "  \"password\": \"" + password + "\"," +
                "  \"firstName\": \"" + firstName + "\"}";
        // первый запрос – курьер успешно создаётся
        given()
                .header("Content-type", "application/json")
                .body(firstBody)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));

        courierId = loginAndGetId(login, password);
        String secondBody = "{ \"login\": \"" + login + "\"," +
                "  \"password\": \"Bobmardilo\"," +
                "  \"firstName\": \"Crokodilo\"}";
        given()
                .header("Content-type", "application/json")
                .body(secondBody)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }
}
