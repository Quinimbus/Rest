package cloud.quinimbus.rest.crud;

import cloud.quinimbus.persistence.repositories.CRUDRepository;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public abstract class AbstractCrudAllResource<T, K> {

    private final CRUDRepository<T, K> repository;

    // to allow CDI proxy creation
    public AbstractCrudAllResource() {
        this.repository = null;
    }

    public AbstractCrudAllResource(CRUDRepository<T, K> repository) {
        this.repository = repository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        return Response.ok(this.repository.findAll())
                .build();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postNew(T entity) {
        this.repository.save(entity);
        return Response.accepted().build();
    }
}
