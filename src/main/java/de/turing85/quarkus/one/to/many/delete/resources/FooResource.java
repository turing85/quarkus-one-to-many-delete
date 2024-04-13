package de.turing85.quarkus.one.to.many.delete.resources;

import java.net.URI;
import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import de.turing85.quarkus.one.to.many.delete.entities.Bar;
import de.turing85.quarkus.one.to.many.delete.entities.Foo;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;

@Path("foos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FooResource {
  @GET
  @WithTransaction
  public Uni<Response> getAllFoos() {
    // @formatter:off
    return Foo.findAll().list()
        .onItem().transform(FooResource::toOkResponse);
    // @formatter:on
  }

  @GET
  @Path("{name}")
  @WithTransaction
  public Uni<Response> getFooByName(@PathParam("name") String name) {
    // @formatter:off
    return Foo.find("name", name).singleResult()
        .onItem().transform(FooResource::toOkResponse);
    // @formatter:on
  }

  @POST
  @WithTransaction
  public Uni<Response> createFoo(Foo foo) {
    // @formatter:off
    return foo.<Foo>persist()
        .onItem().transform(persisted -> FooResource.toCreatedResponse(
            persisted,
            URI.create("foos/%s".formatted(persisted.getName()))));
    // @formatter:on
  }

  @DELETE
  @Path("{name}")
  @WithTransaction
  public Uni<Response> deleteFooByName(@PathParam("name") String name) {
    // @formatter:off
    return Foo.delete("name", name)
        .replaceWith(Response.noContent()::build);
    // @formatter:on
  }

  @GET
  @Path("{fooName}/bars")
  @WithTransaction
  public Uni<Response> getAllBarsByFooName(@PathParam("fooName") String fooName) {
    // @formatter:off
    return Bar
        .<Bar>find("""
                SELECT b FROM Bar b
                JOIN b.foo f
                WHERE f.name = ?1""",
            fooName)
        .list()
        .onItem().transform(FooResource::toOkResponse);
    // @formatter:on
  }

  @GET
  @Path("{fooName}/bars/{barName}")
  @WithTransaction
  public Uni<Response> getBarByFooNameAndBarName(@PathParam("fooName") String fooName,
      @PathParam("barName") String barName) {
    // @formatter:off
    return Bar
        .find("""
                SELECT DISTINCT b FROM Bar b
                INNER JOIN b.foo f
                WHERE f.name = :fooName AND b.name = :barName""",
            Map.of("fooName", fooName, "barName", barName)).singleResult()
        .onItem().transform(FooResource::toOkResponse);
    // @formatter:on
  }

  @POST
  @Path("{name}/bars")
  @WithTransaction
  public Uni<Response> createBar(@PathParam("name") String fooName, Bar bar) {
    // @formatter:off
    return Foo.<Foo>find("name", fooName)
        .singleResult()
        .onItem().invoke(foo -> foo.getBars().add(bar))
        .onItem().invoke(bar::setFoo)
        .onItem().transform(foo -> foo.persist())
        .onItem().transform(foo -> bar)
        .onItem().transform(persisted -> FooResource.toCreatedResponse(
            persisted,
            URI.create("foos/%s/bars/%s".formatted(fooName, persisted.getName()))));
    // @formatter:on
  }

  @DELETE
  @Path("{fooName}/bars/{barName}")
  @WithTransaction
  public Uni<Response> deleteBarByFooNameAndBarName(@PathParam("fooName") String fooName,
      @PathParam("barName") String barName) {
    // @formatter:off
    return Bar
        .delete(
            """
                DELETE FROM Bar b WHERE b.id IN
                (
                    SELECT b.id FROM Bar bb INNER JOIN bb.foo f
                    WHERE f.name = :fooName AND b.name = :barName
                )""",
            Map.of("fooName", fooName, "barName", barName))
        .replaceWith(Response.noContent()::build);
    // @formatter:on
  }

  private static Response toOkResponse(Object o) {
    return toResponse(o, Response.Status.OK.getStatusCode());
  }

  private static Response toResponse(Object o, int statusCode) {
    // @formatter:off
    return Response
        .status(statusCode)
        .entity(o)
        .build();
    // @formatter:on
  }

  private static Response toCreatedResponse(Object o, URI uri) {
    return Response.created(uri).entity(o).build();
  }
}
