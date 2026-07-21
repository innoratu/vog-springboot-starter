package com.vog.example.vog_demo.exception;

/** Thrown when deleting an entity that is still referenced by others. */
public class InUseException extends RuntimeException {

    public InUseException(String message) {
        super(message);
    }
}
