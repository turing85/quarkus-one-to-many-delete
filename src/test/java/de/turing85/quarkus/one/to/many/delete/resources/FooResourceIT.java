package de.turing85.quarkus.one.to.many.delete.resources;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

@QuarkusIntegrationTest
@TestHTTPEndpoint(FooResource.class)
class FooResourceIT extends AbstractFooResourceTest {
  @Override
  protected void createFoo(String fooName) {
    // @formatter:off
    RestAssured
        .given()
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                    {
                      "name": "%s"
                    }
                    """.formatted(fooName))
            .when().post()
            .then()
                .statusCode(is(Response.Status.CREATED.getStatusCode()))
                .contentType(startsWith(MediaType.APPLICATION_JSON))
                .header(HttpHeaders.LOCATION, is(locationOfFoo(fooName)))
                .body("name", is(fooName));
    // @formatter:on
  }

  @Override
  protected void createBar(String barName, String fooName) {
    // @formatter:off
    RestAssured
        .given()
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                {
                  "name": "%s"
                }
                """.formatted(barName))
        .when().post("/%s/bars".formatted(fooName))
        .then()
            .statusCode(is(Response.Status.CREATED.getStatusCode()))
            .contentType(startsWith(MediaType.APPLICATION_JSON))
            .header(HttpHeaders.LOCATION, is(locationOfBar(fooName, barName)))
            .body("name", is(barName));
    // @formatter:on
  }
}
