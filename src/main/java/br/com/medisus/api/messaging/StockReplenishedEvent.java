package br.com.medisus.api.messaging;

public record StockReplenishedEvent(Long stockId, Long medicationId, String medicationName,
                                    Long ubsId, String ubsName) {}
