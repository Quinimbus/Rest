package cloud.quinimbus.rest.crud;

import cloud.quinimbus.binarystore.api.BinaryStoreException;
import cloud.quinimbus.binarystore.persistence.EmbeddableBinary;
import cloud.quinimbus.persistence.repositories.CRUDRepository;
import cloud.quinimbus.rest.crud.binary.MultipartBinaryHandler;
import cloud.quinimbus.tools.throwing.ThrowingOptional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.function.TriFunction;

public abstract class AbstractCrudSingleResource<T, K> extends AbstractSingleEntityResource<T, K> {

    private final Class<T> entityType;

    private final CRUDRepository<T, K> repository;

    private final MultipartBinaryHandler<T> multipartBinaryHandler;

    // to allow CDI proxy creation
    public AbstractCrudSingleResource() {
        super(null, null);
        this.entityType = null;
        this.repository = null;
        this.multipartBinaryHandler = null;
    }

    public AbstractCrudSingleResource(Class<T> entityType, Class<K> keyType, CRUDRepository<T, K> repository) {
        super(entityType, keyType);
        this.entityType = entityType;
        this.repository = repository;
        this.multipartBinaryHandler = new MultipartBinaryHandler(entityType);
    }

    public Optional<T> findEntityById(UriInfo uriInfo) {
        var id = getId(uriInfo);
        return this.repository.findOne(id);
    }

    public Response getById(UriInfo uriInfo) {
        return this.findEntityById(uriInfo)
                .map(Response::ok)
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    public Response replace(T entity) {
        this.repository.save(entity);
        return Response.accepted().build();
    }

    public Response replaceByMultipart(List<EntityPart> parts) throws IOException {
        var entity = parts.stream()
                .filter(ep -> ep.getName().equalsIgnoreCase("entity"))
                .findFirst()
                .orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST))
                .getContent(this.entityType);
        var entityRef = new AtomicReference<>(entity);
        var otherParts = parts.stream()
                .filter(ep -> !ep.getName().equalsIgnoreCase("entity"))
                .collect(Collectors.toMap(EntityPart::getName, e -> e));
        entityRef.updateAndGet(e -> this.multipartBinaryHandler.setBinaryData(e, otherParts));
        this.repository.save(entityRef.get());
        return Response.accepted().build();
    }

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
        this.multipartBinaryHandler.addBinaryWither(field, wither);
    }

    public void addBinaryListWither(String field, TriFunction<T, Integer, EmbeddableBinary, T> wither) {
        this.multipartBinaryHandler.addBinaryListWither(field, wither);
    }

    public Response downloadBinaryFromList(UriInfo uriInfo, Function<T, List<EmbeddableBinary>> binaryListGetter) {
        var index = Integer.parseInt(uriInfo.getPathParameters().getFirst("binaryPropertyIndex"));
        var embededBinary = this.findEntityById(uriInfo)
                .map(binaryListGetter)
                .filter(l -> l != null && l.size() > index)
                .map(l -> l.get(index));
        return downloadBinary(embededBinary);
    }

    public Response downloadBinary(UriInfo uriInfo, Function<T, EmbeddableBinary> binaryGetter) {
        var embededBinary = this.findEntityById(uriInfo).map(binaryGetter);
        return downloadBinary(embededBinary);
    }

    private Response downloadBinary(Optional<EmbeddableBinary> embededBinary) throws WebApplicationException {
        try {
            return ThrowingOptional.ofOptional(embededBinary, BinaryStoreException.class)
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
