package br.com.ifba.sididoc.exception;

public class InvalidDocumentTitleException extends RuntimeException {
    public InvalidDocumentTitleException(String message) {
        super(message);
    }
}