package cloud.quinimbus.rest.crud;

import cloud.quinimbus.persistence.repositories.WeakCRUDRepository;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.Optional;
import java.util.function.Function;

public abstract class AbstractWeakCrudSingleResource<T, K, O> extends AbstractSingleEntityResource<T, K> {

    private final Class<T> entityType;
    private final Class<K> keyType;
    private final Function<UriInfo, Optional<O>> owner;
    private final WeakCRUDRepository<T, K, O> repository;

    public AbstractWeakCrudSingleResource(
            Class<T> entityType,
            Class<K> keyType,
            Function<UriInfo, Optional<O>> owner,
            WeakCRUDRepository<T, K, O> repository) {
        super(entityType, keyType);
        this.entityType = entityType;
        this.keyType = keyType;
        this.owner = owner;
        this.repository = repository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@Context UriInfo uriInfo) {
        return owner.apply(uriInfo)
                .flatMap(o -> this.repository.findOne(o, getId(uriInfo)))
                .map(Response::ok)
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response replace(T entity) {
        this.repository.save(entity);
        return Response.accepted().build();
    }

    @DELETE
    public Response deleteById(@Context UriInfo uriInfo) {
        var id = getId(uriInfo);
        return owner.apply(uriInfo)
                .flatMap(o -> this.repository.findOne(o, id).map(e -> {
                    this.repository.remove(o, id);
                    return Response.accepted();
                }))
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND))
                .build();
    }
}
