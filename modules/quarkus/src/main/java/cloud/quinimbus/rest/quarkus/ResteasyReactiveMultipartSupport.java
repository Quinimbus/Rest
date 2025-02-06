package cloud.quinimbus.rest.quarkus;

import jakarta.ws.rs.core.EntityPart;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;
import name.falgout.jeffrey.throwing.stream.ThrowingStream;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jboss.resteasy.reactive.server.core.ResteasyReactiveRequestContext;

public class ResteasyReactiveMultipartSupport {

    public static List<EntityPart> convert(List<FileUpload> uploads, ResteasyReactiveRequestContext context)
            throws IOException {
        return ThrowingStream.of(uploads.stream(), IOException.class)
                .map(fu -> new ResteasyReactiveEntityPart(
                        fu.name(),
                        fu.contentType(),
                        Files.newInputStream(fu.uploadedFile(), StandardOpenOption.READ),
                        context))
                .collect(Collectors.toList());
    }
}
