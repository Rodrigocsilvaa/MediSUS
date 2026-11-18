package br.com.medisus.api.ai;

public class AiUnavailableException extends RuntimeException {
    public AiUnavailableException() { super("Prescription identification is unavailable; configure the AI provider and credentials"); }
    public AiUnavailableException(Throwable cause) { super("Prescription identification is temporarily unavailable", cause); }
}
