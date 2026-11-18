package br.com.medisus.api.ai;

@FunctionalInterface
public interface PrescriptionModel {
    String identify(byte[] image, String contentType);
}
