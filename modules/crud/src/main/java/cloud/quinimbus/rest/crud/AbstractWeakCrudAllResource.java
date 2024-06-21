package cloud.quinimbus.rest.crud;

import cloud.quinimbus.persistence.repositories.WeakCRUDRepository;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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
    
    public <PT> Response getByProperty(UriInfo uriInfo, PT property, BiFunction<O, PT, Stream<T>> finderRepositoryMethod) {
        return owner.apply(uriInfo)
                .map(o -> Response.ok(finderRepositoryMethod.apply(o, property).toList()))
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND))
                .build();
    }
}
