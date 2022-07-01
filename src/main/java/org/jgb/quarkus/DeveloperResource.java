package org.jgb.quarkus;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jgb.quarkus.model.Developer;

import io.smallrye.mutiny.Uni;

@Path("/v1/developers")
public class DeveloperResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getAll() {
        return Developer.getAllDevelopers()
                .onItem().transform(devs -> Response.ok(devs))
                .onItem().transform(Response.ResponseBuilder::build);
    }

    @GET
    @Path("/{id}")
    public Uni<Response> get(@PathParam("id") Long id) {
        return Developer.findByDeveloperId(id)
                .onItem().ifNotNull().transform(dev -> Response.ok(dev).build())
                .onItem().ifNull().continueWith(Response.ok().status(Response.Status.NOT_FOUND)::build);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> add(Developer developer) {
        return Developer.addDeveloper(developer)
                .onItem().transform(id -> URI.create("/v1/developers/" + id.id))
                .onItem().transform(uri -> Response.created(uri))
                .onItem().transform(Response.ResponseBuilder::build);
    }

    @PUT
    @Path("{id}")
    public Uni<Response> update(@PathParam("id") Long id, Developer developer) {
        if (developer == null || developer.firstName == null) {
            // 422 - Unprocessable Entity
            throw new WebApplicationException("Developer first name was not set on request.", 422);
        }
        return Developer.updateDeveloper(id, developer)
                .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                .onItem().ifNull().continueWith(Response.ok().status(Response.Status.NOT_FOUND)::build);
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(@PathParam("id") Long id) {
        return Developer.deleteDeveloper(id)
                .onItem().transform(entity -> !entity ? Response.serverError().status(Response.Status.NOT_FOUND).build()
                        : Response.ok().status(Response.Status.NO_CONTENT).build());
    }

}
