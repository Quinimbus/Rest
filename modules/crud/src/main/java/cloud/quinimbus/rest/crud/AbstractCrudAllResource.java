package cloud.quinimbus.rest.crud;

import cloud.quinimbus.persistence.repositories.CRUDRepository;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.function.Function;
import java.util.stream.Stream;

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
        return Response.ok(this.repository.findAll()).build();
    }

    @GET
    @Path("/as/ids")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllIDs() {
        return Response.ok(this.repository.findAllIDs()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postNew(T entity) {
        this.repository.save(entity);
        return Response.accepted().build();
    }

    public <PT> Response getByProperty(PT property, Function<PT, Stream<T>> finderRepositoryMethod) {
        return Response.ok(finderRepositoryMethod.apply(property).toList()).build();
    }
}
