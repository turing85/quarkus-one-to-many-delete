package de.turing85.quarkus.one.to.many.delete.resources;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

@TestHTTPEndpoint(FooResource.class)
abstract class AbstractFooResourceTest {
  protected abstract void createFoo(String fooName);

  protected abstract void createBar(String barName, String fooName);

  @Test
  void persistFoo() {
    // GIVEN
    final String expectedFooName = "foo1";
    // @formatter:off
    RestAssured
        .given()
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                {
                  "name": "%s"
                }
                """.formatted(expectedFooName))

    // WHEN
        .when().post()

    // THEN
        .then()
            .statusCode(is(Response.Status.CREATED.getStatusCode()))
            .contentType(startsWith(MediaType.APPLICATION_JSON))
            .header(HttpHeaders.LOCATION, is(locationOfFoo(expectedFooName)))
            .body("name", is(expectedFooName))
            .body("bars", hasSize(0));
    assertFooWithoutBarExists(expectedFooName);
    // @formatter:on
  }

  @Test
  void persistFooWithBar() {
    // GIVEN
    final String expectedFooName = "foo2";
    final String expectedBarName = "bar2";
    // @formatter:off
    RestAssured
        .given()
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                {
                  "name": "%s",
                  "bars": [
                    {
                      "name": "%s"
                    }
                  ]
                }
                """.formatted(expectedFooName, expectedBarName))

    // WHEN
        .when().post()

    // THEN
        .then()
            .statusCode(is(Response.Status.CREATED.getStatusCode()))
            .contentType(startsWith(MediaType.APPLICATION_JSON))
            .header(HttpHeaders.LOCATION, is(locationOfFoo(expectedFooName)))
            .body("name", is(expectedFooName))
            .body("bars", hasSize(1))
            .body("bars.collect { it.name }", hasItem(expectedBarName));
    assertFooWithBarExists(expectedFooName, expectedBarName);
    // @formatter:on
  }

  @Test
  void persistBar() {
    // GIVEN
    final String expectedFooName = "foo3";
    final String expectedBarName = "bar3";
    // @formatter:off
    createFoo(expectedFooName);

    RestAssured
        .given()
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                {
                  "name": "%s"
                }
               """.formatted(expectedBarName))

    // WHEN
        .when().post("/%s/bars".formatted(expectedFooName))

    // THEN
        .then()
            .statusCode(is(Response.Status.CREATED.getStatusCode()))
            .contentType(startsWith(MediaType.APPLICATION_JSON))
            .header(HttpHeaders.LOCATION, is(locationOfBar(expectedFooName, expectedBarName)))
            .body("name", is(expectedBarName));
    assertFooWithBarExists(expectedFooName, expectedBarName);
    // @formatter:on
  }

  @Test
  void deleteFoo() {
    // GIVEN
    final String expectedFooName = "foo4";
    // @formatter:off
    createFoo(expectedFooName);

    // WHEN
    RestAssured
        .when().delete("/%s".formatted(expectedFooName))

    // THEN
        .then()
            .statusCode(is(Response.Status.NO_CONTENT.getStatusCode()))
            .body(is(emptyString()));
    RestAssured
        .when().get()
        .then()
            .statusCode(is(Response.Status.OK.getStatusCode()))
            .contentType(startsWith(MediaType.APPLICATION_JSON))
            .body("collect { it.name }", not(hasItem(expectedFooName)));
    // @formatter:on
  }

  @Test
  void deleteBar() {
    // GIVEN
    final String expectedFooName = "foo5";
    final String expectedBarName = "bar5";
    // @formatter:off
    createFooWithBar(expectedFooName, expectedBarName);

    // WHEN
    RestAssured
        .when().delete("/%s/bars/%s".formatted(expectedFooName, expectedBarName))

    // THEN
        .then()
            .statusCode(is(Response.Status.NO_CONTENT.getStatusCode()))
            .body(is(emptyString()));
    assertFooWithoutBarExists(expectedFooName);
    // @formatter:on
  }

  protected void createFooWithBar(String fooName, String barName) {
    createFoo(fooName);
    createBar(barName, fooName);
  }

  static String locationOfBar(String fooName, String barName) {
    return "%s/bars/%s".formatted(locationOfFoo(fooName), barName);
  }

  static String locationOfFoo(String fooName) {
    return "%s/foos/%s".formatted(baseUrl(), fooName);
  }

  static String baseUrl() {
    return "%s:%d".formatted(RestAssured.baseURI, RestAssured.port);
  }

  private static void assertFooWithoutBarExists(String expectedFooName) {
    // @formatter:off
    RestAssured
        .when().get()
        .then()
            .statusCode(is(Response.Status.OK.getStatusCode()))
            .contentType(startsWith(MediaType.APPLICATION_JSON))
            .body("collect { it.name }", hasItem(expectedFooName));
    RestAssured
        .when().get("/%s".formatted(expectedFooName))
        .then()
            .statusCode(is(Response.Status.OK.getStatusCode()))
            .contentType(startsWith(MediaType.APPLICATION_JSON))
            .body("name", is(expectedFooName));
    RestAssured
        .when().get("/%s/bars".formatted(expectedFooName))
        .then()
            .statusCode(is(Response.Status.OK.getStatusCode()))
            .contentType(startsWith(MediaType.APPLICATION_JSON))
            .body("", hasSize(0));
    // @formatter:on
  }

  private static void assertFooWithBarExists(String expectedFooName, String expectedBarName) {
    // @formatter:off
    RestAssured
        .when().get()
        .then()
            .statusCode(is(Response.Status.OK.getStatusCode()))
            .contentType(startsWith(MediaType.APPLICATION_JSON))
            .body("collect { it.name }", hasItem(expectedFooName))
            .body("find { it.name == '%s' }.bars".formatted(expectedFooName), hasSize(1))
            .body(
                "find { it.name == '%s' }.bars.collect { it.name }".formatted(expectedFooName),
                hasItem(expectedBarName));
    RestAssured
        .when().get("/%s".formatted(expectedFooName))
        .then()
            .statusCode(is(Response.Status.OK.getStatusCode()))
            .contentType(startsWith(MediaType.APPLICATION_JSON))
            .body("name", is(expectedFooName))
            .body("bars", hasSize(1))
            .body("bars. collect { it.name }", hasItem(expectedBarName));
    RestAssured
        .when().get("/%s/bars".formatted(expectedFooName))
        .then()
            .statusCode(is(Response.Status.OK.getStatusCode()))
            .contentType(startsWith(MediaType.APPLICATION_JSON))
            .body("", hasSize(1))
            .body("collect { it.name }", hasItem(expectedBarName));
    RestAssured
        .when().get("/%s/bars/%s".formatted(expectedFooName, expectedBarName))
        .then()
            .statusCode(is(Response.Status.OK.getStatusCode()))
            .contentType(startsWith(MediaType.APPLICATION_JSON))
            .body("name", is(expectedBarName));
    // @formatter:on
  }
}
