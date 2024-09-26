package kappzzang.jeongsan.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static kappzzang.jeongsan.global.common.enumeration.ErrorType.USER_NOT_FOUND;
import static kappzzang.jeongsan.global.common.enumeration.SuccessType.TEAM_CREATED;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExampleControllerTest {

    @LocalServerPort
    private int port;

    @Test
    void successWithNoData() {

        given()
                .baseUri("http://localhost:" + port)
                .when()
                .get("/api/test/success")
                .then()
                .statusCode(TEAM_CREATED.getHttpStatus().value());
    }


    @Test
    void successWithData() {

        given()
                .baseUri("http://localhost:" + port)
                .when()
                .post("/api/test/success/data")
                .then()
                .statusCode(TEAM_CREATED.getHttpStatus().value());
    }

    @Test
    void failure() {

        given()
                .baseUri("http://localhost:" + port)
                .when()
                .get("/api/test/failure")
                .then()
                .statusCode(USER_NOT_FOUND.getHttpStatus().value());
    }
}
