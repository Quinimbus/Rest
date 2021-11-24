package cloud.quinimbus.rest.resources;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
