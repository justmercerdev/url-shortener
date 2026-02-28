package com.urlshortener.exception;

public class SlugConflictException extends RuntimeException {
    public SlugConflictException(String message) {
        super(message);
    }
}
