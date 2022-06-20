package cloud.quinimbus.rest.crud;

import cloud.quinimbus.persistence.repositories.CRUDRepository;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public abstract class AbstractCrudSingleResource<T, K> {

    private final CRUDRepository<T, K> repository;

    // to allow CDI proxy creation
    public AbstractCrudSingleResource() {
        this.repository = null;
    }

    public AbstractCrudSingleResource(CRUDRepository<T, K> repository) {
        this.repository = repository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("entityid") K id) {
        return this.repository.findOne(id)
                .map(e -> Response.ok(e))
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
    public Response deleteById(@PathParam("entityid") K id) {
        return this.repository.findOne(id)
                .map(e -> {
                    this.repository.remove(id);
                    return Response.accepted();
                })
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND))
                .build();
    }
}
