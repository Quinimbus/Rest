package cloud.quinimbus.rest.crud;

import jakarta.ws.rs.core.UriInfo;

public abstract class AbstractSingleEntityResource<T, K> {

    private final Class<T> entityType;
    private final Class<K> keyType;

    public abstract String idPathParameter();

    public AbstractSingleEntityResource(Class<T> entityType, Class<K> keyType) {
        this.entityType = entityType;
        this.keyType = keyType;
    }

    K getId(UriInfo uriInfo) {
        var id = uriInfo.getPathParameters().getFirst(idPathParameter());
        if (id == null) {
            throw new IllegalStateException("Failed to read the id \"%s\" from the path".formatted(idPathParameter()));
        }
        if (String.class.equals(keyType)) {
            return (K) id;
        }
        if (Long.class.equals(keyType)) {
            return (K) Long.valueOf(id);
        }
        if (Integer.class.equals(keyType)) {
            return (K) Integer.valueOf(id);
        }
        throw new IllegalArgumentException("Cannot convert an id of type %s from string".formatted(keyType.getName()));
    }
}
