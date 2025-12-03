package org.pulsar.currency.dto;


import lombok.Builder;

@Builder
public class ErrorResponse {

    private final String message;

    private static final String PLACEHOLDER = """
            {
                "message": "%s"
            }
            """;

    public ErrorResponse(String message) {
        this.message = PLACEHOLDER.formatted(message);
    }

    @Override
    public String toString() {
        return message;
    }
}
