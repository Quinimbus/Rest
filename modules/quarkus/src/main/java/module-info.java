module cloud.quinimbus.rest.quarkus {
    exports cloud.quinimbus.rest.quarkus;

    requires jakarta.ws.rs;
    requires resteasy.reactive.common;
    requires resteasy.reactive;
    requires throwing.interfaces;
    requires throwing.streams;
}
