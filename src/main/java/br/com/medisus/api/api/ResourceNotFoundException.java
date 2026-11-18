package br.com.medisus.api.api;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found: " + id);
    }
}
