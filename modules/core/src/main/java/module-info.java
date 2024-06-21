module cloud.quinimbus.rest {
    exports cloud.quinimbus.rest;
    exports cloud.quinimbus.rest.resources;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires jakarta.enterprise.cdi.api;
    requires java.ws.rs;
}
