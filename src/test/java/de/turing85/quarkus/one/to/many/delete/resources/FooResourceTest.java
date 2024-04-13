package de.turing85.quarkus.one.to.many.delete.resources;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.vertx.mutiny.pgclient.PgPool;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestHTTPEndpoint(FooResource.class)
class FooResourceTest {
  @Inject
  @SuppressWarnings("CdiInjectionPointsInspection")
  PgPool client;

  @ConfigProperty(name = "quarkus.http.test-port")
  int testPort;

  @BeforeEach
  void setup() {
    client.query("DELETE FROM bar").execute().await().atMost(Duration.ofSeconds(5));
    client.query("DELETE FROM foo").execute().await().atMost(Duration.ofSeconds(5));
  }

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
            .header(
                HttpHeaders.LOCATION,
                is("http://localhost:%d/foos/%s".formatted(testPort, expectedFooName)))
            .body("name", is(expectedFooName))
            .body("bars", hasSize(0));
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
            .header(
                HttpHeaders.LOCATION,
                is("http://localhost:%d/foos/%s".formatted(testPort, expectedFooName)))
            .body("name", is(expectedFooName))
            .body("bars", hasSize(1))
            .body("bars.collect { it.name }", hasItem(expectedBarName));
    RestAssured
        .when().get()
        .then()
            .statusCode(is(Response.Status.OK.getStatusCode()))
            .contentType(startsWith(MediaType.APPLICATION_JSON))
            .body("collect { it.name }", hasItem(expectedFooName))
            .body("find { it.name = '%s' }.bars".formatted(expectedFooName), hasSize(1))
            .body(
                "find { it.name = '%s' }.bars.collect { it.name }".formatted(expectedFooName),
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

  @Test
  void persistBar() {
    // GIVEN
    final String expectedFooName = "foo3";
    final String expectedBarName = "bar3";
    // @formatter:off
    client.query("INSERT INTO public.foo(name) VALUES ('%s')".formatted(expectedFooName))
        .executeAndAwait();

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
            .header(
                HttpHeaders.LOCATION,
                is("http://localhost:%d/foos/%s/bars/%s"
                    .formatted(testPort, expectedFooName, expectedBarName)))
            .body("name", is(expectedBarName));
    RestAssured
        .when().get()
        .then()
            .statusCode(is(Response.Status.OK.getStatusCode()))
            .contentType(startsWith(MediaType.APPLICATION_JSON))
            .body("collect { it.name }", hasItem(expectedFooName))
            .body("find { it.name = '%s' }.bars".formatted(expectedFooName), hasSize(1))
            .body(
                "find { it.name = '%s' }.bars.collect { it.name }".formatted(expectedFooName),
                hasItem(expectedBarName));
    RestAssured
        .when().get("/%s".formatted(expectedFooName))
        .then()
            .statusCode(is(Response.Status.OK.getStatusCode()))
            .contentType(startsWith(MediaType.APPLICATION_JSON))
            .body("name", is(expectedFooName))
            .body("bars", hasSize(1))
            .body("bars.collect { it.name }", hasItem(expectedBarName));
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

  @Test
  void deleteFoo() {
    // GIVEN
    final String expectedFooName = "foo4";
    // @formatter:off
    client.query("INSERT INTO public.foo(name) VALUES ('%s')".formatted(expectedFooName))
        .executeAndAwait();

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
            .body("collect { it.name }".formatted(expectedFooName), not(hasItem(expectedFooName)));
    // @formatter:on
  }

  @Test
  void deleteBar() {
    // GIVEN
    final String expectedFooName = "foo5";
    final String expectedBarName = "bar5";
    // @formatter:off
    client.query("INSERT INTO public.foo(name) VALUES ('%s')".formatted(expectedFooName))
        .executeAndAwait();
    client
        .query("""
            INSERT INTO public.bar(name, fk_foo)
            VALUES ('%s', (SELECT id FROM public.foo WHERE name = '%s'))
            """
            .formatted(expectedBarName, expectedFooName))
        .executeAndAwait();

    // WHEN
    RestAssured
        .when().delete("/%s/bars/%s".formatted(expectedFooName, expectedBarName))

    // THEN
        .then()
            .statusCode(is(Response.Status.NO_CONTENT.getStatusCode()))
            .body(is(emptyString()));
    RestAssured
        .when().get()
        .then()
            .statusCode(is(Response.Status.OK.getStatusCode()))
            .contentType(startsWith(MediaType.APPLICATION_JSON))
            .body(".", hasSize(1))
            .body("collect { it.name }", hasItem(expectedFooName))
            .body("find { it.name = '%s' }.bars".formatted(expectedFooName), hasSize(0));
    RestAssured
        .when().get("/%s".formatted(expectedFooName))
        .then()
            .statusCode(is(Response.Status.OK.getStatusCode()))
            .contentType(startsWith(MediaType.APPLICATION_JSON))
            .body("name", is(expectedFooName))
            .body("bars", hasSize(0));
    RestAssured
        .when().get("/%s/bars".formatted(expectedFooName))
        .then()
            .statusCode(is(Response.Status.OK.getStatusCode()))
            .contentType(startsWith(MediaType.APPLICATION_JSON))
            .body("", hasSize(0));
    // @formatter:on
  }
}
