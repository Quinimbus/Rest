package cloud.quinimbus.rest.crud;

import cloud.quinimbus.persistence.repositories.CRUDRepository;
import java.util.Optional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public abstract class AbstractCrudSingleResource<T, K> extends AbstractSingleEntityResource<T, K> {

    private final CRUDRepository<T, K> repository;

    // to allow CDI proxy creation
    public AbstractCrudSingleResource() {
        super(null, null);
        this.repository = null;
    }

    public AbstractCrudSingleResource(Class<T> entityType, Class<K> keyType, CRUDRepository<T, K> repository) {
        super(entityType, keyType);
        this.repository = repository;
    }

    public Optional<T> findEntityById(UriInfo uriInfo) {
        var id = getId(uriInfo);
        return this.repository.findOne(id);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@Context UriInfo uriInfo) {
        return this.findEntityById(uriInfo)
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
        return this.repository
                .findOne(id)
                .map(e -> {
                    this.repository.remove(id);
                    return Response.accepted();
                })
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND))
                .build();
    }
}
