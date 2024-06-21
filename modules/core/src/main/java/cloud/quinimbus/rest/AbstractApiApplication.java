package cloud.quinimbus.rest;

import cloud.quinimbus.rest.resources.RootResource;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractApiApplication extends Application {

    private final Set<Class<?>> classes;

    public AbstractApiApplication() {
        this.classes = new HashSet<>();
        this.addClass(RootResource.class);
        this.addClass(ObjectMapperResolver.class);
    }

    public void addClass(Class<?> cls) {
        this.classes.add(cls);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return Set.copyOf(this.classes);
    }
}
