package cloud.quinimbus.rest.crud;

import cloud.quinimbus.binarystore.api.BinaryStoreException;
import cloud.quinimbus.binarystore.persistence.EmbeddableBinary;
import cloud.quinimbus.persistence.repositories.CRUDRepository;
import cloud.quinimbus.tools.throwing.ThrowingOptional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.Optional;
import java.util.function.Function;

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

    public Response downloadBinary(UriInfo uriInfo, Function<T, EmbeddableBinary> binaryGetter) {
        try {
            return ThrowingOptional.ofOptional(
                            this.findEntityById(uriInfo).map(binaryGetter), BinaryStoreException.class)
                    .map(b -> Response.ok(b.contentLoader().get(), b.contentType())
                            .header("Content-Disposition", "attachment; filename=%s".formatted(b.id()))
                            .build())
                    .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
        } catch (BinaryStoreException ex) {
            throw new WebApplicationException(ex);
        }
    }

    public <MT> Response getByIdMapped(@Context UriInfo uriInfo, Function<T, MT> mapper) {
        return this.findEntityById(uriInfo)
                .map(mapper)
                .map(Response::ok)
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND))
                .build();
    }
}
