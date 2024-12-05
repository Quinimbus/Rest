package cloud.quinimbus.rest.crud;

import cloud.quinimbus.binarystore.persistence.EmbeddableBinary;
import cloud.quinimbus.persistence.repositories.CRUDRepository;
import cloud.quinimbus.rest.crud.binary.MultipartBinaryHandler;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.function.TriFunction;

public abstract class AbstractCrudAllResource<T, K> {

    private final Class<T> entityType;

    private final CRUDRepository<T, K> repository;

    private final MultipartBinaryHandler<T> multipartBinaryHandler;

    // to allow CDI proxy creation
    public AbstractCrudAllResource() {
        this.entityType = null;
        this.repository = null;
        this.multipartBinaryHandler = null;
    }

    public AbstractCrudAllResource(Class<T> entityType, CRUDRepository<T, K> repository) {
        this.entityType = entityType;
        this.repository = repository;
        this.multipartBinaryHandler = new MultipartBinaryHandler(entityType);
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
        var otherParts = parts.stream()
                .filter(ep -> !ep.getName().equalsIgnoreCase("entity"))
                .collect(Collectors.toMap(EntityPart::getName, e -> e));
        entityRef.updateAndGet(e -> this.multipartBinaryHandler.setBinaryData(e, otherParts));
        this.repository.save(entityRef.get());
        return Response.accepted().build();
    }

    public <PT> Response getByProperty(PT property, Function<PT, Stream<T>> finderRepositoryMethod) {
        return Response.ok(finderRepositoryMethod.apply(property).toList()).build();
    }

    public void addBinaryWither(String field, BiFunction<T, EmbeddableBinary, T> wither) {
        this.multipartBinaryHandler.addBinaryWither(field, wither);
    }

    public void addBinaryListWither(String field, TriFunction<T, Integer, EmbeddableBinary, T> wither) {
        this.multipartBinaryHandler.addBinaryListWither(field, wither);
    }

    public <MT> Response getAllMapped(Function<T, MT> mapper) {
        return Response.ok(this.repository.findAll().stream().map(mapper).toList())
                .build();
    }
}
