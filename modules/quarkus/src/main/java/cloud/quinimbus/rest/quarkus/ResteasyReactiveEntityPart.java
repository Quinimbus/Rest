package cloud.quinimbus.rest.quarkus;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.jboss.resteasy.reactive.server.core.ResteasyReactiveRequestContext;
import org.jboss.resteasy.reactive.server.core.multipart.MultipartSupport;

public class ResteasyReactiveEntityPart implements EntityPart {

    private final String name;

    private final String contentType;

    private final InputStream inputStream;

    private final ResteasyReactiveRequestContext context;

    public ResteasyReactiveEntityPart(
            String name, String contentType, InputStream inputStream, ResteasyReactiveRequestContext context) {
        this.name = name;
        this.contentType = contentType;
        this.inputStream = inputStream;
        this.context = context;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<String> getFileName() {
        return Optional.empty();
    }

    @Override
    public InputStream getContent() {
        return inputStream;
    }

    @Override
    public <T> T getContent(Class<T> type)
            throws IllegalArgumentException, IllegalStateException, IOException, WebApplicationException {
        return (T) MultipartSupport.getConvertedFormAttribute(name, type, null, getMediaType(), context);
    }

    @Override
    public <T> T getContent(GenericType<T> type)
            throws IllegalArgumentException, IllegalStateException, IOException, WebApplicationException {
        return (T) MultipartSupport.getConvertedFormAttribute(
                name, type.getRawType(), type.getType(), getMediaType(), context);
    }

    @Override
    public MultivaluedMap<String, String> getHeaders() {
        return new MultivaluedHashMap<>();
    }

    @Override
    public MediaType getMediaType() {
        return MediaType.valueOf(contentType);
    }
}
