package cloud.quinimbus.rest.crud.binary;

import cloud.quinimbus.binarystore.persistence.EmbeddableBinary;
import cloud.quinimbus.binarystore.persistence.EmbeddableBinaryBuilder;
import jakarta.ws.rs.core.EntityPart;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import org.apache.commons.lang3.function.TriFunction;

public class MultipartBinaryHandler<T> {

    private final Class<T> entityType;

    private final Map<String, BiFunction<T, EmbeddableBinary, T>> binaryWither;

    private final Map<String, TriFunction<T, Integer, EmbeddableBinary, T>> binaryListWither;

    public MultipartBinaryHandler(Class<T> entityType) {
        this.entityType = entityType;
        this.binaryWither = new LinkedHashMap<>();
        this.binaryListWither = new LinkedHashMap<>();
    }

    public void addBinaryWither(String field, BiFunction<T, EmbeddableBinary, T> wither) {
        this.binaryWither.put(field, wither);
    }

    public void addBinaryListWither(String field, TriFunction<T, Integer, EmbeddableBinary, T> wither) {
        this.binaryListWither.put(field, wither);
    }

    public T setBinaryData(T entity, Map<String, EntityPart> parts) {
        for (var binaryField : this.binaryWither.entrySet()) {
            try {
                var getter = this.entityType.getDeclaredMethod(binaryField.getKey());
                var binary = (EmbeddableBinary) getter.invoke(entity);
                if (binary != null) {
                    if (binary.multipartId() != null) {
                        var part = parts.get(binary.multipartId());
                        var newBinary = EmbeddableBinaryBuilder.builder()
                                .contentType(part.getMediaType().toString())
                                .newContent(part::getContent)
                                .build();
                        entity = binaryField.getValue().apply(entity, newBinary);
                    }
                }
            } catch (NoSuchMethodException
                    | SecurityException
                    | IllegalAccessException
                    | InvocationTargetException ex) {
                throw new IllegalStateException(ex);
            }
        }
        for (var binaryListField : this.binaryListWither.entrySet()) {
            try {
                var getter = this.entityType.getDeclaredMethod(binaryListField.getKey());
                var binaryList = (List<EmbeddableBinary>) getter.invoke(entity);
                if (binaryList != null) {
                    for (int i = 0; i < binaryList.size(); i++) {
                        var binary = binaryList.get(i);
                        if (binary.multipartId() != null) {
                            var part = parts.get(binary.multipartId());
                            if (part == null) {
                                throw new IllegalArgumentException(
                                        "Missing part in multipart request referenced in the entity: %s"
                                                .formatted(binary.multipartId()));
                            }
                            var newBinary = EmbeddableBinaryBuilder.builder()
                                    .contentType(part.getMediaType().toString())
                                    .newContent(part::getContent)
                                    .build();
                            entity = binaryListField.getValue().apply(entity, i, newBinary);
                        }
                    }
                }
            } catch (NoSuchMethodException
                    | SecurityException
                    | IllegalAccessException
                    | InvocationTargetException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return entity;
    }
}
