package cloud.quinimbus.rest.crud;

import cloud.quinimbus.persistence.repositories.WeakCRUDRepository;
import java.util.Optional;
import java.util.function.Function;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public abstract class AbstractWeakCrudSingleResource<T, K, O> extends AbstractSingleEntityResource<T, K> {

    private final Class<T> entityType;
    private final Class<K> keyType;
    private final Function<UriInfo, Optional<O>> owner;
    private final WeakCRUDRepository<T, K, O> repository;

    public AbstractWeakCrudSingleResource(Class<T> entityType, Class<K> keyType, Function<UriInfo, Optional<O>> owner, WeakCRUDRepository<T, K, O> repository) {
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
        return owner.apply(uriInfo).flatMap(o -> this.repository.findOne(o, id)
                .map(e -> {
                    this.repository.remove(o, id);
                    return Response.accepted();
                }))
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND))
                .build();
    }
}
