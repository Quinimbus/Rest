package cloud.quinimbus.rest.crud;

import cloud.quinimbus.binarystore.api.BinaryStoreException;
import cloud.quinimbus.binarystore.persistence.EmbeddableBinary;
import cloud.quinimbus.binarystore.persistence.EmbeddableBinaryBuilder;
import cloud.quinimbus.persistence.repositories.CRUDRepository;
import cloud.quinimbus.tools.throwing.ThrowingOptional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class AbstractCrudSingleResource<T, K> extends AbstractSingleEntityResource<T, K> {

    private final Class<T> entityType;

    private final Map<String, BiFunction<T, EmbeddableBinary, T>> binaryWither;

    private final CRUDRepository<T, K> repository;

    // to allow CDI proxy creation
    public AbstractCrudSingleResource() {
        super(null, null);
        this.binaryWither = null;
        this.entityType = null;
        this.repository = null;
    }

    public AbstractCrudSingleResource(Class<T> entityType, Class<K> keyType, CRUDRepository<T, K> repository) {
        super(entityType, keyType);
        this.binaryWither = new LinkedHashMap<>();
        this.entityType = entityType;
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

    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response replaceByMultipart(List<EntityPart> parts) throws IOException {
        var entity = parts.stream()
                .filter(ep -> ep.getName().equalsIgnoreCase("entity"))
                .findFirst()
                .orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST))
                .getContent(this.entityType);
        var entityRef = new AtomicReference<>(entity);
        parts.stream()
                .filter(ep -> !ep.getName().equalsIgnoreCase("entity"))
                .forEach(ep -> entityRef.updateAndGet(e -> this.setBinaryData(e, ep)));
        this.repository.save(entityRef.get());
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

    public void addBinaryWither(String field, BiFunction<T, EmbeddableBinary, T> wither) {
        this.binaryWither.put(field, wither);
    }

    private T setBinaryData(T entity, EntityPart part) {
        var field = part.getName();
        if (binaryWither.containsKey(field)) {
            var binary = EmbeddableBinaryBuilder.builder()
                    .contentType(part.getMediaType().toString())
                    .newContent(part::getContent)
                    .build();
            return this.binaryWither.get(field).apply(entity, binary);
        }
        return entity;
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
