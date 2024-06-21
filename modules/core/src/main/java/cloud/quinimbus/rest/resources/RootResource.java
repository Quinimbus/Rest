package cloud.quinimbus.rest.resources;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
@ApplicationScoped
public class RootResource {

    public record RootView(String appname) {}

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public RootView getAppInfo() {
        return new RootView("Test");
    }
}
