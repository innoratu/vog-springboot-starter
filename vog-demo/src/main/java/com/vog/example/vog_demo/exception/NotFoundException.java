package com.vog.example.vog_demo.exception;

/** Thrown when an entity referenced by id does not exist. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
