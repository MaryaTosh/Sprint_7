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


public class CourierLoginTest {
    private String login = "balerina";
    private final String password = "capuchina";
    private Integer courierId;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
        RestAssured.filters(new AllureRestAssured());

        login = "balerina_" + System.currentTimeMillis();
        String json = "{ \"login\": \"" + login + "\", " +
                "\"password\": \"" + password + "\", " +
                "\"firstName\": \"volochkova\" }";

        given()
                .header("Content-type", "application/json")
                .body(json)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(201);
    }
    @AfterEach
    void tearDown() {
        if (courierId != null) {
            given()
                    .pathParam("id", courierId)
                    .when()
                    .delete("/api/v1/courier/{id}")
                    .then()
                    .statusCode(200);
        }
    }


    @Test
    @DisplayName("Успешный логин курьера возвращает ID")
    @Step("Проверка успешного логина курьера")
    public void loginCourierTest() {

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

        courierId = loginResponse.path("id");
    }
    @Test
    @DisplayName("Логин без поля login возвращает ошибку")
    @Step("Проверка ошибки при отсутствии логина")
    public void loginWithoutLoginCourierTest(){
        given()
                .header("Content-type", "application/json")
                .body("{ \"password\":\"" + password + "\"}")
                .when()
                .post("/api/v1/courier/login")
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }
    @Test
    @DisplayName("Логин без поля password возвращает ошибку")
    @Step("Проверка ошибки при отсутствии пароля")
    public void loginWithoutPasswordCourierTest(){
        given()
                .header("Content-type", "application/json")
                .body("{\"login\":\"" + login + "\"}")
                .when()
                .post("/api/v1/courier/login")
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }
    @Test
    @DisplayName("Логин с неправильным паролем возвращает 404")
    @Step("Проверка ошибки при неверном пароле")
    void loginShouldFailWithWrongPassword() {
        given()
                .header("Content-type", "application/json")
                .body("{\"login\":\"" + login + "\", " +
                        "\"password\":\"wrongPassword123\"}")
                .when()
                .post("/api/v1/courier/login")
                .then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }
    @Test
    @DisplayName("Логин с неправильным логином возвращает 404")
    @Step("Проверка ошибки при неверном логине")
    void loginShouldFailWithWrongLogin() {
        given()
                .header("Content-type", "application/json")
                .body("{\"login\":\"troll7485\", " +
                        "\"password\":\"capuchina\"}")
                .when()
                .post("/api/v1/courier/login")
                .then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

}
