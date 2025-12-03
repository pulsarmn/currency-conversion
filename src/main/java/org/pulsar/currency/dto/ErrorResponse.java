package org.pulsar.currency.dto;


import lombok.Builder;

@Builder
public record ErrorResponse(String message) {
}
