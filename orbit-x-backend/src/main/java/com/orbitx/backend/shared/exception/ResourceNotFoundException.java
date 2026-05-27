package com.orbitx.backend.shared.exception;

public class ResourceNotFoundException extends OrbitXException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found with id: " + id);
    }
}
