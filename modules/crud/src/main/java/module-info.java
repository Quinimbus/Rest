module cloud.quinimbus.rest.crud {
    exports cloud.quinimbus.rest.crud;

    requires cloud.quinimbus.binarystore.api;
    requires cloud.quinimbus.binarystore.persistence;
    requires cloud.quinimbus.persistence.repositories;
    requires cloud.quinimbus.tools;
    requires jakarta.ws.rs;
    requires throwing.interfaces;
}
