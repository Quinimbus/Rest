package cloud.quinimbus.rest.crud;

import cloud.quinimbus.binarystore.persistence.EmbeddableBinary;
import cloud.quinimbus.binarystore.persistence.EmbeddableBinaryBuilder;
import cloud.quinimbus.persistence.repositories.CRUDRepository;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class AbstractCrudAllResource<T, K> {

    private final Class<T> entityType;

    private final Map<String, BiFunction<T, EmbeddableBinary, T>> binaryWither;

    private final CRUDRepository<T, K> repository;

    // to allow CDI proxy creation
    public AbstractCrudAllResource() {
        this.binaryWither = null;
        this.entityType = null;
        this.repository = null;
    }

    public AbstractCrudAllResource(Class<T> entityType, CRUDRepository<T, K> repository) {
        this.binaryWither = new LinkedHashMap<>();
        this.entityType = entityType;
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

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response postNewByMultipart(List<EntityPart> parts) throws IOException {
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

    public <PT> Response getByProperty(PT property, Function<PT, Stream<T>> finderRepositoryMethod) {
        return Response.ok(finderRepositoryMethod.apply(property).toList()).build();
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
}
