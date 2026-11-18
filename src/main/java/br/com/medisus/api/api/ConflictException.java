package br.com.medisus.api.api;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
