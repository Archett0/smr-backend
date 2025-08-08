package com.team12.listingservice.exception;

public class PropertyExceptions {

    private PropertyExceptions() {
        // prevent instantiation
    }

    public static class PropertyNotFoundException extends RuntimeException {
        public PropertyNotFoundException(Long id) {
            super("Property with ID " + id + " not found.");
        }
    }

    public static class PropertyCreateException extends RuntimeException {
        public PropertyCreateException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class PropertyDeleteException extends RuntimeException {
        public PropertyDeleteException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
