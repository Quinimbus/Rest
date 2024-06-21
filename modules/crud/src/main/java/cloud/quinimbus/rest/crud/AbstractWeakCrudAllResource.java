package cloud.quinimbus.rest.crud;

import cloud.quinimbus.persistence.repositories.WeakCRUDRepository;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class AbstractWeakCrudAllResource<T, K, O> {

    private final Function<UriInfo, Optional<O>> owner;
    private final WeakCRUDRepository<T, K, O> repository;

    public AbstractWeakCrudAllResource(Function<UriInfo, Optional<O>> owner, WeakCRUDRepository<T, K, O> repository) {
        this.owner = owner;
        this.repository = repository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@Context UriInfo uriInfo) {
        return owner.apply(uriInfo)
                .map(o -> Response.ok(this.repository.findAll(o)))
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @GET
    @Path("/as/ids")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllIDs(@Context UriInfo uriInfo) {
        return owner.apply(uriInfo)
                .map(o -> Response.ok(this.repository.findAllIDs(o)))
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postNew(@Context UriInfo uriInfo, T entity) {
        if (owner.apply(uriInfo).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        this.repository.save(entity);
        return Response.accepted().build();
    }

    public <PT> Response getByProperty(
            UriInfo uriInfo, PT property, BiFunction<O, PT, Stream<T>> finderRepositoryMethod) {
        return owner.apply(uriInfo)
                .map(o -> Response.ok(finderRepositoryMethod.apply(o, property).toList()))
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND))
                .build();
    }
}
