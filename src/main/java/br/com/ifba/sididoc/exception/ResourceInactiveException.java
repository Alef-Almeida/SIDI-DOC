package br.com.ifba.sididoc.exception;

public class ResourceInactiveException extends RuntimeException {
    public ResourceInactiveException(String message) {
        super(message);
    }
}