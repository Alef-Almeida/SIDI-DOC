package br.com.ifba.sididoc.exception;

public class SectorAccessDeniedException extends RuntimeException{
    public SectorAccessDeniedException(String message) {
        super(message);
    }
}
