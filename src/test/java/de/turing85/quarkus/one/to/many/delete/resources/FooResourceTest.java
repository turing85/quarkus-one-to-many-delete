package de.turing85.quarkus.one.to.many.delete.resources;

import java.time.Duration;

import jakarta.inject.Inject;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.mutiny.pgclient.PgPool;

@QuarkusTest
public class FooResourceTest extends AbstractFooResourceTest {
  @Inject
  @SuppressWarnings("CdiInjectionPointsInspection")
  PgPool client;

  @Override
  protected void createFoo(String fooName) {
    // @formatter:off
    client.query("INSERT INTO public.foo(name) VALUES ('%s')".formatted(fooName))
        .execute().await().atMost(Duration.ofSeconds(5));
    // @formatter:on
  }

  @Override
  protected void createBar(String barName, String fooName) {
    // @formatter:off
    client.query("""
            INSERT INTO public.bar(name, fk_foo)
            VALUES ('%s', (SELECT id FROM public.foo WHERE name = '%s'))
            """.formatted(barName, fooName))
        .execute().await().atMost(Duration.ofSeconds(5));
    // @formatter:on
  }
}
